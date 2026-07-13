# Cadence

A cross-platform training log for hybrid/functional athletes (strength + conditioning + running).
**Offline-first**: the local database is the single source of truth, the UI only ever observes it,
and changes sync to a backend and pull back on another device. Built with Kotlin Multiplatform —
shared domain/data/sync/ViewModels, native UI (Jetpack Compose on Android, a thin SwiftUI shell on
iOS).

> Portfolio project. The interesting parts are the **KMP seam**, the **offline-first sync engine**,
> and **measured performance** — see below.

## Architecture

```
Compose UI ──observes──► ViewModel ──► Repository
                                          │
                          ┌───────────────┴───────────────┐
                          ▼                                ▼
                  Local DB (Room-KMP)  ◄── single source ── Sync Engine
                  (UI ALWAYS reads here)     of truth        (Ktor ↔ Ktor server)
```

- **`:shared`** (KMP) — domain, Room 3.0 database, repositories, sync engine, ViewModels
  (`commonMain`); platform seams (`androidMain`/`iosMain`) only for OS-governed capabilities
  (DB driver path, HTTP engine).
- **`:composeApp`** — Android app (Jetpack Compose, navigation, Paging 3 exercise library).
- **`:contracts`** — `@Serializable` sync DTOs shared by client and server.
- **`:server`** — minimal Ktor backend (in-memory) for sync.
- **`:benchmark`** — Macrobenchmark + Baseline Profile (this phase).

### Offline-first & sync
Local writes are instant and enqueue an **outbox** row in the same Room transaction. The sync
engine drains the outbox (**push**) and pulls changes by a server-authoritative **cursor**
(**pull**); conflicts resolve **Last-Write-Wins** by `updatedAt`, applied symmetrically on server
and client. Sessions sync as an **aggregate** (their logged exercises + sets travel as one
document). Full rationale in the ADRs (`docs/PRD.md` appendix).

## Performance (Baseline Profile + Macrobenchmark)

Cold-start and scroll performance are **measured**, not asserted. `:benchmark` runs a Startup
Macrobenchmark (cold launch) and a Scroll Macrobenchmark (frame timing on the History list, seeded
to ~200 sessions), each under two compilation modes: `None` (no AOT — the "before") and
`Partial` + the generated **Baseline Profile** (the "after").

_Measured on a physical `<device model>` — relative improvement from the Baseline Profile._

| Metric | None (before) | Baseline Profile (after) | Change |
|---|---|---|---|
| Cold start — timeToInitialDisplay P50 | _ ms | _ ms | ↓ _% |
| Cold start — timeToInitialDisplay P90 | _ ms | _ ms | ↓ _% |
| History scroll — frameOverrunMs P90 | _ ms | _ ms | ↓ _% |

<!-- Fill from the benchmark output (see "Running the benchmarks"). -->

### Running the benchmarks (physical device, USB debugging)
```bash
# 1. Generate the Baseline Profile (packaged into the app APK).
./gradlew :composeApp:generateBaselineProfile

# 2. Startup: StartupBenchmark runs None vs Baseline Profile (before/after) — cold start.
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.cadence.benchmark.StartupBenchmark

# 3. Scroll frame timing on the seeded History list.
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=dev.cadence.benchmark.ScrollBenchmark
```
Results print per-metric (P50/P90/P99) and are written under `benchmark/build/outputs/`.

## Build & run
- `./gradlew :composeApp:assembleDebug` — Android app
- `./gradlew :shared:testAndroidHostTest` / `:shared:iosSimulatorArm64Test` — shared tests
- `./gradlew :server:run` — sync backend on :8080
- See `AGENTS.md` for the full command list and architecture rules.

## Status
Phases 0–3 complete: offline-first core (logging, exercise library, history, trends), sync engine
(outbox/cursor/LWW, multi-device), and performance instrumentation. Next: a Perfetto-guided fix,
and the iOS app rendering a real shared screen.
