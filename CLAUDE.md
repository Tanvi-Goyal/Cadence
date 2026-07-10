@AGENTS.md

# Claude-specific additions
- Use plan mode by default for changes under `shared/` (sync, DB, expect/actual):
  present the plan and wait for approval before editing.
- Keep the main session context clean — spawn a research subagent for doc-diving
  or large code reads and hand back a short summary, don't dump into main thread.
- Path-scoped rules live in `.claude/rules/`; deep how-tos live in
  `.claude/skills/`. Don't inline that depth here.
- When I correct you, save the durable lesson to auto memory so I don't repeat it.
