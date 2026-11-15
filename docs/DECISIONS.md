# Decision Log

Format: `## ADR-NNN: Title`, then Date, Status, Context, Decision, Consequences. Newest at the bottom.

## ADR-001: Jetpack Compose with Material 3 Expressive
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: A modern, expressive Android UI is a core goal.
- **Decision**: Compose-only UI using the material3 expressive APIs.
- **Consequences**: Some APIs are experimental and need opt-ins. material3 is pinned separately from the BOM (ADR-006).

## ADR-002: Single module to start
- **Date**: 2026-11-12 · **Status**: Accepted (2026-11-13)
- **Context**: Early-stage solo project.
- **Decision**: Keep `:app` only, with package-level layering. Split later if needed.
- **Consequences**: Faster iteration; layer boundaries are kept by convention, not by the compiler. The feature-first scalability rules in ARCHITECTURE.md keep a future split cheap.

## ADR-003: Money as Long minor units
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: Floating-point causes rounding errors in currency math.
- **Decision**: Store and compute amounts as `Long` minor units with a currency code.
- **Consequences**: Formatting must account for each currency's fraction digits.

## ADR-004: Strict M3 Expressive-only UI
- **Date**: 2026-11-12 · **Status**: Accepted (user guideline)
- **Context**: The user requires a consistent M3 Expressive look with no custom or third-party components.
- **Decision**: Use only `androidx.compose.material3` components. No third-party or custom look-alike components. Any gap is escalated to the user.
- **Consequences**: Charts and other gaps need an explicit decision.

## ADR-005: Semantic versioning
- **Date**: 2026-11-12 · **Status**: Accepted (user guideline)
- **Context**: Releases need predictable version numbers, and Play requires an increasing versionCode.
- **Decision**: SemVer for versionName; versionCode = MAJOR*10000 + MINOR*100 + PATCH. See VERSIONING.md.

## ADR-006: material3 version for Expressive APIs
- **Date**: 2026-11-12 · **Status**: Accepted (user decision)
- **Context**: Stable 1.4.0 lacks or gates many Expressive APIs. The user wants every Expressive component and accepts experimental, bleeding-edge versions.
- **Decision**: Use the **newest available `androidx.compose.material3` release (alpha, beta, or RC)**, pinned explicitly in the version catalog and overriding the BOM. Opting into experimental APIs (`ExperimentalMaterial3ExpressiveApi`, `ExperimentalMaterial3Api`) is allowed.
- **Consequences**: APIs may be renamed or changed between versions. Check the release notes on each bump and record the changes in REFERENCES.md. Put opt-ins at file or function level, never module-wide.

## ADR-007: Architecture follows Google's official guide
- **Date**: 2026-11-13 · **Status**: Accepted (user guideline)
- **Context**: The user requires a professional, scalable, standard architecture.
- **Decision**: Use Google's layering (UI → optional domain → data), UDF, StateFlow UI state, Hilt, Room, Navigation 3, and offline-first. Code is organized by feature. Team conventions on top of this are listed in ARCHITECTURE.md. Verified against developer.android.com on 2026-11-13.
- **Consequences**: Feature work must follow ARCHITECTURE.md. Deviations need a new ADR.

