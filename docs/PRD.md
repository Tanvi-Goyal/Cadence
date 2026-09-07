# MindSet — Offline-First Cross-Platform Training Tracker
### Product & Engineering Requirements (Living Document)

*Working title — rename freely. "MindSet" nods to both running tempo and training rhythm; alternatives: Loadout, Splits, Tempo, PR.*

**Status:** Draft v0.1 · **Owner:** Tanvi Goyal · **Last updated:** _[date]_

---

## 0. Why this project exists (read this first)

This is a **portfolio project for L4/senior Android interviews at top product companies**, not a startup. That single fact governs every scoping decision below. The corollary a lot of people miss:

> **The deliverable is not the app. It's the architecture, the README that explains your tradeoffs, and a runnable demo.**

A small, *finished*, well-reasoned app with a great README beats an ambitious half-built one every time. Interviewers skim the README as a proxy for how you think, then poke at the two or three hard decisions you made. So the goal is to build the *smallest* thing that lets you have deep, credible conversations about **KMP code-sharing**, **offline-first sync**, and **measured performance**.

**Hard constraint:** this runs *alongside* DSA prep on a ~2–3 month clock. Treat build time as scarce. Ship something runnable in week 2, not week 10.

**The three interview capabilities this project is engineered to prove:**
1. **KMP depth** — sharing domain + data (and ideally UI) across platforms, and knowing exactly where the `expect/actual` seams belong.
2. **Offline-first & sync** — local DB as source of truth, an outbox, conflict resolution. This is *mobile system-design gold* and directly reinforces your Pratilipi content-app background.
3. **Performance, measured** — Baseline Profiles + Macrobenchmark + Perfetto, with a **before/after table**. This showcases your single strongest differentiator (the p99 latency work) as a repeatable skill, not a one-off.

---

## 1. Product vision & scope

**One-liner:** A cross-platform training log for hybrid/functional athletes (strength + conditioning + running, e.g. Hyrox-style prep) that works fully offline and syncs seamlessly across devices.

**Why this domain:** you'll actually use it (so you'll finish it), it has genuinely interesting data (sets/reps/load + time/distance + heart-rate), and it gives natural reasons to touch platform-specific health APIs — which is where the best "shared vs native" story lives.

**MVP boundary — v1 is DONE when:**
- You can create a workout, log exercises/sets (strength) and time/distance intervals (conditioning), and see history — **fully offline**.
- Data persists in a local DB that is the **single source of truth**; the UI observes it reactively.
- Changes **sync to a backend and pull back** on another device/session, with a defined conflict-resolution rule.
- The shared KMP module (domain + data) is consumed by an **Android app**, with an **iOS target that compiles and runs at least one shared screen**.
- A **Macrobenchmark + Baseline Profile** exists and the README shows a measured startup/jank improvement.

