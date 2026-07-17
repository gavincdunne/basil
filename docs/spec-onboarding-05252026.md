# First-Open Onboarding — Spec

**Status:** Draft  
**Date:** 2026-05-25  
**Author:** Gavin Dunne (PM: Claude)  
**Project:** Basil

---

## Summary

Onboarding is the moment Basil proves it is a companion, not a tracker. The first-open flow must establish Basil's voice and personality before the user ever types a word — replacing the standard "fill in your health profile" setup pattern with a short, warm, conversational introduction that ends with the user's first real check-in.

---

## Problem statement

Every T1D app the user has downloaded starts the same way: a series of forms asking for diagnosis year, insulin type, target ranges, CGM model. That interaction establishes the app as a data-capture tool from the first second. Basil's entire value proposition is that it is not that — but if the first screen looks like a setup wizard, the user won't believe it. The companion relationship has to begin before the user has a chance to classify Basil as "another tracker."

Concretely: a newly diagnosed adult opens Basil for the first time, exhausted by the cognitive load of their diagnosis. The last thing they need is another form. What they need is to feel heard. Onboarding is the first and best chance to deliver that.

---

## User stories

*As a first-time Basil user, I want to feel welcomed by Basil as a person — not prompted to complete a setup form — so that I understand immediately that this app is different from every other T1D tool I've tried.*

*As a newly diagnosed adult with T1D, I want to be asked how I'm doing before I'm asked for any health data, so that I feel like Basil sees me as a person, not a data source.*

*As a long-term T1D patient, I want to tell Basil a little about my experience before the app is "ready," so that Basil has something to remember about me from day one.*

*As a user completing onboarding, I want my first real check-in exchange to happen before I leave the onboarding flow, so that I've had a genuine interaction — not just a tutorial — before I reach the home screen.*

---

## Acceptance criteria

