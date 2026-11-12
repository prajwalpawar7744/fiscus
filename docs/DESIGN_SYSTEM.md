# Design System: Material 3 Expressive (strict)

**Rule:** Fiscus uses only Material 3 Expressive design and only components from `androidx.compose.material3` (plus `material3-adaptive*` and `material-icons` / Material Symbols). Facts and API status are in [REFERENCES.md](REFERENCES.md). The design intent and look & feel are in [M3_EXPRESSIVE.md](M3_EXPRESSIVE.md); run its checklist for every screen.

## What is allowed vs. forbidden
✅ Allowed
- Any component from `androidx.compose.material3`, including **all** Expressive APIs, even experimental ones (ADR-006). Opt in at file or function level.
- **Expressive-first (mandatory, ADR-013):** if an Expressive component or overload exists for a need, use it. The older non-Expressive equivalent is not allowed. **Deprecated APIs are never allowed**: use the replacement named in the deprecation message. When in doubt, check REFERENCES.md and let the compiler's deprecation warnings decide.

  | Need | ✅ Use (Expressive) | ❌ Not |
  |---|---|---|
  | Bottom navigation | `ShortNavigationBar` (via `NavigationSuiteScaffold`) | `NavigationBar` |
  | Side navigation | `WideNavigationRail` (with `contentPadding`) | `NavigationRail`, `PermanentNavigationDrawer` for primary nav |
  | Medium/large top bar | `MediumFlexibleTopAppBar` / `LargeFlexibleTopAppBar` | `MediumTopAppBar` / `LargeTopAppBar` |
  | Bottom actions bar | `HorizontalFloatingToolbar` / `FlexibleBottomAppBar` | `BottomAppBar` |
  | Several related actions | `ButtonGroup` (connected) / `ToggleButton` | a plain `Row` of buttons |
  | Multiple create actions | `FloatingActionButtonMenu` + `ToggleFloatingActionButton` | several separate FABs |
  | Action + options | `SplitButtonLayout` | button + separate menu icon |
  | Short indeterminate wait | `LoadingIndicator` / `ContainedLoadingIndicator` | `CircularProgressIndicator` (indeterminate) |
  | Determinate progress | `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` | flat indicators, unless a calm data context calls for them (ADR) |
  | Lists | `ListItem` content-lambda overload, `SegmentedListItem` | `ListItem(headlineContent = …)` (deprecated) |
  | Theme | `MaterialExpressiveTheme` + `MotionScheme.expressive()` | `MaterialTheme` |
  | Nav suite type | `NavigationSuiteScaffoldDefaults.navigationSuiteType(...)` | `calculateFromAdaptiveInfo(...)` (returns old NavigationBar/Rail) |
  | Nav suite scaffold | overload with `state` | stateless overloads (deprecated) |

  Components that have no separate Expressive version (`Button`, `Card`, `TextField`, `Snackbar`, dialogs, chips, …) are used as-is: they are already the updated M3 Expressive versions in material3 1.5.
- Arranging M3 components with Compose foundation **layout** primitives (`Column`, `Row`, `Box`, `LazyColumn`, `Spacer`, padding).
- Theme tokens: `MaterialTheme.colorScheme`, `.typography`, `.shapes`, `.motionScheme`.
- Material Symbols / Material Icons.

❌ Forbidden
- Third-party UI or component libraries.
- Hand-built replacements for Material components. Examples: a custom button drawn from `Box` + `clickable`, a custom chip, a hand-drawn progress bar, or a custom bottom sheet.
- Hardcoded colors, font sizes, corner radii, or animation specs in screens.
- Patterns that aren't in the M3 guidelines.
- **If a need has no M3 component** (for example, charts), stop and ask the user. Record the outcome as an ADR.

