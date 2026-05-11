# Basil – Type 1 Diabetes Management
*Built by WeekendWare*

Basil is a Kotlin Multiplatform app for managing life with Type 1 diabetes. One codebase, three targets — Android, iOS, and desktop — built with Compose Multiplatform and a clean layered architecture.

---

## Screenshots

| Desktop | iOS | Android |
|:---:|:---:|:---:|
| ![Desktop](docs/screenshots/desktop.png) | ![iOS](docs/screenshots/ios.png) | ![Android](docs/screenshots/android.png) |

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
| Date/Time | kotlinx-datetime 0.6.0 |
| Crash reporting | Sentry Kotlin Multiplatform 0.25.0 |
| Static analysis | Detekt 1.23.7 + detekt-formatting |
| Testing | kotlin-test + Mockito-Kotlin 5.4.0 |

### Architecture

```
presentation/          Compose UI + ViewModels (MVVM)
domain/model/          Pure Kotlin domain models
domain/usecase/        Single-responsibility use cases
data/repository/       Repository interfaces + SQLDelight implementations
data/local/database/   SQLDelight schema, queries, DatabaseDriverFactory
di/                    Koin modules — shared + platform-specific
```

Each layer depends only on the layer below it. ViewModels and use cases depend on repository *interfaces*, keeping them testable without a real database. ViewModels extend `androidx.lifecycle.ViewModel` and are scoped to their nav destination via `koinViewModel<T>()`.

### What's built

- **Dashboard** — last BG reading card with glucose status colouring, today's entry timeline, empty states
- **Log entry** — bottom sheet for logging BG, insulin, and carbs; BG unit preference (mg/dL / mmol/L) persisted across sessions
- **Profile** — name, email, and target BG range with inline editing
- **Settings** — BG unit toggle with persisted preference; notifications placeholder
- **Navigation** — `NavHost`-based navigation with bottom tab bar and settings destination
- **Theme** — custom `BasilColors`, `BasilSpacing`, `BasilTypography`, `BasilShapes` wired into MaterialTheme
- **Auth** — Supabase sign-up / sign-in / session restoration with OS-level splash gate
- **Data layer** — `LogRepository`, `PreferencesRepository`, `UserRepository` backed by SQLDelight
- **Crash reporting** — Sentry across all three targets with `PhiScrubber` stripping health data (BG values, insulin doses, carbs) before any event leaves the device
- **CI/CD** — GitHub Actions running Detekt, Android compile + test, and iOS framework build on every push

### PHI Protection

T1D apps handle sensitive health data. Basil takes a conservative scrubbing approach via [`PhiScrubber`](composeApp/src/commonMain/kotlin/org/weekendware/basil/crash/PhiScrubber.kt):

- User identity is removed from every Sentry event
- Exception messages from health-data packages (`logging`, `dashboard`, `data`, `auth`) are cleared — the exception *type* and *stack trace* are preserved for debugging
- Breadcrumb data payloads are wiped — navigation category and type are kept
- Event contexts are cleared

The scrubbing logic has its own unit test suite ([`PhiScrubberTest`](composeApp/src/desktopTest/kotlin/org/weekendware/basil/crash/PhiScrubberTest.kt)) covering all health package variants, mixed exception lists, case sensitivity, and breadcrumb field preservation.

---

## Roadmap

- [x] Build flavors (dev / staging / prod)
- [x] Supabase auth + user session
- [x] Dashboard with BG status colouring and entry timeline
- [x] Log entry sheet (BG, insulin, carbs) with persisted unit preference
- [x] Profile screen (name, email, target BG range)
- [x] Settings screen (BG unit toggle)
- [x] Sentry crash reporting with PHI scrubbing
- [ ] AI assistant chat (Basil tab) — Rust/Axum API + Anthropic
- [ ] History and trends view with charting
- [ ] Supabase data sync (currently local SQLDelight only)
- [ ] Push notifications
- [ ] User onboarding flow
- [ ] RevenueCat subscription + message caps
- [ ] HIPAA hardening (SQLCipher, session timeout, audit log)
- [ ] Auth completion (password reset, email verification)
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

> Note: `Sentry.xcframework` (Sentry Cocoa 8.57.3) must be present at `iosApp/Sentry.xcframework`. Download from the [sentry-cocoa releases](https://github.com/getsentry/sentry-cocoa/releases/tag/8.57.3) and unzip into `iosApp/`.

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