## ADR-008: compileSdk 37
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: material3 1.5.x and core-ktx 1.19.0 require minCompileSdk 37 (AAR metadata). AGP 9.2.1 is tested up to 37.0, not 37.1.
- **Decision**: `compileSdk = release(37)`. **Update 2026-11-13:** targetSdk raised to 37 after reviewing the Android 17 behavior changes (https://developer.android.com/about/versions/17/behavior-changes-17). None affect the app today; watch "local network permission" (if a sync feature is added) and "OTP protection for SMS" (if SMS parsing is added).
- **Consequences**: SDK Platform 37 must be installed.

## ADR-009: Build variants, output naming, and Gradle caching
- **Date**: 2026-11-13 · **Status**: Accepted
- **Decision**: Debug gets the `.debug` app ID suffix and the `-debug` version suffix; release has none. `base.archivesName = "fiscus-v<versionName>"` gives files named `fiscus-vX.Y.Z-<buildType>.apk`. Gradle build cache, configuration cache, and parallel builds are on.
- **Consequences**: Debug and release install side by side. Unsigned release builds get an `-unsigned` suffix until signing is configured. Build logic must stay compatible with the configuration cache.

## ADR-010: Hero moments and icon style
- **Date**: 2026-11-13 · **Status**: Accepted (user: "go with M3 Expressive")
- **Context**: M3 Expressive says to "stick to one or two hero moments", chosen by asking whether an interaction is emotionally impactful and whether it is a key interaction. M3 also says shapes and type should be "in harmony", and M3 shapes echo the roundness of Google Sans Flex.
- **Decision**:
  1. Hero moments: (a) the **balance overview on Home**, the key information users come for; (b) the **add-transaction flow** (FAB menu, form, satisfying save), the most frequent and important interaction. Everything else uses standard M3 patterns.
  2. Icons: **Material Symbols Rounded**, app-wide, to match the soft, rounded M3 Expressive shapes and type. (The style choice is our reading of the shape/type harmony principle. M3 itself allows outlined, rounded, or sharp.)
- **Consequences**: New hero treatments elsewhere need a new ADR. All icons use the Rounded style; how they are included in Compose is decided when the first icons are added (check REFERENCES first).

## ADR-011: Core dependencies and toolchain upgrade
- **Date**: 2026-11-13 · **Status**: Accepted
- **Context**: M0 needs DI, persistence, navigation, and settings. The template's Kotlin 2.2.10 was too old for current libraries (kotlinx-serialization 1.13 needs Kotlin 2.4; Hilt 2.60 is built with Kotlin 2.3). AGP 9.2.1 only supports compileSdk up to 37.0.
- **Decision**: Use the latest **stable** release of each library (material3 stays on its pre-release per ADR-006). Upgrade the toolchain to AGP 9.5.1, Gradle 9.9.1 (wrapper checksum verified against gradle.org), Kotlin 2.4.21, and KSP 2.3.13. Use **Room 3** (`androidx.room3`, stable 3.0.3) instead of Room 2.x, since it is Google's current major version, with `sqlite-bundled`. Annotation processing uses KSP only (no kapt).
- **Consequences**: Room 3 uses the `androidx.room3` package and plugin, so tutorials written for Room 2 need adapting. Toolchain upgrades are checked with a full build plus tests.

## ADR-012: Theme: brand colors, generation, dynamic color
- **Date**: 2026-11-13 · **Status**: Accepted (user chose the key colors and dynamic-by-default)
- **Context**: Material3 has no seed-to-scheme API at runtime, and M3 requires role-based colors that work in light, dark, and three contrast levels.
- **Decision**: Brand key colors are primary **#DEDACD** and secondary **#228B22**. Tertiary, neutral, and neutral-variant come from the TonalSpot variant of the primary. Schemes are generated with Google's `@material/material-color-utilities` 0.4.0 (the Material Theme Builder engine, color spec 2021) into `ui/theme/Color.kt`: six schemes (light/dark × standard/medium/high contrast). Dynamic color is the default on Android 12+, with a "use brand colors" toggle in Settings (M5). Type and shape stay at M3 defaults.
- **Consequences**: #DEDACD has very low chroma, so `primary` renders as a warm grey (#605F54 light, #CAC6BA dark) and the green secondary carries most of the color. If more color is wanted on primary actions, switch to the fidelity variant or pick a more saturated primary key, then regenerate. Never hand-edit Color.kt.

## ADR-013: Expressive-first, no deprecated APIs
- **Date**: 2026-11-13 · **Status**: Accepted (user guideline)
- **Context**: material3 ships both older components and their Expressive successors, so "prefer" left room for inconsistency.
- **Decision**: When an Expressive component or overload exists, using it is mandatory, and the older equivalent is forbidden. Deprecated APIs are never used. The old → new mapping lives in DESIGN_SYSTEM.md and is extended whenever a new pair is found.
- **Consequences**: Deprecation warnings must be fixed immediately. A non-Expressive choice (for example flat progress in a dense data view) needs its own ADR.

## ADR-014: Use the device's font
- **Date**: 2026-11-13 · **Status**: Accepted (user decision)
- **Decision**: No custom or bundled typeface. Material's default `Typography()` uses `FontFamily.SansSerif` for both its brand and plain typefaces (verified in material3 1.5.0-rc01 `TypefaceTokens`), which is the device's system font, so it is kept as-is.
- **Consequences**: Never set `fontFamily` on text. The app's text follows the user's device font (including OEM font styles).

## ADR-015: App shell and navigation structure
- **Date**: 2026-11-13 · **Status**: Accepted (user chose the tabs and the full placeholder pattern)
- **Decision**: 4 tabs (Home, Transactions, Budgets, Insights) in `NavigationSuiteScaffold` (Expressive ShortNavigationBar/WideNavigationRail via `navigationSuiteType`). Settings is pushed from a top-bar icon, and the navigation suite is hidden there. Navigation 3 back stack is Home-rooted (rules in `FiscusNavigator`). Every destination, even a placeholder, uses the full Route + Screen + ViewModel + UiState pattern. Icons are the official Material Symbols Rounded vector drawables (no icon library; `material-icons-extended` is old Material Icons, frozen at 1.7.8).
- **Consequences**: New destinations need a NavKey, an `entryProvider` entry, and (if a tab) a `TopLevelDestination` entry. New icons are downloaded from the official repo, not drawn.

## ADR-016: Data model v1: currency, seed data, categories
- **Date**: 2026-11-13 · **Status**: Accepted (user decisions)
- **Decision**: Each account has one ISO-4217 currency (default: the device locale, falling back to INR), with no conversion yet. Transactions store their currency too, so multi-currency can come later without a migration of meaning. First launch seeds a **Cash** account and a **starter category set** (9 expense, 4 income), with names from `strings.xml`, as editable rows. Categories store `iconKey` (a Material Symbols name). There's no category color yet (it would be a role mapping; ADR later). Money formatting uses `android.icu.text.NumberFormat`, because JVM `java.text` doesn't do Indian lakh grouping.
- **Consequences**: Totals across accounts with different currencies need conversion rules before they're shown. Formatting tests run on a device.

## ADR-017: Database migration policy
- **Date**: 2026-11-13 · **Status**: Accepted
- **Decision**: Room schemas are exported to `app/schemas/` and committed. Each version bump ships an `@AutoMigration` (with a spec when needed) or a manual `Migration`, plus a migration test. **Never** use `fallbackToDestructiveMigration` on user data. Deleting an account that has transactions is blocked (FK RESTRICT); archiving is the user-facing path.
- **Consequences**: A schema change is never "just bump the version". The migration and its test are part of the same change.

## ADR-018: Screens with arguments, transaction add/edit/delete flow
- **Date**: 2026-11-15 · **Status**: Accepted
- **Decision**:
  1. Screens that need arguments get them from their Navigation 3 key (a `@Serializable data class XxxNavKey`). The ViewModel receives the key by **Hilt assisted injection** (`@HiltViewModel(assistedFactory = …)`, `@AssistedInject`, `hiltViewModel<VM, VM.Factory>(creationCallback = { it.create(key) })`), not `SavedStateHandle`.
  2. Add/edit is a **full-screen pushed destination** (`TransactionEditorNavKey`), opened from a `FloatingActionButtonMenu` (Expense / Income / Transfer) on Home and Transactions only. Edit and delete live on a read-only **detail** screen (`TransactionDetailNavKey`) opened by tapping a transaction; delete needs an `AlertDialog` confirmation.
  3. One-shot outcomes ("saved", "deleted") are UI state (`isFinished`), not event channels, per Google's UI-events guidance.
  3b. The transaction-type selector stays the **connected** button group (a `Row` of `ToggleButton`s with connected shapes). A standard `ButtonGroup` with `toggleableItem`s (press-to-widen motion) was tried on 2026-11-15 and **rejected by the user**; don't switch to it again without asking.
  4. Single choices with an open-ended number of options (accounts, categories) use Expressive `ToggleButton`s (icon + label) in a wrapping `FlowRow`, with radio-button semantics. *(Changed 2026-11-15 from `FilterChip`s in a scrolling `LazyRow`, user decision: chips aren't in the M3 Expressive component set and looked like plain M3 in the hero flow.)* The date uses a docked `DatePicker` below the field.
  5. Time comes from injected `kotlin.time.Clock` and `kotlinx.datetime.TimeZone` (`di/TimeModule.kt`), never `Clock.System` in logic.
  6. Amount input is parsed with `BigDecimal` into minor units (`core/money/MoneyParser.kt`); transfers only go between accounts with the same currency.
- **Consequences**: Every future screen with arguments copies this pattern. The transaction list (next task) wires item taps to `TransactionDetailNavKey`. The date field, option group and currency transformation live in `ui/components/` for reuse (DESIGN_SYSTEM.md "Shared components", 2026-11-15).

## ADR-019: Motion, transitions, confirmations and haptics for the transaction flow
- **Date**: 2026-11-15 · **Status**: Accepted (user approved items 1–6; shared-element FAB transition and custom shape-morph reveal **not** approved/used)
- **Context**: The first build of ADR-018 had the right components but none of the M3 Expressive behaviour (no motion, no hero, no "satisfying save"). M3_EXPRESSIVE.md §9 was not applied.
- **Decision**:
  1. **Hero (editor)** *(revised 2026-11-15, user chose "centered amount" + "inline error")*: a `Card` on the `extraLarge` shape step whose container role follows the type (expense → tertiary, income → secondary, transfer → surfaceContainerHighest + primary accent), colors blended on an **effects** spring. Inside, centered: a header with the type icon in a `MaterialShapes.Cookie9Sided` accent (the matching `tertiary`/`secondary`/`primary` + on-role pair) and the type label; the amount in `displayLargeEmphasized.tabularNumbers` (a `TextField` with no label, transparent indicator and **no placeholder**, the currency symbol added by a display-only `VisualTransformation`); one line below showing, in priority order, the error (error role), `"₹1,250.50 · from Cash"`, or "Enter an amount" while empty, in the card's own text colour (polite live region). *(2026-11-15: the `"₹0.00"` placeholder was removed, user decision: it looked like a typed value, and M3's placeholder role `onSurfaceVariant` fails contrast on these containers at medium/high contrast.)*
  2. **In-screen motion**: section swaps via `AnimatedContent` (fade on effects specs, `SizeTransform` on a spatial spec), `animateContentSize` on the form, docked picker expands on a spatial spec. All specs come from `MaterialTheme.motionScheme`; no `tween`.
  3. **Save**: medium-size Expressive `Button` in the bottom bar (thumb reach); content swaps to `LoadingIndicator` while saving. The keyboard covers it while typing; the form uses `imePadding` so every field stays reachable.
  4. **Confirmations**: one app-level `SnackbarHostState` in `FiscusApp`, provided to screens through `LocalSnackbarHostState` *(superseded the original parameter passing, ADR-020)*. ViewModels expose `@StringRes resultMessage` (state, not events); the Route resolves it and calls `onFinished(message)`, and `FiscusApp` pops and shows the snackbar.
  5. **Haptics**: `LocalHapticFeedback` with action constants only (`Confirm` on save/delete, `Reject` on an invalid amount); nothing else ("less is more", Android haptics principles).
  6. **Screen transitions**: Navigation 3's **default** `NavDisplay` transitions (including predictive back). No app-written transition specs. *(Amended 2026-11-15, see below.)*
  7. **Accent**: the shared `ShapeAccentIcon` (an icon in a `MaterialShapes.Cookie9Sided` `Surface`, `ui/components/`) is the one decorative shape accent: the category icon on the detail screen and the type icon in the editor hero (M3_EXPRESSIVE.md §3).
- **Consequences**: Every screen runs the M3_EXPRESSIVE.md §9 checklist before it is built.
- **Amendment (2026-11-15, user decision)**: A first version used a hand-written shared-axis transition on `motionScheme` springs, and a second attempt mirrored material3's `internal` easing/duration tokens into an app `Motion.kt`. Both were dropped: M3 says transitions use the legacy easing/duration system, which material3 does not expose publicly, so any implementation would be our own hardcoded specs (against DESIGN_SYSTEM.md "no ad-hoc animation specs" and user guideline 5). Decision: use Navigation 3's built-in defaults until Google exposes M3 transition APIs/tokens. Revisit on each material3 / navigation3 bump. The M3 container transform (FAB → editor) also stays out until the user approves it.
- **Also**: the Save button's content swap is a cross-fade only (M3 enter/exit patterns avoid scale).

## ADR-020: Shared layout dimensions, app-wide snackbar host, type display order
- **Date**: 2026-11-15 · **Status**: Accepted (user: "create one shared layout"; snackbars: "do what's better")
- **Context**: A code review found `ContentPadding = 16.dp` repeated in 8 screen files (plus other sizes), a `snackbarHostState` parameter threaded through every screen that shows confirmations, and the transaction types shown in enum order (Income first) although Expense is the default and most common.
- **Decision**:
  1. `ui/theme/Layout.kt` → `FiscusLayout` holds the few app-wide layout dimensions (`ScreenMargin` 16dp, `SectionSpacing` 24dp, `ItemSpacing` 8dp, `TightSpacing` 4dp, `MaxContentWidth` 600dp, `AccentSize` 40dp), following M3 layout guidance (16dp compact margin, 8dp grid). M3 has no public spacing tokens in Compose. Only layout spacing goes there; colours, shapes, type and motion always come from `MaterialTheme` and components.
  2. One `SnackbarHostState`, provided by `FiscusApp` through `LocalSnackbarHostState` (`ui/components/`). Screens use `snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) }`. Why: no parameter threading, every new screen gets it for free, and `FiscusApp` stays the single place that shows confirmations after navigation (state, not events: ViewModels expose `resultMessage`).
  3. `TransactionTypeDisplayOrder` (Expense, Income, Transfer) is the only order the UI uses; never the enum's declaration order.
- **Consequences**: New screens use `FiscusLayout` and `LocalSnackbarHostState`; neither is passed as a parameter or redefined per file.

## ADR-021: Required transaction title (schema v2)
- **Date**: 2026-11-15 · **Status**: Accepted (user: "make title required"; the name "Title" chosen by default since no alternative was picked)
- **Context**: Transactions had only an optional note, so a list (next task) could show nothing but the category and amount.
- **Decision**: `Transaction.title: String`, required and never blank (enforced in the model, trimmed on save). Schema v2 adds `transactions.title TEXT NOT NULL DEFAULT ''` through a **manual** migration (`Migration1To2`) that fills existing rows from the category's name, else the note, else the localized type label, since the model rejects blank titles and ADR-017 forbids destructive migrations. The editor has a single-line Title field right after the hero (Save hint "Add a title"); the detail screen shows the title in its large top bar.
- **Consequences**: Lists and search use the title as the primary line. Every future schema change follows the same pattern: a migration plus a `MigrationTest` case.

## ADR-022: Transaction list, search and filters
- **Date**: 2026-11-15 · **Status**: Accepted (user chose the search placement, the filter set, the segmented day groups, and deferring list-detail)
- **Context**: M1's last item. ADR-018 wires list taps to the detail screen, and ADR-021 makes the title the primary line. The list isn't a hero moment (ADR-010), so it stays calm, standard M3.
- **Decision**:
  1. **Grouping**: transactions are grouped by **local day** (injected `TimeZone`), newest first. Each day has a header (Today / Yesterday / full localized date, plus the day's net) above one joined group of Expressive `SegmentedListItem`s (`ListItemDefaults.segmentedShapes(index, count)`, `SegmentedGap`). The net is income minus expense; transfers don't count; it's hidden when the day mixes currencies (no conversion yet, ADR-016).
  2. **Search**: the Transactions tab keeps `LargeFlexibleTopAppBar` (the feature pattern is unchanged). The first list item is a collapsed `SearchBar(state, inputField)` that expands into `ExpandedFullScreenSearchBar`, sharing one `SearchBarState`. *(A plain search icon was the first choice, but `ExpandedFullScreenSearchBar` animates out of the collapsed bar's position (`collapsedCoords`), so the user picked the collapsed bar instead.)* The query matches the title or note and is combined with the chip filters. Closing search clears it.
  3. **Filters** *(type chips amended by ADR-023)*: `FilterChip`s (with `shapes`) in a scrolling row. The type chips (in `TransactionTypeDisplayOrder`) toggle in place. Account and Category open a `ModalBottomSheet` of checkable `SegmentedListItem`s (multi-select), and Date opens `DatePickerDialog` + `DateRangePicker` (whole local days, inclusive). Filtering by an account includes transfers into it. "Clear filters" resets the chips.
  4. **Filtering happens in SQL**: one `TransactionDao.observeFiltered` query, reached through `TransactionRepository.observeTransactions(TransactionFilter)`. `TransactionFilter` lives in `model/`. The search is a `LIKE` with `%`, `_` and `\` escaped (`containsLikePattern`). No schema change; the existing indexes apply.
  5. **List-detail** (two panes on medium/expanded widths) is **deferred** to its own roadmap item. It needs the material3-adaptive Navigation 3 scene strategy, a new dependency. Until then the list is a single pane capped at `FiscusLayout.MaxContentWidth`.
- **Consequences**: `TransactionListItem`, `signedAmountText`/`signedNetText` and `dayLabel` are shared components, for Home's recent list (M4). The detail screen now uses `signedAmountText`. SQLite's `LIKE` only folds case for ASCII, so non-ASCII search is case-sensitive for now.

## ADR-023: More expressive transaction list
- **Date**: 2026-11-15 · **Status**: Accepted (user: "go ahead with all recommendations except date one, leave date range picker as it is")
- **Context**: The user found the first list (ADR-022) too plain. ADR-010 still limits the app to two hero moments, and M3 says finance data stays calm, so the list gets expressiveness from **accents, containment and emphasis**, not from a hero treatment.
- **Decision**:
  1. **Type filter in a sheet**: the Type chip opens a `ModalBottomSheet` like Account and Category, so all four chips are picker chips. Every filter sheet ends with a `ButtonGroup` of **Clear** (that sheet's filter only, `FilterCleared(picker)`) and **Done**. Toggles apply right away.
  2. **Type accents on items**: each item's icon (category, or type for transfers) sits in a `ShapeAccentIcon` with the type's accent role pair from `TransactionType.accentRoles()` (expense tertiary, income secondary, transfer primary). The editor hero uses the same mapping, so the two match.
  3. **Summary card** above the days: the net in `headlineMediumEmphasized`, plus income and expense with their accents, for whatever the list shows (labelled with the date range, or "All time"). It's a `Card` on `surfaceContainer` with default elevation. It's hidden when the list is empty or mixes currencies, and the search results don't show it.
  4. **"Today" header** in `titleMediumEmphasized` on `onSurface`; the other days stay `titleSmall` on `onSurfaceVariant`.
  5. **Empty states** get a large `ShapeAccentIcon` (`FiscusLayout.LargeAccentSize` 96dp, icon 48dp).
  6. The date filter keeps the **modal `DateRangePicker`** (user decision; M3's docked picker is single-date only).
  7. The filters stay `FilterChip`s; swapping them for `ToggleButton`s was offered and not chosen.
- **Consequences**: `ShapeAccentIcon` takes `size`/`iconSize`; `accentRoles()` is the only type → accent mapping. This isn't a third hero moment: there's no motion beyond the item springs, and the numbers stay tabular.

## ADR-024: More expressive detail screen and search
- **Date**: 2026-11-15 · **Status**: Accepted (user: "okay go ahead" on the proposal A–D, S1–S3). *Items 1, 2 and 4 superseded by ADR-025* (the user rejected the floating toolbar and the detail rows after trying them).
- **Context**: Following ADR-023, the user asked for the same treatment on the transaction detail screen and on search. The restraint rules still apply (ADR-010, M3_EXPRESSIVE §1).
- **Decision**:
  1. **Detail amount hero**: the editor's type-coloured card (`TransactionType.heroRoles()` container pair, `extraLarge` shape, type in a `ShapeAccentIcon` with `accentRoles()`), with the signed amount centered in `displayLargeEmphasized`. This is the viewing side of hero moment 2, not a new hero. The editor and detail share the colour mappings; the layouts stay separate (the editor's hero is an input).
  2. **Details as one segmented group** (`SegmentedListItem`s with `segmentedShapes`), like the list. Only the category keeps the shape accent.
  3. **Top bar subtitle**: `LargeFlexibleTopAppBar(subtitle = …)` shows "Today · 7:30 pm" (`dayLabel` + localized short time; `today` comes from the injected `Clock`).
  4. **Actions in a `HorizontalFloatingToolbar`** (vibrant colors), centered at the bottom through the `Scaffold` FAB slot. It holds a Delete icon button (with a content description) and an Edit `Button` with icon and label (actions keep text labels, M3_EXPRESSIVE §6). This replaces the `ButtonGroup` at the end of the content, and the content pads by `FloatingToolbarDefaults.ContainerSize` so nothing hides behind the toolbar.
  5. **Search**: with no query typed, the expanded search shows a large-accent hint ("Search by title or note.") instead of the whole list. While searching, a result count ("3 results", polite live region) sits above the results. Matched text in each title is set in the emphasized weight (`bodyLargeEmphasized.fontWeight`, weight only, no colour). No-result states use the same large accent.
- **Consequences**: `TransactionListItem` takes `highlight`; `heroRoles()` is the only type → hero container mapping. If the user rejects the toolbar, restore the `ButtonGroup` (git history) and amend this ADR.

## ADR-025: Contained list rows, amount pills, category-led detail
- **Date**: 2026-11-15 · **Status**: Accepted (user chose each option from previews: "Bold title + type pill amount", "Edit bottom bar, Delete in top bar", "Category-led hero + tiles")
- **Context**: The user found the list item "too plain / flat" (hierarchy, amount styling, background) and disliked the detail page's floating toolbar and rows (ADR-024). Cause of the flatness: `SegmentedListItem`'s default container is `surface` (`ListTokens.ItemSegmentedContainerColor`), the same colour as the screen, so joined groups had no visible containment.
- **Decision**:
  1. **Containment**: segmented items that sit on the screen background use `containedSegmentedColors()` (`ui/components/`), i.e. `ListItemDefaults.segmentedColors(containerColor = surfaceContainer)`, the next surface tier up. Applies to the transaction list and the detail rows.
  2. **List row hierarchy**: the title in `titleMedium`; the supporting text keeps the default (`bodyMedium`, `onSurfaceVariant`). The amount sits on a small tonal `Surface` (`shapes.small`) in the type's `heroRoles()` pair (expense tertiaryContainer, income secondaryContainer, transfer surfaceContainerHighest), with the text in `titleSmall.tabularNumbers`. That's a paired role, so contrast is guaranteed and no measurement was needed. The local time (`labelSmall`) sits below the amount. The accent icon stays.
  3. **Detail hero, led by the category**: a large `ShapeAccentIcon` with the category's icon (the type's for a transfer) in `accentRoles()`, the signed amount in `displayLargeEmphasized`, and "Expense · Food & dining" in `titleMedium`, on the `heroRoles()` card. TalkBack reads it as one node.
  4. **Detail rows**: account(s), date and note as one contained segmented group, with values in `titleMedium`. The category isn't repeated (it's in the hero).
  5. **Detail actions**: **Edit** is a medium Expressive `Button` in the bottom bar (same sizing helpers as the editor's Save, thumb reach). **Delete** is a top-bar icon button, out of thumb reach and still behind the `AlertDialog`. The floating toolbar is removed.
- **Consequences**: Other screens with segmented lists on the background (e.g. Home's recent list, M4) use `containedSegmentedColors()`. Lists inside sheets keep the default, since the sheet is already a raised surface.

## ADR-026: Direction arrows on list amounts, type/category detail rows, full-height filter sheets
- **Date**: 2026-11-15 · **Status**: Accepted (user requests; amends ADR-025 items 2 and 4)
- **Decision**:
  1. **List amounts**: the tonal pill is removed. The amount (`titleSmall.tabularNumbers`, unsigned) follows the type's arrow, the same `iconRes` used everywhere (expense ↓ money out, income ↑ money in, transfer ⇄), at `FiscusLayout.InlineIconSize` (18dp) and tinted by the content color (no coloured icons). TalkBack still reads "Expense, ₹450.00". (Day nets and the summary now use arrows too, ADR-032.)
  2. **Detail rows**: **Type** and **Category** join the contained segmented group (Type, Category, Account(s), Date, Note; transfers have no category). The hero keeps its category-led look.
  3. **Filter sheets open fully expanded**: `ModalBottomSheet(sheetState = rememberBottomSheetState(initialValue = Hidden, enabledValues = {Hidden, Expanded}))`. **Bug fixed**: starting half-height, a real swipe on the Category sheet first dragged the sheet open and then scrolled the list. After that, taps on the rows (the income categories, which sit below the fold) were ignored. Reproduced with injected swipes in `TransactionsFilterFlowTest` (0 of 2 taps registered). With no partial state it passes. `rememberModalBottomSheetState(skipPartiallyExpanded)` is deprecated in rc01, so it isn't used.
- **Consequences**: Any future sheet with a scrolling list uses the same expanded-only state.

## ADR-027: Amounts never wrap, danger in the error role, simpler rows and sheets
- **Date**: 2026-11-15 · **Status**: Accepted (user requests)
- **Decision**:
  1. **`AmountText`** (`ui/components/`): amounts on cards and heroes are one line. It's material3 `Text(autoSize = TextAutoSize.StepBased(maxFontSize = style.fontSize))`, so the type token stays the size and a long amount ("₹40,000.00") steps down instead of wrapping. Used for the detail hero amount and the summary card's net and totals.
  2. **Danger actions in the error role**: the top-bar Delete (`IconButtonDefaults.iconButtonColors(contentColor = error)`), the delete dialog's icon (`iconContentColor = error`) and its confirm button (`textButtonColors(contentColor = error)`). The error role is static in M3 (it doesn't follow dynamic color), adapts to light and dark, and is the role M3 defines for destructive actions.
  3. **List rows show no time**; `TransactionListEntry` no longer carries it.
  4. **Filter sheets have no title**: the chip that opened a sheet already names it.

## ADR-028: One action group for every page, list item grows into its page
- **Date**: 2026-11-15 · **Status**: Accepted (user: "i want a consistent button group for all pages action"; Delete "in the group: [Delete | Edit]"; "when we click on transaction item it grows into page… when we go back it should shrink back", with no custom motion). Amends ADR-019 item 6 and ADR-025 item 5.
- **Decision**:
  1. **`ActionButtonGroup`** (`ui/components/`) holds every page's actions: the editor's [Save], the detail page's [Delete | Edit] and the filter sheets' [Clear | Done]. It's one M3 Expressive `ButtonGroup` of medium-height buttons that share the width; the pressed one widens (built-in). Styles: `Primary` (filled), `Secondary` (outlined), `Danger` (filled, `errorContainer`/`onErrorContainer`). A loading action cross-fades to a `LoadingIndicator`. Delete leaves the top bar and is still confirmed by the dialog. Dialog buttons stay M3 dialog `TextButton`s.
  2. **Container transform with library defaults only**: `FiscusApp` wraps `NavDisplay` in Compose's `SharedTransitionLayout`. An entry decorator (`navVisibilityScopeDecorator()`) hands each destination its `LocalNavAnimatedContentScope`. `Modifier.sharedContainer(key)` applies `sharedBounds` with **every parameter at its default** (`SharedTransitionDefaults.BoundsTransform`, `fadeIn`/`fadeOut`, `scaleToBounds`). A transaction's list item and its detail page share `transactionContainerKey(id)`. The page grows out of the row on open and shrinks back into it on Back. No app-written specs; the `NavDisplay` cross-fade stays the default. The detail state carries `transactionId` from the nav key, so the key is there on the first frame.
  3. Search results don't share bounds: they live in the search dialog's own window, outside the layout.
- **Consequences**: Any future "item → its page" pair uses `sharedContainer` with one key helper. Previews and tests are unaffected (the modifier is a no-op outside `NavDisplay`). Revisit when material3 ships a container-transform API.

## ADR-029: Decimals only when needed; the list shows this month by default
- **Date**: 2026-11-15 · **Status**: Accepted (user: "dont show decimals until there is one… everywhere"; "only this months transaction should be visible")
- **Decision**:
  1. **Money display**: `displayFractionDigits(money)` is 0 for a whole amount and the currency's full digits otherwise. `formatMoney` ("₹200", "₹50.02") and the editor's `formatMoneyInput` ("200") both use it. Storage and maths are unchanged (Long minor units, ADR-003).
  2. **Default period = the current calendar month**: with no picked range, the Transactions list, its day groups and the summary card cover this month (`currentMonth(today)` from the injected clock and time zone). The Date chip reads "This month" and isn't "selected" (it's the default, not a filter). Picking a range replaces it, and Clear goes back to this month. An empty month says "No transactions this month yet."
  3. **Search looks through every month** unless a range was picked, because finding an old transaction is what search is for.
- **Consequences**: There's no "all time" view yet; it's reachable by picking a wide range. If one is wanted, add it as a preset later.

## ADR-030: All transitions on the M3 Expressive springs
- **Date**: 2026-11-15 · **Status**: Accepted (user: "move all the transitions from page to elements to the spring/expressive"). **Supersedes the ADR-019 amendment** (Navigation 3 defaults) and ADR-028's "library defaults" for the container transform.
- **Context**: Navigation 3's default is a 700 ms `tween` cross-fade, and Compose's `sharedBounds` default is a generic foundation spring. Neither comes from M3 Expressive. The user asked for Expressive motion everywhere.
- **Decision**:
  1. **Pages** (`FiscusApp`): `NavDisplay(transitionSpec, popTransitionSpec, predictivePopTransitionSpec)` all use `pageCrossFade(motionScheme)`: the same cross-fade pattern as before, with enter on `defaultEffectsSpec` and exit on `fastEffectsSpec` (exits faster than enters, M3 guidance). Tabs and pushed screens both use it.
  2. **Item → page** (`sharedContainer`): bounds on `slowSpatialSpec` (M3: slow for full-screen), content `fadeIn(defaultEffectsSpec)` / `fadeOut(fastEffectsSpec)`.
  3. **In-screen content swaps**: every `AnimatedContent` sets its `SizeTransform` on a spatial spec (fast for small swaps such as button content, default for screen-level swaps), so no size change falls back to Compose's default spring.
  4. Only theme tokens are used: no numbers, no `tween`, no copied internal tokens (LESSONS #3 still holds).
- **Consequences**: There's no M3 easing-and-duration API for transitions in material3 (M3 says transitions still use that legacy system); springs are the closest public equivalent. Revisit on each material3 / navigation3 bump.

## ADR-031: One shared empty state
- **Date**: 2026-11-15 · **Status**: Accepted (user: "i want shared empty state of all the pages")
- **Decision**: `EmptyState(icon, message, action?)` (`ui/components/`) is the only empty-state UI: a large decorative `ShapeAccentIcon` (`FiscusLayout.LargeAccentSize`), the message centered in `bodyLarge`/`onSurfaceVariant`, and an optional action below (e.g. "Clear filters"). Used on Transactions (empty month, no matches, search hint, no search results), Home, Budgets, Insights and Settings. Icons: Home and Transactions `receipt_long`, filtered/search `search`, Budgets `savings`, Insights `bar_chart`, Settings `settings`. The editor's inline "no accounts/categories" lines inside its option groups are form hints, not page empty states, so they stay as they are.

## ADR-032: Direction arrows on every amount, no −/+ on screen
- **Date**: 2026-11-16 · **Status**: Accepted (user: consistency, "in some places we can still see - and + instead of arrows"). **Amends ADR-026 item 1** (nets kept their signs) and ADR-025 item 3 (signed hero amount). Arrows themselves were flipped the same day: expense ↓ money out, income ↑ money in.
- **Decision**: The day nets show the unsigned amount after an arrow, like the list rows. The two heroes (the detail amount and the summary card's net) show the unsigned amount with no arrow and no sign (user: arrows not necessary there); on the detail hero the type label and accent icon carry the meaning. A net uses the arrow of its direction (`netIconRes`: negative ↓, positive ↑, zero none). `AmountText(icon)` puts the icon inline in the text at `1.em`, so it scales and steps down with the type token instead of using a fixed size; tinted by the content color, decorative. TalkBack still reads the signed text (`signedAmountText`/`signedNetText`), so the direction is spoken.
- **Consequences**: No −/+ is drawn anywhere; the signed strings remain only for accessibility. List rows keep their `DirectionalAmount` (18dp `InlineIconSize`).

## ADR-033: Home dashboard with what exists today
- **Date**: 2026-11-16 · **Status**: Accepted (user chose "Centered hero card" and the sections "Accounts" + "Recent transactions"; "Top spending this month" was offered and not chosen)
- **Context**: Home was a placeholder (`ElevatedCard` with a fake "₹0.00"). Accounts, balances and transactions already exist (M1), so Home can show real data before M2. It's hero moment 1 (ADR-010).
- **Decision**:
  1. **Balance hero**: a `Card` on the `extraLarge` shape step in the `primaryContainer`/`onPrimaryContainer` pair (the same card family as the editor and detail heroes; primary because the balance isn't tied to a transaction type). Centered: a large `ShapeAccentIcon` (`account_balance_wallet`, `primary`/`onPrimary`), the total of every **active** account in `displayLargeEmphasized` (`AmountText`), and "Across 2 accounts" in `titleMedium` (2026-11-16, user: the "Total balance" label was removed on screen; TalkBack still says it). With mixed currencies the total reads "Several currencies" (no conversion, ADR-016). TalkBack reads the total as one node.
  2. **This month inside the hero**: two `surface` tiles (`shapes.medium` = 28dp card − 16dp padding, optical roundness) holding the shared `PeriodTotal` (income, expense; arrows in the type accents), with "This month" below. Hidden when the month has no income or expense. The tiles are paired roles (surface/onSurface/onSurfaceVariant), so no extra contrast measurement was needed.
  3. **Accounts**: one contained segmented group (`SegmentedListItem` non-interactive overload, `containedSegmentedColors()`), account-type icon in the default `ShapeAccentIcon`, name in `titleMedium`, balance in `titleSmall` tabular. Not tappable until account screens exist (M2).
  4. **Recent transactions**: the newest 5 (`RecentTransactionCount`) with the shared `TransactionListItem`, growing into the detail page (`sharedContainer`, ADR-028). A `TextButton` "See all" in the section header opens the Transactions tab (a section link, not a page action, so not `ActionButtonGroup`). With none, the shared `EmptyState`.
  5. **Shared code moved out of Transactions** (features don't import each other): `PeriodSummary` + `summarize()` and `TransactionListEntry` + `toListEntries()` to `model/`, `currentMonth()` to `core/time/`, `SummaryTotal` → `ui/components/PeriodTotal`, `supportingText` → `ui/components/transactionSupportingText`, plus `AccountType.iconRes` (`payments`, `account_balance`, `credit_card`, `account_balance_wallet`).
- **Consequences**: Recent takes the first 5 of the full transaction stream; add a `LIMIT` query if that stream gets heavy. Medium/expanded layouts stay single-pane, capped at `MaxContentWidth`. Accounts become tappable in M2.

## ADR-034: More expressive Home (carousel, wavy spent bar, larger hero shape)
- **Date**: 2026-11-16 · **Status**: Accepted (user picked items 1, 2, 4 and 5 of the proposal, plus "change the colors of income and expense tile"; the period toggle was not chosen). Amends ADR-033 items 1–4.
- **Decision**:
  1. **Accounts carousel**: `HorizontalMultiBrowseCarousel` (`androidx.compose.material3.carousel`, stable in rc01) replaces the segmented account list. Each item is a `Surface` in `secondaryContainer`/`onSecondaryContainer` clipped with `maskClip(shapes.extraLarge)`, holding the account type in a `ShapeAccentIcon` (`secondary`/`onSecondary`), the name (`titleMedium`) and the balance (`headlineSmall`, `AmountText`). `preferredItemWidth = FiscusLayout.CarouselItemWidth` (186dp, from the official sample), `itemSpacing = ItemSpacing` (8dp, as in the sample). Masking and resizing on scroll are the carousel's own motion.
  2. **Spent bar**: under the month tiles, a determinate `LinearWavyProgressIndicator` (default colours) of expense ÷ income, with the meaning in a `labelLarge` line: "Spent 25% of this month's income", "Spent more than this month's income" (bar full) or "Spent ₹x this month, no income yet" (bar full). The line is the TalkBack text; the bar never carries meaning alone. Percent is Long maths, rounded down (`percentOf`).
  3. **Hero shape**: the Home hero moves up to `shapes.extraLargeIncreased` (32dp), one step above the transaction heroes; tiles use `shapes.large` (32 − 16dp padding, optical roundness).
  4. **Tile colours**: the income and expense tiles use each type's `heroRoles()` pair (income `secondaryContainer`, expense `tertiaryContainer`), matching the editor and detail heroes. `PeriodTotal` gained `labelColor`, and the tiles pass their own on-color, since `onSurfaceVariant` fails contrast on these containers (DESIGN_SYSTEM.md, LESSONS #13).
  5. **"See all"**: an Expressive `FilledTonalButton(shapes = ButtonDefaults.shapes())` (corners change on press) instead of a `TextButton`.
- **Consequences**: Account carousel items become tappable in M2. The "This month" caption is gone; the spent line names the month.

## ADR-035: Spent ring leads the Home hero
- **Date**: 2026-11-16 · **Status**: Accepted (user: "instead of icon on hero card … show a circular indicator and spent %"). Amends ADR-034 item 2 and ADR-033 item 1.
- **Decision**: The wallet `ShapeAccentIcon` and the `LinearWavyProgressIndicator` are removed. The hero opens with a determinate `CircularWavyProgressIndicator` (default colours and stroke) at `FiscusLayout.LargeAccentSize` (96dp, the accent's slot) with a short label centered in `titleMediumEmphasized.tabularNumbers`: "25%", "100%+" (over income, full ring), "—" (expense but no income, full ring), "0%" (nothing yet). The balance below stays the largest text. The spent line in words ("Spent 25% of this month's income") is the ring's TalkBack text; on screen it was removed (2026-11-16, user), so the ring's label carries the number.
- **Consequences**: When budgets exist (M3), revisit whether the ring should show the overall budget instead, so Home and Budgets don't show two similar rings with different meanings.
