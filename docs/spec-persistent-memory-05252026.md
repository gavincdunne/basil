# Persistent Memory — Spec

**Status:** Draft  
**Date:** 2026-05-25  
**Author:** Gavin Dunne (PM: Claude)  
**Project:** Basil

---

## Summary

Basil needs to remember things about users across conversations so the companion experience can actually deliver on its core promise. Without cross-session memory, every conversation starts from zero — Basil is a capable chatbot, not a companion.

---

## Problem statement

Today every Basil chat session starts fresh. A user who has told Basil three times that they always go low after long walks has to tell it again. A user who mentioned a stressful month at work last week gets no acknowledgement of that context the next time they check in. The "knows you" value proposition — the thing that makes Basil genuinely different from every other AI chat — cannot be delivered without a memory layer.

Concrete scenario: A T1D user has a rough check-in on a Wednesday, tells Basil they have been fighting unexplained highs all week and that work stress is probably a factor. They come back on Friday. Basil has no idea any of that happened. The user has to re-establish context before Basil can be useful. After enough of these resets, the companion relationship feels hollow — or the user stops opening the app.

---

## PM interview — questions and answers

*This section documents the simulated interview used to scope this spec.*

**Q: What specific user problem does this solve?**  
Every conversation starts fresh. The companion experience depends on continuity — the longer you use Basil, the better it knows you. Without memory, that value can never accumulate. Users re-explain their context each session, Basil cannot surface patterns, and the relationship never deepens.

**Q: Who benefits most from this?**  
All Basil users, but most acutely: long-term users who are already using Basil regularly for check-ins and want it to reference their history. Also newly diagnosed users who are establishing patterns — having those patterns remembered and reflected back is where Basil's companion value is clearest.

**Q: If this ships and works, what does the user do differently?**  
Users stop re-explaining themselves. Basil opens a new session with awareness of recent emotional context and known T1D patterns. Conversations feel continuous. Users notice that Basil "paid attention" to what they said last time — the same way you would notice if a friend remembered something you told them.

**Q: What's the simplest v1 that would still be valuable?**  
A curated memory store: facts about the user extracted from conversations and stored as structured entries (life context, T1D patterns, expressed goals, notable events). Each conversation injects a distilled summary of relevant memories into the system prompt context. No UI required in v1 — the user does not need to see or manage memories yet; they just experience Basil knowing them better.

**Q: What dependencies exist?**  
End-to-end AI chat must be working first. User authentication (Supabase) is already in place and provides the user_id needed to scope memories. The memory store needs to be server-side (not device-local) to persist across platforms and survive app reinstalls. Anthropic BAA must be signed before any PHI-containing memories reach the model.

**Q: What are the risks?**  
PHI — memories will contain health-related information. Stale or wrong memories — if Basil extracts something incorrect from context and stores it, that incorrect fact could be worse than no memory (e.g., incorrectly inferring a pattern and reflecting it back confidently). Clinical boundary — Basil can hold the pattern "I always go low after exercise" but must not act on it in a clinical way. User trust — people need to know what is being remembered about them and have the ability to delete it. That is out of scope for v1 but must be designed for.

---

## Confirmation of understanding

The goal is to give Basil persistent knowledge of each user that builds across sessions. The user pain is that every conversation starts from zero, making the companion feel like a stranger each time and preventing the relationship from deepening. A v1 would be a curated, user-scoped memory store — structured facts and patterns extracted from conversations — that gets injected as summarised context into each new session's system prompt. Memory management UI (viewing and deleting memories) is v2.

---

## User stories

1. *As a T1D user who checks in with Basil regularly, I want Basil to remember patterns I have mentioned in past sessions, so that I do not have to re-explain my context every time I open the app.*

2. *As a user who told Basil about a stressful week at work, I want Basil to reference that context in future conversations, so that the companion relationship feels continuous rather than transactional.*

3. *As a user with a known T1D pattern ("I always go low after long walks"), I want Basil to hold that knowledge and factor it in naturally when it is relevant, so that Basil's responses reflect what it already knows about my body.*

4. *As a user, I want my remembered context to improve Basil's responses without Basil reciting my memories back to me robotically, so that the experience feels like a knowledgeable friend rather than a database lookup.*

5. *As a user, I want to know that Basil's memory of me is scoped to my account only and cannot be accessed by anyone else, so that I can share sensitive context without fear.*

---

## Acceptance criteria

