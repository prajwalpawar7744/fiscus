# CLAUDE.md — Fiscus

Fiscus is an offline-first personal finance tracker for Android, built with **Kotlin + Jetpack Compose** using **Material 3 Expressive**.

## Stack (source of truth: `gradle/libs.versions.toml`, `app/build.gradle.kts`)
- AGP 9.2.1 · Kotlin 2.2.10 · Compose BOM 2026.02.01
- material3 1.5.0-rc01: **latest pre-release, pinned over the BOM**; all Expressive APIs, experimental allowed (ADR-006)
- compileSdk 37 · targetSdk 36 · minSdk 26 · Java 11
- Package / applicationId: `com.prajwalpawar.fiscus`
- Single module: `:app`
- Build types: debug → `com.prajwalpawar.fiscus.debug`, `X.Y.Z-debug`; release → no suffix
- Outputs are named `fiscus-v<versionName>-<buildType>.apk`
- Gradle build cache, configuration cache, and parallel builds are on (`gradle.properties`)

## Commands
```bash
./gradlew assembleDebug          # build
./gradlew installDebug           # install on device/emulator
./gradlew test                   # unit tests
./gradlew connectedAndroidTest   # instrumented tests
./gradlew lint                   # Android lint
```

## Docs index (read the relevant ones before feature work)
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): layers, packages, libraries
- [docs/M3_EXPRESSIVE.md](docs/M3_EXPRESSIVE.md): M3 Expressive design language: intent, look & feel, tokens, per-screen checklist
- [docs/DESIGN_SYSTEM.md](docs/DESIGN_SYSTEM.md): M3 Expressive theme, motion, components, a11y
- [docs/CONVENTIONS.md](docs/CONVENTIONS.md): code style, Compose patterns, testing, commits
- [docs/DOMAIN.md](docs/DOMAIN.md): finance glossary and data model
- [docs/ROADMAP.md](docs/ROADMAP.md): milestones and current status
- [docs/DECISIONS.md](docs/DECISIONS.md): architecture decision log
- [docs/VERSIONING.md](docs/VERSIONING.md): semantic versioning and release rules
- [docs/REFERENCES.md](docs/REFERENCES.md): verified facts from official sources, with URLs and dates

## Working rules for Claude
1. **User Guidelines below override everything else** in these docs.
2. Add dependencies only via the version catalog (`libs.versions.toml`), never hardcoded.
3. Money is a `Long` in minor units (paise/cents). Never `Float`/`Double` for amounts.
4. Keep changes small and scoped to the task; don't refactor unrelated code.
5. Record any architectural choice in `docs/DECISIONS.md`.
6. Tick off / update `docs/ROADMAP.md` when a task finishes. Commits use `type(scope): summary` (see CONVENTIONS.md). Bump the version per VERSIONING.md when releasing.
7. Build (`./gradlew assembleDebug`) before saying a change is done.
8. Items marked *proposed* in docs are defaults that are not final yet. Confirm with the user before relying on them heavily.
9. Before using an M3 component API, verify its current name and status (stable or experimental) in [REFERENCES.md](docs/REFERENCES.md) or the official docs. Add anything new you learn to REFERENCES.md.

## User Guidelines (non-negotiable)
1. **Professional, scalable architecture.** Follow Google's official Android architecture guide and recommendations, plus professional coding standards. See [ARCHITECTURE.md](docs/ARCHITECTURE.md) and [CONVENTIONS.md](docs/CONVENTIONS.md).
2. **Consistency everywhere.** Use one pattern for code structure, naming, state handling, and UI/UX across the whole project. Before writing anything new, copy the pattern of existing code rather than inventing a new one.
3. **Semantic versioning.** See [VERSIONING.md](docs/VERSIONING.md).
4. **Material 3 Expressive only.** The entire app follows the M3 Expressive design language and its guidelines. Nothing outside them.
5. **Only M3 Expressive components.** Use only components from `androidx.compose.material3`, which includes the Expressive APIs. No third-party UI component libraries, and no hand-built custom components that replace or imitate a Material component. Composing Material components together (for example, a card made of `ElevatedCard` + `Text` + `ButtonGroup`) is allowed. If no Material component fits a need, **stop and ask the user**.
6. **Reference sources.** Use https://m3.material.io/ and https://developer.android.com/ as the primary sources, with other reputable web sources as secondary. Record each useful finding in [REFERENCES.md](docs/REFERENCES.md) with its URL and the date it was checked.
7. **Read the docs first.** Before any feature, UI, or fix, read CLAUDE.md and the relevant `docs/` files, and follow them.
