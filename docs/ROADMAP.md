# Roadmap

> Status: **proposed**. Update checkboxes as work lands.

## M0 Foundation
- [x] Project template created
- [x] Foundation docs (CLAUDE.md, docs/)
- [x] User guidelines integrated into docs
- [x] Set version to 0.1.0 (versionCode 100); debug suffix `.debug` / `-debug`
- [x] Decide material3 version → latest pre-release, all Expressive APIs (ADR-006)
- [x] Pin latest material3 pre-release in version catalog (1.5.0-rc01, compileSdk 37)
- [x] Gradle build cache, configuration cache, parallel builds (ADR-009)
- [x] APK naming `fiscus-vX.Y.Z-<buildType>` (ADR-009)
- [x] Architecture verified against Google's guide (ADR-007)
- [x] Hero moments and icon style decided (ADR-010)
- [ ] Decide chart approach (no M3 component; ask user)
- [ ] Release signing (keystore kept out of git) and enable R8 full mode
- [x] Raise targetSdk to 37 (ADR-008)
- [x] Add deps: Hilt, Room 3/KSP, Navigation 3, DataStore, serialization, datetime; toolchain upgrade (ADR-011)
- [x] M3 Expressive theme (`MaterialExpressiveTheme`, dynamic color, brand schemes ×3 contrast levels, expressive motion, tabular numbers) (ADR-012)
- [x] App shell: navigation suite, Navigation 3 back stack, 5 placeholder features with the full pattern (ADR-015)

## M1 Transactions
- [x] M1a data foundation: models, Room 3 schema v1, seed data, repositories, DI, tests (ADR-016/017)
- [x] Add / edit / delete transaction: FAB menu (Home, Transactions), full-screen editor, detail screen with edit/delete (ADR-018)
- [x] Required transaction title, schema v2 with migration (ADR-021)
- [x] Transaction list grouped by day, with search and filter (ADR-022)
- [ ] Transactions list-detail (two panes) on medium/expanded widths (ADR-022; needs the material3-adaptive Navigation 3 scene strategy)

## M2 Accounts & Categories
- [x] Manage accounts and balances: list, page, add/edit, archive, delete-if-unused, reconcile (ADR-036)
- [x] Manage categories (list by kind, add/edit, icon picker, archive, delete-if-unused) from Settings; defaults seeded (ADR-040)

## M3 Budgets
- [ ] Per-category and overall budgets with progress

## M4 Insights
- [x] Home dashboard with today's data: balance hero with this month's in/out, accounts, recent transactions (ADR-033)
- [ ] Charts: spending by category, trend over time

## M5 Data & Polish
- [ ] Backup/restore and CSV export
- [ ] Recurring transactions
- [ ] App lock (biometric), settings (incl. "use brand colors" toggle, ADR-012), onboarding
- [ ] Baseline and Startup Profiles with Macrobenchmark
