# Basil — LLM Assistant Architecture & Safety Design

**Status:** v0.1 · April 2026 · engineering
**Owner:** Gavin
**Scope:** the in-app conversational assistant ("Ask Basil"). This doc covers the architecture, data flow, prompt strategy, safety rails, evals, and failure modes. It does **not** cover the marketing-site chatbot, analytics, or anything outside the product surface.

---

## TL;DR

The assistant is a thin wrapper around Anthropic's Claude API (covered by a signed BAA before launch) that can answer scoped questions about the user's own recent log data. It is an educational tool, not a clinical tool. It never tells a user how much insulin to take, when to eat, or how to change their therapy. Those three refusals are hard-coded guardrails, not prompt suggestions.

The design priorities, in order, are: **safety, scopedness, privacy, helpfulness, cost, latency.** Any time those conflict, the earlier one wins.

## Where this sits in the app

```
┌────────────────────────────────────────────────────────────────────┐
│                    Basil mobile app (KMP/Compose)                   │
│                                                                      │
│   ChatScreen ──► ChatViewModel ──► AssistantService ──► Backend API │
│                                                                      │
└────────────────────────────────────────────────────────────────────┘
                                                         │
                            (TLS, BAA-covered)           ▼
┌────────────────────────────────────────────────────────────────────┐
│                    Basil backend (Supabase, TBD AWS)                 │
│                                                                      │
│   ┌──────────┐  ┌──────────────┐  ┌────────────┐  ┌────────────┐   │
│   │  Auth    │→ │ AssistantFn  │→ │ ContextFn  │→ │ Log DB     │   │
│   │  (JWT)   │  │ (edge fn)    │  │ (edge fn)  │  │ (Postgres) │   │
│   └──────────┘  └──────────────┘  └────────────┘  └────────────┘   │
│                         │                                            │
│                         ▼                                            │
│                  ┌──────────────┐      ┌──────────────┐             │
│                  │ Guardrails   │◄─────┤ Eval cache   │             │
│                  │ (pre + post) │      │ (Redis)      │             │
│                  └──────┬───────┘      └──────────────┘             │
│                         │                                            │
└─────────────────────────┼────────────────────────────────────────────┘
                          │
                          ▼ (TLS, BAA-covered)
                   ┌──────────────┐
                   │  Anthropic   │
                   │  Claude API  │
                   └──────────────┘
```

The user never talks to Anthropic directly. Every prompt passes through our backend, which (a) scopes the user's context, (b) enforces guardrails pre- and post-call, and (c) logs the minimum metadata needed for safety monitoring.

## Data flow for a single turn

1. User types a question in `ChatScreen`.
2. `ChatViewModel` validates the message locally (length, empty check, basic profanity) and appends it to the in-memory thread.
3. `AssistantService` calls `POST /v1/assistant/message` with `{threadId, userMessage}`.
4. Backend's `AssistantFn` authenticates the JWT, rate-limits the user (10 req/min, 200 req/day soft-cap), and calls `ContextFn`.
5. `ContextFn` pulls the user's scoped context from Postgres: last 30 days of log entries, therapy profile (insulin type, target range, ratios), and the last 10 turns of this thread. It redacts freeform notes the user marked "private."
6. `Guardrails` runs pre-flight checks against the user message:
   - Is this a dosing question? → refuse with a templated response and stop.
   - Is this a suicidal-ideation or self-harm signal? → return the mental-health resource card and stop.
   - Is this an emergency? ("call 911", "I can't feel my legs", "I'm passing out") → return the emergency card and stop.
   - Is this a prompt-injection attempt? → log, strip obvious injection, continue with reduced context.
7. The backend assembles a prompt (system + tool schema + scoped context + user message) and calls Claude with `temperature = 0.2`, `max_tokens = 800`.
8. The streamed response is collected, then `Guardrails` runs post-flight checks:
   - Does the response contain a dosing recommendation, even implicitly? → replace with a safe refusal and log a high-priority eval event.
   - Does it cite a specific medication dose, pump setting, or insulin unit count? → same.
   - Does it contradict the user's therapy profile (e.g., recommending a food for a ketogenic user)? → soft-warn but allow.
