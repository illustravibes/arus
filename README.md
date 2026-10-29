# Arus

Arus is an Android-first, offline-first personal finance app built with Expo SDK 58, React Native, Expo Router, `@expo/ui`, and `expo-sqlite`.

## Start development

```bash
npm install
npx expo start
```

Use Expo Go for UI and standard SQLite work. Use the EAS `development` profile for capabilities that require a rebuilt native binary.

## Quality checks

```bash
npm run lint
npm run typecheck
npm run foundation:test
npm run doctor
```

## Repository context

- `arus-erd.md` — authoritative data and domain model.
- `arus-user-flow.md` — authoritative navigation, screen behavior, and edge cases.
- `plan/` — product, architecture, security, testing, release, and Expo implementation plans.
- `wireframes/` — original screen references.
- `_bmad-output/` — BMAD initiative, epics, stories, and implementation plans.

BMAD runtime files are intentionally not committed here. Install BMAD and repository skills on a new device, then use this repository as the project root; the planning context is already committed above.

## BMAD setup on a new device

From the repository root, install the BMAD runtime and skills locally, then configure the active initiative from the committed `_bmad-output/` tree. The BMAD runtime (`_bmad/`) and installed skill cache (`.agents/`) are local tooling; product context and backlog are versioned in this repository.
