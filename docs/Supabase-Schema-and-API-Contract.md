# Basil — Supabase Schema & API Contract

**Status:** v0.1 · April 2026 · engineering
**Owner:** Gavin
**Scope:** the initial Supabase schema (users, therapy, logs, sync, sharing, assistant) and the HTTP contract the KMP client talks to. Pairs with the backend decision memo (which selected Supabase Team + HIPAA for MVP).

---

## TL;DR

One Postgres schema (`basil`), row-level-security on every user-owned table, a handful of Edge Functions for the bits that don't fit PostgREST cleanly (assistant, report export, integration webhooks). The client talks to Supabase directly for simple CRUD (authenticated as the user) and to Edge Functions for anything that needs the service role (e.g., calling Anthropic under BAA, generating a PDF). No custom server in v1.

Design principles:

1. **User-owned by default.** Every user-facing table has `user_id` and RLS clamps to `auth.uid()`.
2. **Append-only where it matters.** Log entries are soft-deletable, never hard-overwritten. Audit tables are pure append.
3. **Minimum necessary over the wire.** API returns only the columns the client asked for. No `select *` leaks.
4. **Offline-first sync.** The client is the source of truth for local logs until acknowledged by the server. Conflict resolution is last-write-wins on `updated_at`, with manual reconciliation behind a feature flag for the edge cases.
5. **HIPAA posture built in, not bolted on.** BAA with Supabase, column-level encryption for freeform notes and photos, audit logs retained per HIPAA's 6-year requirement.

---

## Migration layout

Supabase migrations live in `supabase/migrations/NNNN_name.sql`, applied by the Supabase CLI. The client mirror (SQLDelight) sits in `composeApp/src/commonMain/sqldelight/`.

| Migration | Purpose |
|---|---|
| `0001_initial_schema.sql` | Core tables, RLS policies, indexes |
| `0002_assistant_audit.sql` | AssistantAuditLog, AssistantThread, AssistantMessage |
| `0003_sharing.sql` | Care-team invites and follow relationships |
| `0004_integrations.sql` | CGM / pump / HealthKit integration bookkeeping |
| `0005_audit.sql` | Global audit log for access events |
| `0006_reports.sql` | Report export records |

Only `0001` is shipped with v1 of this doc. The rest are stubs here so the direction is clear.

---

## 0001 — Core schema

### Users and profiles

`auth.users` is Supabase-managed. We extend with one-to-one rows in our own schema.

```sql
create schema if not exists basil;

-- User profile (1:1 with auth.users)
create table basil.user_profile (
  user_id          uuid primary key references auth.users (id) on delete cascade,
  display_name     text,
  date_of_birth    date,
  timezone         text not null default 'UTC',
  created_at       timestamptz not null default now(),
  updated_at       timestamptz not null default now()
);

create trigger trg_user_profile_updated
  before update on basil.user_profile
  for each row execute function basil.set_updated_at();

-- Therapy profile (per user, mutable, historical via therapy_profile_history)
create table basil.therapy_profile (
  user_id                    uuid primary key references auth.users (id) on delete cascade,
  diabetes_type              text not null default 'T1D' check (diabetes_type in ('T1D','T2D','LADA','MODY','Other')),
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
```

### Log entries

The client-side `LogEntry.sq` defines the on-device version. The server table mirrors it with the addition of `user_id`, soft delete, and server-assigned timestamps.

