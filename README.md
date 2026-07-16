# Basil
*Built by WeekendWare*

Most health apps treat you like a data point. Basil doesn't. It's a companion for people living with Type 1 Diabetes — something that knows what your days actually look like, remembers what you've shared, and is worth talking to when you have a question, a hard moment, or just need to think out loud.

The conversation is the product. Everything else is infrastructure.

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
- **Navigation** — `NavHost`-based bottom tab bar (Home, Chat, Profile) and Settings destination
- **Theme** — `BasilColors`, `BasilSpacing`, `BasilTypography`, `BasilShapes` wired into MaterialTheme
- **Crash reporting** — Sentry across all three targets with `PhiScrubber` stripping health data before any event leaves the device
- **CI/CD** — GitHub Actions running Detekt, Android compile + test, and iOS framework build on every push

**Screens**
- **Chat** — streaming conversation screen wired to the companion API
- **Profile** — name, email, and profile photo
- **Settings** — notifications placeholder and app version

### PHI Protection

T1D apps handle sensitive health data. Basil scrubs it before it can leave the device via [`PhiScrubber`](composeApp/src/commonMain/kotlin/org/weekendware/basil/crash/PhiScrubber.kt):

- User identity is removed from every Sentry event
- Exception messages from health-related packages (`chat`, `data`, `auth`) are cleared — exception type and stack trace are preserved for debugging
- Breadcrumb data payloads are wiped — navigation category and type are kept

---

## Roadmap

The foundation is in. What ships next is the core product.

- [ ] **Check-in system** — Basil reaches out. You respond. That exchange is stored and becomes the basis for everything that follows.
- [ ] **Onboarding** — first conversation, context setting, the moment Basil becomes yours
- [ ] **Persistent memory** — Basil builds a real picture of this person over time
- [x] Build flavors (dev / staging / prod)
- [x] Supabase auth + user session
- [x] Profile and settings screens
- [x] Sentry crash reporting with PHI scrubbing
- [x] AI chat screen (UI + ViewModel)
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