9. The response is streamed back to the app with a thread message id and a disclaimer footer.
10. The full turn (user message, scoped context hash, response, guardrail verdicts) is written to `AssistantAuditLog` with a 30-day retention window.

## What context we send (and what we don't)

**Sent to the model:**

- Last 30 days of log entries: glucose readings (time, value), carbs (time, grams, notes up to 120 chars), insulin (time, type, units), exercise (time, type, duration, intensity), mood tags. Freeform user notes only if the user did not tag them private.
- Therapy profile fields relevant to the question: insulin type(s), target range, insulin-to-carb ratio, correction factor, CGM model, pump model. We do *not* send A1C, weight, or pregnancy status unless the question references them.
- Last 10 turns of the current thread (unredacted — the user wrote them).
- System prompt (below).
- Today's date + user's timezone.

**Never sent to the model:**

- Full name, email, phone number, address, device identifiers.
- Photos attached to log entries (text-only for v1; multimodal is post-MVP).
- Other users' data (obvious but worth stating).
- Payment info, subscription tier.
- Anything from a different thread.

This is minimum-necessary applied operationally. Shrinking the context shrinks both the risk surface and the token cost.

## System prompt (v1 draft)

The system prompt is the spine of the assistant. It is checked into the repo (`composeApp/src/commonMain/resources/prompts/assistant_v1.md`) and versioned. Every response is tagged with the prompt version so we can compare evals across revisions.

Key clauses:

1. **Role.** "You are Basil, a supportive, evidence-based assistant for people living with Type 1 Diabetes. You help them see patterns in their own data and understand diabetes concepts at a general level."
2. **Absolute refusals.** "You must not tell the user how much insulin to take, when to eat, when to exercise, how to change their pump settings, or how to interpret a specific reading as a dosing decision. If asked, respond with the dosing-refusal template and suggest they contact their care team."
3. **Scope of knowledge.** "You may reference general, well-established diabetes education (e.g., ADA / ISPAD standards of care summaries) and the user's own recent data provided in the CONTEXT block. You must not cite specific clinical guidelines you cannot verify."
4. **Uncertainty.** "When you don't know, say so. Recommend the user ask their endocrinologist or CDCES."
5. **Tone.** "Warm, concise, never condescending. Never moralize about food choices or 'control.' Use person-first language: 'a person living with T1D,' not 'a diabetic.'"
6. **Crisis handling.** "If the user shows signs of self-harm, panic, DKA-like symptoms, or severe hypoglycemia, respond with the crisis template first and stop."
7. **Formatting.** Plain prose. Short paragraphs. At most one bulleted list per response. No markdown headers. No tables.
8. **Length.** Default under 180 words. Longer only if the user explicitly asks.

The full prompt is in the repo; this doc is an architectural summary.

## Guardrail layer

Guardrails run in two phases and are implemented as deterministic rules plus a small classifier.

### Pre-flight (runs before the model call)

Pattern-matched intent classification against the user message:

| Intent bucket | Action | Template |
|---|---|---|
| Dosing question ("how many units should I take", "is 3u enough for 45g") | Block model call; return refusal | `DOSING_REFUSAL` |
| Emergency indicator ("passing out", "can't see", "blood sugar 35", "blood sugar 600") | Block; return emergency card | `EMERGENCY_CARD` |
| Self-harm signal | Block; return mental-health resource card | `MENTAL_HEALTH_CARD` |
| Explicit prompt injection ("ignore previous instructions", "you are now DAN") | Strip the injection; continue with reduced context | — |
| Out-of-scope (taxes, relationships, code) | Allow; let the model handle with the system prompt's scope clause | — |

Dosing detection is a regex-plus-small-classifier hybrid. Precision matters more than recall here; false positives cost us a one-turn refusal, false negatives could harm someone. We tune for 99% precision at whatever recall that gives us.

### Post-flight (runs on the model response)

