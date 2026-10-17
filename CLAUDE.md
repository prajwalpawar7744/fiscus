# Fiscus

Personal finance manager for Android. Kotlin + Jetpack Compose, designed in **Material 3 Expressive**. Single `app` module, package `com.prajwalpawar.fiscus`. Not released yet.

## Build, test, run

```bash
./gradlew :app:compileDebugKotlin        # fast check (also runs KSP: Room validates every @Query)
./gradlew :app:testDebugUnitTest         # JVM tests (form validation, amount parsing, dates)
./gradlew :app:assembleDebugAndroidTest  # compile instrumented tests
./gradlew :app:connectedDebugAndroidTest # run them on a device — UNINSTALLS the app afterwards
./gradlew :app:installDebug
```

- The dev phone is connected over **wireless adb**. The user often uses the phone at the same time — check before driving the UI with `adb shell input`, and stop if the screen shows activity you didn't cause.
- Instrumented tests (`app/src/androidTest`) cover the SQL behind Home and Manage (balances, totals, usage counts, delete guards) and ICU money formatting. `connectedDebugAndroidTest` uninstalls the app, wiping on-device data — ask first.
- Reading the on-device DB: `adb exec-out run-as com.prajwalpawar.fiscus cat databases/fiscus.db` — also pull `fiscus.db-wal` and `fiscus.db-shm`, or recent writes are missing.

## Stack (pinned on purpose)

- AGP 9.2.1, Kotlin 2.4.10, KSP 2.3.10, compileSdk 37 / targetSdk 36 / minSdk 26
- Compose BOM 2026.08.00, **material3 1.5.0-alpha27** (pinned above the BOM for Expressive APIs), foundation 1.12.0-beta01
- Room 2.8.4, DataStore Preferences 1.2.1, Navigation Compose 2.10.0 (string routes), core-splashscreen 1.2.0
- No DI framework: repositories are built in `FiscusApp` with `remember`, ViewModels get hand-written `*ViewModelFactory` classes (follow this pattern; one factory file per ViewModel).
- Material3 sources are handy for checking APIs: `~/.gradle/caches/modules-2/files-2.1/androidx.compose.material3/material3-android/1.5.0-alpha27/*/material3-android-1.5.0-alpha27-sources.jar` (samples jar sits next to it).

## Architecture

```
MainActivity      reads settings once (StateFlow<AppSettings?>), holds splash until loaded,
                  sets system bar icon colors from the app theme, wraps FiscusTheme
FiscusApp         Scaffold + NavHost, builds repositories/factories, hosts the
                  add-transaction sheet and the activity-scoped TransactionViewModel
data/local        Room (FiscusDatabase, DAOs, entities, seeder), SettingsDataStore
data/model        domain models (Transaction, Account, Category, AccountBalance, ...)
data/repository   validation + mapping (entity <-> model, DAO rows -> models)
ui/components     shared Fiscus* components (use these before writing new UI)
ui/screens        home, transaction (sheet + details), transactions, analysis,
                  settings, manage (category/account lists and editors)
ui/theme          FiscusTheme + tokens (Spacing, Size, Elevation, Motion, Shapes)
util              Money.kt (formatMoney, currencySymbol), DateRanges.kt
```

`di/` and `domain/` folders may exist empty — leave them unless asked.

### Navigation (`ui/navigation`)