1. After a user session where the user mentions a T1D pattern or meaningful life context, that information is extracted and persisted as a memory entry scoped to that user's account.
2. When a new conversation session begins, the most relevant memory entries for that user are included in the system prompt context block sent to the model.
3. The total tokens contributed by the memory block do not exceed 1,000 tokens per turn (to stay within the existing 6,000-token input budget defined in the architecture doc).
4. Basil references known user context naturally in responses — it does not recite memories verbatim or announce "I remember that you said..."
5. Memories are stored server-side (Supabase/Postgres), scoped by user_id, and are not accessible across users.
6. Memory extraction does not log raw conversation text beyond what is already retained under the existing 30-day audit window. Memory entries store distilled facts, not verbatim quotes.
7. Memories containing health-related information are treated as PHI and subject to the same data-handling rules as log data (TLS, BAA-covered path to the model, no third-party model provider without a separate BAA).
8. If the memory extraction step fails (e.g., the extraction model call errors), the conversation continues without memory injection rather than failing the user's turn.
9. The guardrail layer continues to apply to all responses regardless of what memories are injected — memory does not bypass any safety check.
10. Unit tests cover: memory extraction logic, memory retrieval and ranking, memory injection into the context block, and graceful degradation on extraction failure.

---

## Out of scope (v1)

- Memory management UI — users cannot yet view, edit, or delete individual memories. This is a required v2 item and must be designed for before v1 ships (data model must support deletion at the row level).
- Memory export or portability.
- User-configurable memory categories (e.g., "remember this, not that").
- CGM or structured log data feeding the memory layer — v1 memory is conversational/narrative only. Structured log data already goes into the 30-day context window separately.
- Automatic memory expiry or decay (e.g., time-weighted relevance). Simple retention in v1; expiry logic is v2.
- Cross-user memory (family or care partner context). Out of scope entirely until safeguarding is designed.
- Memories surfaced to the user as a "what Basil knows about you" view.
- Any memory feature accessible on the free tier. Persistent memory is a Basil Care (paid) feature per the monetisation model.

---

## Dependencies

| Dependency | Status | Notes |
|---|---|---|
| End-to-end AI chat working (basil-chat-api wired in) | Blocking | Memory is only useful once chat is live. Build this after chat is stable. |
| Anthropic BAA signed | Blocking | Memories containing PHI cannot be sent to the model until the BAA is in place. |
| Supabase user auth (JWT, user_id) | Done | Needed to scope memory entries per user. |
| Supabase Postgres (server-side storage) | Done | Memory entries go here. A new `user_memory` table is required. |
| Memory extraction model call design | Required | Need to decide: same Claude call with an extraction prompt, or a separate lightweight extraction pass after each turn? See open questions. |
| Subscription/tier gating (RevenueCat or equivalent) | Not yet built | Memory must be gated behind the paid tier. Can be soft-gated (feature flag) in v1 if RevenueCat is not yet integrated. |

---

## Risks and open questions

| Risk / Question | Owner | Resolution |
|---|---|---|
| Stale or incorrect memories — model extracts a wrong fact and it becomes persistent context that misleads future responses | Gavin | Design extraction prompt to be conservative (only extract explicitly stated facts, not inferences). Add a confidence threshold. Flag for v2 memory management UI as a correction mechanism. |
| Memory injection pushes total context over token budget | Gavin | Cap memory block at 1,000 tokens. Rank memories by recency and relevance; truncate to fit. Monitor token usage per turn post-launch. |
| PHI in memory entries — health patterns and emotional context are likely PHI under HIPAA | Gavin | Treat all memory entries as PHI. Apply same data handling rules as log data. Do not store verbatim quotes; distil to structured facts. Ensure deletion-at-row-level is possible before v1 ships. |
| Clinical boundary — Basil holds a pattern ("I go low after exercise") and starts giving advice that crosses the wellness/clinical line | Gavin | Memory injection does not bypass guardrails. Extraction prompt explicitly excludes dosing facts and clinical recommendations. System prompt clause covers this; add a test case to the eval golden set. |
| Memory extraction approach — when and how are memories extracted? | Gavin | Open question: (a) synchronous extraction pass at end of each turn, (b) async background job after session ends, or (c) periodic batch extraction. Recommendation: async job after session ends to avoid adding latency to live turns. Decision required before implementation. |
| User trust and transparency — users may not know Basil is building a persistent profile | Gavin | Add a clear disclosure during onboarding for paid-tier users. Memory management UI is already planned for v2 — must ship before the memory layer has been in production for more than one release cycle. |
| Free-tier users get no memory — does that create a frustrating half-experience? | Gavin | Consistent with the monetisation model: free tier is a capable chatbot, paid tier is the companion. Make the upgrade path clear in-app. |

---

## Platform targets

- **Android** — Yes, primary.
- **iOS** — Yes, primary.
- **Desktop (JVM)** — Yes, same implementation (memory is server-side; no platform-specific code required on client).
- **API (basil-chat-api, Rust/Axum)** — Yes, this is where memory injection and extraction live. The client platforms are not involved in memory logic beyond receiving the Basil response.

No platform-specific differences in v1. All memory logic lives in the backend (basil-chat-api + Supabase).

---

## What happens after this spec

Once Gavin approves this spec, it goes to the **Architect agent** to design the technical approach — specifically: the `user_memory` table schema, the extraction prompt design, the memory ranking/retrieval algorithm, and the injection strategy within the existing context-assembly flow in `ContextFn`. No implementation begins until the spec is approved and the architecture is designed.