```sql
create table basil.log_entry (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid not null references auth.users (id) on delete cascade,
  client_id       uuid not null, -- device-assigned id for idempotency
  kind            text not null check (kind in (
                    'glucose','carbs','insulin_bolus','insulin_basal',
                    'exercise','mood','medication','note')),
  occurred_at     timestamptz not null,
  -- Kind-specific payload in jsonb to keep the schema stable as we add kinds.
  -- For "glucose": { value_mg_dl, source: 'fingerstick'|'cgm'|'manual' }
  -- For "carbs":   { grams, meal_type, notes }
  -- For "insulin_bolus": { units, insulin_type, reason }
  -- For "exercise": { type, duration_minutes, intensity }
  -- For "mood":    { tags: [], note }
  payload         jsonb not null,
  -- Denormalized convenience columns for indexing and analytics:
  glucose_mg_dl   int generated always as ((payload->>'value_mg_dl')::int) stored,
  carbs_grams     numeric(6,2) generated always as ((payload->>'grams')::numeric) stored,
  insulin_units   numeric(6,2) generated always as ((payload->>'units')::numeric) stored,
  -- Encrypted freeform fields (pgcrypto, server-side key)
  notes_encrypted bytea,
  -- Sync metadata
  created_at      timestamptz not null default now(),
  updated_at      timestamptz not null default now(),
  deleted_at      timestamptz,
  unique (user_id, client_id)
);

create index log_entry_user_time on basil.log_entry (user_id, occurred_at desc)
  where deleted_at is null;
create index log_entry_user_kind_time on basil.log_entry (user_id, kind, occurred_at desc)
  where deleted_at is null;

create trigger trg_log_entry_updated
  before update on basil.log_entry
  for each row execute function basil.set_updated_at();
```

**Why `jsonb` for payload:** the log kinds will grow. Adding a new kind (`sleep`, `mood`, `period`) should not require a migration of every downstream query. The two or three fields we index heavily are pulled up as generated columns so Postgres can use btree indexes on them.

**Why `client_id` with a unique constraint:** the client generates UUIDs at the moment of logging and replays them on sync. The unique constraint turns duplicate inserts (reconnects, retries) into idempotent no-ops.

**Why `deleted_at` instead of DELETE:** HIPAA right-of-access and eventual reporting need a record of entries. Hard delete is the explicit account-deletion flow, not the normal edit flow.

### Attachments (meal photos, etc.)

Photos go to Supabase Storage. The DB row is metadata only.

```sql
create table basil.log_attachment (
  id             uuid primary key default gen_random_uuid(),
  user_id        uuid not null references auth.users (id) on delete cascade,
  log_entry_id   uuid not null references basil.log_entry (id) on delete cascade,
  storage_key    text not null,             -- path in the 'log-attachments' bucket
  mime_type      text not null,
  byte_size      int not null,
  created_at     timestamptz not null default now()
);

create index log_attachment_entry on basil.log_attachment (log_entry_id);
```

The corresponding bucket `log-attachments` has RLS keyed off `storage.foldername(name)[1] = auth.uid()::text` so users can only read and write their own folder.

### Helper functions and triggers

```sql
create or replace function basil.set_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at := now();
  return new;
end;
$$;
```

### Row-level security

RLS is non-negotiable. It is on for every table that holds user data; no `FOR ALL USING (true)` escape hatches, ever.

```sql
alter table basil.user_profile enable row level security;
alter table basil.therapy_profile enable row level security;
alter table basil.log_entry enable row level security;
alter table basil.log_attachment enable row level security;

create policy user_profile_self on basil.user_profile
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy therapy_profile_self on basil.therapy_profile
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy log_entry_self on basil.log_entry
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());

create policy log_attachment_self on basil.log_attachment
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());
```

Service-role operations (Edge Functions) bypass RLS by necessity; they are individually responsible for enforcing the rule that a user can only read their own data. Every service-role handler starts with `requireUserId(jwt)` and passes that id into every query.

### Extensions

```sql
create extension if not exists "pgcrypto";
create extension if not exists "pg_trgm";
```

`pgcrypto` for `gen_random_uuid()` and symmetric encryption. `pg_trgm` for future full-text search on notes.

---

## 0002 — Assistant audit and threads (stub)

The LLM assistant architecture doc describes the flow; the schema it needs:

```sql
create table basil.assistant_thread (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null references auth.users (id) on delete cascade,
  title        text,
  created_at   timestamptz not null default now(),
  archived_at  timestamptz
);

create table basil.assistant_message (
  id                 uuid primary key default gen_random_uuid(),
  thread_id          uuid not null references basil.assistant_thread (id) on delete cascade,
  user_id            uuid not null references auth.users (id) on delete cascade,
  role               text not null check (role in ('user','assistant','system')),
  content            text not null,   -- the user message or post-flight assistant response
  prompt_version     text,
  created_at         timestamptz not null default now()
);

create table basil.assistant_audit_log (
  id                   uuid primary key default gen_random_uuid(),
  user_id              uuid not null,
  thread_id            uuid,
  prompt_version       text not null,
  pre_flight_verdict   text not null,
  post_flight_verdict  text not null,
  guardrail_hits       jsonb,
  model                text not null,
  latency_ms           int,
  input_tokens         int,
  output_tokens        int,
  created_at           timestamptz not null default now()
);

alter table basil.assistant_thread enable row level security;
alter table basil.assistant_message enable row level security;
-- audit_log has no RLS policy; service-role only.

create policy assistant_thread_self on basil.assistant_thread
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());
create policy assistant_message_self on basil.assistant_message
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());
```

`assistant_audit_log` intentionally has RLS off and no policy — the `audit_log` must be unreadable by the user whose actions it captures. Only the service role writes to it; only ops (via Supabase console or a maintenance function gated behind a break-glass procedure) reads it.

---

## 0003 — Sharing (stub)

```sql
create table basil.follow (
  id              uuid primary key default gen_random_uuid(),
  patient_id      uuid not null references auth.users (id) on delete cascade,
  follower_id     uuid not null references auth.users (id) on delete cascade,
  role            text not null check (role in ('clinician','family','caregiver')),
  permissions     text[] not null default '{read_reports}',
  status          text not null default 'pending' check (status in ('pending','active','revoked')),
  created_at      timestamptz not null default now(),
  activated_at    timestamptz,
  revoked_at      timestamptz,
  unique (patient_id, follower_id)
);

alter table basil.follow enable row level security;

create policy follow_visible_to_both on basil.follow
  for select using (patient_id = auth.uid() or follower_id = auth.uid());

create policy follow_insert_by_patient on basil.follow
  for insert with check (patient_id = auth.uid());

create policy follow_update_by_patient on basil.follow
  for update using (patient_id = auth.uid()) with check (patient_id = auth.uid());
```

The clinician or family member accepts or rejects via a second-factor flow handled by an Edge Function that updates `status`.

---

## 0004 — Integrations (stub)

Holds per-user integration tokens and last-sync bookkeeping. Tokens are encrypted with a server-side key (`pgp_sym_encrypt`).

```sql
create table basil.integration (
  id                 uuid primary key default gen_random_uuid(),
  user_id            uuid not null references auth.users (id) on delete cascade,
  provider           text not null check (provider in ('healthkit','health_connect','dexcom','libre','tandem','omnipod')),
  status             text not null default 'active' check (status in ('active','paused','revoked')),
  access_token_enc   bytea,
  refresh_token_enc  bytea,
  expires_at         timestamptz,
  last_synced_at     timestamptz,
  created_at         timestamptz not null default now(),
  unique (user_id, provider)
);

alter table basil.integration enable row level security;
create policy integration_self on basil.integration
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());
```

---

## 0005 — Global audit log (stub)

```sql
create table basil.access_audit_log (
  id              bigserial primary key,
  user_id         uuid,
  actor           text not null, -- 'user' | 'service' | 'admin'
  action          text not null, -- 'read_log_entry', 'export_report', ...
  subject_type    text,
  subject_id      uuid,
  request_id      text,
  ip_hash         text,
  user_agent      text,
  occurred_at     timestamptz not null default now()
);

-- No RLS; service-role only.
create index access_audit_user_time on basil.access_audit_log (user_id, occurred_at desc);
```

Retention: 6 years per HIPAA, behind a partitioned-by-month table once the row count warrants it. For now, a single table is fine.

---