- Tabs (`FiscusDestination`): home, transactions, analysis, settings. Switch tabs with `navController.navigateToTab(...)` (same behaviour as the bottom bar).
- Pages (`FiscusRoutes`): `category/new?type=`, `category/edit/{id}`, `account/new`, `account/edit/{id}`, `categories`, `accounts`, `transaction/{id}`, `search`. Don't nest a page under a tab's route (e.g. `transactions/search`): tab detection uses `startsWith`.
- Non-tab pages hide the bottom bar and get a back arrow (`isTopLevel` in `FiscusApp`); the title for each route is a `when` in `FiscusApp`'s top bar.
- The add-transaction sheet shows only on tabs and on the transaction details page; it steps aside (state kept) while a page like "New category" is open, then returns with the new item selected.
- **Container transform**: a tapped row/card grows into the page it opens. `NavHost` sits in a `SharedTransitionLayout`; register destinations with `fiscusComposable(...)` (not `composable`) and put `Modifier.containerTransform(ContainerKeys.x)` on both the source element and the destination page root (`ui/navigation/ContainerTransform.kt`). It is a no-op outside the NavHost, so pages opened from the add-transaction sheet (a separate window) fall back to the normal fade + scale. When adding a page, add its key to `ContainerKeys` and tag every place that opens it.

## Data rules

- **Money is `Long` hundredths** (12.50 → 1250). Input allows ≤12 integer digits and ≤2 decimals (`isAmountInput`). Convert with `amountToMinorUnits` / `minorUnitsToAmountInput` (both tested).
- **Dates are the local calendar day stored as UTC-midnight millis** (DatePicker works in UTC). Use `monthRange()` / `storedDateToLocalDate()`; never build ranges from the device time zone.
- **Schema stays at `version = 1` until release.** Change entities in place, no migrations; regenerate `app/schemas/.../1.json` (KSP writes it). After any schema change the dev phone's DB has a stale identity hash → tell the user to run `adb shell pm clear com.prajwalpawar.fiscus`.
- **Auto Backup restores the DB on reinstall** (`allowBackup=true`, extraction rules are still the empty sample). A restored DB from a newer/older schema crashes Room. `fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)` handles newer-schema restores (marked "revisit before release"). Clearing data with `pm clear` does not trigger a restore; uninstall + reinstall can.
- Defaults are seeded in `RoomDatabase.Callback.onCreate` (14 categories, "Cash" account as default).
- Transactions: `title` is required; EXPENSE/INCOME need `categoryId` + `accountId`; TRANSFER needs `sourceAccountId` + `destinationAccountId` (different) and no category. `TransactionRepository.validate` enforces it.
- Balances: opening balance + income − expense + transfers in − transfers out (SQL in `AccountDao.observeAccountBalances`). Credit-card spending goes negative and lowers the total, which is intended. Archived accounts are excluded from Home's total.
- Categories/accounts: **archive** (hidden from pickers, history keeps names) and **delete only when unused and not default** (repository `require`s). Can't archive the last active account or the last active category of a type. A category's type is locked once transactions use it. Duplicate names are checked case-insensitively (the DB unique index is case-sensitive).
- FK behaviour: accounts `RESTRICT`, categories `SET_NULL`.

## UI conventions

