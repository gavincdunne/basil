# Basil – Type 1 Diabetes Management
*Built by WeekendWare*

Basil is a Kotlin Multiplatform app for managing life with Type 1 diabetes. One codebase, three targets — Android, iOS, and desktop — built with Compose Multiplatform, a clean layered architecture, and an emphasis on correctness.

---

## Targets

| Platform | Status |
|---|---|
| Android | Running |
| iOS (Simulator) | Running |
| Desktop (JVM) | Running |

---

## Architecture

```
presentation/          Compose UI + ViewModels (MVVM)
domain/model/          Pure Kotlin domain models — no framework dependencies
data/repository/       Repository interfaces + SQLDelight implementations
data/local/database/   SQLDelight schema, generated queries, DatabaseDriverFactory
di/                    Koin modules — shared + platform-specific
```

Each layer depends only on the layer below it. ViewModels and use cases depend on repository *interfaces*, not the SQLDelight implementations — keeping them testable without a real database.

---

## Stack

| Concern | Library |
|---|---|
| UI | Compose Multiplatform 1.8.1 |
| Navigation | Voyager 1.0.0 |
| DI | Koin 4.0.4 |
| Database | SQLDelight 2.0.1 |
| Date/Time | kotlinx-datetime 0.6.0 |
| Static analysis | Detekt 1.23.7 + detekt-formatting (ktlint) |
| Testing | kotlin-test + Mockito-Kotlin 5.4.0 |

---

## What's Built

- **Dashboard** — last BG reading summary card with glucose status colouring, today's entry timeline, empty states
- **Log entry sheet** — bottom sheet for logging BG, insulin, and carbs; validates and persists via `LogRepository`
- **Preferences** — persisted BG unit preference (mg/dL or mmol/L) via `PreferencesRepository`
- **Theme system** — custom `BasilColors`, `BasilSpacing`, `BasilTypography`, `BasilShapes` wired into MaterialTheme
- **Repository layer** — `LogRepository`, `PreferencesRepository`, `UserRepository` interfaces with SQLDelight-backed implementations
- **CI/CD** — GitHub Actions running Detekt, Android compile + test, and iOS framework build on every push

---

## What's Next

- Flow-based SQLDelight queries (live UI updates)
- Coroutine-scoped ViewModels
- Use case layer
- `Result<T>` error handling
- Migrate to Compose Multiplatform Navigation
- Build flavors (dev / staging / prod)
- Supabase auth + user session
- Sentry crash reporting

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

**iOS** — open `iosApp/iosApp.xcodeproj` in Xcode and run on any iOS 16+ simulator.

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