## 0006 — Report exports (stub)

```sql
create table basil.report_export (
  id              uuid primary key default gen_random_uuid(),
  user_id         uuid not null references auth.users (id) on delete cascade,
  requested_by    uuid not null references auth.users (id),
  kind            text not null check (kind in ('weekly','monthly','custom')),
  period_start    date not null,
  period_end      date not null,
  storage_key     text,
  status          text not null default 'pending' check (status in ('pending','ready','failed')),
  created_at      timestamptz not null default now(),
  completed_at    timestamptz
);

alter table basil.report_export enable row level security;
create policy report_export_self on basil.report_export
  for all using (user_id = auth.uid()) with check (user_id = auth.uid());
```

Report generation is an Edge Function that writes a PDF to the `reports` storage bucket and updates the row.

---

## API contract

The client uses two surfaces:

1. **PostgREST** for CRUD. Covered by RLS. The client uses the user's JWT.
2. **Edge Functions** for anything else. Each function validates the JWT, derives `user_id`, enforces scoping, and performs the privileged action.

### PostgREST conventions

- Base URL: `https://<project>.supabase.co/rest/v1/basil`.
- `Prefer: return=representation` for inserts that should echo the row.
- Always `?select=explicit,column,list` — never `*`.
- Paginate with `Range` headers. Default page size 100, max 500.
- Timestamps are ISO 8601 UTC over the wire. The client applies the user's timezone locally.

### Edge Functions

Each function is a deno serverless file under `supabase/functions/<name>`.

#### `POST /functions/v1/assistant-message`

Send a message to the AI assistant.

Request:
```json
{
  "threadId": "uuid-or-null",
  "userMessage": "string"
}
```

Response (streamed SSE):
```
event: token
data: {"text": "Sure, "}

event: token
data: {"text": "here's ..."}

event: done
data: {"messageId": "uuid", "threadId": "uuid", "promptVersion": "v1.2.3"}
```

Behavior:

1. Authenticate JWT → `userId`.
2. Rate-limit: 10/min, 200/day (configurable by tier).
3. Build scoped context from `basil.log_entry` + `basil.therapy_profile`.
4. Run pre-flight guardrails. If blocked: return a canned message; write audit row; stop.
5. Call Anthropic with streaming. Relay tokens to the client.
6. Run post-flight on the assembled response. If redacted: replace the response with the safe-completion template.
7. Persist the user + assistant messages; write audit row.

Errors:
- `401` unauthenticated
- `429` rate-limited, body includes `retry_after_seconds`
- `422` user message failed pre-flight validation
- `502` upstream (Anthropic) failure; include `request_id`
- `503` feature disabled for this user (check user preferences)

#### `POST /functions/v1/report-generate`

Kick off a PDF report generation.

Request:
```json
{
  "kind": "weekly" | "monthly" | "custom",
  "periodStart": "YYYY-MM-DD",
  "periodEnd": "YYYY-MM-DD",
  "includeAssistantSummaries": false
}
```

Response:
```json
{ "reportId": "uuid", "status": "pending" }
```

Generation is asynchronous. The client polls `/rest/v1/report_export?id=eq.<reportId>` or subscribes to the realtime channel.

#### `POST /functions/v1/integration-sync`

Trigger a sync for a connected integration (HealthKit-style syncs are push from the device; this endpoint handles pull integrations like Dexcom).

Request:
```json
{ "provider": "dexcom", "since": "ISO-8601-or-null" }
```

Response:
```json
{ "imported": 120, "skipped_duplicates": 3, "last_synced_at": "ISO-8601" }
```

#### `POST /functions/v1/account-delete`

Initiates the account deletion flow.

Request:
```json
{ "confirmationPhrase": "delete my account" }
```

Response:
```json
{ "deletionScheduledFor": "ISO-8601", "undoUntil": "ISO-8601" }
```

Deletion is scheduled 30 days out (per Privacy Policy). The function sets a `deletion_requested_at` flag on `user_profile` and enqueues a background job.

