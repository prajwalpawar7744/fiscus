# Fiscus

Personal finance manager for Android. Kotlin + Jetpack Compose, designed in **Material 3 Expressive**. Single `app` module, package `com.prajwalpawar.fiscus`. In public beta (first release `0.1.0-beta.1`). Fully offline: no `INTERNET` permission, no accounts, all data on the device.

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
- **Debug builds** use package `com.prajwalpawar.fiscus.debug`, version suffix `-debug` and the name "Fiscus Debug", so they install beside a signed release build (different signatures can't replace each other). Each has its own data.
- Reading the on-device DB (debug build): `adb exec-out run-as com.prajwalpawar.fiscus.debug cat databases/fiscus.db` — also pull `fiscus.db-wal` and `fiscus.db-shm`, or recent writes are missing.

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
- Pages (`FiscusRoutes`): `category/new?type=`, `category/edit/{id}`, `account/new`, `account/edit/{id}`, `categories`, `accounts`, `profile`, `transaction/{id}`, `search`. Don't nest a page under a tab's route (e.g. `transactions/search`): tab detection uses `startsWith`.
- Non-tab pages hide the bottom bar and get a back arrow (`isTopLevel` in `FiscusApp`); the title for each route is a `when` in `FiscusApp`'s top bar.
- The add-transaction sheet shows only on tabs and on the transaction details page; it steps aside (state kept) while a page like "New category" is open, then returns with the new item selected.
- **Container transform** (hero moments only — see Motion): a tapped transaction row or Home account card grows into the page it opens. `NavHost` sits in a `SharedTransitionLayout`; register destinations with `fiscusComposable(...)` (not `composable`) and put `Modifier.containerTransform(ContainerKeys.x)` on both the source element and the destination page root (`ui/navigation/ContainerTransform.kt`). It is a no-op outside the NavHost, so pages opened from the add-transaction sheet (a separate window) fall back to the normal fade + scale. When adding a page, add its key to `ContainerKeys` and tag every place that opens it.

## Data rules

- **Money is `Long` hundredths** (12.50 → 1250). Input allows ≤12 integer digits and ≤2 decimals (`isAmountInput`). Convert with `amountToMinorUnits` / `minorUnitsToAmountInput` (both tested).
- **Dates are the local calendar day stored as UTC-midnight millis** (DatePicker works in UTC). Use `monthRange()` / `storedDateToLocalDate()`; never build ranges from the device time zone.
- **Schema history**: 1 shipped with 0.1.0-beta.1; **2** adds `transaction_items` (`AutoMigration(1, 2)`, tested in `androidTest/.../MigrationTest.kt` via `room-testing`; schemas are androidTest assets).
- **Schema 1 shipped with 0.1.0-beta.1 — it is frozen.** Real users now have this database, so never edit entities in place again: every schema change bumps `version` in `FiscusDatabase` and adds a migration (prefer `AutoMigration(from, to)`; write a `Migration` for anything it can't infer), keeps the exported `app/schemas/.../N.json` files committed, and gets a migration test. Wiping data (`pm clear`) is no longer an acceptable fix.
- **Auto Backup restores the DB on reinstall** (`allowBackup=true`, extraction rules are still the empty sample). Older-schema restores upgrade through the migrations. `fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)` wipes data only on a downgrade (installing an older build over a newer one). Clearing data with `pm clear` does not trigger a restore; uninstall + reinstall can.
- Defaults (14 categories, "Cash" as the default account) are seeded in `RoomDatabase.Callback.onCreate`, and again in `onOpen` via `FiscusDatabaseSeeder.seedEmptyTables` — but only into a table that is completely empty. An Auto Backup restore at install brings back an existing database, so `onCreate` never runs for it; without the `onOpen` check a restored empty database had no defaults. Never re-seed into a table with rows (it would re-add renamed or archived items).
- **Items** (`TransactionItem`: name, quantity and unit price, both `Long` hundredths; line total = quantity × price ÷ 100, rounded half-up): optional breakdown for expense/income, never transfers. When present, the transaction's `amount` must equal the sum of item totals (`TransactionRepository.validate`). Items are saved in the same DB transaction and replaced as a whole on update; `ON DELETE CASCADE` removes them with the transaction.
- Transactions: `title` is required; EXPENSE/INCOME need `categoryId` + `accountId`; TRANSFER needs `sourceAccountId` + `destinationAccountId` (different) and no category. `TransactionRepository.validate` enforces it.
- Balances: opening balance (set in the account editor; text with optional leading "-" for money owed) + income − expense + transfers in − transfers out (SQL in `AccountDao.observeAccountBalances`). Credit-card spending goes negative and lowers the total, which is intended. Archived accounts are excluded from Home's total.
- Categories/accounts: **archive** (hidden from pickers, history keeps names) and **delete only when unused and not default** (repository `require`s). Can't archive the last active account or the last active category of a type. A category's type is locked once transactions use it. Duplicate names are checked case-insensitively (the DB unique index is case-sensitive).
- FK behaviour: accounts `RESTRICT`, categories `SET_NULL`.

## UI conventions

- **Reference: https://m3.material.io/** — read the official Material 3 (Expressive) guidelines there for any component or design-language question: which component to use, its anatomy, usage do's and don'ts, and what to avoid. Follow it over memory or habit; the pinned material3 sources decide what the API actually offers.
- **All UI must follow Material 3 Expressive.** For every new screen or component, reach for the Expressive version first: `MaterialExpressiveTheme` tokens, emphasized type styles (`*Emphasized`), `MaterialShapes` via `FiscusShapes.icon`, spring motion from `MaterialTheme.motionScheme`, and Expressive components (connected `ButtonGroup`, segmented lists, flexible top app bars, `ShortNavigationBar`, extended FABs, `HorizontalFloatingToolbar`, `LoadingIndicator`, wavy progress, carousels, container transforms). Use a baseline M3 component only when there's no Expressive equivalent, and say so. Check the existing `Fiscus*` components before building new UI, and verify Expressive APIs against the pinned material3 sources rather than from memory.
- **Icons are `Icons.Rounded.*`** (`AutoMirrored.Rounded` for directional ones), matching M3E's Material Symbols Rounded. Only the bottom bar's unselected tab icons stay `Icons.Outlined` (selected = filled rounded).
- **Every button gets the press shape morph**: `shapes = ButtonDefaults.shapes()` on Button/TextButton/OutlinedButton/FilledTonalButton, `IconButtonDefaults.shapes()` on IconButton (needs `ExperimentalMaterial3ExpressiveApi`).
- App bar titles use the bars' default colour and style (on-surface, no bold or primary override).
- **Progress and loading are always the wavy / Expressive versions.** Any ratio, share, progress or meter (spent vs income, a category's share, a goal) uses `LinearWavyProgressIndicator`, or `CircularWavyProgressIndicator` where a ring fits; indeterminate waits use `LoadingIndicator` (or `ContainedLoadingIndicator` on a busy background). Never the baseline `LinearProgressIndicator` / `CircularProgressIndicator`. Pass progress as a lambda clamped to 0–1 (spring animations can overshoot), and only show a meter where a full track honestly means 100%.
- **Design tokens only — no raw `.dp` outside `ui/theme/`.** `FiscusSpacing` (gaps/padding, incl. `none`), `FiscusSize` (component dimensions), `FiscusElevation` (M3 levels 0–5), `FiscusMotion` (shared motion values). Add a token rather than inlining. Platform constants (`ButtonDefaults.*`, `ListItemDefaults.SegmentedGap`, `FloatingToolbarDefaults.*`) are fine.
- Theme: `MaterialExpressiveTheme` with Expressive default shapes/motion/system font; dynamic color on Android 12+ (toggle in Settings), else the user's **palette** (`PalettePreference`, Settings → Appearance → Palette swatches, shown only when Dynamic color is off/unsupported; `PalettePreference.colorScheme(dark)` in `Theme.kt`): Monochrome (default; hand-made in `ui/theme/FiscusColors.kt`, one neutral grey palette, black/white primary, only error red) or Blue / Green / Purple / Rose / Amber — in `ui/theme/FiscusPalettes.kt`, each Material's tonal-spot scheme (2025 spec) computed from one seed colour noted in the file; all 49 roles incl. fixed. Change a palette as a whole from a new seed (e.g. Material Theme Builder), never role by role. The project is Kotlin only — no helper scripts. The palette is part of the SETTINGS backup.
- **`FiscusShapes.icon`** is the user-chosen decorative shape (Settings → Icon shape; default Cookie). Use it for every decorative icon container (list badges, empty states, previews, detail heroes). Chips, buttons, segmented rows and the icon-picker toggles keep their own Material shapes on purpose.
- **Motion follows M3's transition patterns** (m3.material.io → Motion → Transitions; those still use easing + duration, so their timings are `FiscusMotion` tokens: 300 ms, fade out 90 ms then in 210 ms, emphasized easing). Component motion (rows, popups, morphs) uses `MaterialTheme.motionScheme` springs: spatial for movement, effects for opacity.
  - **Navigation** (`ui/navigation/FiscusTransitions.kt`, `transitionBetween(from, to)`): tabs = **top-level fade through** (no slide, no scale); pages = Android **forward/backward** (short slide + fade); **container transform** only for hero moments — a transaction row → details (any list, search) and a Home account card → its page. Settings rows, Manage rows, the avatar and editors use forward/backward (M3: container transform "may feel excessive when used frequently" and doesn't suit deep hierarchies).
  - **Periods** (Transactions, Analysis): `LateralPeriodContent` — M3 **lateral**: the period's content slides as one group from the side you're moving to, **no fade**; the switcher stays put. Changes within a period don't slide.
  - **Lists**: every lazy row uses `fiscusAnimateItem()` (fade + spring placement) so nothing pops in or jumps.
  - **Enter/exit**: components expand/shrink along one axis from their edge (docked date picker drops down; item cards expand from the top) — never scale in/out (M3: scale implies elevation change).
  - **Loading**: screens show the wavy `LoadingIndicator` while data loads (the user's choice over M3's skeleton loaders), then the content fades in quickly.
  - **Shape morph** shows state (palette swatch circle → cookie via a graphics-shapes `Morph`).
  - **Reduced motion**: `LocalReducedMotion` / `FiscusReducedMotion.enabled` (system "Remove animations", animator scale 0, re-read on resume). When on, every pattern becomes a short fade, container transforms and lateral slides are off and no shape morph. Any new animation must check it.
- Shared components to reuse:
  - `FiscusSegmentedListItem` — segmented rows on `surfaceContainer` (`onClick = null` = static row, no ripple/button role). Shapes come from position: `ListItemDefaults.segmentedShapes(index, count)`.
  - `FiscusListIcon` — tonal badge in `FiscusShapes.icon`.
  - `FiscusSectionHeader` — `labelLarge`, primary, heading semantics.
  - `FiscusConnectedToggleGroup` — single-choice `ButtonGroup` with `animateWidth` press motion (transaction type, category type).
  - `FiscusDropdownMenu` — Expressive grouped menu; Settings uses dropdowns for every choice (user's decision, for consistency).
  - `FiscusChipIcon` — filter-chip leading icon that crossfades to a check.
  - `FiscusSubmitButton` — full-width medium Expressive button with shape morph.
  - `FiscusSplitButton` + `FiscusMenuAction` — M3 split button (main action + ▾ menu of *related* alternatives; ▾ rotates, has expanded/collapsed state and a tooltip; menu anchored to ▾; `fillWidth` and `height` for size). Not used yet. Only for variants of one action — never to hide unrelated or destructive actions (e.g. Archive/Delete behind Save).
  - `FiscusTransactionRow` + `transactionsByDay { transaction, shapes -> }` — the one transaction row and day-grouped list used by Home, Transactions and search (`showDate = true` for search results).
  - `FiscusTransactionAmount` — amount with direction arrow instead of a sign: ↑ income (primary), ↓ expense, ⇄ transfer; arrows carry TalkBack labels.
  - `FiscusEmptyState`, `DockedDatePicker` (date field + animated docked popup; tapping the field opens it), `DockedDateRangePicker` (same docked popup with `DateRangePicker`; both share `DockedPickerPopup` / `DockedPickerActions`), `FiscusPeriodSwitcher`, `FiscusTopAppBar` (small/medium/large flexible, optional subtitle and back arrow).
  - `ItemEditorLayout` + `ItemManageActions` for category/account editors.
- **Charts** (drawn with plain Compose layout, no chart library): pick the form first, colour last. One series → one colour (`primary`); to single out one item use emphasis (`primary` vs muted `outlineVariant`), not extra hues — dynamic colour makes multi-hue palettes impossible to check for colour-blind safety. No pies/donuts, no dual axes. Marks are thin (`FiscusSize.chart*` tokens) with a 4dp rounded data end and a square baseline; one solid hairline baseline, no dashed grids. Values and labels use text colours, never the mark colour; label selectively and give every mark a TalkBack description. Load the `dataviz` skill before adding a chart.
- Accessibility: decorative icons get `contentDescription = null`; when a visible label is hidden (icons-only nav, collapsed FAB) the icon gets the label instead.
- Strings are still hard-coded (no `strings.xml` yet).

### What m3.material.io says (checked 2026-10, from the site's content as of 2026-10-13)

The site is a JS app; a plain fetch returns only titles. Its pages are JSON at `https://m3.material.io/_dsm/content/m3/<carbonVersion>/<page-id>` (version and slug→id map are in the site's `main.*.js`).

Follow (and Fiscus does):
- **App bars**: small (with subtitle), *medium flexible*, *large flexible*. Baseline medium/large are not recommended. App bars hold the title, navigation and 1–2 essential actions; page actions go in a toolbar. A *search app bar* is the emphasised global search entry (Fiscus uses a search icon instead — allowed, less emphasised).
- **Navigation bar**: the flexible one (`ShortNavigationBar`); baseline navigation bar not recommended. 3–5 fixed destinations.
- **Toolbars** replace the bottom app bar (not recommended). Floating toolbar for contextual actions, may pair with a FAB, vibrant colours for emphasis; **never show a toolbar together with the navigation bar**.
- **FABs**: small/medium/large extended FAB; baseline extended FAB and surface FABs not recommended. FAB menu replaces speed dial / stacked small FABs.
- **Buttons / icon buttons**: shape morph on press and select; round or square; sizes XS–XL; sentence-case concise labels. Toggle icon buttons: outlined icon unselected, filled selected.
- **Connected button groups** replace segmented buttons (not recommended).
- **Loading indicator** replaces the indeterminate circular spinner (waits under ~5 s; never decorative); progress indicators may be wavy.
- **Lists**: the expressive segmented style is recommended. **Menus**: the new vertical menus (grouped, with gaps) are recommended.
- **Carousel** is for visual items (images/video) with brief text; multi-browse should be avoided for text-heavy items — use uncontained.
- **Dividers** only group things when space can't; never bold.
- **Motion**: physics springs, expressive scheme for most things; spatial springs (may overshoot) for position/size/shape, effects springs (no overshoot) for colour/opacity; fast for small parts, slow for full screen.
- **Shape**: 10-step corner scale (none 0 … extra-extra-large 48, full); 35 Material shapes with morphing. Use abstract shapes sparingly and for decoration (avatars, image crops, badges) — not as a meaning; morph to show state.
- **Type**: 15 baseline + 15 emphasized styles; emphasized for highlighted moments (heroes, key actions).
- **Icons**: Material Symbols (outlined/rounded/sharp); keep one consistent style; 24dp standard.
- **Colour**: dynamic colour is fine but don't rely on it alone — keep a brand scheme (Fiscus: `FiscusColors.kt`). Use primary/secondary/tertiary contrast and surface tones for hierarchy.
- **Expressive tactics**: vary shape to draw attention, rich colour for the key element, emphasized type, contain important content, fluid spring motion.

## Screens (current behaviour)

- **Home**: app bar title is a time-of-day greeting with the first name (`greeting()` in `FiscusApp`; morning 5–11, afternoon 12–16, evening 17–20, night otherwise) and the profile avatar as the action (opens Profile); total balance hero, this month's Income/Spent tiles, wavy progress of spent vs income (hidden when no income), accounts carousel (`HorizontalUncontainedCarousel` — M3 reserves multi-browse for visual items and it cropped the text cards; last card "Add account", tap a card to edit), last 10 transactions grouped by day with "See all". Month shown as the app bar subtitle.
- **Add transaction sheet**: type toggle → amount hero (currency symbol + "0" drawn inside the field via `VisualTransformation`; read-only and showing the items' sum once items exist) → title → items (expense/income: "Split into items" adds segmented item cards with name, Qty, Price each and a line total; an incomplete item blocks submit with its own hint) → category / account chip rows (edge-to-edge `LazyRow`, "Add" assist chip last, unkeyed) → date → note → submit, with a hint naming the first missing field. Draft lives in `TransactionViewModel` (survives rotation and dismiss, not process death). Edit mode (`startEditing`) sets aside any unfinished add draft and restores it afterwards.
- **Transaction details**: hero, segmented details, an "N items" segmented list when itemised, `HorizontalFloatingToolbar` (vibrant) with Edit as its FAB and Delete (confirm) inside. Live — edits show immediately.
- **Settings**: Profile (row with `FiscusAvatar` + name → Profile page: photo via the system Photo Picker, no permissions, copied as a 512px square JPEG to `filesDir/profile.jpg` by `ProfilePhotoStore`; name and a photo version — 0 = none, bumps to reload the avatar — live in `SettingsDataStore`), Appearance (theme, dynamic color, icon shape, app bar size, bottom bar labels, app bar scroll), Manage (Categories, Accounts), Data (Back up / Restore / Wipe data — see Backups), About (Version — read from the installed package, Developer, GitHub). "Collapse" scroll is hidden and read as Pinned when the app bar is Small.
- **Periods**: Transactions and Analysis each keep their own `Period` (`util/Period.kt`: start/end dates, inclusive; a whole month by default, or a single day, or any range). `FiscusPeriodSwitcher` shows ‹ label ▾ ›: arrows step by the period's own length (month → month, day → day, 15 days → 15 days) and never past today; tapping the label opens `DockedDateRangePicker` anchored under it (tap a date twice for a single day; future dates disabled; "This month" resets). Use `period.toDateRange()` for queries and `period.label()` for text.
- **Transactions**: period by period (see Periods), Income/Spent/Net summary computed from the rows shown (so it follows filters, with a "N matching transactions" line), filters (type as a connected button group; Account and Category filter chips with menus; Clear), list grouped by day. Deleting happens only from the details page (swipe-to-delete was removed: its red background showed through the container transform). Uses the app's normal title bar (so the app bar size/scroll settings apply) with a **search icon** that opens the Search page (`search` route): its own search field as the header, focused on arrival, results across all months (title or note, `LIKE` with escaped wildcards) that container-transform into details. The app title bar is hidden only on the Search page. (A search icon can't open `ExpandedFullScreenContainedSearchBar`: it grows out of an on-screen search field's position, which only `SearchBar`/`AppBarWithSearch` record.) Filtering happens in `TransactionsViewModel` over the period's rows.
- **Analysis**: same period switcher; 2×2 stat tiles (Spent + income, change vs the same-length previous period in words — "vs last month" for whole months — daily average over the period's days up to today, and savings rate); "By category" ranked by amount, each row a `LinearWavyProgressIndicator` meter of the category's share of the total (full track = 100%), with an Expense/Income toggle (top 6 + "Other"); a trend that adapts to the period (`trendScale` in `AnalysisMath.kt`): hidden for a single day, the 6-month context with the month emphasised for a whole month, daily columns for ranges up to 31 days, monthly columns (edge months counting only in-range days) for longer ranges; tapping a column opens that day or month. For a single day the daily-average tile becomes a transaction count; "Biggest expenses" (top 5, rows open details). Maths is pure and unit-tested in `AnalysisMath.kt`; monthly grouping uses SQLite `strftime` in UTC, matching the UTC-midnight date storage.

## Backups (`data/backup/BackupManager.kt`)

- A **`.fiscus` file is a zip**: `manifest.json` (`app: "fiscus"`, `format`, `createdAt`, `parts`, `databaseVersion`, appearance `settings`, `profileName`), plus `fiscus.db` and `profile.jpg` when included. Parts (`BackupPart`): MONEY (whole DB: transactions, items, accounts, categories), SETTINGS (appearance), PROFILE (name + photo). The user ticks parts when backing up, and again when restoring (only those the file has).
- Files go through the system picker (`CreateDocument` / `OpenDocument`) — no storage permission, still no internet.
- Backup checkpoints the WAL (`PRAGMA wal_checkpoint(FULL)`) before copying the DB file.
- Restore reads the whole zip first (a broken file changes nothing), then closes Room (`FiscusDatabaseProvider.close()`), deletes `fiscus.db` + `-wal`/`-shm`/`-journal`, writes the backup's DB and **restarts the app** (`util/restartApp`) so nothing keeps the old instance. Older-version backups upgrade through the migrations on open; a backup with a newer `databaseVersion` is refused.
- Feedback (per M3 dialog/snackbar guidance): a backup shows a `LoadingIndicator` on its row and a "Backup saved" snackbar — nothing blocks; restore/wipe show a non-dismissable busy dialog until the restart; any failure (not a backup, newer version, I/O) is a dialog, never a snackbar.
- **Wipe data** = fresh install: deletes the DB files, clears `SettingsDataStore` and the profile photo, restarts; the seeder recreates the defaults.
- **When the schema changes**, backups need nothing extra — but every migration must exist, since restores of old backups rely on them. If settings keys are added, add them to `toJson`/`toSettings` (unknown values fall back to defaults).

## Known bugs and workarounds

- **Stale keyboard padding after a predictive-back swipe** (fixed with a workaround): Compose foundation-layout 1.12.0-beta01 `InsetsListener.onEnd` only applies final insets when `durationMillis > 0`; the predictive-back keyboard hide is user-driven, so a blank band stayed in the sheet. `ImeInsetsRepair()` re-dispatches insets when `ime` disagrees with `imeAnimationTarget`. It must run once **per window** (in `FiscusApp` and inside the add-transaction sheet, which is its own window; any future dialog-based search or sheet with text fields needs it too). Remove when Compose fixes it — re-test by swiping back with the keyboard open in the sheet.
- **`ModalBottomSheet` handles IME and nav-bar insets itself** (and consumes them); don't add `imePadding()` / `navigationBarsPadding()` inside sheet content.
- **`java.text` can't do Indian digit grouping** (₹12,34,567.89); `formatMoney` uses `android.icu.text.NumberFormat`, so its tests are instrumented, not JVM.
- **`DatePicker` has a 360dp minimum width**; don't pad around it (`FiscusSize.datePickerMaxWidth` matches).
- **Keyed "Add" item in a lazy chip row** anchored the scroll to the end while data loaded — keep that item unkeyed.
- A full-text `MediumTopAppBar`/`LargeTopAppBar` title style override clipped the collapsed title — let the flexible bars choose title styles.
- Possible issue (unconfirmed): after a force-stop + relaunch the add-transaction sheet was seen already open. May have been the user tapping; check `showAddTransactionSheet` restoration if it recurs.

## Releasing

Every build handed to testers or users goes through these steps:

1. **Version** in `app/build.gradle.kts`: bump `versionCode` by 1 (Android refuses an update with an equal or lower code) and set `versionName` by SemVer (`0.1.0-beta.1` → `-beta.2` …, `0.1.0` when stable). Which pre-release label to use (see *Release stages* below): **Fiscus currently ships only betas** — keep using `-beta.N` until the user decides to go stable.
2. **Changelog**: add a section to `CHANGELOG.md` (Keep a Changelog, written for users, not developers) with *Added / Changed / Fixed* and a *Known limitations* list, plus the compare link at the bottom.
3. **Schema check**: if any entity changed since the last release, the database version must be bumped with a migration and a migration test (see Data rules). Never ship an in-place schema change.
4. **Tests**: `./gradlew :app:testDebugUnitTest`, then `./gradlew :app:connectedDebugAndroidTest` on the phone — ask the user first, it uninstalls the app.
5. **Build** a signed release: `./gradlew :app:bundleRelease` (`.aab` for Play testing tracks) or `./gradlew :app:assembleRelease` (APK for a GitHub release). Install the signed release build on the phone and smoke-test it — R8/release-only problems don't show up in debug.
6. **Tag and publish**: `git tag -a vX.Y.Z[-beta.N] -m "Fiscus X.Y.Z[-beta.N]"` and push the tag; attach the APK and the changelog section to the GitHub release (or upload the `.aab` and a ≤500-character "What's new").

### Release stages

| Stage | Version | When to use it | Expect |
|---|---|---|---|
| **Alpha** | `X.Y.Z-alpha.N` | Features still being built or changing shape; only for you and a few close testers. | Things may break, change or be removed; data loss is possible. |
| **Beta** | `X.Y.Z-beta.N` | Feature set for the version is mostly in place; real people use it day to day and report bugs. | Mostly works; rough edges and known limitations. Data must survive updates (migrations, no wipes). |
| **Release (stable)** | `X.Y.Z` | Tested, no known serious bugs, data format settled; for everyone. | Reliable; only bug-fix (`X.Y.Z+1`) or feature (`X.Y+1.0`) updates follow. |

- **Fiscus uses beta only.** It went straight to `0.1.0-beta.1` (no alpha), and every build handed out is the next `-beta.N`. Don't introduce alpha builds or a stable `0.1.0` unless the user asks.
- SemVer ordering: `alpha` < `beta` < stable, so `0.1.0-beta.3` < `0.1.0`. Going stable means dropping the suffix (`0.1.0`), not bumping the number.
- Before `1.0.0`, the version means "still early": `0.x` releases may still change things in breaking ways.
- `versionCode` keeps going up by 1 across every stage, whatever the label.
- Debug builds add `-debug` automatically (`versionNameSuffix`); never put that in a release.

Release facts to keep straight:

- **Signing keystore** (`fiscus-release.jks`) and its passwords live outside git, read from a git-ignored `keystore.properties`. Losing it means no more updates to existing installs — keep it backed up. Never commit it or print its passwords.
- **Play Data safety**: no data collected or shared, no internet access.
- **App icon**: being replaced by the user (the previous vector icon was removed). The manifest expects `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`; the 512×512 Play listing icon goes in `app/src/main/ic_launcher-playstore.png`.
- **Splash screen** (`Theme.Fiscus.Starting`, parent `Theme.SplashScreen`, no icon background circle): the launcher icon's foreground (`@mipmap/ic_launcher_foreground`) directly on a page background from `splash_colors.xml` — the wallpaper palette's surface colour on Android 12+ (`system_neutral1_10` light, `system_neutral1_900` dark), cream/dark brand fallback before. Changing the launcher icon updates the splash automatically.
- **Release setup still missing** (do before the first public build): explicit backup rules. Release signing is wired: `app/build.gradle.kts` reads `keystore.properties` (project root, git-ignored along with `*.jks`/`*.keystore`; keys `storeFile`, `storePassword`, `keyAlias`, `keyPassword`) and signs `assembleRelease`/`bundleRelease` only when `storeFile` points at an existing file — otherwise the output is `app-release-unsigned.apk`. R8 (`isMinifyEnabled` + `isShrinkResources`) is on: the release APK is ~2 MB (14 MB without it, mostly unused `material-icons-extended`). After adding a library or reflection-based code, test the signed release on the phone for R8 crashes and add keep rules to `proguard-rules.pro` if needed.

## Open items

- Month on Home is fixed when it opens (`shortcut:` in `HomeViewModel`).
- Draft isn't kept across process death.
- Clean-up: move strings to `strings.xml`.

## Working with the user

- **Releases** follow the *Releasing* section above.
- Commit messages: Conventional Commits, `type(scope): summary` (scopes used: theme, settings, transaction, home, manage, navigation, ui, state, db). The user commits themselves; suggest a message when work is done.
- Prefers being asked before larger design decisions; explains choices in Material 3 Expressive terms.
