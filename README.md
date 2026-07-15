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

**Cold start** (`timeToInitialDisplay`, min/median/max):

| Metric | None (before) | Baseline Profile (after) | Change |
|---|---|---|---|
| TTID median | 249.3 ms | 202.4 ms | ↓ 18.8% |
| TTID max (worst case) | 264.2 ms | 214.7 ms | ↓ 18.7% |
| TTID min (best case) | 229.6 ms | 191.2 ms | ↓ 16.7% |

The Baseline Profile shifts the **entire distribution** down (~19% at the median), not just the
average — the win comes from AOT-compiling the hot startup path so it isn't JIT-compiled on the
critical path of the first launch.

**History scroll** (`FrameTimingMetric` over a fling on the seeded list):

| Metric | None (before) | Baseline Profile (after) |
|---|---|---|
| `frameOverrunMs` P90 | −8.9 ms | −9.4 ms |
| `frameOverrunMs` P99 | −6.0 ms | −6.6 ms |
| `frameDurationCpuMs` P90 | 5.7 ms | 5.6 ms |

**Reading this honestly:** `frameOverrunMs` is negative across every percentile in _both_ modes —
each frame finishes ~6–11 ms **ahead** of its deadline, so the fling never janks, with or without
the profile. Unlike startup, steady-state scrolling JIT-warms almost immediately, so AOT compilation
barely moves it. The measurable Baseline-Profile win is concentrated in **cold start**; the scroll
benchmark's value here is _confirming the absence of jank_ (a negative result worth stating), not
demonstrating a speedup. Profiling the cold-start trace likewise found no pathological main-thread
work — no synchronous DB open, no eager DI (all Koin singletons are lazy).

### The profiling-guided fix: Compose stability across the KMP seam

Profiling ruled out jank, so I turned the Compose compiler's own **stability report**
(`-PcomposeReports=true`) on the scroll path. It surfaced a latent issue:

```
// before
fun SessionRow( unstable session: Session, stable volumeKg: Double )
```

`Session` is an immutable `data class` (all `val`, primitives only), but it lives in `:shared`,
which has no Compose compiler — so the compiler can't infer its stability and defaults it to
**unstable**. `SessionRow` still skipped, but only via Strong Skipping's reference (`===`)
comparison; if the backing `Flow` ever re-emitted equal-but-new instances, every visible row would
recompose needlessly.

The fix is a **stability configuration file** ([`composeApp/compose_stability.conf`](composeApp/compose_stability.conf))
listing the immutable `:shared` model types — asserting their stability to the Compose compiler
_without_ adding a Compose dependency to `:shared` (which would violate the KMP seam). After:

```
// after
fun SessionRow( stable session: Session, stable volumeKg: Double )
```

`Session`, `SetEntry`, `LoggedItem`, `PlannedSession`, `HistoryRow`, `HomeStats` all flipped
unstable → **stable**, so those composables now skip on **value** equality. Honestly scoped: this
is a **robustness/correctness** fix (measured via the compiler report, not frame time — a Pixel 9
Pro has too much headroom to jank either way). The List/Map-holding UI states (`HomeUiState`,
`StatsUiState`, `LoggedItemUi`) remain unstable by design — the right fix there is
`kotlinx.collections.immutable`, tracked as a follow-up.

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
