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
- To read next: [UI layer](https://developer.android.com/jetpack/guide/ui-layer), [data layer](https://developer.android.com/jetpack/guide/data-layer), [Compose architecture](https://developer.android.com/develop/ui/compose/architecture), [Navigation 3](https://developer.android.com/guide/navigation/navigation-3)

> Note (2026-11-12): fetching m3.material.io pages automatically returned no content (the site is client-rendered). Read them in a browser, or use developer.android.com and the API reference instead.

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

⚠️ Unverified (taken from a summary of the release notes; check against the API reference before use):
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
