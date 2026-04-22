# Basil — CI/CD Plan (Solo-Dev Starter)

**Status:** v0.1 · April 2026
**Scope:** get Basil to green CI on every PR, with near-zero setup cost, and a clear upgrade path to full beta distribution when we have testers.

---

## TL;DR

A single GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every pull request and push to `main`. It lints, builds both platforms, runs the common Kotlin test suite, and uploads a debug APK. That's the whole v1 pipeline. No Fastlane, no signing keys, no store uploads, no TestFlight automation — those arrive in a later iteration when we actually have external testers.

This doc explains what's in the pipeline, what intentionally isn't, and the order we'll add the rest.

## Goals for v1

1. **Every PR gets a build signal.** If it breaks on Android or iOS, we find out before merging.
2. **Fast feedback.** Target: under 10 minutes for Ubuntu jobs, under 20 for the macOS iOS compile.
3. **Zero manual secret-management.** A solo dev should not have to configure five secrets just to turn CI on.
4. **Good reports.** When something breaks, the test HTML + lint HTML are one click away.
5. **Upgradeable.** The pipeline should extend cleanly when we add beta distribution, signing, and store uploads.

## Non-goals for v1

- No Fastlane. No App Store Connect uploads.
- No Play Store Internal Testing auto-submit.
- No code-coverage gates (we barely have tests; enforcing coverage now is theater).
- No Firebase App Distribution.
- No release tagging automation.
- No screenshot/visual diff tests.

---

## What's in the pipeline (as shipped)

### `.github/workflows/ci.yml`

Three parallel jobs:

- **`lint`** (Ubuntu) — Gradle Android lint on the `composeApp` module. Uploads HTML/XML reports as an artifact. 5–10 min.
- **`android`** (Ubuntu) — Compiles common + Android, runs `allTests` (which executes `commonTest`), and assembles an unsigned debug APK. Uploads test reports and the APK as artifacts. 10–15 min.
- **`ios`** (macOS 14) — Links the iOS simulator ARM64 framework to confirm the KMP side compiles, and runs `iosSimulatorArm64Test` if any tests exist. 15–25 min (macOS runners are slow on cold start).

All three jobs cache Gradle, use JDK 17 Temurin, and cancel stale runs on new PR pushes (`concurrency`).

### `.github/dependabot.yml`

Weekly grouped dependency updates for Gradle, plus monthly for GitHub Actions themselves. Bumps are grouped by ecosystem (Kotlin/Compose/AGP, Voyager, SQLDelight, Koin) so one Dependabot PR bumps a coherent set instead of five separate PRs.

### `.github/pull_request_template.md`

Consistent PR checklist: what/why/how-to-test/risk/screenshots. Forces 30 seconds of discipline that pays dividends in review.

### `google-services.json` handling

Firebase is configured but `google-services.json` is git-ignored (as it should be). Each CI job stubs a dummy `google-services.json` if none is present so debug builds compile on PRs from forks. Release builds will require a real key via a repo secret.

---

## What's missing (and will be added in waves)

### Wave 2 — "before the first external tester"

- **ktlint + detekt** wired into Gradle and run in the `lint` job.
  - ktlint: minimal config, pre-commit hook via Husky-like setup (Gradle `ktlintFormat`).
  - detekt: add `config/detekt/detekt.yml` with the Kotlin Multiplatform-sane ruleset.
- **`verifyMigrations`** turned on for SQLDelight (`build.gradle.kts` currently has it `false` — flip it when the schema settles, and the CI will catch unintended schema drift).
- **Firebase App Distribution** for dogfood Android + iOS builds, triggered manually via `workflow_dispatch`.
- **Signed debug builds** with a debug keystore checked into the repo so QA can install builds across devices without reinstalling.

### Wave 3 — "public beta"

- **Android release signing** via a Base64-encoded keystore in GitHub Secrets + `signingConfigs.release` in Gradle.
- **iOS signing** via Apple App Store Connect API key (AuthKey_*.p8) + match / manual profiles. This is the part Fastlane actually earns its keep on.
- **Fastlane lanes**:
  - `fastlane android beta` → AAB → Play Console Internal Testing track
  - `fastlane ios beta` → IPA → TestFlight external group
- **Tag-driven release workflow**: pushing `v0.2.0` triggers Fastlane lanes for both platforms in parallel.

### Wave 4 — "public launch"

- **Release notes automation** from commit messages (conventional-commits + `release-drafter`).
- **Screenshot tests** via Roborazzi or Paparazzi on Compose, stored as git-LFS-backed golden images.
- **CD to App Store / Play Store production** behind a manual approval gate.
- **Status page + uptime monitors** (once the backend exists).
- **Code coverage** with Kover, surfaced to PRs via a lightweight comment action. Still no hard gate.

### Wave 5 — "enterprise readiness"

- **SBOM generation** + vulnerability scanning (Snyk, Trivy, or GitHub native).
- **SAST** in the pipeline (SonarCloud or Semgrep).
- **Compliance evidence** for SOC2 / HIPAA (when we start caring): CI logs, change management via PRs, branch protection rules, mandatory reviews.