**Explicit non-goals (say "no" out loud so scope doesn't creep):**
- No social features, no feed, no auth beyond the minimum sync needs.
- No coaching/AI plan generation in v1 (great v3 backlog item).
- No Wear OS / watchOS / widgets in v1 (v2+).
- Not aiming for App Store / Play Store release — a runnable demo + screen recording is enough.

---

## 2. Feature set & app flows

### Core loop
`Plan or start a session → log sets/intervals as you train → review history & trends → data syncs quietly in the background.`

### v1 — MVP (build this)
| Feature | Flow | Notes |
|---|---|---|
| **Workout log** | Home → "New session" → pick template or blank → add exercises → log sets (reps × load) or intervals (time/distance) → save | The heart of the app. Local write is instant. |
| **Exercise library** | Searchable list of movements (bundled seed data) | Ships offline; good excuse for a Paging 3 list + search. |
| **History** | List of past sessions, tappable to detail | Reactive list observing the DB via Flow. Instrument this screen for scroll jank. |
| **Basic trends** | Simple per-exercise volume / best-time chart | 1–2 charts. Keep it small in v1. |
| **Offline-first persistence** | Everything above works with airplane mode on | The point of the whole project. |
| **Background sync** | Silent push/pull; a subtle "last synced" indicator | See §5. This is the centerpiece. |

### v2 — Enhancements (after MVP is runnable)
- **Health integration** (Health Connect on Android / HealthKit on iOS) — pull heart-rate & workout data, write sessions back. *This is the richest platform-differentiation feature.*
- **Home-screen widget** — today's plan / quick-log (Glance on Android, WidgetKit on iOS).
- **Rest timer** with notifications + (iOS) Live Activity / Dynamic Island countdown.
- **Program builder** — multi-week training plans, not just ad-hoc sessions.
- **Hyrox mode** — the 8-station + running format as a first-class session type with pacing targets.

### v3+ — Backlog (the "enhance as I learn" runway)
- Wear OS / watchOS companion for on-wrist logging.
- Multi-device conflict stress-testing harness (great for a blog post).
- Encrypted DB (SQLCipher) + local biometric lock.
- Shared **Compose Multiplatform** UI expanded to cover most screens (measure how much UI you actually shared — a real metric for the README).
- Desktop target (Compose for Desktop) as a "coach's console."
- Export / import (CSV, JSON), data portability.
- On-device ML: rep detection or plan suggestions (only if you want an ML story).

### Key flows to spec in detail (do these as you build)
1. **Log a set offline → app killed → reopen** → data is still there (proves DB-as-truth).
2. **Log on device A offline → go online → open device B** → session appears (proves pull sync).
3. **Edit the same session on two devices** → defined winner (proves conflict resolution — have the answer ready for interviews).

---

## 3. Platform differentiation — the "shared vs native" story

This section is what makes it a *KMP* project rather than "an Android app that also compiles on iOS." Interviewers love a candidate who can articulate **exactly where the seam is and why.**

### What lives in `commonMain` (shared)
- Domain models & use-cases (Session, Exercise, Set, Interval, SyncState).
- Repository interfaces + offline-first orchestration logic.
- Room-KMP database, DAOs, entities.
- Ktor client, DTOs, `kotlinx.serialization`.
- ViewModels (KMP `ViewModel`) and sync engine.
- **Optionally**, Compose Multiplatform UI (shared screens).

### What must be platform-specific (`expect`/`actual` or native)
This is the *interesting* list — the stuff that genuinely can't or shouldn't be shared:

| Concern | Android (`androidMain`) | iOS (`iosMain` / Swift) | Why it can't be shared |
|---|---|---|---|
| **Health data** | Health Connect | HealthKit | Entirely different platform SDKs & permission models |
| **Background sync scheduling** | WorkManager | `BGTaskScheduler` / background URLSession | OS-governed execution policies differ fundamentally |
| **Local notifications / timers** | `NotificationManager` | `UNUserNotificationCenter` | Platform APIs |
| **Rich live timer** | Foreground service / notification | **Live Activity + Dynamic Island** | iOS-only surface — a great differentiator to show |
| **Home-screen widget** | Glance | WidgetKit | Separate widget frameworks |
| **Secure storage / DB key** | Keystore | Keychain | Platform crypto stores |
| **DB file path / driver** | `BundledSQLiteDriver` (Android) | native SQLite driver | Room-KMP needs a platform driver via `expect/actual` |
| **File export / share sheet** | `Intent` / SAF | `UIActivityViewController` | Platform UX |

**The talking point to rehearse:** *"I shared domain, data, and sync logic in commonMain — roughly X% of the codebase — and drew the `expect/actual` line at OS-governed capabilities: health stores, background execution, notifications, and secure storage. I deliberately kept navigation and a couple of platform-flavored surfaces (Live Activities, widgets) native, because forcing them into shared code would have added complexity for no real reuse."* Back the X% with a real `cloc` count in the README.

**iOS scope discipline:** for v1, you do **not** need a polished iOS app. A thin iOS app that consumes the shared module and renders **one real screen** (e.g. history) proves the sharing claim. Expanding shared Compose UI is a v3 enhancement you can measure and blog about.

---

## 4. Technical architecture

### Module structure (start simple, split later)
```
:composeApp        // Android app (Compose) — or :androidApp
  iosApp/          // Xcode project consuming :shared
:shared            // KMP: domain + data + sync + ViewModels
  commonMain
  androidMain      // Room driver, Health Connect, WorkManager, Glance
  iosMain          // native SQLite driver, HealthKit bridge, BGTask
:benchmark         // Android Macrobenchmark + Baseline Profile module
```
Keep it a handful of modules for v1. Over-modularizing early is a classic time sink; you can split `:shared` into `:core:domain / :core:data / :feature:*` later and *write about the migration* (another blog post).

### Layering — offline-first, DB as source of truth
```
Compose UI  ──observes──►  ViewModel  ──►  Repository
                                              │
                          ┌───────────────────┴───────────────────┐
                          ▼                                        ▼
                  Local DB (Room-KMP)  ◄── single source ──   Sync Engine
                  (UI ALWAYS reads here)      of truth        (Ktor ↔ backend)
```
The non-negotiable rule: **the UI never reads from the network.** It observes the DB via `Flow`. The network's only job is to *feed* the DB (pull) and *drain* local changes to the server (push). This is the same principle behind your Pratilipi content app and behind Now-in-Android — worth naming that lineage in interviews.

### Recommended stack (grounded in the mid-2026 ecosystem)
| Layer | Choice | Rationale |
|---|---|---|
| Language / build | Kotlin 2.2, K2, Gradle version catalogs, KSP | Current stable baseline |
| Shared UI (optional v1, expand v3) | Compose Multiplatform 1.8+ | iOS UI went stable May 2025 |
| Local DB | **Room-KMP** (2.7+/3.0) | You already know Room → reuse expertise; compile-time verified SQL; Google-backed. *Alternative: SQLDelight (SQL-first) — knowing why you didn't pick it is itself interview value.* |
| Networking | Ktor 3 client + `kotlinx.serialization` | KMP-native HTTP |
| Preferences | DataStore (KMP) | Sync tokens, settings |
| DI | Koin | Simplest KMP DI; Hilt is Android-only |
| Async | Coroutines + Flow | Plays to your coroutines deep-dive |
| Backend | **Supabase** (Postgres + REST + auth + realtime) *or* a minimal **Ktor server** | Supabase = fastest path, real sync target, near-zero ops. A tiny Ktor backend = full-stack-Kotlin signal but more time. Either way, keep it swappable behind a repository interface. |
| Logging | Napier / Kermit | KMP logging |

### Data model sketch (v1)
- `Session(id, startedAt, type, notes, updatedAt, deleted, syncStatus)`
- `Exercise(id, name, category, unit)` — seeded, mostly read-only
- `LoggedItem(id, sessionId, exerciseId, order)` — an exercise within a session
- `SetEntry(id, loggedItemId, reps, loadKg, timeSec, distanceM, rpe)`
- `OutboxEntry(id, entityType, entityId, opType, payload, createdAt, attempts)` — the sync queue

Note the sync-critical columns on every synced entity: **`updatedAt`, `deleted` (soft delete), `syncStatus`.** Designing these in from day one is the difference between "I added sync" and "I designed for sync."

---

## 5. Offline-first & sync design (the interview centerpiece — go deep here)

This is the highest-value section for interviews. Build the *simplest correct version*, but understand and be able to *discuss* the harder ones.

### Write path (optimistic, local-first)
1. User logs a set → write to Room **immediately** in a transaction → UI updates instantly (it's observing the DB).
2. In the **same transaction**, append an `OutboxEntry` describing the mutation.
3. A sync worker (WorkManager on Android / BGTask on iOS) drains the outbox to the backend when connectivity + constraints allow. On success, mark synced; on failure, retry with **exponential backoff** and an attempt cap.

This **outbox pattern** is the thing to name explicitly — it decouples "the write succeeded locally" from "the write reached the server," which is the essence of offline-first.

### Read/pull path
- Track a **sync token / cursor** (server-provided watermark or `lastPulledAt`).
- On sync, pull changes since the token, upsert into Room, advance the token.
- Use **soft deletes** (a `deleted` flag) so deletions propagate — a hard delete can't be synced after the row is gone.

### Conflict resolution — build v1, discuss the ladder
- **v1: Last-Write-Wins by `updatedAt`.** Simple, correct-enough for single-user multi-device. *Ship this.*
- **Know where LWW breaks:** concurrent edits to *different fields* of the same row lose data; clock skew makes "last" ambiguous.
- **The evolution ladder to articulate** (don't build unless you want the blog post): per-field merge → **version vectors / Lamport timestamps** for causal ordering → **CRDTs** for truly concurrent collaborative edits. Being able to walk *up* this ladder and justify stopping at LWW for a single-user app is exactly the judgment senior interviews probe.

### Reliability details worth implementing (cheap, high-signal)
- **Idempotency:** client-generated UUIDs as primary keys so retried pushes don't duplicate.
- **Atomic local writes:** data + outbox in one transaction (no "wrote data but lost the sync intent" gap).
- **Connectivity-aware scheduling:** sync constraints (network, optionally charging) via WorkManager.

---

## 6. Performance instrumentation (project #3, folded in)

This turns a good project into a memorable one and makes your latency strength *repeatable and demonstrable*. Keep it Android-focused (Baseline Profiles & Macrobenchmark are Android tools; that's expected and fine for Android-targeted interviews).

### Build these
1. **Baseline Profile** — generate via the `:benchmark` module; measure cold-start before/after. Expect a real, quotable startup improvement.
2. **Macrobenchmark suite:**
   - `StartupBenchmark` — cold / warm / hot startup timing.
   - `ScrollBenchmark` — frame timing on the History screen; capture **jank / frozen frames**. (Seed a few hundred sessions so scrolling is non-trivial.)
3. **Perfetto / system traces** — capture a cold start and a scroll session; identify one real bottleneck, fix it, re-capture. *The fix + the trace screenshots are the story.*
4. **README before/after table**, e.g.:

   | Metric | Before | After | Change |
   |---|---|---|---|
   | Cold start (P50) | _ ms | _ ms | ↓ _% |
   | Cold start (P90) | _ ms | _ ms | ↓ _% |
   | Janky frames (scroll) | _ % | _ % | ↓ _% |

**The narrative this unlocks:** *"I didn't just claim the app is fast — I measured it. Baseline Profiles cut P90 cold start by X%, and a Perfetto trace showed [main-thread work / recomposition / DB query on the wrong dispatcher] causing scroll jank, which I fixed by [Y], dropping janky frames from A% to B%."* That is the same muscle as your Pratilipi p99 work, packaged so an interviewer can see the method.

---

## 7. Roadmap & milestones (scoped for ~2–3 months, part-time alongside DSA)

Time-boxed and front-loaded so you have something runnable early. Adjust week counts to your real availability — the *ordering* matters more than the dates.

### Phase 0 — Skeleton & first runnable Android build *(Week 1–2)*
- KMP project scaffold (Android + iOS targets), version catalogs, Koin, Room-KMP with platform drivers wired.
- One end-to-end vertical slice: create session → save to Room → see it in a list. **Runs on Android.**
- **DoD:** you can log and view a session offline. Repo is public with a stub README.

### Phase 1 — Offline-first core *(Week 3–4)*
- Full workout logging (strength + intervals), exercise library, history, one trend chart.
- DB-as-source-of-truth wired through ViewModels + Flow.
- Seed data; airplane-mode test passes.
- **DoD:** the whole core loop works with **zero network**.

### Phase 2 — Sync engine *(Week 5–6)*
- Backend stood up (Supabase or minimal Ktor).
- Outbox + push, cursor-based pull, LWW conflict rule, soft deletes, UUID idempotency.
- Multi-device test (emulator + device) passes.
- **DoD:** log on A → appears on B; conflict rule is demonstrable.

### Phase 3 — Performance instrumentation *(Week 7)*
- `:benchmark` module, Baseline Profile, startup + scroll Macrobenchmarks, one Perfetto-guided fix.
- **DoD:** before/after table in README with real numbers.

### Phase 4 — iOS proof + README + polish *(Week 8)*
- iOS app consuming `:shared`, rendering ≥1 real screen (proves the sharing claim).
- Write the real README (see §8): architecture diagram, ADRs, tradeoffs, `cloc` sharing %, demo GIF/video.
- **DoD:** a stranger can read the README and understand your decisions in 5 minutes.

### Then: enhancement runway (ongoing, low priority vs DSA)
Pick from §2 v2/v3 as time allows — Health Connect/HealthKit is the highest-value next feature and the best "shared vs native" upgrade. Each enhancement is also a **blog post** (Compose-internals momentum → sync design → performance method → KMP sharing measured).

> **Reality check:** if the clock tightens, cut in this order — iOS polish, trends/charts, v2 features. **Never** cut: offline-first core, sync engine, the README. Those three *are* the project.

---

## 8. The README (this is graded — treat it as a deliverable)

An interviewer reads this before they read your code. It must contain:
- **One-paragraph what/why** + a demo GIF or short screen recording near the top.
- **Architecture diagram** (the §4 layering) — a picture earns disproportionate credit.
- **Architecture Decision Records** — short, honest writeups: *Room-KMP vs SQLDelight; LWW vs CRDT; Supabase vs custom backend; how much UI I shared and why not more.* Include what you'd do differently at scale.
- **Offline-first & sync section** — outbox, conflict rule, the evolution ladder.
- **Performance section** — the before/after table + one Perfetto screenshot.
- **Code-sharing metric** — actual `cloc` numbers for commonMain vs platform.
- **"Known limitations / next steps"** — signals maturity and self-awareness (senior marker).

**Interview stories this project is designed to hand you** (rehearse each as a 2–3 min narrative):
1. *Designing for offline-first sync* — the outbox, why the DB is the source of truth, the conflict ladder.
2. *Drawing the KMP seam* — what you shared, what you kept native, and the judgment behind it.
3. *Making performance measurable* — Baseline Profiles + Perfetto, before/after (mirrors your p99 latency win).
4. *Scoping under constraint* — what you deliberately cut and why (a senior-level answer to "tell me about a tradeoff").

---

## 9. Open questions to decide as you go
- Backend: Supabase (fast) vs Ktor (full-stack-Kotlin signal)? *Lean Supabase unless you want the backend story.*
- Auth: anonymous device ID for v1, or real accounts? *Anonymous is enough for single-user multi-device.*
- How much Compose Multiplatform UI to share in v1 vs keep Android-native and expand later? *Start Android-native for speed; measure and expand in v3.*
- Charts: a KMP chart lib vs hand-rolled Canvas? *A tiny hand-rolled chart avoids a dependency and shows Compose drawing skill.*

## 10. First-week actions (start here)
1. Create the public repo + KMP scaffold (Android Studio KMP wizard). Push day one — a live repo with commits over time is itself signal.
2. Wire Room-KMP with the Android + iOS drivers; get one entity saving and reading.
3. Build the create-session → save → list vertical slice on Android.
4. Drop a stub README with the vision paragraph and the architecture diagram placeholder.
5. Timebox it: a fixed few hours/week, protected *after* your daily DSA block.

---

## Appendix: Architecture Decision Records (as built)

**ADR-1 — Koin over Metro for DI.** Chose Koin (mature, first-class KMP+Compose) over Metro
(compile-time compiler-plugin DI, ~8 months old, native multi-module aggregation still gated on
KT-75865 — exactly this module shape). All business classes use plain constructor injection; the
only Koin surface is the `di/` package, so a Metro swap is bounded. Added Koin's K2 compiler plugin
(DSL mode, no class annotations) for compile-time graph work without lock-in.

**ADR-2 — LWW, aggregate (per-session) sync.** Conflict resolution is Last-Write-Wins by
`updatedAt`, applied symmetrically (server rejects stale pushes; client ignores stale pulls). The
**session is the sync unit**: its logged items + sets travel as one document, and any child edit
bumps the parent `updatedAt`. Rationale: a session is a bounded aggregate with effectively one
writer at a time, so per-set outbox/conflict granularity is unnecessary complexity. The pull path
replaces a session's children wholesale. Exercises are seeded reference data (stable slug ids),
referenced by id and never synced. Evolution ladder if needed: per-field merge → version vectors →
CRDTs; stopping at aggregate-LWW is the deliberate single-user-multi-device call.

**ADR-3 — Room 3.0 upgrade + Paging 3 in commonMain (vs filtered Flow).** The exercise library uses
a real `PagingSource` DAO in commonMain, which required upgrading Room 2.8.4 → 3.0 (`androidx.room3`,
`room3-paging`'s `@DaoReturnTypeConverters`). Honest tradeoff: for a ~50-row bundled catalog a
filtered `Flow<List>` would suffice and Paging is arguably over-engineering here — the defensible
version of this choice is knowing the threshold (large/remote data) at which Paging earns its keep,
and that cross-platform Paging on KMP is what forces Room 3.0.

**ADR-4 — Room `@Relation` avoided in commonMain.** Room 3.0's `@Relation`/`@Embedded` relation
POJOs fail KSP codegen in commonMain (KMP); "items with their sets" is composed from two plain
queries in the repository instead — more robust across targets, explicit join logic.

*Living document — expand the v2/v3 sections and ADRs as you build. Every phase completed is also a blog post; every blog post is a public depth signal that almost no L4 candidate brings.*
