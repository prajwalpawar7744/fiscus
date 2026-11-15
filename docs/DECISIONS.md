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
