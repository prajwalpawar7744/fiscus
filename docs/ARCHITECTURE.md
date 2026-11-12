# Architecture

> Follows Google's official guide (see [REFERENCES.md](REFERENCES.md#architecture-checked-2026-11-12)). Accepted in ADR-007. Library versions are pinned in the version catalog (ADR-011).

## Shape
- **Single activity** (`MainActivity`) using **Navigation 3**.
- **MVVM + unidirectional data flow**: UI sends events to the `ViewModel`, the ViewModel exposes `StateFlow<UiState>`, and the UI renders that state.
- **Offline-first**: local DB is the source of truth. No backend for now.

## Layers
| Layer | Responsibility | Depends on |
|---|---|---|
| `ui` | Screens, components, ViewModels, navigation, theme | domain (if present) and data |
| `domain` (optional) | Use cases *only when logic is complex or reused* | data |
| `data` | Repositories (the public API), Room DB/DAOs and DataStore as data sources | `model` |
| `model` | Plain Kotlin models shared by all layers (like Now in Android's `core:model`) | — |

Dependencies point **downward, as in Google's guide**: UI → domain → data. A ViewModel may call a repository directly when no use case is needed. Repositories expose models, never Room entities.

## Package layout
```
com.prajwalpawar.fiscus
├── FiscusApp.kt            # Application (Hilt entry)
├── MainActivity.kt
├── core/                   # money formatting, dates, utils
├── model/                  # shared plain Kotlin models
├── data/
│   ├── db/                 # Room 3 database, entities, DAOs
│   ├── prefs/              # DataStore
│   └── repository/
├── domain/                 # optional use cases only
│   └── usecase/
└── ui/
    ├── theme/              # Color, Type, Shape, Motion, Theme
    ├── components/         # shared composables
    ├── navigation/
    └── feature/<name>/     # Screen + ViewModel + UiState per feature
```
Start as one module; split into `:core`, `:feature:*` only if build times or boundaries demand it.

**Scalability rules (keep the codebase ready to modularize):**
- Organize by **feature**, not by type. Each feature lives in `ui/feature/<name>/` and owns its Screen, ViewModel, and UiState.
- Features never import from each other. Shared code goes in `core/`, `model/`, `domain/`, `data/`, or `ui/components/`.
- Navigation between features goes only through `ui/navigation/` routes.
- The reference for a future split into modules with `build-logic/` convention plugins is Google's [Now in Android](https://github.com/android/nowinandroid) app. Any split needs an ADR first.

## Libraries (ADR-011)
- Room 3 (`androidx.room3`, KSP) with the bundled SQLite driver: persistence. Schemas are exported to `app/schemas/`
- Hilt (+ `hilt-lifecycle-viewmodel-compose` for `hiltViewModel()`): DI
- Navigation 3 (official recommendation) + `lifecycle-viewmodel-navigation3` for ViewModels scoped to back stack entries
- DataStore Preferences: settings
- kotlinx-serialization (route keys), kotlinx-datetime, kotlinx-coroutines
- Testing: JUnit 4, kotlinx-coroutines-test, Turbine
- Charts: **open question.** M3 has no chart component; ask the user (see DESIGN_SYSTEM.md)

## Data rules
- Amounts: `Long` minor units plus ISO-4217 currency code.
- Timestamps: UTC epoch millis in DB; convert at the UI edge.
- Repositories expose `Flow`; one-shot writes are `suspend`.
- Every repository is an **interface** (`TransactionRepository`) with an implementation (`OfflineFirstTransactionRepository`), bound with Hilt. Tests use **fakes** of the interface, not mocks.
- Data and domain code is **main-safe**: it switches to an **injected** `CoroutineDispatcher` (via a Hilt qualifier), never a hardcoded `Dispatchers.IO`.
- Entities map to models in the data layer (`TransactionEntity.asModel()`).

## ViewModel rules (official)
- Extend plain `ViewModel`, never `AndroidViewModel`. Don't reference Context, Activity, or Resources.
- Use ViewModels at screen level only. Expose `val uiState: StateFlow<XxxUiState>`. When state comes from repository streams, use `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`. For simple local state, a private `MutableStateFlow` exposed as a `StateFlow` is acceptable.
- Use cases are named verb + noun + `UseCase` (`GetMonthlySummaryUseCase`), expose `operator fun invoke` (overloads allowed), and hold no mutable data.
- Model one-off outcomes as state (for example `userMessage: String?` that the UI clears), not as Channels/SharedFlow. Navigation is a UI concern: the screen calls a navigation lambda passed in by the nav graph.
- Repositories are injected through the constructor (Hilt). Use one repository per data type, even when it wraps a single data source. The UI never touches DAOs.
- Keep lifecycle types out of ViewModels. In Compose, use `LifecycleStartEffect`/`LifecycleResumeEffect` instead of Activity callbacks.

## Testing (official)
- Prefer fakes to mocks. Test ViewModels (assert on `uiState.value`), repositories, data sources, and navigation.

## Performance (official, strongly recommended)
- Release builds use R8 in full mode.
- Generate Baseline Profiles and Startup Profiles, measured with Macrobenchmark (planned for M5).
- Status: R8 is **not on yet** (`isMinifyEnabled = false`). It will be enabled together with release signing (ROADMAP M0).

## Which rules are Google's vs ours
Verified 2026-11-13 against the developer.android.com architecture pages (UI, events, data, domain, recommendations, modularization).
- **Google**: layers and optional domain, dependency direction, UDF, ViewModel rules, state over events, lifecycle-aware collection, single activity with Navigation 3, one repository per type, main-safety, fakes over mocks, performance items above.
- **Team conventions, consistent with Google** (Google shows them in examples but doesn't require them): repository interfaces, the `OfflineFirst*` naming, dispatchers injected via a Hilt qualifier, the shared `model/` package (from Now in Android), `XxxRoute`/`XxxScreen`, `onEvent`.

## Navigation (ADR-015)
- Keys: one `@Serializable data object XxxNavKey : NavKey` per destination in `ui/navigation/FiscusNavKeys.kt`.
- `TopLevelDestination` (enum) lists the tabs: Home, Transactions, Budgets, Insights. Settings is a **pushed** destination, opened from the top-bar icon.
- `FiscusNavigator` (plain class, unit-tested) owns the back-stack rules: Home is always the root; a tab gives `[Home, tab]`; pushed screens go on top; Back never removes Home.
- `ui/FiscusApp.kt` hosts `NavigationSuiteScaffold` (hidden on pushed screens) around `NavDisplay`, with entry decorators `rememberSaveableStateHolderNavEntryDecorator()` and `rememberViewModelStoreNavEntryDecorator()` (each screen gets its own ViewModel scope).
- Every destination is registered in the `entryProvider` in `FiscusApp.kt`. Screens never touch the navigator; they receive navigation lambdas.

## Feature pattern: copy this for every feature
`ui/feature/<name>/`, using `home/` as the reference:
| File | Contents |
|---|---|
| `XxxUiState.kt` | immutable `data class` with defaults |
| `XxxViewModel.kt` | `@HiltViewModel`, `@Inject constructor(...)`, exposes `val uiState: StateFlow<XxxUiState>` |
| `XxxRoute.kt` | stateful: `hiltViewModel()`, `collectAsStateWithLifecycle()`, navigation lambdas in, calls `XxxScreen` |
| `XxxScreen.kt` | stateless: `Scaffold` + `LargeFlexibleTopAppBar` + content; previews in light, dark, and large font |
Tests: `test/.../ui/feature/<name>/XxxViewModelTest.kt` using `MainDispatcherRule` and Turbine.
