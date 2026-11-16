# References

Verified knowledge from official sources. **Always add new findings here** with the source URL and the date checked. Mark anything not confirmed word-for-word in the source as ⚠️ *unverified*.

## Primary sources
- Material 3 / M3 Expressive: https://m3.material.io/ · components: https://m3.material.io/components
- Compose Material 3: https://developer.android.com/develop/ui/compose/designsystems/material3
- material3 release notes: https://developer.android.com/jetpack/androidx/releases/compose-material3
- Architecture guide: https://developer.android.com/topic/architecture
- Architecture recommendations: https://developer.android.com/topic/architecture/recommendations
- Kotlin style guide: https://developer.android.com/kotlin/style-guide
- App versioning: https://developer.android.com/studio/publish/versioning
- Architecture sub-pages: see "Architecture detail check" below (read 2026-11-13)
- To read next: [Compose architecture](https://developer.android.com/develop/ui/compose/architecture), [Navigation 3](https://developer.android.com/guide/navigation/navigation-3), [modularization](https://developer.android.com/topic/modularization)

> Note: m3.material.io is client-rendered, so plain fetching returns only the title. Render it with headless Chromium instead (see "Design language sources" below).

---

## Material 3 / Expressive (checked 2026-11-12)
- "M3 Expressive is an expansion of Material Design 3, including research-backed updates to theming, components, motion, typography, and more." (Compose M3 page)
- Library status as of Nov 4, 2026: **material3 stable 1.4.0**, **1.5.0-rc01** latest RC. (release notes)
- Wear OS uses the separate Wear Compose Material 3 library.
- Color: 5 key colors, each with a tonal palette of 13 tones. Use color roles (primary, primaryContainer, onPrimaryContainer, tertiaryContainer…). Dynamic color is available on API 31+ (`Build.VERSION_CODES.S`).
- Shape scale: extraSmall, small, medium, large, extraLarge, set through `Shapes` in `MaterialTheme`.
- Type scale: display, headline, title, body, label, each in large/medium/small, set through `Typography`.
- Elevation: tonal overlays and shadow.
- Opt-in annotation for experimental APIs: `@ExperimentalMaterial3Api`. There is also `@ExperimentalMaterial3ComponentOverrideApi` (since 1.4.0-alpha06).

### Expressive API history (from release notes)
Verified word-for-word:
- `MotionScheme` drives component motion (1.4.0-alpha01). Instances: `MotionScheme.standard()` and `MotionScheme.expressive()` (1.4.0-alpha02).
- `MediumFlexibleTopAppBar` / `LargeFlexibleTopAppBar` (1.4.0-alpha06).
- `FloatingToolbar`, renamed from FloatingAppBar (1.4.0-alpha06). `TwoRowsTopAppBar` (alpha07).
- `ButtonGroup` connected variant (alpha08). Overflow into a dropdown menu (alpha13).
- `NavigationSuiteScaffold` (1.4.0-alpha12).

⚠️ Historical notes from a summary of the release notes. **Superseded for 1.5.0-rc01 by the bytecode check below.**
- 1.5.0 alphas promoted these to stable: ButtonGroup, FAB + `FloatingActionButtonMenu`, `ToggleButton`, `SplitButton` (`SplitButtonLayout` is deprecated), `MotionScheme`, the flexible top app bars, expressive list items, and FloatingToolbar.
- `ShortNavigationBar`, `WideNavigationRail`, and `NavigationItem` graduated in 1.4.0-alpha18.
- `LoadingIndicator` and `MaterialShapes` promotion to stable was reverted, so they are still experimental.
- `MaterialExpressiveTheme`: exact name and status not confirmed. A `@Material3ExpressiveApi` annotation that needs no opt-in was reportedly added in 1.5.0-alpha18.
- **Implication:** on material3 1.4.0, many Expressive APIs may still need `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`. **Decided (ADR-006): use the latest pre-release with opt-ins.**

## Architecture (checked 2026-11-12)
- At least two layers: **UI** and **data**. The **domain** layer is optional; add it only when needed (for complex or reused logic).
- Compose is the recommended toolkit. Use a single activity with **Navigation 3**.
- **UDF**: state flows down, events flow up (strongly recommended).
- Use `ViewModel`, not `AndroidViewModel`. Don't give it a Context, Activity, or Resources. Use ViewModels only at screen level.
- Expose `uiState: StateFlow`. Use `stateIn(..., SharingStarted.WhileSubscribed(5000), ...)` for streams. Don't send one-off events from the ViewModel to the UI; model them as state.
- Collect state with `collectAsStateWithLifecycle()`.
- Use one repository per data type. The UI never calls data sources directly. The single source of truth is the data's owner; in offline-first apps that is the DB.
- Use DI with constructor injection. Hilt is recommended for complex projects.
- Expose coroutines and Flow between layers, and make them main-safe.
- Testing: prefer fakes over mocks; unit-test ViewModels and repositories; use Macrobenchmark for performance.
- Performance: R8 full mode, Baseline Profiles, Startup Profiles.

## Kotlin style (checked 2026-11-12)
- 4-space indent, 100-column limit, no wildcard imports, ASCII-sorted imports, K&R braces.
- PascalCase for classes; camelCase for functions and properties; UPPER_SNAKE_CASE for constants. Unit-returning `@Composable` functions use PascalCase nouns.
- Package names are lowercase with no underscores. A file with a single top-level class is named after it.

## Versioning (checked 2026-11-12)
See [VERSIONING.md](VERSIONING.md). Key facts: versionCode increases monotonically, its max is 2100000000, used codes can't be reused, and the docs suggest SemVer for versionName.

## Build requirements of material3 (checked 2026-11-12 from AAR metadata on Google Maven)
| material3 | minCompileSdk | min AGP |
|---|---|---|
| 1.4.0 (stable) | 35 | 8.6.0 |
| 1.5.0-alpha29 / 1.5.0-rc01 (newest) | **37** | 9.1.0 |
→ The bleeding-edge Expressive APIs (ADR-006) require **compileSdk 37**. Source: `META-INF/com/android/build/gradle/aar-metadata.properties` in `https://dl.google.com/android/maven2/androidx/compose/material3/material3-android/<ver>/`

## Architecture detail check (2026-11-13)
Sources: [UI layer](https://developer.android.com/topic/architecture/ui-layer), [UI events](https://developer.android.com/topic/architecture/ui-layer/events), [data layer](https://developer.android.com/topic/architecture/data-layer), [domain layer](https://developer.android.com/topic/architecture/domain-layer), [recommendations](https://developer.android.com/topic/architecture/recommendations), [modularization patterns](https://developer.android.com/topic/modularization/patterns)
- "The domain layer is an *optional* layer… depends on the data layer." Add use cases "only when required."
- Use cases: "*verb in present tense* + *noun/what (optional)* + *UseCase*", "should not contain mutable data", and "You can also overload `invoke()`".
- `stateIn(WhileSubscribed(5000))` applies when data is a stream. For simpler cases, a `MutableStateFlow` exposed as a `StateFlow` is acceptable.
- "ViewModel events should always result in a UI state update." "Do not send events from the ViewModel to the UI" (strongly recommended).
- Create a repository "for each different type of data". Each data source works with "only one source of data". Repository and data source calls "should be *main-safe*".
- Strongly recommended: prefer fakes to mocks; R8 full mode; Baseline and Startup Profiles; Macrobenchmark.
- Not stated by Google: repository interfaces as a requirement, a mandatory dispatcher-injection rule, or a `core:model` module. These are team conventions (see ARCHITECTURE.md).

## material3 1.5.0-rc01 API status (checked 2026-11-13 by inspecting the published AAR with `javap`)
Method: downloaded `material3-android-1.5.0-rc01.aar` from Google Maven and scanned the class annotations for `ExperimentalMaterial3ExpressiveApi` / `ExperimentalMaterial3Api` / `Deprecated`.
- **Exist and are stable (no opt-in)**: `MaterialExpressiveTheme`, `MotionScheme` (`expressive()`/`standard()`), `ButtonGroup`, `FloatingActionButtonMenu`, `ToggleFloatingActionButton`, `ContainedLoadingIndicator`, `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `ShortNavigationBar`, `SplitButtonLayout`, `ToggleButton`, `HorizontalFloatingToolbar`, `LinearWavyProgressIndicator`, `CircularWavyProgressIndicator`, `SegmentedListItem`, `SegmentedButtonRow`.
- **Still need `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`**: the `MaterialShapes` class, and **`LoadingIndicator`** (confirmed by the compiler on 2026-11-13; the bytecode scan missed it, so **the compiler is the final authority**).
- **Deprecated in rc01**: `ListItem(headlineContent = …)`. Use the overload where the headline is the trailing `content` lambda.
- **Still need `@OptIn(ExperimentalMaterial3Api::class)`**: e.g. `rememberModalBottomSheetState`, `SheetState`, `rememberStandardBottomSheetState`, `RangeSliderState`, `SearchBarColors`, `TextFieldColors`.
- **Deprecated**: the `WideNavigationRail` overload without `contentPadding` ("Deprecated in favor of function with contentPadding parameter").
- `NavigationSuiteScaffold` is in the separate `material3-adaptive-navigation-suite` artifact.
- Re-run this check on every material3 bump (ADR-006).

## Design language sources (checked 2026-11-13)
Summarized in [M3_EXPRESSIVE.md](M3_EXPRESSIVE.md).
- Research: https://design.google/library/expressive-material-design-google-research (46 studies, 18k+ participants, key elements found up to 4× faster, restraint for banking-style contexts)
- Launch: https://blog.google/products-and-platforms/platforms/android/material-3-expressive-android-wearos-launch/
- Reference Compose app: https://android-developers.googleblog.com/2025/05/androidify-building-delightful-ui-with-compose.html (MaterialExpressiveTheme, MotionScheme.expressive(), MaterialShapes, HorizontalFloatingToolbar)
- Design-language principles: https://developer.android.com/design/ui/wear/guides/get-started/design-language
- Views library: MDC-Android is in maintenance mode, and Google recommends Compose (https://github.com/material-components/material-components-android/blob/master/docs/getting-started.md)
- Tokens in rc01 [javap]: Shapes = extraSmall, small, medium, large, largeIncreased, extraLarge, extraLargeIncreased, extraExtraLarge. Typography = 15 base + 15 `*Emphasized`. MotionScheme = {fast,default,slow}{Spatial,Effects}Spec. MaterialShapes = 35 shapes. `expressiveLightColorScheme()` exists.
- m3.material.io motion, easing, shape, typography, color, and components pages: **read on 2026-11-13** with the local headless Chromium (`~/.cache/ms-playwright/chromium_headless_shell-*/…/chrome-headless-shell --headless --dump-dom <url>`). Summary is in M3_EXPRESSIVE.md [M3].
- Also read 2026-11-13: the M3 Expressive blog, corner radius scale, shape morph, type scale tokens, applying type, color roles, dynamic color, elevation, icons, breakpoints, canonical layouts, states, and accessibility (all summarized in M3_EXPRESSIVE.md).
- ⚠️ Still unread: transition patterns (render times out), https://m3.material.io/blog/m3-expressive-motion-theming, component detail pages (one per component), and Android platform UX (edge-to-edge, predictive back, haptics) on developer.android.com.

## Dependency versions (checked 2026-11-13 from Google Maven and Maven Central metadata)
- Latest stable: AGP 9.5.1, Kotlin 2.4.21, KSP 2.3.13, Compose BOM 2026.09.00, Hilt 2.60.1, androidx.hilt 1.4.0, Room 3 3.0.3 (`androidx.room3`), sqlite-bundled 2.7.1, Navigation 3 1.2.1, lifecycle 2.11.0, DataStore 1.3.0, kotlinx-serialization 1.13.0, kotlinx-datetime 0.8.0, coroutines 1.11.0, Turbine 1.2.1, Gradle 9.9.1.
- Kotlin requirements: kotlinx-serialization-core 1.13.0 depends on kotlin-stdlib 2.4.10; hilt-android 2.60.1 depends on kotlin-stdlib 2.3.21.
- Gradle 9.9.1 bin sha256: `d8a18a7a841dd5df825d35a685fad6f2d7f12ac0c280e57b67c27b4a520ba228` (from https://services.gradle.org/distributions/gradle-9.9.1-bin.zip.sha256).
- To check for newer versions: `curl -s https://dl.google.com/android/maven2/<group/path>/<artifact>/maven-metadata.xml` (Google) or `https://repo1.maven.org/maven2/...` (Maven Central).

## Brand color scheme generation (ADR-012, 2026-11-13)
Tool: `@material/material-color-utilities` 0.4.0 (npm, Google). Run it outside the repo:
`npm i @material/material-color-utilities`. The package uses extension-less ESM imports, so run with a resolve hook that appends `.js` (`node --import ./reg.mjs gen.mjs`). Then convert the output `schemes.json` into `ui/theme/Color.kt` (`internal val <name>Scheme = light/darkColorScheme(...)`).
```js
import {Hct, SchemeTonalSpot, DynamicScheme, TonalPalette, MaterialDynamicColors, Variant, argbFromHex, hexFromArgb} from '@material/material-color-utilities';
const P=argbFromHex('#DEDACD'), S=argbFromHex('#228B22');
const roles=['primary','onPrimary','primaryContainer','onPrimaryContainer','inversePrimary','secondary','onSecondary','secondaryContainer','onSecondaryContainer','tertiary','onTertiary','tertiaryContainer','onTertiaryContainer','background','onBackground','surface','onSurface','surfaceVariant','onSurfaceVariant','surfaceTint','inverseSurface','inverseOnSurface','error','onError','errorContainer','onErrorContainer','outline','outlineVariant','scrim','surfaceBright','surfaceContainer','surfaceContainerHigh','surfaceContainerHighest','surfaceContainerLow','surfaceContainerLowest','surfaceDim','primaryFixed','primaryFixedDim','onPrimaryFixed','onPrimaryFixedVariant','secondaryFixed','secondaryFixedDim','onSecondaryFixed','onSecondaryFixedVariant','tertiaryFixed','tertiaryFixedDim','onTertiaryFixed','onTertiaryFixedVariant'];
const mdc=new MaterialDynamicColors();
const out={};
for (const dark of [false,true]) for (const [name,c] of [['',0],['MediumContrast',0.5],['HighContrast',1]]) {
  const base=new SchemeTonalSpot(Hct.fromInt(P),dark,c);
  const s=new DynamicScheme({sourceColorHct:Hct.fromInt(P),variant:Variant.TONAL_SPOT,contrastLevel:c,isDark:dark,
    primaryPalette:TonalPalette.fromInt(P),secondaryPalette:TonalPalette.fromInt(S),tertiaryPalette:base.tertiaryPalette,
    neutralPalette:base.neutralPalette,neutralVariantPalette:base.neutralVariantPalette,specVersion:'2021'});
  const r={}; for (const k of roles){ const dc=mdc[k](); r[k]=hexFromArgb(dc.getArgb(s)).toUpperCase(); }
  out[(dark?'dark':'light')+name+'Scheme']=r;
}
import fs from 'fs'; fs.writeFileSync('schemes.json',JSON.stringify(out,null,1));
for (const k of ['lightScheme','darkScheme']) console.log(k, ['primary','onPrimary','primaryContainer','secondary','secondaryContainer','tertiary','tertiaryContainer','surface','surfaceContainer','onSurface'].map(x=>x+'='+out[k][x]).join(' '));
```

## App shell APIs (checked 2026-11-13 via javap and the compiler)
- Navigation 3 1.2.1: `NavDisplay(backStack, onBack, entryDecorators, entryProvider)`, `rememberNavBackStack(vararg NavKey)`, `rememberSaveableStateHolderNavEntryDecorator()`, `entryProvider { entry<Key> { } }`.
- lifecycle-viewmodel-navigation3 2.11.0: `rememberViewModelStoreNavEntryDecorator()`.
- androidx.hilt 1.4.0: `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()`.
- material3-adaptive-navigation-suite 1.5.0-rc01: `NavigationSuiteScaffold(navigationItems, modifier, navigationSuiteType, …, state, content)`, `rememberNavigationSuiteScaffoldState()` (`show()`/`hide()`), `NavigationSuiteItem(selected, onClick, icon, label)`. `NavigationSuiteScaffoldDefaults.navigationSuiteType()` returns the Expressive types `ShortNavigationBarCompact/Medium` and `WideNavigationRailCollapsed/Expanded`. `calculateFromAdaptiveInfo()` returns the old `NavigationBar`/`NavigationRail`. The stateless scaffold overloads are deprecated ("in favor of NavigationSuiteScaffold with state parameter").
- `LargeFlexibleTopAppBar` compiles without the Expressive opt-in in rc01.
- kotlinx-coroutines-test 1.11.0: `Dispatchers.setMain/resetMain` still need `@OptIn(ExperimentalCoroutinesApi::class)`.

## Icons (checked 2026-11-13)
- Official Material Symbols Rounded Android drawables: `https://raw.githubusercontent.com/google/material-design-icons/master/symbols/android/<name>/materialsymbolsrounded/<name>_24px.xml` (outline) and `<name>_fill1_24px.xml` (filled). License: Apache-2.0. Browse names at https://fonts.google.com/icons.
- These files use `android:tint="?attr/colorControlNormal"` (an AppCompat attribute; this app has no AppCompat), so remove that attribute when importing. Compose `Icon` tints them. `arrow_back` already includes `android:autoMirrored="true"`.
- Google's guidance (https://developer.android.com/develop/ui/compose/graphics/images/material, checked 2026-11-13): the Material Icons artifact (`material-icons`, the `Icons.Rounded.*` objects) "is no longer maintained or recommended for use in your apps, as it contains an older look and feel and can also increase the build time of your apps significantly. Instead, we recommend using Google Font Icons and download the XML file" (Material Symbols).

## Dynamic system colors for XML (checked 2026-11-13 in android-37.0 android.jar)
- Android 14+ (API 34): `@android:color/system_surface_light`, `system_surface_dark`, plus `system_surface_container_*`, `system_background_*` and other M3 roles.
- Android 12+ (API 31): tonal palettes `@android:color/system_neutral1_0…1000`, `system_accent1_*` etc. `neutral1_10` ≈ tone 99 and `neutral1_900` ≈ tone 10 (closest to M3 surface; M3 dark surface is tone 6).
- The Android 12+ system splash uses `android:windowSplashScreenBackground` (falls back to `windowBackground`).

## Room 3 (3.0.3) facts (checked 2026-11-13 via javap and the compiler)
- Package `androidx.room3`. `Room.databaseBuilder(context, Class, name)`, `Room.inMemoryDatabaseBuilder(context, Class)`. Builder: `setDriver(BundledSQLiteDriver())` (`androidx.sqlite.driver.bundled`), `setQueryCoroutineContext`, `addCallback`, `addMigrations`.
- `RoomDatabase.Callback.onCreate(connection: SQLiteConnection)` is `suspend`. Use `connection.prepare(sql).use { bindText/bindLong; step(); reset() }`.
- Raw SQL on a database: `database.useWriterConnection { it.executeSQL("...") }` (imports `androidx.room3.useWriterConnection`, `androidx.room3.executeSQL`). Transactions: `withWriteTransaction { }`.
- Enums are stored as TEXT automatically. Custom converters use `@ColumnTypeConverter`.
- `@Upsert` with a `Long` return works (rowId on insert, -1 on update).
- No `@ConstructedBy` needed for an Android-only database built with the `Class` overload.

## Money formatting (checked 2026-11-13)
- JVM `java.text.NumberFormat` (en-IN) gives "₹124,580.00" (no lakh grouping). Android `android.icu.text.NumberFormat` (API 24+) gives "₹1,24,580.00". Confirmed on a moto g57 power, Android 17. The yen symbol differs between runtimes (fullwidth "￥" on the JVM), so tests shouldn't assert exact currency glyphs except for stable cases.

## Transaction flow APIs (checked 2026-11-15 in the material3-android 1.5.0-rc01 sources jar from Google Maven, and the compiler)
- `FloatingActionButtonMenu(expanded, button, modifier, horizontalAlignment, content: FloatingActionButtonMenuScope.() -> Unit)`, `FloatingActionButtonMenuItem(onClick, text, icon, …)`, `ToggleFloatingActionButton(checked, onCheckedChange, …, content: ToggleFloatingActionButtonScope.() -> Unit)` (scope has `checkedProgress`), `ToggleFloatingActionButtonDefaults.animateIcon(checkedProgress)` (Expressive opt-in).
- `ButtonGroup(overflowIndicator, modifier, expandedRatio, horizontalArrangement, verticalAlignment, content: ButtonGroupScope.() -> Unit)`; items are `clickableItem`, `toggleableItem` or `customItem(buttonGroupContent, menuContent)`; the scope has `Modifier.weight` and `Modifier.animateWidth(interactionSource)`. `ButtonGroupDefaults.OverflowIndicator(menuState)`.
- Connected (single-select) button group: the official sample is a `Row(horizontalArrangement = spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween))` of `ToggleButton(checked, onCheckedChange, shapes = ButtonGroupDefaults.connectedLeading/Middle/TrailingButtonShapes())` with `Role.RadioButton` semantics.
- Expressive overloads: `FilterChip(selected, onClick, label, shapes: ChipShapes = FilterChipDefaults.shapes(), …)` (tonal colors by default), `Button(onClick, shapes: ButtonShapes, …)` with `ButtonDefaults.shapes()` / `shapesFor(height)`, `contentPaddingFor`, `iconSizeFor`, `textStyleFor`, `MediumContainerHeight`. `ListItem(…, content)` is the Expressive list item; `ListItem(headlineContent = …)` is deprecated.
- `LoadingIndicator()` needs `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` in rc01.
- `OutlinedTextField` has `String`, `TextFieldValue` and `TextFieldState` overloads; none are deprecated in rc01.
- Date pickers: `DatePicker(state, …, title, headline, showModeToggle)`, `DatePickerDialog`, `DateRangePicker`. There is **no separate docked date picker composable**; the docked variant is a `DatePicker` shown in a `Popup` anchored below a read-only text field (Compose docs pattern, https://developer.android.com/develop/ui/compose/components/datepickers; page not re-fetched on this date). `DatePicker` works in UTC-midnight millis.
- androidx.hilt 1.4.0: `hiltViewModel<VM, VMF>(viewModelStoreOwner, key, creationCallback: (VMF) -> VM)` for assisted ViewModels.
- compose-ui-test: `androidx.compose.ui.test.junit4.createComposeRule` is deprecated; use `androidx.compose.ui.test.junit4.v2.createComposeRule` (StandardTestDispatcher).
- Material Symbols: `local_grocery_store` has no file in the repo (404); it is an alias of `shopping_cart`, which the app uses for that seed `iconKey`.

## Motion, transitions and haptics (checked 2026-11-15)
- M3 blog "Adding Motion Physics with Jetpack Compose" (https://m3.material.io/blog/m3-expressive-motion-theming, rendered with headless Chromium): `MaterialExpressiveTheme` defaults to the Expressive scheme; **spatial** specs for position/orientation/size/shape (overshoot), **effects** specs for color/opacity (no overshoot); speeds: fast = small components, default = partial-screen, slow = full-screen. Custom animations should use `MaterialTheme.motionScheme.*Spec()` instead of `tween()`. Springs retarget smoothly when interrupted.
- Navigation 3 animations (https://developer.android.com/guide/navigation/navigation-3/animate-destinations): `NavDisplay(transitionSpec, popTransitionSpec, predictivePopTransitionSpec)`, each returning a `ContentTransform`; per-entry overrides via metadata `NavDisplay.TransitionKey` / `PopTransitionKey` / `PredictivePopTransitionKey`. In navigation3-ui 1.2.1 `predictivePopTransitionSpec` also receives the swipe edge (`Int`).
- Haptics (https://developer.android.com/develop/ui/views/haptics/haptic-feedback, https://developer.android.com/develop/ui/views/haptics/haptics-principles): use action-oriented constants (no `VIBRATE` permission; they respect user settings); "less is more"; prefer no haptics over buzzy ones. Compose `HapticFeedbackType` in ui 1.12+: `Confirm`, `Reject`, `ToggleOn/Off`, `SegmentTick`, `SegmentFrequentTick`, `GestureThresholdActivate`, `GestureEnd`, `ContextClick`, `KeyboardTap`, `VirtualKey`, `LongPress`, `TextHandleMove`.
- `LocalResources` (compose-ui) gives `Resources` in composition; use it instead of `LocalContext.current.resources`.
- material3-adaptive 1.3.0: `currentWindowAdaptiveInfo()` is **deprecated** ("use V2 version… to support L and XL width size classes"); use `currentWindowAdaptiveInfoV2()`.
- material3 1.5.0-rc01: `MaterialShapes.Cookie9Sided` + `RoundedPolygon.toShape()`; `MediumFlexibleTopAppBar`/`LargeFlexibleTopAppBar` take `subtitle`, `titleHorizontalAlignment`; `ButtonDefaults.LargeContainerHeight` exists (96dp class, too big for a form action).
- Transition patterns page: now read (pasted by the user, 2026-11-15); see "M3 motion pages (read 2026-11-15)" below. The custom shared-axis transition was removed (ADR-019 amendment).
- Testing note: with the soft keyboard open in a plain `ComponentActivity` test host, taps on content below the focused field did not register in `TransactionEditorScreenTest` (device run 2026-11-15); the test selects the category before typing. Not yet reproduced or ruled out in the real (edge-to-edge) app.

## M3 motion pages (read 2026-11-15; the user pasted the rendered text because headless rendering failed)
**Motion physics system** (https://m3.material.io/styles/motion/overview/how-it-works)
- Introduced with M3 Expressive (May 2025); it is **replacing** easing and duration. Availability: Jetpack Compose available; MDC-Android available but not added to components; Flutter unavailable; Web compatible with Compose springs.
- Two preset schemes: **expressive** (opinionated, overshoots; for most situations, especially hero moments and key interactions) and **standard** (minimal bounce; utilitarian products). Custom schemes are possible.
- Springs = stiffness + damping + initial velocity; they handle gestures, interruptions and retargeting. "All component motion is driven by two tokens: expressive fast spatial and expressive fast effects."
- Tokens: spatial and effects × default/fast/slow, e.g. `md.sys.motion.spring.fast.spatial`. The scheme is applied at product level, not part of the token. **Spatial**: x/y, rotation, size, rounded corners (overshoots). **Effects**: color, opacity (no overshoot).
- Speed: default for partial-screen (bottom sheet, expanded rail), fast for small components (switches, buttons), slow for full-screen animations. Token values differ per device class.
- On Compose, 21 Material components use it by default; custom components should use the spring tokens. Customization levels: 1 default scheme (should be sufficient), 2 custom `MotionScheme`, 3 swap scheme per element via CompositionLocal.

**Easing and duration** (https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration)
- "In the expressive update, components and motion now use the motion physics system… Products should migrate. The easing and duration system is **still used for transitions**… but is **no longer maintained**."
- Suggested pairs: Emphasized 500ms (begin and end on screen), Emphasized decelerate 400ms (enter), Emphasized accelerate 200ms (exit), Standard 300/250/200ms. Emphasized is recommended for most transitions; Standard for small utility transitions. Exit permanently = accelerate; exit temporarily = emphasized. Duration scales with area; exits are shorter than enters.
- In material3 1.5.0-rc01 these exist only as **internal** `MotionTokens` (e.g. EmphasizedDecelerate `CubicBezierEasing(0.05, 0.7, 0.1, 1.0)`, DurationMedium4 = 400). They are **not public API**; do not copy them into the app (ADR-019 amendment).

**Transitions / transition patterns** (https://m3.material.io/styles/motion/transitions/transition-patterns)
- "M3 transitions use the legacy easing and duration system. They'll eventually be updated to use the motion physics system." Transitions "should be the top priority for a strong motion implementation."
- Six patterns:
  1. **Container transform**: an element transforms to show more detail (card → details page). Used with cards, lists, galleries, search, sheets, **FABs**, chips. Strongest relationship, most expressive. Persistent container/element connects start and end.
  2. **Forward and backward**: consecutive hierarchy levels (inbox → thread). Horizontal slide; **Android uses a fade as screens slide** (iOS uses parallax).
  3. **Lateral**: peers at the same level (tabs, carousels, galleries). Slide in unison, no fade or parallax.
  4. **Top level**: navigation bar/rail/drawer destinations. "The exiting screen quickly fades out and then the entering screen fades in", with no grouping or persistent elements.
  5. **Enter and exit**: within screen bounds (FABs, dialogs, menus, snackbars, time pickers, tooltips), Android components **expand and collapse along x or y**; **scale and z-axis motion is avoided** (implies elevation change, which doesn't fit M3's reduced elevation). Direction follows screen position (top menu expands down, snackbar expands up). Beyond screen bounds (app bars, banners, nav bar/rail/drawer, sheets): expand/collapse along an axis while sliding; can be scroll-driven. Entry edges build a spatial model (notifications top, drawer left, sheets and keyboard bottom).
  6. **Skeleton loaders**: hint at content layout while loading, subtle pulse from top-left to bottom-right, content fades in on top.

## ToggleButton (checked 2026-11-15 in the material3 1.5.0-rc01 sources jar)
- One public overload: `ToggleButton(checked, onCheckedChange, modifier, buttonSize: ToggleButtonSize = ToggleButtonDefaults.size, enabled, icon: (@Composable () -> Unit)? = null, shapes = ToggleButtonDefaults.shapesFor(buttonSize), colors, elevation, border, contentPadding = contentPaddingFor(buttonSize, hasStartIcon), interactionSource, content)`. Not deprecated, no opt-in.
- The `icon` slot sizes and spaces the icon itself (`ButtonDefaults.iconSizeFor/iconSpacingFor(buttonSize.height)`).
- `ToggleButtonSize`: `ExtraSmall`, `Small`, `Medium` (= `ButtonDefaults.MediumContainerHeight`), `Large`. Variants: `ElevatedToggleButton`, `FilledTonalToggleButton`, `OutlinedToggleButton`.
- Chips (`FilterChip` etc.) are **not** in the M3 Expressive component set (M3_EXPRESSIVE.md §1). ⚠️ The m3.material.io chips and button-groups guideline pages are still unread (headless rendering fails); ask the user to paste them before relying on their single-selection guidance.

## ButtonGroup motion (checked 2026-11-15 in the material3 1.5.0-rc01 sources jar)
- `ButtonGroupScope.toggleableItem(checked, label, onCheckedChange, icon, weight, enabled)` renders a `ToggleButton` wrapped in `EnlargeOnPressElement`: on press the item grows by `expandedRatio` (`ButtonGroupDefaults.ExpandedRatio = 0.15f`) and neighbours compress, animated with `MotionSchemeKeyTokens.FastSpatial` (the theme's fast spatial spring). `clickableItem` does the same with a `Button`; `customItem` content needs `Modifier.animateWidth(interactionSource)`.
- `toggleableItem` doesn't accept a modifier or semantics, so items get toggle-button semantics, not `Role.RadioButton`.
- The "connected" button group samples (`SingleSelectConnectedButtonGroupSample`) are a `Row` of `ToggleButton`s with `connected*ButtonShapes()`. They have **no** enlarge-on-press motion.

## Hero amount field (checked 2026-11-15)
- A `TextField` `prefix` stays at the start edge when `textAlign = Center`, so a centered amount can't use it. Use a display-only `VisualTransformation` (public `androidx.compose.ui.text.input` API) that prepends the symbol, with an `OffsetMapping` shifted by its length. The stored value stays plain digits.
- A field without a visible label still needs an accessible name: `Modifier.semantics { contentDescription = … }` on the `TextField`. Tests then find it with `onNodeWithContentDescription`.

## Room 3 migrations (checked 2026-11-15 via javap on room3-runtime-android and room3-testing-android 3.0.3)
- `androidx.room3.migration.Migration(startVersion, endVersion)` with `suspend fun migrate(connection: SQLiteConnection)`. Register with `RoomDatabase.Builder.addMigrations(...)`. Use `connection.execSQL(...)` (`androidx.sqlite.execSQL`) or `connection.prepare(sql).use { bindText; step() }`.
- A column added by migration must match the entity: `ALTER TABLE … ADD COLUMN title TEXT NOT NULL DEFAULT ''` ⇔ `@ColumnInfo(defaultValue = "") val title: String`.
- `androidx.room3:room3-testing` (3.0.3 matches the runtime): `MigrationTestHelper(instrumentation, file, driver, databaseClass, databaseFactory = …, autoMigrationSpecs = …)` is a JUnit rule; `createDatabase(version)` and `runMigrationsAndValidate(version, migrations)` return a `SQLiteConnection`. It reads the exported schemas in `app/schemas/`.

## Search, segmented lists, filter chips (checked 2026-11-15 in the material3-android 1.5.0-rc01 sources jar and with javap)
- Current search API: `SearchBar(state: SearchBarState, inputField, modifier, shape, colors, tonalElevation, shadowElevation)`, `ExpandedFullScreenSearchBar(state, inputField, modifier, collapsedShape, colors, tonalElevation, shadowElevation, windowInsets, properties, content: ColumnScope.() -> Unit)`, `ExpandedDockedSearchBar`, `AppBarWithSearch`, `rememberSearchBarState()`, `SearchBarState.currentValue` (`SearchBarValue.Collapsed/Expanded`), `animateToExpanded()`/`animateToCollapsed()`. `SearchBarDefaults.InputField(textFieldState, searchBarState, onSearch, …, placeholder, leadingIcon, trailingIcon, …)`. Needs `@OptIn(ExperimentalMaterial3Api::class)`.
- `ExpandedFullScreenSearchBar` "is displayed in a new full-screen dialog" and renders only while `state.isExpanded`. It **expands out of the collapsed `SearchBar`**: the collapsed bar records its position in `state.collapsedCoords`, and the expanded bar animates from it. So a collapsed `SearchBar` sharing the same state must be on screen.
- Deprecated in rc01: `TopSearchBar` ("Renamed to `AppBarWithSearch`"), the query-style `SearchBar`/`DockedSearchBar` ("Use SearchBar with SearchBarState, and ExpandedFullScreenSearchBar or ExpandedDockedSearchBar…"), `InputField(state, onSearch, expanded, onExpandedChange, …)` and `InputField(query, onQueryChange, …)`.
- `SegmentedListItem` (`@material3expressive`, no opt-in) has four overloads: non-interactive `(shapes, …)`, `(onClick, shapes, …, onLongClick, onLongClickLabel)`, selectable `(selected, onClick, shapes, …)` and checkable `(checked, onCheckedChange, shapes, …)`; the headline is the trailing `content` lambda. `ListItemDefaults.segmentedShapes(index, count, defaultShapes = shapes())` gives first/middle/last/single shapes; `ListItemDefaults.SegmentedGap` (2dp) is the gap between items; `segmentedColors()` is the default.
- `FilterChip` has an Expressive overload with `shapes: ChipShapes` (`FilterChipDefaults.shapes()`); the `shape` overload isn't deprecated, but ADR-013 says use the Expressive one.
- `DateRangePicker(state, …)`, `rememberDateRangePickerState(initialSelectedStartDateMillis, initialSelectedEndDateMillis, …)` (UTC-midnight millis), `DatePickerDialog(onDismissRequest, confirmButton, …, dismissButton, …, content: ColumnScope.() -> Unit)`.
- `ModalBottomSheet` still needs `@OptIn(ExperimentalMaterial3Api::class)`.
- kotlinx-datetime 0.8.0 has `LocalDateRange` (`start..endInclusive`).
- SQLite `LIKE` is case-insensitive for ASCII characters only; use `ESCAPE '\'` to match `%` and `_` literally.

## Floating toolbar and flexible app bar subtitle (checked 2026-11-15 in the material3-android 1.5.0-rc01 sources jar and the compiler)
- `HorizontalFloatingToolbar(expanded, modifier, colors = FloatingToolbarDefaults.standardFloatingToolbarColors(), contentPadding, scrollBehavior, shape, leadingContent, trailingContent, expandedShadowElevation, collapsedShadowElevation, content: RowScope.() -> Unit)`. There's also an overload with `floatingActionButton` (an icon-only FAB, `FloatingToolbarDefaults.VibrantFloatingActionButton` / `StandardFloatingActionButton`) and `floatingActionButtonPosition`. It compiled without the Expressive opt-in in rc01.
- `FloatingToolbarDefaults.vibrantFloatingToolbarColors()` (`fabContainerColor`/`fabContentColor` give the matching FAB pair), `ContainerSize`, `ScreenOffset`.
- `LargeFlexibleTopAppBar(title, modifier, subtitle: (@Composable () -> Unit)? = null, navigationIcon, actions, titleHorizontalAlignment, …)`.
- `Typography.bodyLargeEmphasized.fontWeight` gives the emphasized weight for inline emphasis (`SpanStyle(fontWeight = …)`).

## Bottom sheet state (checked 2026-11-15 with the rc01 compiler and sources)
- `rememberModalBottomSheetState(skipPartiallyExpanded, confirmValueChange)` is **deprecated** in material3 1.5.0-rc01: "Use rememberBottomSheetState with Hidden initial value."
- Replacement: `rememberBottomSheetState(initialValue: SheetValue, enabledValues: Set<SheetValue> = setOf(Hidden, PartiallyExpanded, Expanded), confirmValueChange)` (`@ExperimentalMaterial3Api`). For a sheet that opens fully expanded: `initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)`, passed as `ModalBottomSheet(sheetState = …)`.

## Text auto-size and Navigation 3 default transitions (checked 2026-11-15 in the sources jars)
- material3 1.5.0-rc01 `Text(…, autoSize: TextAutoSize? = null)` (foundation `TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = 112.sp, stepSize)`). Pass `maxFontSize = style.fontSize` so auto-size only shrinks. It needs `maxLines = 1` / `softWrap = false` for single-line fitting.
- navigation3-ui 1.2.1 on Android: `defaultTransitionSpec()` and `defaultPopTransitionSpec()` are both a plain **cross-fade** (`fadeIn`/`fadeOut`, `tween(700)`); the predictive-back pop is its own spec. So opening and closing a pushed screen fade the same way, with no movement or container transform.
- material3 rc01 has **no** container-transform / shared-element API and no `MotionScheme.sharedElementTransitionSpec`. Shared bounds exist only in Compose foundation (`SharedTransitionLayout`, `Modifier.sharedBounds`), and Navigation 3 exposes `LocalNavAnimatedContentScope` for them.

## Carousel, wavy progress, tonal button shapes (checked 2026-11-16 in the material3-android 1.5.0-rc01 sources jar)
- `HorizontalMultiBrowseCarousel(state, preferredItemWidth, modifier, itemSpacing = 0.dp, flingBehavior = singleAdvanceFlingBehavior, userScrollEnabled, minSmallItemWidth = 40.dp, maxSmallItemWidth = 56.dp, contentPadding, content: CarouselItemScope.(Int) -> Unit)`, package `androidx.compose.material3.carousel`, **no opt-in**. "Carousel then lays out items using the large item size and clips (or masks) items depending on their scroll offset." `rememberCarouselState(initialItem = 0, itemCount: () -> Int)`. `CarouselItemScope`: `Modifier.maskClip(shape)`, `maskBorder`, `rememberMaskShape`, `carouselItemDrawInfo` (don't read it in composition).
- Official sample (https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/samples/src/main/java/androidx/compose/material3/samples/CarouselSamples.kt, checked 2026-11-16): `preferredItemWidth = 186.dp`, `itemSpacing = 8.dp`, items `maskClip(MaterialTheme.shapes.extraLarge)`.
- `LinearWavyProgressIndicator(progress: () -> Float, modifier, color = indicatorColor, trackColor, stroke, trackStroke, gapSize, stopSize, amplitude, wavelength, waveSpeed)` (determinate) and an indeterminate overload; `@material3expressive`, no opt-in.
- `FilledTonalButton(onClick, shapes: ButtonShapes, …)` is the Expressive overload (with `ButtonDefaults.shapes()`); the `shape` overload still exists.
- `CircularWavyProgressIndicator(progress: () -> Float, modifier, color, trackColor, stroke = circularIndicatorStroke, trackStroke, gapSize, amplitude, wavelength = CircularWavelength, waveSpeed)` (determinate), `@material3expressive`, no opt-in; `WavyProgressIndicatorDefaults.CircularContainerSize` is its default size (checked 2026-11-16, rc01 sources).

## Account management APIs (checked 2026-11-16 in the material3-android 1.5.0-rc01 sources jar and the compiler)
- `ExposedDropdownMenuBox(expanded, onExpandedChange, modifier, content: ExposedDropdownMenuBoxScope.() -> Unit)` (`@ExperimentalMaterial3Api`). In the scope: `Modifier.menuAnchor(type: ExposedDropdownMenuAnchorType, enabled = true)` (`PrimaryEditable` for a typed filter, `PrimaryNotEditable` for read-only), `ExposedDropdownMenu(expanded, onDismissRequest, …)` as a scope **extension** (the member version is deprecated, hidden), `ExposedDropdownMenuDefaults.TrailingIcon(expanded)`.
- Menu items: `DropdownMenuItem(selected, onClick, text, shapes, …)` is **deprecated (hidden)**: "Use SelectableDropdownMenuItem instead." `SelectableDropdownMenuItem(selected, onClick, text, shapes: MenuItemShapes, …)` (`@material3expressive`, no opt-in, radio-button role). Shapes by position: `MenuDefaults.itemShape(index, count)`.
- `ExtendedFloatingActionButton(text, icon, onClick, modifier, expanded = true, …)` animates between icon+label and icon only; `Small`/`Medium`/`LargeExtendedFloatingActionButton` variants exist.
- `Modifier.minimumInteractiveComponentSize()` (material3) gives a 48dp touch target. (`SwitchRow` no longer uses it; it is a toggleable `SegmentedListItem` since ADR-039.)
- Testing note (2026-11-16): `ExtendedFloatingActionButton`'s label isn't in the merged semantics tree under `onNodeWithText`; the test uses `useUnmergedTree = true`. A device run where all Compose tests failed with "No compose hierarchies found" was the phone locking mid-run (re-run passed), not the app (LESSONS #8).
- `HorizontalUncontainedCarousel(state, itemWidth, modifier, itemSpacing, flingBehavior = noSnapFlingBehavior(), userScrollEnabled, contentPadding, content)` keeps every item at `itemWidth` (checked 2026-11-16, rc01 sources). Multi-browse sizes items to fit the container, so with one or two short items they can shrink toward `minSmallItemWidth` (40dp): the "stick" the user saw.

## Expressive buttons, toggleable list item, date picker tokens (checked 2026-11-16 with `javap` on the material3-android 1.5.0-rc01 classes)
- `IconButton(onClick, shapes: IconButtonShapes, modifier, enabled, colors, interactionSource, content)` is the Expressive overload; `IconButtonDefaults.shapes()` (and `shapes(shape, pressedShape)`). The `shape: Shape` overload and the plain one still exist.
- `TextButton(onClick, shapes: ButtonShapes, …)` is the Expressive overload, with `ButtonDefaults.shapes()`.
- `Switch(checked, onCheckedChange, modifier, thumbContent: (@Composable () -> Unit)? = null, …)`: an icon in the thumb is opt-in through `thumbContent`, sized `SwitchDefaults.IconSize` (official "Switch with icon" sample, https://developer.android.com/develop/ui/compose/components/switch, checked 2026-11-16; compiles against rc01).
- `SegmentedListItem(checked: Boolean, onCheckedChange: (Boolean) -> Unit, shapes: ListItemShapes, …)` is the toggleable overload (also a `selected`/`onClick` one).
- `DatePickerDefaults.TonalElevation` and `DatePickerDefaults.shape` are public; there is no public shadow-elevation token for a docked picker.
- `Small`/`Medium`/`LargeExtendedFloatingActionButton` exist.
- Material Symbols `archive` (Rounded) downloaded from the official repo as `ic_archive_rounded` (account status row).
