# Architecture

> Follows Google's official guide (see [REFERENCES.md](REFERENCES.md#architecture-checked-2026-11-12)). Library picks are **proposed**.

## Shape
- **Single activity** (`MainActivity`) using **Navigation 3**.
- **MVVM + unidirectional data flow**: UI sends events to the `ViewModel`, the ViewModel exposes `StateFlow<UiState>`, and the UI renders that state.
- **Offline-first**: local DB is the source of truth. No backend for now.

## Layers
| Layer | Responsibility | Depends on |
|---|---|---|
| `ui` | Screens, components, ViewModels, navigation, theme | domain |
| `domain` | Models; use cases *only when logic is complex or reused* (pure Kotlin) | — |
| `data` | Room DB, DAOs, DataStore, repository impls | domain |

## Package layout
```
com.prajwalpawar.fiscus
├── FiscusApp.kt            # Application (Hilt entry)
├── MainActivity.kt
├── core/                   # money formatting, dates, utils
├── data/
│   ├── db/                 # Room database, entities, DAOs
│   ├── prefs/              # DataStore
│   └── repository/
├── domain/
│   ├── model/
│   └── usecase/
└── ui/
    ├── theme/              # Color, Type, Shape, Motion, Theme
    ├── components/         # shared composables
    ├── navigation/
    └── feature/<name>/     # Screen + ViewModel + UiState per feature
```
Start as one module; split into `:core`, `:feature:*` only if build times or boundaries demand it.

## Planned libraries (proposed)
- Room (KSP): persistence
- Hilt: DI
- Navigation 3 (official recommendation)
- DataStore Preferences: settings
- kotlinx-datetime, kotlinx-coroutines
- Charts: **open question.** M3 has no chart component; ask the user (see DESIGN_SYSTEM.md)

## Data rules
- Amounts: `Long` minor units plus ISO-4217 currency code.
- Timestamps: UTC epoch millis in DB; convert at the UI edge.
- Repositories expose `Flow`; one-shot writes are `suspend`.

## ViewModel rules (official)
- Extend plain `ViewModel`, never `AndroidViewModel`. Don't reference Context, Activity, or Resources.
- Use ViewModels at screen level only. Expose `val uiState: StateFlow<XxxUiState>` built with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`.
- Model one-off outcomes as state (for example `userMessage: String?` that the UI clears), not as event channels.
- Repositories are injected through the constructor (Hilt). Use one repository per data type; the UI never touches DAOs.
