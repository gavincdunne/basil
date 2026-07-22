# Basil
*Built by WeekendWare*

Type 1 Diabetes doesn't pause. It's there at 2am when your CGM alerts. It's there before a run, before a meal, in every conversation where you have to explain yourself. People living with T1D carry a cognitive load that most health apps make heavier — tracking inputs, logging outputs, reducing a life to data points.

Basil is built on a different premise. Not a tracker. A companion. Something that knows what your days actually look like, remembers what you've shared, and is worth talking to — when you have a question, a hard moment, or just need to think out loud.

The conversation is the product. Everything else is infrastructure.

---

## Why this matters

There are roughly 9 million people living with Type 1 Diabetes globally. Most of them are already expert patients — they've been managing this since childhood. What they don't have is something that understands them as a whole person rather than a set of metrics.

Basil isn't a replacement for a care team. It's the thing you can talk to at 11pm that doesn't require an appointment.

The market for AI-powered chronic condition companions is early and largely unoccupied. Basil is built to own the T1D segment first, with the architecture to extend to adjacent conditions.

---

## Product

### What Basil does

- **Remembers.** Basil builds a real picture of the user over time — what they're managing, how they talk about it, what matters to them. Not a database. A context that makes every conversation better than the last.
- **Listens.** No forms. No dashboards. The entire interface is a conversation, and Basil meets the user where they are — not the other way around.
- **Respects the line.** Basil never gives medical advice. It never asks for BG numbers. It never makes the user feel like a data point. This is a hard constraint, not a guideline.

### The experience

The app is designed around time-of-day — color, tone, and lighting shift through four periods (morning, day, evening, night) with smooth 10-second transitions. It's a product that feels alive because it responds to the actual moment the user is in, not a generic session state.

<table>
  <tr>
    <td align="center"><img src=".github/assets/morning.png" width="180" alt="Morning theme"/><br/><sub>Morning · 6:32</sub></td>
    <td align="center"><img src=".github/assets/day.png" width="180" alt="Day theme"/><br/><sub>Day · 12:30</sub></td>
    <td align="center"><img src=".github/assets/evening.png" width="180" alt="Evening theme"/><br/><sub>Evening · 7:45</sub></td>
    <td align="center"><img src=".github/assets/night.png" width="180" alt="Night theme"/><br/><sub>Night · 11:47</sub></td>
  </tr>
</table>

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
- **Auth** — Supabase sign-up / sign-in / session restoration with OS-level splash gate
- **Navigation** — `NavHost`-based bottom tab bar (Home, Chat, Profile) and Settings destination
- **Theme** — `BasilColors`, `BasilSpacing`, `BasilTypography`, `BasilShapes` wired into MaterialTheme; 8 color schemes (4 time slots × light/dark), animated 10-second transitions driven by a background `viewModelScope` coroutine — the correct scheme is already in place before the user foregrounds the app
- **Crash reporting** — Sentry across all three targets with `PhiScrubber` stripping health data before any event leaves the device
- **CI/CD** — GitHub Actions running Detekt, Android compile + test, and iOS framework build on every push

**Screens**
- **Chat** — streaming conversation screen wired to the companion API
- **Onboarding** — conversational first-run flow that establishes the user's context (management type, how long they've been T1D, what they're looking for)
- **Profile** — name, email, and profile photo
- **Settings** — notifications placeholder and app version

### PHI Protection

T1D apps handle sensitive health data. Basil scrubs it before it can leave the device via [`PhiScrubber`](composeApp/src/commonMain/kotlin/org/weekendware/basil/crash/PhiScrubber.kt):

- User identity is removed from every Sentry event
- Exception messages from health-related packages (`chat`, `data`, `auth`) are cleared — exception type and stack trace are preserved for debugging
- Breadcrumb data payloads are wiped — navigation category and type are kept

This is table stakes for any health product. It ships from day one, not as a compliance afterthought.

---

## Roadmap

The foundation is in. What ships next is the core product.

- [ ] **Check-in system** — Basil reaches out. You respond. That exchange is stored and becomes the basis for everything that follows.
- [ ] **Persistent memory** — Basil builds a real picture of this person over time. Not a summary. A context.
- [ ] **Supabase data sync** — currently local SQLDelight only; sync layer in the architecture, not yet wired
- [ ] **HIPAA hardening** — SQLCipher, session timeout, audit log
- [ ] **Auth completion** — password reset, email verification
- [ ] **Push notifications**
- [ ] **RevenueCat subscription + message caps**
- [ ] **Desktop persistence** — file-backed SQLite driver

**Already shipped**
- [x] Build flavors (dev / staging / prod)
- [x] Supabase auth + user session
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
