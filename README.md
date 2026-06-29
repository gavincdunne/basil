# Basil
*Built by WeekendWare*

Basil is an AI companion for people living with Type 1 Diabetes. Not a tracker. Not a charting tool. A companion — something that knows what your life with T1D actually feels like, remembers what you tell it, and is useful to talk to when you have a question, a rough day, or just need to process something.

---

## Engineering

### Stack

| Concern | Library |
|---|---|
| UI | Compose Multiplatform 1.8.1 |
| Navigation | Compose Multiplatform Navigation 2.8.0-alpha13 |
| ViewModel | androidx.lifecycle 2.9.0 |
| DI | Koin 4.0.4 |
| Database | SQLDelight 2.0.1 |
| Networking | Ktor |
| Date/Time | kotlinx-datetime 0.6.0 |
| Crash reporting | Sentry Kotlin Multiplatform 0.25.0 |
| Static analysis | Detekt 1.23.7 + detekt-formatting |
| Testing | kotlin-test + Mockito-Kotlin 5.4.0 |

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
- **Auth** — Supabase sign-up / sign-in / session restoration with OS-level splash gate
- **Navigation** — `NavHost`-based navigation with bottom tab bar (Home, Chat, Profile) and Settings destination
- **Theme** — custom `BasilColors`, `BasilSpacing`, `BasilTypography`, `BasilShapes` wired into MaterialTheme
- **Crash reporting** — Sentry across all three targets with `PhiScrubber` stripping health data before any event leaves the device
- **CI/CD** — GitHub Actions running Detekt, Android compile + test, and iOS framework build on every push

**Data & context**
- **Dashboard** — last BG reading card with glucose status colouring, today's entry timeline
- **Log entry** — bottom sheet for logging BG, insulin, and carbs; BG unit preference persisted across sessions
- **Profile** — name, email, profile photo (Supabase Storage), and target BG range

**AI chat tab**
- **Chat** — streaming chat screen, `ChatViewModel`, and `KtorChatRepository` wired end-to-end with `basil-chat-api`

### Companion service

[`basil-chat-api`](https://github.com/gavincdunne/basil-chat-api) — a Rust/Axum service that proxies streaming requests to the Anthropic API. Enforces an API key gate and context window cap.

### PHI Protection

T1D apps handle sensitive health data. Basil takes a conservative scrubbing approach via [`PhiScrubber`](composeApp/src/commonMain/kotlin/org/weekendware/basil/crash/PhiScrubber.kt):

- User identity is removed from every Sentry event
- Exception messages from health-data packages (`logging`, `dashboard`, `data`, `auth`) are cleared — the exception *type* and *stack trace* are preserved for debugging
- Breadcrumb data payloads are wiped — navigation category and type are kept

The scrubbing logic has its own unit test suite covering all health package variants, mixed exception lists, case sensitivity, and breadcrumb field preservation.

---

## Roadmap

- [ ] **Check-in system** — the core product interaction: a daily conversational prompt, free-text response, Basil reply, stored as the foundation for persistent memory
- [ ] **Onboarding**
- [ ] **Persistent memory** — Basil builds an understanding of this specific person over time
- [x] Build flavors (dev / staging / prod)
- [x] Supabase auth + user session
- [x] Dashboard, log entry, profile, settings screens
- [x] Sentry crash reporting with PHI scrubbing
- [x] AI chat screen (UI + ViewModel)
- [ ] Deploy `basil-chat-api`
- [ ] Supabase data sync (currently local SQLDelight only)
- [ ] HIPAA hardening (SQLCipher, session timeout, audit log)
- [ ] Auth completion (password reset, email verification)
- [ ] Push notifications
- [ ] RevenueCat subscription + message caps
- [ ] Desktop persistence (file-backed SQLite driver)

---

## Running Locally

**Android**
```
./gradlew :composeApp:assembleDebug
```

**Desktop**
```
./gradlew :composeApp:run
```

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run on any iOS 18.2+ simulator.

> `Sentry.xcframework` (Sentry Cocoa 8.57.3) must be present at `iosApp/Sentry.xcframework`. Download from the [sentry-cocoa releases](https://github.com/getsentry/sentry-cocoa/releases/tag/8.57.3) and unzip into `iosApp/`.

**AI chat**

Add to `local.properties`:

```
chat.api.url=http://localhost:8080
chat.api.key=<your API key>
```

Start `basil-chat-api`:
```
cargo run
```

**Tests**
```
./gradlew desktopTest
```

**Static analysis**
```
./gradlew detekt
```

---

## License

MIT