---

## Branch protection recommendations

Enable on `main` once CI is green:

- Require a pull request before merging.
- Require status checks to pass before merging: `lint`, `android`, `ios`.
- Require branches to be up to date before merging.
- Require conversation resolution before merging.
- Restrict who can push to matching branches (solo: just you, but set it for habit).

## Secrets we'll eventually need (not in v1)

| Secret | Purpose | When |
|---|---|---|
| `GOOGLE_SERVICES_JSON_BASE64` | Real Firebase config for release builds | Wave 2 |
| `ANDROID_KEYSTORE_BASE64` | Release signing | Wave 3 |
| `ANDROID_KEYSTORE_PASSWORD` | Release signing | Wave 3 |
| `ANDROID_KEY_ALIAS` | Release signing | Wave 3 |
| `ANDROID_KEY_PASSWORD` | Release signing | Wave 3 |
| `APPSTORE_CONNECT_API_KEY_ID` | TestFlight upload | Wave 3 |
| `APPSTORE_CONNECT_API_ISSUER_ID` | TestFlight upload | Wave 3 |
| `APPSTORE_CONNECT_API_KEY_BASE64` | TestFlight upload | Wave 3 |
| `FIREBASE_APP_DISTRIBUTION_TOKEN` | Dogfood distribution | Wave 2 |
| `PLAY_STORE_JSON_KEY_BASE64` | Play Store upload | Wave 3 |
| `SLACK_WEBHOOK_URL` | Build notifications | Wave 2 (optional) |

Use GitHub Environments to scope release secrets separately from default secrets.

## Local developer experience

Keep the local dev loop aligned with CI so nothing surprises you:

```bash
# lint + tests like CI does:
./gradlew :composeApp:lint :composeApp:allTests

# just the test suite:
./gradlew :composeApp:allTests

# assemble debug APK:
./gradlew :composeApp:assembleDebug

# iOS framework compile (requires Xcode 15+ on macOS):
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

A `make ci` or `./scripts/ci-local.sh` wrapper that runs the same matrix is a nice-to-have, not required.

## Why not GitLab / Bitbucket / CircleCI?

GitHub Actions is free for public repos and generous for private ones (2,000 min/mo on Free; macOS minutes are 10x, which is the thing to watch). Native integration with GitHub PRs is worth more than any CI-specific feature we'd miss. Revisit if macOS minutes become a bottleneck — self-hosted macOS runners via a Mac mini are cheap once there's a fixed workstation.

## Metrics to watch

- **PR lead time to CI-green.** Target: median < 15 min. If this creeps up, cache is probably stale or macOS runner is queueing.
- **Flake rate.** Target: < 2% of runs. Anything above that and we fix the test or remove it.
- **macOS minutes burned per month.** 10x cost of Linux; keep an eye as we add iOS tests.

## Open questions to resolve this quarter

1. Do we want **ktlint or detekt first**, or both together? (Recommendation: ktlint first — auto-format is a bigger quality-of-life jump.)
2. Should the **iOS sim compile** be gated on PRs, or nightly? (Current: gated — worth the cost because breaking the iOS framework build is painful to discover late.)
3. When should **`verifyMigrations`** flip to `true`? (Recommendation: the moment we ship the first build with cloud sync.)
4. Do we want a **public build status badge** in the README? (Yes; a green badge is free trust.)

---

## Appendix A — What changes when we add a backend

When the HIPAA backend lands (Supabase per the backend decision memo), CI gains:

- An **integration test** job that stands up a local Postgres container (via `services:` in the workflow), runs schema migrations, and exercises repository code against real SQL.
- **Type generation** for any API schema (OpenAPI or GraphQL) run as part of the build, with `git diff --exit-code` to fail the job if generated sources are out of date.
- A **backend-only** workflow (`backend-ci.yml`) for when we split backend into its own repo or subdirectory.

## Appendix B — Recommended repo hygiene tasks (one-time, 30 minutes total)

- Turn on branch protection on `main`.
- Add a **CODEOWNERS** file: `* @gavincdunne` until there are more humans.
- Add a **LICENSE** file (MIT per README; create the file).
- Add a **SECURITY.md** describing how to report a vulnerability (even a placeholder email works).
- Add a **CONTRIBUTING.md** describing the dev loop. (Copy-paste the "Local developer experience" section above as a starting point.)
- Turn on **Dependabot security updates** in Settings → Code security.
- Turn on **secret scanning** in Settings → Code security (free on public; paid on private, but worth it once you have Firebase keys).

## Appendix C — Quick troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `iOS` job hangs on cold start | macOS runner queue | Retry; consider self-hosted Mac later |
| `Could not resolve google-services` | Missing stub | Re-check the stub step in `ci.yml` |
| Out-of-memory during link | Gradle default JVM too small | Bump `org.gradle.jvmargs` in `gradle.properties` |
| Long cache fetch | Gradle cache eviction | Normal on first build of the day |
| `iosSimulatorArm64Test` fails with "No tests" | Expected while test suite is empty | The job treats missing tests as a soft pass |