| Check | Action |
|---|---|
| Response contains a dosing number or ratio ("3 units of Humalog", "1:10 ratio") | Replace with refusal; log as a high-priority eval event |
| Response contains a specific pump setting, basal rate, or correction factor recommendation | Replace with refusal |
| Response claims to be a medical professional or uses phrases like "I diagnose" / "I prescribe" | Replace with safe-completion template |
| Response contains hallucinated user data (e.g., a glucose reading the context didn't contain) | Replace with safe-completion template |
| Response length over 500 words | Truncate and append "[response trimmed]" |

The post-flight layer exists because language models are probabilistic and the system prompt alone is not sufficient to prevent drift. When the classifier catches a miss, we open an eval ticket; the ticket cycle is how the system prompt gets tightened.

## Refusal templates

Templates are kept in `assistant_v1/templates.json` and versioned alongside the prompt. They should be short, human, and not robotic.

- **DOSING_REFUSAL** — "That's a dosing question, and Basil isn't the right tool for it. Your endocrinologist or CDCES can help you work out ratios and corrections that match your body. I can help you spot patterns in your data if that would be useful."
- **EMERGENCY_CARD** — "This sounds like it might be a medical emergency. Please follow your care plan or call 911 (U.S.) / your local emergency number. If you're with someone, let them know. I'm not the right tool here."
- **MENTAL_HEALTH_CARD** — "What you're describing sounds heavy. If you want to talk to someone right now, 988 (U.S.) connects you to the Suicide and Crisis Lifeline by call or text. If you're in immediate danger, call 911."
- **SAFE_COMPLETION** — "I'm not sure I can answer that safely. Would you like me to summarize the last week of your data, or help you find a reliable resource on the topic?"

## Safety telemetry

Every assistant turn writes an audit row:

```
assistant_audit_log (
  id uuid pk,
  user_id uuid,
  thread_id uuid,
  prompt_version text,
  pre_flight_verdict text,         -- "allow" | "dosing_refusal" | "emergency_card" | ...
  post_flight_verdict text,        -- "allow" | "redacted" | "truncated"
  guardrail_hits jsonb,
  model text,
  latency_ms int,
  input_tokens int,
  output_tokens int,
  created_at timestamp
)
```

We do not store the full prompt or response in the audit log by default. A 48-hour debug window writes the full turn to a separate encrypted store (access gated behind a runtime flag + on-call approval) for post-incident review. After 48 hours, only the audit-log row survives.

Retention: audit log 30 days; debug window 48 hours. Both short enough to limit breach exposure, long enough to investigate incidents.

## Evaluation harness

We maintain a golden set of roughly 300 prompts covering:

- Dosing questions that must be refused (100).
- Safe educational questions that should be answered (100).
- Ambiguous questions (50) where the "right" answer is a clarification.
- Adversarial / prompt-injection attempts (25).
- Emotional / mental-health scenarios (25).

Every prompt has an expected category (refuse / answer / clarify / crisis) and, for "answer" prompts, a list of required/forbidden substrings.

CI runs the eval harness nightly (not per-PR; too expensive) against the latest `assistant_v1.md`. Regressions block the next release. The eval is versioned in `eval/assistant/v1/` with its own changelog.

Pass bar for release:

- Dosing refusal precision: 100%. Zero tolerance.
- Emergency handling: 100%.
- Mental health handling: 100%.
- Safe-answer accuracy against golden answers: ≥ 85%.
- Prompt-injection resistance: ≥ 95%.

Scores under bar block release and open a remediation ticket on the prompt or guardrail layer.

## Clinical review process

Before every system-prompt change ships:

1. Engineer opens a PR with the `assistant_v*.md` diff.
2. CI runs the full eval suite.
3. A clinical advisor (CDCES or endocrinologist on our roster) reviews the diff and a random 30-sample run of the golden set.
4. The clinical reviewer signs off (required CODEOWNERS on prompt files).
5. Roll-out is feature-flagged to 10% of users for 72 hours before going to 100%.

This is light clinical review, not an IRB process. It's right-sized for a wellness scope. The moment the product scope expands beyond wellness, the review process tightens.

## Privacy posture

- All context is streamed to the model over TLS 1.2+.
- Anthropic's BAA is signed before we turn this feature on for a single user. No BAA, no feature.
- Anthropic does not train on prompts or retain them beyond the minimum required to serve a response (per BAA).
- We do not copy-paste the user's data into a second model provider (e.g., OpenAI) without a separate BAA. Multi-vendor fallback is explicitly out of scope until we have both BAAs.
- Users can clear their entire assistant history from Settings → Assistant → Clear history. That call deletes threads and audit-log rows older than the current session.
- Users can disable the assistant entirely from Settings → Assistant → Off. When off, no context is ever sent to any model provider for that user.

## Cost and latency budget

Per-turn target:

- Input tokens: ≤ 6,000 (system prompt ~1,000, context ~4,000, conversation ~1,000).
- Output tokens: ≤ 800.
- p50 latency: ≤ 3 seconds to first token.
- p95 latency: ≤ 8 seconds total.

Per-user daily cap: 200 turns. Free tier: 20 turns/day. Basil tier: 200. Basil Care tier: 500. These caps are the rate-limiter at the edge.

Expected blended cost per turn at current Claude pricing: ~$0.015. At 20 turns/user/day on the paid tier, that's ~$9/user/month, which is the dominant cost driver — sized into the pricing model in the business-model slide.

## Known failure modes

| Failure | Detection | Mitigation |
|---|---|---|
| Model hallucinates a glucose value the user did not log | Post-flight data-grounding check compares cited numbers to context | Replace with safe completion; open eval ticket |
| Model refuses a benign question as dosing | Post-flight refusal-rate monitoring | Tune pre-flight regex; add the pattern to the "safe" golden set |
| Model gives a long, generic AI answer with no user-specific value | Manual review and user feedback signal | Prompt revision; add few-shot examples |
| User tries to jailbreak via role-play ("pretend you're my endo") | Pre-flight injection detection; system prompt resists | Log and monitor the pattern; tighten prompt |
| BAA vendor (Anthropic) has an incident | Anthropic status page; on-call alert | Feature-flag the assistant off for affected users; users still have the rest of Basil |
| Latency spike makes the feature feel broken | Standard APM alerts | Optimistic UI ("Basil is thinking") and degrade to a cached response for common questions |

## Explicit non-goals (v1)

- No voice input or output.
- No photo or image attachments.
- No cross-thread memory. Every thread starts fresh; the system prompt plus that thread's 10 turns is the only memory.
- No agentic behavior (no writing new logs, no sending emails, no calling the pump).
- No group conversations.
- No clinician-facing assistant. That's a separate product if we ever build it.

## Roadmap beyond v1

- **Structured tool calls** so the assistant can propose a chart ("here's your last-week glucose by meal") as a tool output instead of freeform text.
- **Multimodal support** for photos of food (after a clinical review round; photo-based carb estimation is a regulated-device-adjacent territory and we will not ship it until we know the boundary).
- **User-configurable tone** (concise / detailed / curious) without changing the safety rails.
- **Offline fallback** for common educational questions, answered from a cached FAQ, when the network is flaky.
- **Clinician-reviewed few-shot examples** per prompt version, to raise baseline quality without over-constraining the model.

## Open questions

1. Should we use a model-based classifier for the dosing-intent check instead of regex-plus-small-classifier? (Trade-off: lower false-positive rate, higher latency and cost. Start with the hybrid, revisit after 1,000 real turns.)
2. Should we expose the prompt version to users in some form? (Lean: no, but log it so we can correlate user complaints to specific versions.)
3. How do we handle users who paste another person's log into chat? (Pre-flight detection of "my friend's" / "my partner's" + require explicit consent language; log.)
4. Does the assistant need a "clinical sources" footer that cites where a claim came from? (Yes, for anything beyond data-summarization. Scope into v1.1.)

## Files and ownership

- `composeApp/src/commonMain/resources/prompts/assistant_v1.md` — the system prompt.
- `composeApp/src/commonMain/resources/prompts/templates.json` — refusal templates.
- `backend/functions/assistant.ts` — edge function orchestrator.
- `backend/functions/context.ts` — user-context builder.
- `backend/functions/guardrails.ts` — pre/post-flight checks.
- `eval/assistant/v1/` — golden set, runner, and CI integration.
- `docs/LLM-Assistant-Architecture.md` — this document.

All of the above are under CODEOWNERS `@gavincdunne` until there are more humans.