### Realtime channels

Supabase's Realtime publishes row changes over WebSocket. The client subscribes to:

- `log_entry:user_id=<self>` — for cross-device log sync.
- `assistant_message:thread_id=<current>` — for follower views (v1.1).
- `report_export:id=<pending>` — for "your report is ready" notifications.

---

## Client-side sync

The KMP client treats the local SQLDelight database as the source of truth for writes, and the server as the source of truth for reads that span devices.

Sync algorithm (v1, deliberately simple):

1. **Outbound.** The client maintains an `outbox` table of local log entries with a `sync_status` column. On every app foreground:
   - Find all rows with `sync_status in ('pending','retry')`.
   - Upsert each to the server using `client_id` for idempotency.
   - On success, mark `sync_status = 'synced'` and persist the server-assigned id.
   - On conflict (409 / RLS violation), surface a reconciliation UI (rare).

2. **Inbound.** After outbound completes:
   - Pull rows from the server `log_entry` where `updated_at > last_sync_at`.
   - Upsert locally by `client_id`. Last-write-wins on `updated_at`.
   - Persist `last_sync_at`.

3. **Realtime.** When online, subscribe to `log_entry` changes for the current user and apply them directly (skipping the pull step for those rows).

Conflict edge case: if the user edits a log on device A offline, then edits the same log on device B (online), B's version is reflected on the server by the time A reconnects. When A syncs, its outbound upsert gets rejected because `updated_at` is older than server. A then picks up the server version via pull and the local edit is replaced. For v1 this is acceptable; v1.1 we show a "this entry was edited on another device" toast with undo.

---

## Error model

Client-facing errors are always `{ "error": { "code": "...", "message": "...", "details": {} } }`. No generic 500s leaked to the UI; the backend translates unknown errors to `{ "code": "internal", "message": "Something went wrong. We've logged it." }` and the full trace goes to Sentry.

Error codes (non-exhaustive):

| Code | HTTP | Meaning |
|---|---|---|
| `unauthenticated` | 401 | Missing or invalid JWT |
| `forbidden` | 403 | RLS denied; user doesn't own the row |
| `rate_limited` | 429 | Cap reached; body has `retry_after_seconds` |
| `validation_failed` | 422 | Body failed validation; body has per-field details |
| `guardrail_refusal` | 422 | AI assistant pre-flight blocked |
| `upstream_unavailable` | 502 | Anthropic or an integration vendor is down |
| `internal` | 500 | Generic server error |

---

## Open questions

1. **Do we store raw CGM data?** The log_entry payload holds glucose readings, but a full Dexcom stream is ~288 readings/day. Storing every one is fine at low scale; we may need a separate `cgm_reading` table with partitioning for heavy users. Defer until we have real integration data.
2. **Photo OCR / carb estimation** — out of v1 scope per the LLM architecture doc. Schema anticipates it by leaving `log_attachment.mime_type` free-form.
3. **Multi-device assistant thread sync** — feasible via realtime; scoping work for v1.1.
4. **Cost modeling of jsonb payload indexes** — generated columns for glucose/carbs/insulin are fine now; if we add ten more index-worthy fields we should revisit and normalize.
5. **When to flip SQLDelight `verifyMigrations` on** — per the CI plan, when cloud sync ships. That's this milestone. Flip it with 0001.

---

## File map

- `supabase/migrations/0001_initial_schema.sql` — this doc's section 0001, runnable.
- `supabase/migrations/000{2..6}_*.sql` — to be written when each area ships.
- `supabase/functions/assistant-message/` — Edge Function for the AI assistant.
- `supabase/functions/report-generate/` — report PDF generator.
- `supabase/functions/integration-sync/` — pull-integration orchestrator.
- `supabase/functions/account-delete/` — deletion workflow entrypoint.
- `composeApp/src/commonMain/sqldelight/` — client schema mirror (existing).
- `docs/Supabase-Schema-and-API-Contract.md` — this doc.
