# Changelog

All notable changes to MindSet are recorded here. Format loosely follows
[Keep a Changelog](https://keepachangelog.com/); the project is pre-1.0 so versions are milestones,
not published releases.

## [Unreleased]

### Data model v2 (Phase 1 data-layer track — `data-layer-rethink`)

The persistence layer was rebuilt to the HLD/LLD-v1 model behind a real, tested migration.

**Added**
- 4-level session model: `Session → Block → ExerciseEntry → SetEntry` (blocks make supersets/
  circuits/intervals expressible; legacy flat sessions fold into one implicit `STRAIGHT` block).
- First-class `Modality` (STRENGTH / CONDITIONING / RUN / MOBILITY) and a 5-value `MetricType`
  (WEIGHT_REPS / REPS_ONLY / DISTANCE_TIME / DURATION / CALORIES) on exercises, driving the
  polymorphic `CaptureFields` logging cells.
- Pure-Kotlin domain models in `com.mindset.model`, mapped to Room entities at a single seam
  (`Mappers.kt`) so the UI/domain never touch Room types.
- Hand-rolled **UUIDv7** generator (`:core:common` seam) — time-ordered client ids.
- `PersonalRecord` cache + pure `detectPrs` (Epley e1RM, max weight/reps, best-time-per-distance,
  max calories) that runs on set completion.
- 8 Hyrox stations tagged as first-class exercises; ~870-row catalog re-seeded with modality/metric;
  calorie-erg seeds so all 5 capture metrics are represented.
- Real `MIGRATION_7_8` with a fixture test (`MigrationTest`, iosTest) validating the fold against the
  exported v8 schema.
- Sync wire (`:contracts`) now carries the full block tree with per-entity ids + envelope; child
  identity is preserved across push/pull (no more local id minting).
- Compose capture-flow UI test (`CaptureFlowTest`) — wired, executed on device/CI.

**Changed**
- Soft delete moved from a boolean `deleted` to a `deletedAt` tombstone; every syncable entity now
  carries `createdAt`/`updatedAt`/`deletedAt`.
- Repository/ViewModels expose domain types via `SessionDetail`; `SyncEngine` walks the real block
  graph. `:server` unchanged (aggregate transport — it treats the session DTO opaquely).

**Removed**
- Destructive Room migration fallback (replaced by the versioned migration).
- Hardcoded demo `PlannedSession` seed.
