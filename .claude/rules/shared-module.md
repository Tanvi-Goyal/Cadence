---
paths:
  - "shared/**"
---
# :shared module rules (loaded only when touching shared/)
- Room-KMP DB is the single source of truth; expose data to UI as Flow/StateFlow only.
- Every synced entity: client UUIDv7 id, createdAt, updatedAt (LWW key), deletedAt
  (soft-delete tombstone; null = live). `syncStatus` is local-only (never on the wire).
  Data write + outbox enqueue happen in ONE transaction.
- commonMain is platform-agnostic. Anything needing Context / NSFileManager / OS
  scheduling goes in androidMain/iosMain via expect/actual — justify each new seam.
- No `!!`; explicit visibility on public API; new public API gets intent-level KDoc.
- Prefer a failing unit test first for domain/data logic.
