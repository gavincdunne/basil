# Check-In System — Spec

**Status:** Draft  
**Date:** 2026-05-25  
**Author:** Gavin Dunne (PM: Claude)  
**Project:** Basil

---

## Summary

The check-in system is Basil's primary interaction model: a periodic, low-friction prompt that asks the user how they're doing and stores their free-text response as the foundation for Basil's persistent memory. Without it, Basil is a chatbot. With it, Basil becomes a companion that knows this specific person's experience over time.

---

## Problem statement

Every T1D app on the market demands structured input — log your BG, log your carbs, log your insulin. Users dealing with burnout or mental load often stop logging entirely, which makes the app useless. There is no existing tool that simply asks "how are you?" in a way that feels human rather than clinical, and remembers the answer.

A user managing T1D on a difficult week — poor sleep, a stressful work deadline, unexplained afternoon highs — has no place to put that context. Their CGM captures the numbers. Nothing captures what's actually going on. Basil's check-in is that place. A sentence, a few words, or a longer entry: whatever the user can manage. Basil listens, responds, and remembers.

This is the interaction that distinguishes Basil from every other T1D app. It must feel like talking to someone who cares, not filling in a form.

---

## User stories

*As a T1D adult, I want to be gently prompted to share how I'm doing today, so that I don't have to initiate every interaction with Basil myself.*

*As a T1D adult, I want to respond in my own words without a fixed format, so that I can express what's actually going on — not just what a form asks for.*

*As a T1D adult, I want Basil to acknowledge what I've shared and respond warmly, so that the check-in feels like a conversation rather than a data entry task.*

*As a T1D adult, I want my check-in responses to be remembered over time, so that Basil's understanding of me improves with every interaction.*

*As a T1D adult experiencing a hard moment, I want Basil to respond appropriately if I share something distressing, so that I feel heard and am pointed to the right support if needed.*

---

## Acceptance criteria

1. The app surfaces a check-in prompt once per day on app open (or via local notification if the user has not opened the app by a configurable time — default 7:00 PM local). The prompt text is warm and conversational, never clinical. Example: *"Hey — how are you doing today?"*

2. The check-in prompt is presented in a dedicated UI surface (not the general chat screen) that is visually distinct, calm, and low-friction. The text input is the primary element. No logging fields, no required structured data.

3. The user can submit a free-text response of 1–1,000 characters. Empty submissions are not accepted. There is no minimum length enforcement beyond "not empty" — a single word is a valid response.

4. On submission, the response is sent to the basil-chat-api backend, which returns a Basil reply. The reply is acknowledged-first in tone: it reflects what the user said before asking anything else. If Basil asks a follow-up, it asks exactly one question — not multiple.

5. The Basil reply passes through the existing guardrail layer. If the check-in response contains a self-harm signal, crisis indicator, or emergency phrase, the appropriate guardrail template is returned (`MENTAL_HEALTH_CARD` or `EMERGENCY_CARD`) and no further follow-up is asked.

6. The check-in exchange (user response + Basil reply) is stored locally in SQLDelight with: `check_in_id`, `user_id`, `created_at`, `user_text`, `basil_reply`, `guardrail_verdict`. This table is the primary data source for the future persistent memory layer.

7. The check-in prompt does not appear again on the same calendar day once a check-in has been completed. If the user dismisses the prompt without completing it, it re-surfaces on the next app open within the same day (max once more), then clears until the following day.

8. The check-in UI must be reachable from the app's main navigation as a manual entry point (not only from a notification or automatic prompt) so the user can initiate a check-in any time.

9. A check-in response is never sent to the model with any other user's data. The context sent to the API is: the system prompt (companion persona), today's date and time, the user's check-in text, and — if prior check-ins exist — a brief summary of the last 3 check-ins (text only, no BG data). No structured health data is included in check-in context in v1.

10. All check-in data is treated as PHI-adjacent. It is stored encrypted at rest (SQLDelight + platform keystore), transmitted over TLS only, and never logged in plaintext in crash reporters (Sentry PHI scrubbing rules apply).

11. The feature ships on Android and iOS. Desktop is out of scope for v1.

12. String resources for all UI text are defined in `strings.xml`. No hardcoded strings in composables.

13. `CheckInViewModel` accepts `coroutineScope: CoroutineScope? = null` for testability.

14. Unit tests cover: successful check-in submission and storage, empty-submission guard, guardrail trigger path (mock crisis response), same-day deduplication logic, and network error state.

---

## Out of scope (v1)

