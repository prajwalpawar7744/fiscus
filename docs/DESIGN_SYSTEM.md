# Design System: Material 3 Expressive (strict)

**Rule:** Fiscus uses only Material 3 Expressive design and only components from `androidx.compose.material3` (plus `material3-adaptive*` and `material-icons` / Material Symbols). Facts and API status are in [REFERENCES.md](REFERENCES.md). The design intent and look & feel are in [M3_EXPRESSIVE.md](M3_EXPRESSIVE.md); run its checklist for every screen.

## What is allowed vs. forbidden
✅ Allowed
- Any component from `androidx.compose.material3`, including **all** Expressive APIs, even experimental ones (ADR-006). Opt in at file or function level.
- Prefer the Expressive variant whenever one exists (for example `FloatingActionButtonMenu` over a plain FAB, `ButtonGroup` over plain button rows, flexible top app bars, `LoadingIndicator`).
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
- An expressive theme setup in `ui/theme/` (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`). Use `MaterialExpressiveTheme` with `motionScheme = MotionScheme.expressive()` (both are stable in 1.5.0-rc01, see REFERENCES).
- Dynamic color on API 31+, with a brand fallback scheme for light and dark, generated from M3 tonal palettes (Material Theme Builder).
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
- **Icons** (ADR-010): Material Symbols **Rounded** only, app-wide.
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