1. Basil sends the first message. The user is never shown a blank text input as their first interaction.
2. No form fields, dropdowns, or structured data-entry screens appear during onboarding. All information gathered is conversational.
3. Onboarding collects at minimum: what the user would like to be called, and one piece of free-text context about their T1D experience (how long they've had it, how they're feeling about it, or what they're hoping for from Basil — user chooses what to share).
4. Onboarding does not ask for: A1C, insulin doses, target ranges, CGM model, weight, or any clinical parameters. Those remain optional and are surfaced later through normal conversation, not setup.
5. The flow ends with the user's first genuine check-in turn — a free-text response to Basil's opening question — before the user reaches the home screen.
6. Basil's response to the first check-in turn uses the AI chat layer (not a scripted fallback), so the user's first experience of Basil's voice is the real companion, not a mock.
7. Guardrails are fully active during onboarding. If the user's first message contains a crisis signal, the crisis template fires correctly.
8. Onboarding state is persisted. If the user closes the app mid-flow and returns, they resume where they left off and do not see the intro again.
9. Users who have already completed onboarding never see the intro flow again, even after sign-out and sign-back-in on the same device.
10. Onboarding completes in under 5 screens / message exchanges total (intro message + 2–3 questions + first check-in response).
11. The name the user provides during onboarding is used by Basil in subsequent conversations.
12. All strings shown during onboarding are in `strings.xml` — no hardcoded text in composables.

---

## Out of scope (v1)

- Collecting clinical parameters (insulin type, target range, pump/CGM model) during onboarding. These can be added later from settings or surfaced conversationally.
- Animated or video intro sequences. The character is established through writing, not production value.
- Skip / "I'll do this later" option for the first check-in turn. v1 requires the first check-in to complete onboarding. A skip path can be added in v2 if data shows meaningful drop-off.
- Onboarding for users migrating from a previous version of the app. This spec covers new account creation only.
- Notifications permission request during onboarding. Permissions are requested contextually in v1 (when the user first encounters a feature that needs them).
- A/B testing of onboarding copy or flow variants. Ship one thoughtful version first.
- Clinician or caregiver onboarding paths. Basil is patient-facing only.
- Desktop-specific onboarding design. Android and iOS are the launch targets; desktop inherits the same flow.

---

## Dependencies

**Must be true before build begins:**

- Supabase auth is functional. The user must have an account before onboarding starts. Onboarding sits between auth completion and the home screen.
- The basil-chat-api is functional end-to-end. The final check-in exchange in onboarding must use the real AI layer, not a scripted response. This is the primary technical dependency — if the chat API is not ready, onboarding cannot ship in its full form.
- The system prompt (assistant_v1.md) is written and reviewed. Basil's onboarding voice must match its ongoing voice. The first check-in response is the system prompt's first real impression.

**Nice to have but not blocking v1:**

- Persistent memory layer. Ideally, the name and context the user shares during onboarding is written into Basil's memory so it persists across sessions. In v1, this data can be stored in a local structured table (e.g., `user_profile` in SQLDelight) and injected into the system prompt context, even without the full memory architecture.

**External:**

- Anthropic BAA signed before onboarding ships to any user. The first check-in turn sends user input to the Claude API. No BAA, no live AI response in onboarding.

---

## Risks and open questions

| Risk / Question | Owner | Resolution |
|---|---|---|
| What exactly does Basil say in the intro? The copy is load-bearing — it sets the entire relationship tone. This spec does not write that copy; it needs a dedicated writing pass. | Gavin | TBD before implementation |
| Users may share emotionally heavy content in their very first check-in (e.g., "I was just diagnosed and I'm terrified"). The AI response to this first turn must be warm and competent. Test the crisis guardrail path in onboarding explicitly. | Gavin | Include crisis-signal test cases in onboarding QA checklist |
| The "no form fields" constraint means Basil must extract the user's name from free text (e.g., "Call me Sarah" or just "Sarah"). A parsing step is needed — or Basil can ask a direct question that still feels conversational ("What would you like me to call you?"). Decide approach before build. | Gavin | TBD — lean toward direct conversational question |
| If the chat API is unavailable during onboarding, the first check-in turn will fail. Define a graceful fallback: scripted warm response, with a note that Basil will respond fully once connected. Flag this clearly so users aren't confused. | Gavin | TBD — define fallback copy and retry behavior |
| Onboarding data (name, first T1D context) is personal narrative. It is not structured PHI in the clinical sense, but it is identifiable and sensitive. Confirm HIPAA posture for storing this in SQLDelight and/or Supabase. | Gavin | Carry existing HIPAA-conscious design forward; confirm with privacy review before launch |
| Does onboarding need to mention that Basil is an AI? Given FTC guidance on AI disclosure and the "meeting a companion" framing, this needs a deliberate decision — not an oversight. | Gavin | TBD — lean toward a natural, non-legalistic disclosure woven into Basil's intro |

---

## Platform targets

**Android and iOS** — primary launch targets. Full onboarding flow ships on both.

**Desktop (JVM)** — inherits the same flow with no platform-specific differences in v1. Desktop is a lower-priority surface; revisit if usage data shows meaningful desktop onboarding drop-off.

**Platform-specific notes:**
- Back-gesture behavior on Android during onboarding: tapping back during the intro sequence should not exit to auth or produce a broken state. Define explicit back-stack behavior (likely: suppress back during onboarding, or treat back as "previous message" if applicable).
- iOS safe area and keyboard insets must be handled correctly for the chat-style input used in the first check-in turn.

---

## What happens after the spec

Once Gavin approves this spec, the next step is a writing pass on the onboarding copy — the exact words Basil uses to introduce itself. That copy is the product design, and it should happen before the Architect agent designs the implementation. A companion whose intro is placeholder text ships as a tool.

After copy is approved, hand to the Architect agent to design:
- Onboarding state machine and navigation flow
- `user_profile` schema (name, onboarding completion flag, first check-in content)
- Integration point between onboarding and the chat API
- Fallback behavior when the chat API is unavailable
