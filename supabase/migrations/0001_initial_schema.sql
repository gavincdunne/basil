-- 0001_initial_schema.sql
-- Basil — initial Supabase schema.
-- Pairs with docs/Supabase-Schema-and-API-Contract.md.
--
-- Apply with:
--   supabase db push
--
-- Notes:
--   * Every user-owned table has RLS enabled with a `<table>_self` policy.
--   * `auth.uid()` is the signed-in user's UUID from Supabase Auth.
--   * Service-role requests bypass RLS; Edge Functions must enforce scoping manually.

create schema if not exists basil;
create extension if not exists "pgcrypto";
create extension if not exists "pg_trgm";

-- -----------------------------------------------------------------
-- helper: updated_at trigger function
-- -----------------------------------------------------------------
create or replace function basil.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end;
$$;

-- -----------------------------------------------------------------
-- user_profile (1:1 with auth.users)
-- -----------------------------------------------------------------
create table basil.user_profile (
  user_id        uuid primary key references auth.users (id) on delete cascade,
  display_name   text,
  date_of_birth  date,
  timezone       text not null default 'UTC',
  created_at     timestamptz not null default now(),
  updated_at     timestamptz not null default now()
);

create trigger trg_user_profile_updated
  before update on basil.user_profile
  for each row execute function basil.set_updated_at();

alter table basil.user_profile enable row level security;

create policy user_profile_self on basil.user_profile
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

-- -----------------------------------------------------------------
-- therapy_profile
-- -----------------------------------------------------------------
create table basil.therapy_profile (
  user_id                    uuid primary key references auth.users (id) on delete cascade,
  diabetes_type              text not null default 'T1D'
                               check (diabetes_type in ('T1D','T2D','LADA','MODY','Other')),
  therapy_type               text check (therapy_type in ('MDI','Pump','Hybrid-Closed-Loop','DIY')),
  insulin_basal              text,
  insulin_bolus              text,
  carb_ratio_grams_per_unit  numeric(6,2),
  correction_factor_mg_dl    numeric(6,2),
  target_range_low_mg_dl     int default 70,
  target_range_high_mg_dl    int default 180,
  cgm_model                  text,
  pump_model                 text,
  updated_at                 timestamptz not null default now()
);

create trigger trg_therapy_profile_updated
  before update on basil.therapy_profile
  for each row execute function basil.set_updated_at();

alter table basil.therapy_profile enable row level security;

create policy therapy_profile_self on basil.therapy_profile
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

-- -----------------------------------------------------------------
-- log_entry
-- -----------------------------------------------------------------
create table basil.log_entry (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid not null references auth.users (id) on delete cascade,
  client_id       uuid not null,
  kind            text not null check (kind in (
                    'glucose','carbs','insulin_bolus','insulin_basal',
                    'exercise','mood','medication','note')),
  occurred_at     timestamptz not null,
  payload         jsonb not null,
  glucose_mg_dl   int          generated always as ((payload->>'value_mg_dl')::int) stored,
  carbs_grams     numeric(6,2) generated always as ((payload->>'grams')::numeric) stored,
  insulin_units   numeric(6,2) generated always as ((payload->>'units')::numeric) stored,
  notes_encrypted bytea,
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  deleted_at      timestamptz,
  unique (user_id, client_id)
);

create index log_entry_user_time
  on basil.log_entry (user_id, occurred_at desc)
  where deleted_at is null;

create index log_entry_user_kind_time
  on basil.log_entry (user_id, kind, occurred_at desc)
  where deleted_at is null;

create trigger trg_log_entry_updated
  before update on basil.log_entry
  for each row execute function basil.set_updated_at();

alter table basil.log_entry enable row level security;

create policy log_entry_self on basil.log_entry
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

-- -----------------------------------------------------------------
-- log_attachment (metadata; binaries live in Storage)
-- -----------------------------------------------------------------
create table basil.log_attachment (
  id            uuid primary key default gen_random_uuid(),
  user_id       uuid not null references auth.users (id) on delete cascade,
  log_entry_id  uuid not null references basil.log_entry (id) on delete cascade,
  storage_key   text not null,
  mime_type     text not null,
  byte_size     int  not null,
  created_at    timestamptz not null default now()
);

create index log_attachment_entry on basil.log_attachment (log_entry_id);

alter table basil.log_attachment enable row level security;

create policy log_attachment_self on basil.log_attachment
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

-- -----------------------------------------------------------------
-- Storage bucket: log-attachments
-- Policies are applied post-migration via the Supabase dashboard or
-- the storage.policy_create() helper; documenting here for the record.
--
--   insert into storage.buckets (id, name, public) values ('log-attachments','log-attachments',false);
--
--   create policy "own folder read"
--     on storage.objects for select
--     using (bucket_id = 'log-attachments' and storage.foldername(name)[1] = auth.uid()::text);
--
--   create policy "own folder write"
--     on storage.objects for insert
--     with check (bucket_id = 'log-attachments' and storage.foldername(name)[1] = auth.uid()::text);
-- -----------------------------------------------------------------

-- -----------------------------------------------------------------
-- Grants: PostgREST exposes `basil` schema to `anon` and `authenticated`.
-- RLS does the actual access control.
-- -----------------------------------------------------------------
grant usage on schema basil to anon, authenticated;
grant select, insert, update, delete on all tables in schema basil to authenticated;
alter default privileges in schema basil
  grant select, insert, update, delete on tables to authenticated;