## Theme
- `FiscusTheme(darkTheme, dynamicColor = true)` in `ui/theme/Theme.kt` wraps `MaterialExpressiveTheme` with `MotionScheme.expressive()`, `FiscusShapes` (Shape.kt) and `FiscusTypography` (Type.kt). Every screen and preview uses `FiscusTheme`.
- **Brand colors** (ADR-012): primary key **#DEDACD**, secondary key **#228B22**. Schemes in `Color.kt` are generated, never hand-edited: light and dark × standard, medium and high contrast. On Android 14+ the brand scheme follows the user's contrast setting (`UiModeManager.contrast`).
- `Shapes()` and `Typography()` stay at M3 defaults (they already include the Expressive scale and emphasized styles). Remap in `Shape.kt` or `Type.kt` only, never per screen.
- **Font: the device's font** (ADR-014). The default `Typography()` already uses the system sans-serif, which follows the phone's font and OEM font settings. Never bundle a font or set `fontFamily` on text.
- Amounts: `style.tabularNumbers` (Type.kt), e.g. `MaterialTheme.typography.displayLargeEmphasized.tabularNumbers`.
- **Startup window and splash** use `@color/window_background`: `system_surface_light/dark` on Android 14+, `system_neutral1_10/900` on 12–13 (wallpaper-based), brand surface below 12. So the splash follows Material You like the app. Note: when the M5 "use brand colors" toggle is on, the splash still shows dynamic colors (resources can't read app settings). Handle that with the core-splashscreen API if needed.
- Theme check: `ui/theme/ThemePreview.kt` (brand light, dark, and large font; dynamic).
- Dynamic color is the default on API 31+; brand schemes are used below that or when `dynamicColor = false` (a Settings toggle comes in M5).
- Income/expense colors use existing roles (for example tertiary/primary vs. error containers). Any extended color must be generated with the M3 tooling and harmonized with the scheme.

## Expressive principles
- **Restraint**: be expressive on hero and primary actions; keep data (amounts, lists, forms) calm and familiar. Google's research warns that banking-style apps need restraint (see M3_EXPRESSIVE.md §1).
- **Motion**: use the theme's `MotionScheme` (expressive, spring-based) for all animation. Spatial specs for movement and size, effects specs for color and alpha. No ad-hoc `tween` specs.
- **Shape**: use the shape scale (now including `largeIncreased`, `extraLargeIncreased`, `extraExtraLarge`). Use `MaterialShapes` and shape morphing only through M3 APIs.
- **Type**: use the M3 type scale (`*Emphasized` variants exist for all 15 styles; use them sparingly) for hierarchy. Key figures such as balances use display or headline styles.
- **Color and emphasis**: express emphasis with container/on-container role pairs, not custom colors.
- **Hero moments** (ADR-010): only (1) the balance overview on Home and (2) the add-transaction flow. Everything else is standard M3.
- **Numbers**: all amounts use tabular figures (`fontFeatureSettings = "tnum"`). Use display or headline styles for hero figures.
- **Elevation**: keep component defaults; build hierarchy with surface-container tiers, not shadows.
- **Corners**: no large or full corners on information-dense cards; nested shapes use optical roundness (inner = outer − padding).
- **Icons** (ADR-010): Material Symbols **Rounded** only, app-wide, as vector drawables from the official repo (see REFERENCES). Navigation items are filled when selected and outline otherwise. Tint always comes from the M3 component; never set a color on an icon.
- **Adaptive**: every screen supports compact, medium, and expanded breakpoints using a canonical layout (transactions → list-detail).

## Component map (Fiscus need → M3 component)
| Need | Component |
|---|---|
| App structure | `Scaffold`, `NavigationSuiteScaffold` (`material3-adaptive-navigation-suite`) / `ShortNavigationBar` / `WideNavigationRail` (use the overload with `contentPadding`; the old one is deprecated) |
| Top bars | `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `TopAppBar` |
| Quick add | `FloatingActionButtonMenu` (+ `ToggleFloatingActionButton`) |
| Primary actions | `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`, `SplitButtonLayout` |
| Period / type selectors | `ButtonGroup` (connected), `ToggleButton`, `SegmentedButton` |
| Filters | `FilterChip`, `InputChip` |
| Lists | `ListItem`, `SegmentedListItem` |
| Containers | `Card`, `ElevatedCard`, `OutlinedCard` |
| Forms | `OutlinedTextField`, `DatePicker`, `TimePicker`, `ExposedDropdownMenuBox` |
| Sheets and dialogs | `ModalBottomSheet`, `AlertDialog` |
| Progress (budgets) | `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` (or the flat versions) |
| Loading | `LoadingIndicator`, `ContainedLoadingIndicator` |
| Feedback | `Snackbar` via `SnackbarHost` |
| Toolbars | `HorizontalFloatingToolbar` / `VerticalFloatingToolbar` |
| Charts | ❓ No M3 component. Decision needed (ask the user) |

## Accessibility
- Minimum 48dp touch targets (M3 defaults provide this; don't shrink them). WCAG AA contrast.
- Amounts get a full `contentDescription` (for example "Expense, 1,250 rupees"). Never rely on color alone.
- Respect font scaling and reduced motion.
- Preview every screen in light, dark, and large-font configurations.
