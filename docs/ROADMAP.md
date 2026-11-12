# Roadmap

> Status: **proposed**. Update checkboxes as work lands.

## M0 Foundation
- [x] Project template created
- [x] Foundation docs (CLAUDE.md, docs/)
- [x] User guidelines integrated into docs
- [x] Set version to 0.1.0 (versionCode 100); debug suffix `.debug` / `-debug`
- [x] Decide material3 version → latest pre-release, all Expressive APIs (ADR-006)
- [x] Pin latest material3 pre-release in version catalog (1.5.0-rc01, compileSdk 37)
- [ ] Decide chart approach
- [ ] Add deps: Hilt, Room/KSP, Navigation 3, DataStore, serialization, datetime
- [ ] M3 Expressive theme (dynamic color, fallback, extended finance colors, motion)
- [ ] App shell: navigation suite, Navigation 3 back stack, placeholder screens

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
- [ ] App lock (biometric), settings, onboarding
