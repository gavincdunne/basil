# Basil — Claude Code Rules

## Git workflow
- Branch from `develop`, never from `main`
- Branch naming: `MMDDYYYY-description` (e.g. `05222026-android-fix`) — date first so branches sort chronologically
- PRs always target `develop` — pass `--base develop` explicitly with `gh pr create`
- `main` is production-only; only merged from `develop` at release time
- No co-author lines, no AI attribution in commit messages

## Development standards
- TDD: write tests before or alongside implementation
- All ViewModels accept `coroutineScope: CoroutineScope? = null` for testability (defaults to `viewModelScope`)
- String resources in `strings.xml` — no hardcoded strings in composables
- Errors surfaces as `StringResource` on state, never raw exception messages
- `basilSpacing` is accessed via `MaterialTheme.basilSpacing.X` inside composables

## Android
- Koin and Sentry init live in `BasilApplication.onCreate`, never in `MainActivity.onCreate`
  (putting them in Activity causes `KoinApplicationAlreadyStartedException` on recreation)
- Run with Java 21: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home`
  (Java 26 is installed but breaks the Kotlin compiler in this Gradle version)
- Install and launch: `./gradlew installDevDebug` then `adb shell am start -n org.weekendware.basil.dev/org.weekendware.basil.MainActivity`

## Architecture
- KMP targets: Android, iOS Simulator (Arm64), Desktop (JVM)
- DI: Koin — `chatModule`, `supabaseModule`, `databaseModule`, `useCaseModule`, `sharedModule`, `platformModule`
- Database: SQLDelight
- Build flavors: `dev` / `staging` / `prod` (via BuildKonfig)
- Navigation: sealed `AppRoute` class, `NavHost` with 3-tab bottom nav + Settings stack

## HIPAA
- HTTPS guard in `KtorChatRepository` is exempt for `dev` flavor (localhost testing)
- Chat history is cleared on sign-out via `LaunchedEffect` in `App.kt`
- Never log request/response bodies — they may contain PHI

## companion repo
- `basil-chat-api` lives at `~/Desktop/Projects/basil-chat-api` (Rust/Axum)
- Follows identical branching, TDD, and commit standards
- Needs `.env` with `ANTHROPIC_API_KEY`, `API_KEY`, `PORT=8080` to run locally
- Start locally: `~/.cargo/bin/cargo run`
- `local.properties` in basil: `chat.api.url=http://localhost:8080`, `chat.api.key=<matches API_KEY in .env>`
