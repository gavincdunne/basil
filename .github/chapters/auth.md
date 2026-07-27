← [Back to README](../../README.md) · Previous: [← Basil Theme](theme.md)

# Chapter: Auth

Basil's auth system is built around one idea: the conversation comes first. A brand-new user never sees a login screen before they've talked to Basil — onboarding runs entirely before any account exists, and account creation happens at the end, framed as "save your progress," not "sign up."

## Full silent account provisioning

Where an unauthenticated user lands is a pure function of two persisted facts, not a fixed screen:

| State | Destination |
|---|---|
| Onboarding conversation not finished | **Onboarding** — the conversational flow (name → how they manage T1D → how long they've had it → what they're hoping for) |
| Onboarding finished, no account created yet | **Save your progress** — account creation, framed as preserving what they've already shared, not a cold sign-up form |
| Onboarding finished, account exists but currently signed out | **Sign in** — the standard auth screen, for a returning user |

This routing lives in [`unauthenticatedDestination()`](../../composeApp/src/commonMain/kotlin/org/weekendware/basil/presentation/session/SessionViewModel.kt), a pure function `SessionViewModel` derives by combining the local onboarding-complete flag with whether this device has ever completed a real sign-up. Zero AI inference happens during onboarding — every interaction before an account exists is written to local storage only, guarded end-to-end (`OnboardingViewModel` checks `userId != null` before every Supabase call). The moment an account is created, [`SyncOnboardingToSupabaseUseCase`](../../composeApp/src/commonMain/kotlin/org/weekendware/basil/domain/usecase/SyncOnboardingToSupabaseUseCase.kt) pushes everything collected during onboarding up in one shot.

## Signing in

- **Email + password**, with a detect-by-email heuristic: type an email that matches this device's last-used one and the screen advances straight to a password field; anything else shows a "no account found" state with a path back into onboarding. (True server-side email lookup isn't possible — Supabase's enumeration protection rules it out — so this is a client-side heuristic, not a real account check.)
- **Google and Apple**, via `compose-auth`'s native credential flow — Android gets the real `CredentialManager` picker for Google, iOS gets native `AuthenticationServices` for Apple; every other platform/provider combination (iOS-Google, Android-Apple, all of Desktop) falls back to a standard OAuth browser redirect automatically, no extra code needed on our side for that fallback.
- **Passkeys** — scaffolded (the `PasskeyManager` contract and `AuthUiState`/`AuthViewModel` biometric-attempt states exist and are tested) but not implemented or wired into UI. The TDD gates this explicitly behind DevOps prerequisites — `apple-app-site-association` and `assetlinks.json` served from a live production domain — that don't exist yet.

## After sign-in

- **Email verification** — a dismissible banner in chat for a recently-signed-up unverified user; a full-screen soft block (`VerificationWallScreen`) if 30+ days have passed unverified. Both are driven by the same pure `authenticatedDestination()` function, mirroring the unauthenticated-side routing.
- **Password reset** — request a reset link, deep link (`basil://reset-password`) brings the user back into the app to set a new one. Deep links are validated against an exact allow-list (`DeepLinkValidator`) before anything is exchanged for a session — a naive prefix check would let `basil://reset-password-evil.com` through, so the real check is boundary-aware.
- **Sign out** — a row at the bottom of Settings, calling straight through to `AuthRepository.signOut()`. No confirmation dialog currently — an open question, not yet resolved either way.

## What's not yet verified

The full routing rearchitecture above compiles clean and passes 246 automated tests across Android, iOS, and Desktop, but hasn't been walked through screen-by-screen on a physical device yet in this state — that's the next real-world check before calling it done. A cold-launch crash *was* caught and fixed this way (`hasPasskeyEnrolled()` was a `TODO()` stub that unit tests never exercised because they only ever run against a fake repository) — a reminder that the test suite proves correctness, not that the app actually runs.

← [Back to README](../../README.md) · Previous: [← Basil Theme](theme.md)
