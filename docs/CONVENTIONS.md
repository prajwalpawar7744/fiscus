# Conventions

> Base: [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide). **Consistency rule:** every new file must mirror the structure of existing files of the same kind.

## Kotlin
- Android Kotlin style guide: 4-space indent, 100-column limit, no wildcard imports, trailing commas.
- Prefer `val`, immutable data classes, `sealed interface` for UI events and states.
- No `!!`. Handle nulls explicitly.
- Coroutines: `viewModelScope`; inject dispatchers, never hardcode `Dispatchers.IO` in logic.
- Experimental opt-ins (`@OptIn(...)`) go at function or file level, never module-wide (ADR-006).

## Compose
- Each screen has a stateful `XxxRoute(viewModel)` and a stateless `XxxScreen(state, onEvent)`.
- `modifier: Modifier = Modifier` is the first optional parameter and is applied to the root.
- Hoist state; composables don't touch repositories.
- Collect flows with `collectAsStateWithLifecycle()`.
- Every public composable (screens and shared components) has `@Preview`s in light, dark and large font (`fontScale = 2f`), wrapped in `FiscusTheme(dynamicColor = false)`.
- UI is built **only from M3 components** (see DESIGN_SYSTEM.md). No custom look-alikes.
- **Expressive-first**: use the Expressive component when one exists (mapping in DESIGN_SYSTEM.md). Zero deprecation warnings are allowed; treat `w: … is deprecated` as a build failure.
- Text lives in `strings.xml` (including signs such as "−%1$s"). Colors, type, shapes and motion come from `MaterialTheme`; layout spacing comes from `FiscusLayout` (ADR-020). Never define per-file spacing constants.

## Naming
- Screens: `XxxRoute` (stateful) and `XxxScreen` (stateless). ViewModels `XxxViewModel`, state `XxxUiState`, events `XxxEvent`.
- Data: repository interface `XxxRepository`, implementation `OfflineFirstXxxRepository`. Room: `XxxEntity`, `XxxDao`. Mappers: `XxxEntity.asModel()`.
- Use cases: verb + noun + `UseCase` (`GetMonthlySummaryUseCase`).
- Models in `model/` carry no suffix (`Transaction`).
- Navigation: `XxxNavKey` (`@Serializable data object`), tabs in `TopLevelDestination`.
- Icons: `res/drawable/ic_<symbol_name>_rounded.xml` (outline) and `ic_<symbol_name>_rounded_filled.xml` (filled), the official Material Symbols Rounded drawables with the AppCompat `android:tint` line removed.

## Testing
- Unit tests for use cases, ViewModels and money math (JUnit + coroutines-test). Prefer fakes to mocks.
- ViewModel state (ARCHITECTURE.md): start a collector in `backgroundScope` with `UnconfinedTestDispatcher` (needed for `stateIn(WhileSubscribed)`), advance with `advanceUntilIdle()`, then assert on `uiState.value`. Use Turbine (`test { awaitItem() }`) only when the sequence of emissions is what's being tested.
- Compose UI tests for critical flows (add transaction, budget).
- Name tests with backticks: `` `adding expense reduces balance` ``.

## Git
- Conventional Commits with a **required scope**: `type(scope): summary`, e.g. `feat(transactions): add edit screen`, `chore(build): bump material3`. Types: feat, fix, refactor, docs, chore, test, build, ci, perf, style.
- Small, focused commits.
- Releases follow [VERSIONING.md](VERSIONING.md).
