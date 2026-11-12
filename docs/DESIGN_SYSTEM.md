# Design System: Material 3 Expressive (strict)

**Rule:** Fiscus uses only Material 3 Expressive design and only components from `androidx.compose.material3` (plus `material3-adaptive*` and `material-icons` / Material Symbols). Facts and API status are in [REFERENCES.md](REFERENCES.md).

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
- An expressive theme setup in `ui/theme/` (`Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`). Use `MaterialExpressiveTheme` with `motionScheme = MotionScheme.expressive()` (experimental opt-in is allowed).
- Dynamic color on API 31+, with a brand fallback scheme for light and dark, generated from M3 tonal palettes (Material Theme Builder).
- Income/expense colors use existing roles (for example tertiary/primary vs. error containers). Any extended color must be generated with the M3 tooling and harmonized with the scheme.

## Expressive principles
- **Motion**: use the theme's `MotionScheme` (expressive, spring-based) for all animation. No ad-hoc `tween` specs.
- **Shape**: use the shape scale. Use `MaterialShapes` and shape morphing only through M3 APIs.
- **Type**: use the M3 type scale (and emphasized variants where M3 provides them) for hierarchy. Key figures such as balances use display or headline styles.
- **Color and emphasis**: express emphasis with container/on-container role pairs, not custom colors.

## Component map (Fiscus need → M3 component)
| Need | Component |
|---|---|
| App structure | `Scaffold`, `NavigationSuiteScaffold` / `ShortNavigationBar` / `WideNavigationRail` |
| Top bars | `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `TopAppBar` |
| Quick add | `FloatingActionButtonMenu` (+ `ToggleFloatingActionButton`) |
| Primary actions | `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`, `SplitButton` |
| Period / type selectors | `ButtonGroup` (connected), `ToggleButton`, `SegmentedButton` |
| Filters | `FilterChip`, `InputChip` |
| Lists | `ListItem` (expressive list items) |
| Containers | `Card`, `ElevatedCard`, `OutlinedCard` |
| Forms | `OutlinedTextField`, `DatePicker`, `TimePicker`, `ExposedDropdownMenuBox` |
| Sheets and dialogs | `ModalBottomSheet`, `AlertDialog` |
| Progress (budgets) | `LinearProgressIndicator` / `CircularProgressIndicator` (wavy variants) |
| Loading | `LoadingIndicator`, `ContainedLoadingIndicator` |
| Feedback | `Snackbar` via `SnackbarHost` |
| Toolbars | `FloatingToolbar` |
| Charts | ❓ No M3 component. Decision needed (ask the user) |

## Accessibility
- Minimum 48dp touch targets (M3 defaults provide this; don't shrink them). WCAG AA contrast.
- Amounts get a full `contentDescription` (for example "Expense, 1,250 rupees"). Never rely on color alone.
- Respect font scaling and reduced motion.
- Preview every screen in light, dark, and large-font configurations.
