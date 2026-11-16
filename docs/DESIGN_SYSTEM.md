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
  | Icon / text actions | `IconButton(onClick, shapes = IconButtonDefaults.shapes())`, `TextButton(onClick, shapes = ButtonDefaults.shapes())` (ADR-039) | `IconButton`/`TextButton` without `shapes` |
  | Several related actions | `ButtonGroup` (connected) / `ToggleButton` | a plain `Row` of buttons |
  | Multiple create actions | `FloatingActionButtonMenu` + `ToggleFloatingActionButton` | several separate FABs |
  | Action + options | `SplitButtonLayout` | button + separate menu icon |
  | Short indeterminate wait | `LoadingIndicator` / `ContainedLoadingIndicator` | `CircularProgressIndicator` (indeterminate) |
  | Determinate progress | `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` | flat indicators, unless a calm data context calls for them (ADR) |
  | Lists | `ListItem` content-lambda overload, `SegmentedListItem` | `ListItem(headlineContent = …)` (deprecated) |
  | Search | `SearchBar(state, inputField)` + `ExpandedFullScreenSearchBar` / `ExpandedDockedSearchBar`, `AppBarWithSearch`, `SearchBarDefaults.InputField(textFieldState, searchBarState, …)` | `TopSearchBar` (renamed), query-style `SearchBar`/`DockedSearchBar`, `InputField(query, …)` / `InputField(state, expanded, …)` (all deprecated) |
  | Selectable menu item | `SelectableDropdownMenuItem(selected, onClick, text, shapes = MenuDefaults.itemShape(i, n))` | `DropdownMenuItem(selected, …)` (deprecated, hidden) |
  | Sheet state | `rememberBottomSheetState(initialValue = Hidden, enabledValues = …)` | `rememberModalBottomSheetState(...)` (deprecated) |
  | Filter chips | `FilterChip(selected, onClick, label, shapes = FilterChipDefaults.shapes(), …)` | `FilterChip` without `shapes` |
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
- **Writing our own design tokens or copying library-internal ones** (e.g. material3's `internal MotionTokens` easing/durations). If the library doesn't expose it publicly, use the library default or ask the user.
- Screen-transition specs other than the app's `pageCrossFade` (in `FiscusApp`) and `sharedContainer`, or any transition with numeric timings (`tween`, durations). Both run only on `MaterialTheme.motionScheme` springs (ADR-030).
- Scale-based enter/exit animations (M3 transition patterns avoid scale).
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
- Layout spacing: `ui/theme/Layout.kt` → `FiscusLayout` (ADR-020). The only place for spacing/size constants.
- **Contrast check for custom text-on-container colors:** compute WCAG contrast in **all six** brand schemes (standard, medium, high × light, dark), since role pairs are only guaranteed in their intended combination. `onSurfaceVariant` on tertiary/secondary containers drops to 1.8–2.3:1 at medium/high contrast (measured 2026-11-15), so it must not be used there.
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

## Shared components (`ui/components/`)
**Check this list before building any UI.** If what you need is here, use it. If you write a composable that isn't tied to one feature, put it in `ui/components/` right away (public, documented, previews in light, dark and large font), and add it here. Features never copy each other's composables (ARCHITECTURE.md).
| Component | Use for | Notes |
|---|---|---|
| `AddTransactionFabMenu` | Quick add (Expense / Income / Transfer) | Home and Transactions only (ADR-018) |
| `DockedDatePickerField(date, onDateSelected, label)` | Picking one date inside a form | Docked `DatePicker` in a `Popup`, expands on the theme springs; container uses `DatePickerDefaults.colors().containerColor` + `DatePickerDefaults.TonalElevation` (no app-made elevation, ADR-039); UTC conversion is inside |
| `SingleChoiceToggleGroup(title, options, selectedKey, key, label, onSelect, emptyMessage, icon?)` | One choice from an open-ended list (account, category, account type) | `ToggleButton`s in a wrapping `FlowRow`, radio semantics. For 2–5 fixed options, use the connected button group |
| `CurrencyPrefixTransformation(symbol)` | Showing a currency symbol in an amount field | Display only; works with centered text, unlike `prefix` |
| `categoryIconRes(iconKey)` | Category icon drawable | Unknown keys fall back to "category" |
| `ShapeAccentIcon(icon, containerColor?, contentColor?, size?, iconSize?)` | The one decorative shape accent (icon in a `MaterialShapes.Cookie9Sided`) | Pass a matching role pair; accents only, never data containers. Empty states: `FiscusLayout.LargeAccentSize` / `LargeAccentIconSize` |
| `ActionButtonGroup(actions: List<GroupAction>)` | **Every page's actions** (Save; Delete/Edit; Clear/Done) | One Expressive `ButtonGroup`, medium buttons; `ActionStyle.Primary/Secondary/Danger`; loading state (ADR-028). Never a lone `Button` row |
| `Modifier.sharedContainer(key)` + `transactionContainerKey(id)` | An item that opens its own page | Grows into the page and shrinks back (`sharedBounds`, bounds on `slowSpatialSpec`, fades on effects specs; ADR-028/030); no-op outside `NavDisplay` |
| `EmptyState(icon, message, action?)` | **Every page's empty state** (nothing yet, no matches, search hint, placeholder pages) | Large `ShapeAccentIcon` + centered `bodyLarge` message + optional action (ADR-031). Never a bare `Text` |
| `AmountText(text, style, textAlign?, icon?, color?)` | Any amount on a card or hero | One line; steps down from `style`'s size instead of wrapping (ADR-027). Tabular figures. Optional direction arrow inline before it at `1.em` (ADR-032) |
| `TransactionType.heroRoles()` | The type's hero-card container pair (expense tertiaryContainer, income secondaryContainer, transfer surfaceContainerHighest) | Editor and detail heroes (ADR-024/025) |
| `containedSegmentedColors()` | `SegmentedListItem`s on the screen background | `surfaceContainer` instead of the default `surface`, so groups are visibly contained (ADR-025). Not inside sheets |
| `TransactionType.accentRoles()` | The type's accent role pair (expense tertiary, income secondary, transfer primary) | The only type → accent mapping (editor hero, list, summary; ADR-023) |
| `LocalSnackbarHostState` | Showing confirmations | `snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) }`; provided by `FiscusApp` (ADR-020) |
| `TransactionTypeDisplayOrder` | Any list of transaction types | Expense, Income, Transfer (ADR-020) |
| `TransactionType.labelRes` / `.iconRes` | Type label and icon | |
| `TransactionListItem(transaction, supportingText, icon, onClick, shapes)` | One transaction in a list | `SegmentedListItem`; pass `ListItemDefaults.segmentedShapes(index, count)`; contained rows, `titleMedium` headline, the amount after its type's arrow (no −/+, ADR-026) with an optional `caption` (time) below; optional `highlight` emphasizes the search match; icon in a `ShapeAccentIcon` with the type's `accentRoles()` (ADR-023), signed tabular amount, TalkBack reads "Expense, ₹450.00" (ADR-022) |
| `signedAmountText(type, amount)` / `signedNetText(net)` | TalkBack text for an amount ("−₹450.00", "+₹50,000.00") | Sign from `strings.xml`; transfers and zero unsigned. Not drawn: on screen amounts show an arrow (ADR-032) |
| `netIconRes(net)` / `netAmountText(net)` | A net shown on screen | Arrow by direction (↓ negative, ↑ positive, none at zero) and the unsigned amount (ADR-032) |
| `PeriodTotal(type, amount, labelColor?)` | A period's income or expense total | Type arrow in a `ShapeAccentIcon` (accent roles), `labelMedium` label, `titleMedium` tabular amount. Default label `onSurfaceVariant` only on surface-family containers; elsewhere pass the container's on-color (Home tiles, ADR-034) |
| `transactionSupportingText(entry)` | The supporting line of a `TransactionListItem` | "Food · Cash", or "Cash → Bank" for transfers; "Unknown" for removed refs |
| `AccountType.iconRes` | Account type icon | Cash `payments`, Bank `account_balance`, Card `credit_card`, Wallet `account_balance_wallet` (ADR-033) |
| `DetailRow(icon, label, value, shapes)` | One labelled fact on a detail page | Contained `SegmentedListItem`, label as overline, value `titleMedium` tabular (transaction and account pages) |
| `SwitchRow(label, checked, onCheckedChange, supportingText?, shapes?)` | An on/off setting in a form | Contained toggleable `SegmentedListItem(checked, onCheckedChange, shapes)` with an M3 `Switch` (no click of its own) as trailing content, `Role.Switch`; the thumb shows the rounded check icon (`SwitchDefaults.IconSize`) when on. Rows of one form are one segmented group (`segmentedShapes(index, count)`, `SegmentedGap`) (in debt + default account; ADR-039) |
| `DayHeader(date, today, net)` | The header above one day of transactions | `dayLabel`, Today emphasized, net after its arrow; a heading (Transactions, account page) |
| `ConnectedChoiceGroup(options, selected, onSelect, label, icon?)` | One choice from **2–5 fixed options** (transaction type, account type, balance mode) | M3 Expressive connected `ToggleButton` row, radio semantics, equal widths (ADR-018 3b, ADR-036). Open-ended lists use `SingleChoiceToggleGroup` |
| `SaveHint(hint)` | Why a form's Save is disabled, above the action group | Expands/collapses vertically on the theme springs, polite live region |
| `accountContainerKey(id)` | An account card/row and its page | With `sharedContainer` (ADR-036) |
| `AccountType.labelRes` / `AccountTypeDisplayOrder` | Account type label and order | Cash, Bank, Card, Wallet |
| `dayLabel(date, today)` | Day headers | "Today", "Yesterday", else the localized full date; `today` from the injected clock |

Kept inside a feature on purpose: the editor's `AmountHero` (hero moment 2, type colors). Extract a plain shared `AmountField` when a second screen (budgets, opening balance) needs amount input.

## Built-in motion (what each component animates by itself, checked in the 1.5.0-rc01 sources)
Reference only: what each component animates by itself. Hand-animating something a component already does is forbidden.
| Component | Built-in motion |
|---|---|
| Standard `ButtonGroup` items (`clickableItem`, `toggleableItem`; `customItem` + `Modifier.animateWidth`) | Pressed item **widens by `ExpandedRatio` (15%)** and neighbours compress, on the theme's fast spatial spring. The most visible Expressive press motion. |
| `ToggleButton` (alone or in a `Row`/`FlowRow`, including the "connected" sample) | Corner shape changes on press and checked, plus a colour change. **No width or size motion.** |
| Expressive `Button(shapes = …)` | Corner shape changes on press. |
| `FloatingActionButtonMenu` + `ToggleFloatingActionButton` | Container morph, items stagger in, icon rotates (`animateIcon`). |
| `FilterChip(shapes = …)` | Subtle corner change on select. |
| `LoadingIndicator` | Shape-morphing polygons. |
| Flexible top app bars | Collapse on scroll (`exitUntilCollapsedScrollBehavior`). |
| `HorizontalMultiBrowseCarousel` / `HorizontalUncontainedCarousel` | Multi-browse masks and resizes items (large/medium/small) as they scroll; uncontained keeps one `itemWidth` and scrolls freely (no snap by default). |
| `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` (determinate) | The wave's amplitude follows progress (`indicatorAmplitude`); it moves at one wavelength per second. |

## Component map (Fiscus need → M3 component)
| Need | Component |
|---|---|
| App structure | `Scaffold`, `NavigationSuiteScaffold` (`material3-adaptive-navigation-suite`) / `ShortNavigationBar` / `WideNavigationRail` (use the overload with `contentPadding`; the old one is deprecated) |
| Top bars | `MediumFlexibleTopAppBar`, `LargeFlexibleTopAppBar`, `TopAppBar` |
| Quick add | `FloatingActionButtonMenu` (+ `ToggleFloatingActionButton`) |
| Primary actions | `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`, `SplitButtonLayout` |
| Period / type selectors | `ButtonGroup` (connected), `ToggleButton`, `SegmentedButton` |
| Single choice from an open-ended list (account, category) | `ToggleButton` (icon + label) in a wrapping `FlowRow`, `Role.RadioButton` semantics (ADR-018) |
| Filters | `FilterChip` (with `shapes`), `InputChip` |
| Search | Collapsed `SearchBar` + `ExpandedFullScreenSearchBar` (ADR-022) |
| Lists | `ListItem`, `SegmentedListItem` |
| Containers | `Card`, `ElevatedCard`, `OutlinedCard` |
| Forms | `OutlinedTextField`, `DatePicker`, `TimePicker`, `ExposedDropdownMenuBox` + `SelectableDropdownMenuItem`, `SwitchRow` (in debt, default account) |
| A list page's create action | `ExtendedFloatingActionButton(text, icon, onClick, expanded)` (Accounts; collapses on scroll) |
| Sheets and dialogs | `ModalBottomSheet`, `AlertDialog` |
| Browsing peers (accounts) | `HorizontalUncontainedCarousel(itemWidth)` + `rememberCarouselState`, items clipped with `maskClip`; one width for every card (ADR-037) |
| Progress (budgets, Home spent ring) | `LinearWavyProgressIndicator` / `CircularWavyProgressIndicator` (or the flat versions) |
| Loading | `LoadingIndicator`, `ContainedLoadingIndicator` |
| Feedback | `Snackbar` via `SnackbarHost` |
| Toolbars | `HorizontalFloatingToolbar` / `VerticalFloatingToolbar` |
| Charts | ❓ No M3 component. Decision needed (ask the user) |

## Accessibility
- Minimum 48dp touch targets (M3 defaults provide this; don't shrink them). WCAG AA contrast.
- Amounts get a full `contentDescription` (for example "Expense, 1,250 rupees"). Never rely on color alone.
- Respect font scaling and reduced motion.
- Preview every screen in light, dark, and large-font configurations.
