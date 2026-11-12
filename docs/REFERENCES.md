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

