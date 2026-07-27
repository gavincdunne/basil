# Basil
*Built by WeekendWare*

- **[The Basil Theme](.github/chapters/theme.md)**
- **[Auth](.github/chapters/auth.md)**

Type 1 Diabetes doesn't pause. It's there at 2am when your CGM alerts. It's there before a run, before a meal, in every conversation where you have to explain yourself. People living with T1D carry a cognitive load that most health apps make heavier — tracking inputs, logging outputs, reducing a life to data points.

Basil is built on a different premise. Not a tracker. A companion. Something that knows what your days actually look like, remembers what you've shared, and is worth talking to — when you have a question, a hard moment, or just need to think out loud.

The conversation is the product. Everything else is infrastructure.

---

## Why this matters

There are roughly 9 million people living with Type 1 Diabetes globally. Most of them are already expert patients — they've been managing this since childhood. What they don't have is something that understands them as a whole person rather than a set of metrics.

Basil isn't a replacement for a care team. It's the thing you can talk to at 11pm that doesn't require an appointment.

The market for AI-powered chronic condition companions is early and largely unoccupied. Basil is built to own the T1D segment first, with the architecture to extend to adjacent conditions.

---

## Features

- **Conversation-first** — no forms, no dashboards, no BG numbers. The entire interface is a conversation.
- **Persistent memory** — a real, evolving picture of who someone is, not a session that resets every time they open the app.
- **Time-of-day presence** — color, tone, and lighting shift through the day, so the app feels like it's actually there with the user.
- **A hard boundary** — never medical advice, never a data point. Support, not supervision.

---

## Engineering

### Stack

| Concern | Library |
|---|---|
| UI | Compose Multiplatform 1.8.1 |
| ViewModel | androidx.lifecycle 2.9.0 |
| DI | Koin 4.0.4 |
| Database | SQLDelight 2.0.1 |
| Backend / Auth | Supabase-kt 3.1.4 (auth, postgrest, storage) |
| Networking | Ktor 3.1.2 |
| Image loading | Coil 3.1.0 |
| Date/Time | kotlinx-datetime 0.6.0 |
| Crash reporting | Sentry Kotlin Multiplatform 0.25.0 |
| Static analysis | Detekt 1.23.7 + detekt-formatting |
| Testing | kotlin-test + Turbine — hand-written `Fake*Repository` test doubles, no mocking framework |

Kotlin Multiplatform targeting Android, iOS, and Desktop from a single shared codebase.

### Architecture

```
presentation/          Compose UI + ViewModels (MVVM)
domain/model/          Pure Kotlin domain models
domain/usecase/        Single-responsibility use cases
data/repository/       Repository interfaces + SQLDelight / Supabase implementations
data/local/database/   SQLDelight schema, queries, DatabaseDriverFactory
data/remote/           Ktor-based API client (chat)
di/                    Koin modules — shared + platform-specific
```

Each layer depends only on the layer below it. ViewModels and use cases depend on repository *interfaces*, keeping them testable without a real database. ViewModels extend `androidx.lifecycle.ViewModel` and are scoped to their nav destination via `koinViewModel<T>()`.

### What's built

**App infrastructure**
- **Auth** — see [Auth](.github/chapters/auth.md)
- **Navigation** — state-based routing driven by `AnimatedContent` and pure functions (`unauthenticatedDestination()`, `authenticatedDestination()`) rather than a navigation library; bottom tab bar (Profile, Chat, More); Settings slides in as a full-screen overlay via `graphicsLayer` translation
- **Theme** — see [The Basil Theme](.github/chapters/theme.md)
- **Crash reporting** — Sentry across all three targets with `PhiScrubber` stripping health data before any event leaves the device
- **CI/CD** — GitHub Actions running Detekt, Android compile + test, and iOS framework build on every push

**Screens**
- **Chat** — streaming conversation screen wired to the companion API, with a dismissible email-verification banner
- **Onboarding** — conversational first-run flow, runs entirely pre-auth (name, management type, how long they've had T1D, what they're hoping for)
- **Save your progress** — account creation at the end of onboarding, not the start
- **Profile** — name, email, and profile photo
- **Settings** — notifications placeholder, app version, sign out

### PHI Protection

T1D apps handle sensitive health data. Basil scrubs it before it can leave the device via [`PhiScrubber`](composeApp/src/commonMain/kotlin/org/weekendware/basil/crash/PhiScrubber.kt):

- User identity is removed from every Sentry event
- Exception messages from health-related packages (`chat`, `data`, `auth`) are cleared — exception type and stack trace are preserved for debugging
- Breadcrumb data payloads are wiped — navigation category and type are kept

This is table stakes for any health product. It ships from day one, not as a compliance afterthought.

---

## Roadmap

The foundation is in. What ships next is the core product.

- [ ] **Passkeys** — the contract and biometric-attempt UI states are scaffolded and tested, but implementation is blocked on DevOps prerequisites (`apple-app-site-association` / `assetlinks.json` on a live production domain) — see [Auth](.github/chapters/auth.md)
- [ ] **Check-in system** — Basil reaches out. You respond. That exchange is stored and becomes the basis for everything that follows.
- [ ] **Persistent memory** — Basil builds a real picture of this person over time. Not a summary. A context.
- [ ] **Chat history sync** — conversation history not yet persisted to Supabase; each session is stateless
- [ ] **HIPAA hardening** — SQLCipher, session timeout, audit log
- [ ] **Push notifications**
- [ ] **RevenueCat subscription + message caps**
- [ ] **Desktop persistence** — file-backed SQLite driver

**Already shipped**
- [x] Build flavors (dev / staging / prod)
- [x] Full silent account provisioning — onboarding runs pre-auth, account creation deferred to "save your progress"
- [x] Email/password + native Google/Apple sign-in, email verification, password reset, sign-out
- [x] Conversational onboarding
- [x] Time-of-day color system with animated transitions
- [x] Profile and settings screens
- [x] Sentry crash reporting with PHI scrubbing
- [x] AI chat screen (UI + ViewModel)

---

## Running Locally

**Android**
```
./gradlew :composeApp:assembleDebug
```

**Desktop**

Requires Java 21. If `java -version` shows Java 26 or later, prefix with `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home` or set it permanently in your shell profile.

```
./gradlew :composeApp:run
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run on any iOS 18.2+ simulator.

> `Sentry.xcframework` (Sentry Cocoa 8.57.3) must be present at `iosApp/Sentry.xcframework`. Download from the [sentry-cocoa releases](https://github.com/getsentry/sentry-cocoa/releases/tag/8.57.3) and unzip into `iosApp/`.

**Tests**
```
./gradlew test
```

**Static analysis**
```
./gradlew detekt
```

---

## License

MIT
