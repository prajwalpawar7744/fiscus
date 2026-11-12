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
- [ ] Decide when to raise targetSdk to 37
- [x] Add deps: Hilt, Room 3/KSP, Navigation 3, DataStore, serialization, datetime; toolchain upgrade (ADR-011)
- [x] M3 Expressive theme (`MaterialExpressiveTheme`, dynamic color, brand schemes ×3 contrast levels, expressive motion, tabular numbers) (ADR-012)
- [x] App shell: navigation suite, Navigation 3 back stack, 5 placeholder features with the full pattern (ADR-015)

## M1 Transactions
- [ ] Room schema: Account, Category, Transaction
- [ ] Add / edit / delete transaction (FAB menu)
- [ ] Transaction list grouped by day, with search and filter

## M2 Accounts & Categories
- [ ] Manage accounts and balances
- [ ] Manage categories; seed defaults

## M3 Budgets
- [ ] Per-category and overall budgets with progress

## M4 Insights
- [ ] Home dashboard (balance hero, recent, month summary)
- [ ] Charts: spending by category, trend over time

## M5 Data & Polish
- [ ] Backup/restore and CSV export
- [ ] Recurring transactions
- [ ] App lock (biometric), settings (incl. "use brand colors" toggle, ADR-012), onboarding
- [ ] Baseline and Startup Profiles with Macrobenchmark
