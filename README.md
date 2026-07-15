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

_Measured on a physical **Google Pixel 9 Pro**, 10 iterations per mode. `timeToInitialDisplay`
(TTID) is the time from launch to the first frame with content — Macrobenchmark reports it as
min/median/max (a trace metric, not sampled), so no percentiles here._

| Metric | None (before) | Baseline Profile (after) | Change |
|---|---|---|---|
| Cold start — TTID median | 249.3 ms | 202.4 ms | ↓ 18.8% |
| Cold start — TTID max (worst case) | 264.2 ms | 214.7 ms | ↓ 18.7% |
| Cold start — TTID min (best case) | 229.6 ms | 191.2 ms | ↓ 16.7% |

The Baseline Profile shifts the **entire distribution** down (~19% at the median), not just the
average — the win comes from AOT-compiling the hot startup path so it isn't JIT-compiled on the
critical path of the first launch.

_History scroll (`frameOverrunMs`) is instrumented and runnable but not yet reported here —
capturing it requires an idle, undisturbed device (a foreground call or other app skews frame
timing and steals focus mid-fling). Numbers land once a clean run is captured._

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
Results print per-metric (min/median/max for startup; P50/P90/P95/P99 for frame timing) and are
written under `benchmark/build/outputs/`.

## Build & run
- `./gradlew :composeApp:assembleDebug` — Android app
- `./gradlew :shared:testAndroidHostTest` / `:shared:iosSimulatorArm64Test` — shared tests
- `./gradlew :server:run` — sync backend on :8080
- See `AGENTS.md` for the full command list and architecture rules.

## Status
Phases 0–3 complete: offline-first core (logging, exercise library, history, trends), sync engine
(outbox/cursor/LWW, multi-device), and performance instrumentation. Next: a Perfetto-guided fix,
and the iOS app rendering a real shared screen.