- Scheduling configuration by the user (custom notification time, frequency). Default daily cadence only.
- Multiple check-ins per day (beyond manual re-entry). The system records one prompted check-in per day.
- BG, insulin, or carb data surfaced in check-in context. Data logging is a separate feature.
- Persistent memory layer. Check-ins are stored in a format designed for memory ingestion, but the memory system itself is a separate spec and separate feature.
- Check-in history / journal view. The stored data is there; surfacing it to the user is v2.
- Pattern detection or reflection ("you've mentioned rough mornings three times this week"). That's the Patterns feature, which depends on the memory layer.
- Sentiment analysis or tagging of check-in content. Raw text storage only in v1.
- Desktop platform.
- Push notification infrastructure for check-in reminders. Local notifications only in v1.
- Offline mode / response queuing when the network is unavailable. The user is informed of the error and can retry.

---

## Dependencies

**Must be done before this ships:**

- `basil-chat-api` end-to-end integration is working — the chat path from `ChatViewModel` → `AssistantService` → backend → Anthropic API must be live and tested. The check-in uses the same path with a different system prompt.
- Guardrail layer is implemented on the backend (pre-flight and post-flight checks as specified in `LLM-Assistant-Architecture.md`). The check-in must not ship without crisis handling.
- Anthropic BAA is signed. No check-in responses may be sent to the model before this is in place.
- SQLDelight schema for `check_ins` table is defined and migrated.
- A companion-persona system prompt variant is written, reviewed, and checked in. The check-in uses a different prompt than the general assistant — it is warmer, less data-focused, and explicitly frames this as a check-in context. This prompt requires the same clinical review process as `assistant_v1.md`.

**External:**

- Anthropic BAA (see above).
- Platform notification permissions (Android `POST_NOTIFICATIONS` permission for local notifications on API 33+).

---

## Risks and open questions

| Risk / Question | Owner | Resolution |
|---|---|---|
| What does the companion system prompt look like for check-ins vs. the general assistant? Is it the same prompt with a different preamble, or a separate prompt file? | Gavin | Decide before implementation begins. Recommendation: separate prompt file (`checkin_companion_v1.md`) so it can evolve independently without touching general-assistant guardrails. |
| If the user shares something that's emotionally heavy but not a crisis signal (e.g., "I'm really burnt out and I don't care about my diabetes anymore"), how does Basil respond? This sits in a grey area between companion empathy and clinical concern. | Gavin | Needs explicit guidance in the companion system prompt. Flag for clinical advisor review before prompt is finalised. |
| The check-in stores PHI-adjacent narrative text locally. What is the deletion behaviour — is it cleared on sign-out? | Gavin | Per existing HIPAA posture in `App.kt`, chat history is cleared on sign-out. Check-in data should follow the same rule. Confirm and implement accordingly. |
| How should the check-in prompt text vary over time? Using the same opening line every day ("Hey — how are you doing today?") may feel robotic after a week. | Gavin | v1 can use a small set of rotating prompts (3–5 variants) defined in `strings.xml`. More dynamic prompting is v2. |
| Local notification infrastructure: does it exist in the codebase already, or does it need to be built? | Gavin | Investigate before scoping the implementation task. If not present, local notification support is a sub-dependency that may affect estimate. |
| Desktop platform: should the check-in still be reachable on Desktop even if notifications aren't relevant? | Gavin | Deferred to v2 for full experience, but if the shared KMP logic is clean, the UI may be available on Desktop incidentally. Do not block on this. |
| Does a dismissed check-in (no response submitted) still get recorded in the local store for analytics or memory purposes? | Gavin | Recommendation: no. Only completed check-ins are stored. Dismiss events may be counted in a lightweight analytics counter (no PHI) for engagement monitoring. |

---

## Platform targets

**v1 ships on:** Android, iOS  
**Desktop:** Out of scope for v1. The KMP architecture means the ViewModel logic will exist on Desktop, but the UI and notification trigger are not implemented.

**Platform-specific notes:**

- **Android:** Uses `POST_NOTIFICATIONS` runtime permission (required API 33+). Local notification via `WorkManager` or `AlarmManager` — evaluate which is appropriate for a daily, non-exact trigger. Install and test on `dev` flavor: `./gradlew installDevDebug`.
- **iOS:** Uses `UNUserNotificationCenter` via the KMP iOS target. Notification permission request must occur at an appropriate moment in onboarding (not on first app open cold-start).
- The `CheckInViewModel` is shared KMP code. Platform notification scheduling is handled in `platformModule` via an `expect`/`actual` interface.

---

*Once approved, this spec goes to the Architect agent for technical design. No implementation begins before that review.*
