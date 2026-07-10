---
paths:
  - "shared/**"
---
# :shared module rules (loaded only when touching shared/)
- Room-KMP DB is the single source of truth; expose data to UI as Flow/StateFlow only.
- Every synced entity: client-UUID id, updatedAt, deleted (soft delete), syncStatus.
  Data write + outbox enqueue happen in ONE transaction.
- commonMain is platform-agnostic. Anything needing Context / NSFileManager / OS
  scheduling goes in androidMain/iosMain via expect/actual — justify each new seam.
- No `!!`; explicit visibility on public API; new public API gets intent-level KDoc.
- Prefer a failing unit test first for domain/data logic.