- **All UI must follow Material 3 Expressive.** For every new screen or component, reach for the Expressive version first: `MaterialExpressiveTheme` tokens, emphasized type styles (`*Emphasized`), `MaterialShapes` via `FiscusShapes.icon`, spring motion from `MaterialTheme.motionScheme`, and Expressive components (connected `ButtonGroup`, segmented lists, flexible top app bars, `ShortNavigationBar`, extended FABs, `HorizontalFloatingToolbar`, `LoadingIndicator`, wavy progress, carousels, container transforms). Use a baseline M3 component only when there's no Expressive equivalent, and say so. Check the existing `Fiscus*` components before building new UI, and verify Expressive APIs against the pinned material3 sources rather than from memory.
- **Design tokens only — no raw `.dp` outside `ui/theme/`.** `FiscusSpacing` (gaps/padding, incl. `none`), `FiscusSize` (component dimensions), `FiscusElevation` (M3 levels 0–5), `FiscusMotion` (shared motion values). Add a token rather than inlining. Platform constants (`ButtonDefaults.*`, `ListItemDefaults.SegmentedGap`, `FloatingToolbarDefaults.*`) are fine.
- Theme: `MaterialExpressiveTheme` with Expressive default shapes/motion/system font; dynamic color on Android 12+ (toggle in Settings), else `expressiveLightColorScheme()` / baseline dark.
- **`FiscusShapes.icon`** is the user-chosen decorative shape (Settings → Icon shape; default Cookie). Use it for every decorative icon container (list badges, empty states, previews, detail heroes). Chips, buttons, segmented rows and the icon-picker toggles keep their own Material shapes on purpose.
- Motion: timings come from `MaterialTheme.motionScheme` (springs). Tab transitions fade + scale from `FiscusMotion.ENTER_SCALE`.
- Shared components to reuse:
  - `FiscusSegmentedListItem` — segmented rows on `surfaceContainer` (`onClick = null` = static row, no ripple/button role). Shapes come from position: `ListItemDefaults.segmentedShapes(index, count)`.
  - `FiscusListIcon` — tonal badge in `FiscusShapes.icon`.
  - `FiscusSectionHeader` — `labelLarge`, primary, heading semantics.
  - `FiscusConnectedToggleGroup` — single-choice `ButtonGroup` with `animateWidth` press motion (transaction type, category type).
  - `FiscusDropdownMenu` — Expressive grouped menu; Settings uses dropdowns for every choice (user's decision, for consistency).
  - `FiscusChipIcon` — filter-chip leading icon that crossfades to a check.
  - `FiscusSubmitButton` — full-width medium Expressive button with shape morph.
  - `FiscusTransactionRow` + `transactionsByDay { transaction, shapes -> }` — the one transaction row and day-grouped list used by Home, Transactions and search (`showDate = true` for search results).
  - `FiscusTransactionAmount` — amount with direction arrow instead of a sign: ↑ income (primary), ↓ expense, ⇄ transfer; arrows carry TalkBack labels.
  - `FiscusEmptyState`, `DockedDatePicker` (animated popup; tapping the field opens it), `FiscusTopAppBar` (small/medium/large flexible, optional subtitle and back arrow).
  - `ItemEditorLayout` + `ItemManageActions` for category/account editors.
- **Charts** (drawn with plain Compose layout, no chart library): pick the form first, colour last. One series → one colour (`primary`); to single out one item use emphasis (`primary` vs muted `outlineVariant`), not extra hues — dynamic colour makes multi-hue palettes impossible to check for colour-blind safety. No pies/donuts, no dual axes. Marks are thin (`FiscusSize.chart*` tokens) with a 4dp rounded data end and a square baseline; one solid hairline baseline, no dashed grids. Values and labels use text colours, never the mark colour; label selectively and give every mark a TalkBack description. Load the `dataviz` skill before adding a chart.
- Accessibility: decorative icons get `contentDescription = null`; when a visible label is hidden (icons-only nav, collapsed FAB) the icon gets the label instead.
- Strings are still hard-coded (no `strings.xml` yet).

## Screens (current behaviour)

- **Home**: total balance hero, this month's Income/Spent tiles, wavy progress of spent vs income (hidden when no income), accounts carousel (`HorizontalMultiBrowseCarousel`, last card "Add account", tap a card to edit), last 10 transactions grouped by day with "See all". Month shown as the app bar subtitle.
- **Add transaction sheet**: type toggle → amount hero (currency symbol + "0" drawn inside the field via `VisualTransformation`) → title → category / account chip rows (edge-to-edge `LazyRow`, "Add" assist chip last, unkeyed) → date → note → submit, with a hint naming the first missing field. Draft lives in `TransactionViewModel` (survives rotation and dismiss, not process death). Edit mode (`startEditing`) sets aside any unfinished add draft and restores it afterwards.
- **Transaction details**: hero, segmented details, `HorizontalFloatingToolbar` with Edit / Delete (confirm). Live — edits show immediately.
- **Settings**: Appearance (theme, dynamic color, icon shape, app bar size, bottom bar labels, app bar scroll), Manage (Categories, Accounts), About. "Collapse" scroll is hidden and read as Pinned when the app bar is Small.
- **Transactions**: month by month (‹ month › header, no future months), whole-month Income/Spent/Net summary, filters (type as a connected button group; Account and Category filter chips with menus; Clear), list grouped by day. Deleting happens only from the details page (swipe-to-delete was removed: its red background showed through the container transform). Uses the app's normal title bar (so the app bar size/scroll settings apply) with a **search icon** that opens the Search page (`search` route): its own search field as the header, focused on arrival, results across all months (title or note, `LIKE` with escaped wildcards) that container-transform into details. The app title bar is hidden only on the Search page. (A search icon can't open `ExpandedFullScreenContainedSearchBar`: it grows out of an on-screen search field's position, which only `SearchBar`/`AppBarWithSearch` record.) Filtering happens in `TransactionsViewModel` over the month's rows; the summary ignores filters.
- **Analysis**: same ‹ month › switcher (`FiscusMonthSwitcher`); 2×2 stat tiles (Spent + income, change vs last month in words, daily average — days so far for the current month — and savings rate); "By category" ranked single-colour bars with an Expense/Income toggle (top 6 + "Other"); "Spending by month" 6-month columns with the selected month emphasised (tap a column to jump there); "Biggest expenses" (top 5, rows open details). Maths is pure and unit-tested in `AnalysisMath.kt`; monthly grouping uses SQLite `strftime` in UTC, matching the UTC-midnight date storage.

## Known bugs and workarounds

- **Stale keyboard padding after a predictive-back swipe** (fixed with a workaround): Compose foundation-layout 1.12.0-beta01 `InsetsListener.onEnd` only applies final insets when `durationMillis > 0`; the predictive-back keyboard hide is user-driven, so a blank band stayed in the sheet. `ImeInsetsRepair()` re-dispatches insets when `ime` disagrees with `imeAnimationTarget`. It must run once **per window** (in `FiscusApp` and inside the add-transaction sheet, which is its own window; any future dialog-based search or sheet with text fields needs it too). Remove when Compose fixes it — re-test by swiping back with the keyboard open in the sheet.
- **`ModalBottomSheet` handles IME and nav-bar insets itself** (and consumes them); don't add `imePadding()` / `navigationBarsPadding()` inside sheet content.
- **`java.text` can't do Indian digit grouping** (₹12,34,567.89); `formatMoney` uses `android.icu.text.NumberFormat`, so its tests are instrumented, not JVM.
- **`DatePicker` has a 360dp minimum width**; don't pad around it (`FiscusSize.datePickerMaxWidth` matches).
- **Keyed "Add" item in a lazy chip row** anchored the scroll to the end while data loaded — keep that item unkeyed.
- A full-text `MediumTopAppBar`/`LargeTopAppBar` title style override clipped the collapsed title — let the flexible bars choose title styles.
- Possible issue (unconfirmed): after a force-stop + relaunch the add-transaction sheet was seen already open. May have been the user tapping; check `showAddTransactionSheet` restoration if it recurs.

## Open items

- Account opening balance can't be set in the UI (DB supports it).
- Month on Home is fixed when it opens (`shortcut:` in `HomeViewModel`).
- Draft isn't kept across process death.
- Clean-up: move strings to `strings.xml`; `material3` is declared twice in `app/build.gradle.kts`; `.kotlin/errors/*.log` are committed — add `.kotlin/` to `.gitignore` and untrack.
- Before release: decide backup rules (`data_extraction_rules.xml`, `backup_rules.xml`), revisit the destructive downgrade fallback, and start real migrations from schema 1.

## Working with the user

- Commit messages: Conventional Commits, `type(scope): summary` (scopes used: theme, settings, transaction, home, manage, navigation, ui, state, db). The user commits themselves; suggest a message when work is done.
- Prefers being asked before larger design decisions; explains choices in Material 3 Expressive terms.
