# Decision Log

Format: `## ADR-NNN: Title`, then Date, Status, Context, Decision, Consequences. Newest at the bottom.

## ADR-001: Jetpack Compose with Material 3 Expressive
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: A modern, expressive Android UI is a core goal.
- **Decision**: Compose-only UI using the material3 expressive APIs.
- **Consequences**: Some APIs are experimental and need opt-ins. material3 is pinned separately from the BOM (ADR-006).

## ADR-002: Single module to start
- **Date**: 2026-11-12 · **Status**: Accepted (2026-11-13)
- **Context**: Early-stage solo project.
- **Decision**: Keep `:app` only, with package-level layering. Split later if needed.
- **Consequences**: Faster iteration; layer boundaries are kept by convention, not by the compiler. The feature-first scalability rules in ARCHITECTURE.md keep a future split cheap.

## ADR-003: Money as Long minor units
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: Floating-point causes rounding errors in currency math.
- **Decision**: Store and compute amounts as `Long` minor units with a currency code.
- **Consequences**: Formatting must account for each currency's fraction digits.

## ADR-004: Strict M3 Expressive-only UI
- **Date**: 2026-11-12 · **Status**: Accepted (user guideline)
- **Context**: The user requires a consistent M3 Expressive look with no custom or third-party components.
- **Decision**: Use only `androidx.compose.material3` components. No third-party or custom look-alike components. Any gap is escalated to the user.
- **Consequences**: Charts and other gaps need an explicit decision.

## ADR-005: Semantic versioning
- **Date**: 2026-11-12 · **Status**: Accepted (user guideline)
- **Context**: Releases need predictable version numbers, and Play requires an increasing versionCode.
- **Decision**: SemVer for versionName; versionCode = MAJOR*10000 + MINOR*100 + PATCH. See VERSIONING.md.

## ADR-006: material3 version for Expressive APIs
- **Date**: 2026-11-12 · **Status**: Accepted (user decision)
- **Context**: Stable 1.4.0 lacks or gates many Expressive APIs. The user wants every Expressive component and accepts experimental, bleeding-edge versions.
- **Decision**: Use the **newest available `androidx.compose.material3` release (alpha, beta, or RC)**, pinned explicitly in the version catalog and overriding the BOM. Opting into experimental APIs (`ExperimentalMaterial3ExpressiveApi`, `ExperimentalMaterial3Api`) is allowed.
- **Consequences**: APIs may be renamed or changed between versions. Check the release notes on each bump and record the changes in REFERENCES.md. Put opt-ins at file or function level, never module-wide.

## ADR-007: Architecture follows Google's official guide
- **Date**: 2026-11-13 · **Status**: Accepted (user guideline)
- **Context**: The user requires a professional, scalable, standard architecture.
- **Decision**: Use Google's layering (UI → optional domain → data), UDF, StateFlow UI state, Hilt, Room, Navigation 3, and offline-first. Code is organized by feature. Team conventions on top of this are listed in ARCHITECTURE.md. Verified against developer.android.com on 2026-11-13.
- **Consequences**: Feature work must follow ARCHITECTURE.md. Deviations need a new ADR.

## ADR-008: compileSdk 37
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: material3 1.5.x and core-ktx 1.19.0 require minCompileSdk 37 (AAR metadata). AGP 9.2.1 is tested up to 37.0, not 37.1.
- **Decision**: `compileSdk = release(37)`. targetSdk stays 36 for now.
- **Consequences**: SDK Platform 37 must be installed. Raising targetSdk to 37 is a separate task, because it changes runtime behavior.

## ADR-009: Build variants, output naming, and Gradle caching
- **Date**: 2026-11-13 · **Status**: Accepted
- **Decision**: Debug gets the `.debug` app ID suffix and the `-debug` version suffix; release has none. `base.archivesName = "fiscus-v<versionName>"` gives files named `fiscus-vX.Y.Z-<buildType>.apk`. Gradle build cache, configuration cache, and parallel builds are on.
- **Consequences**: Debug and release install side by side. Unsigned release builds get an `-unsigned` suffix until signing is configured. Build logic must stay compatible with the configuration cache.

## ADR-010: Hero moments and icon style
- **Date**: 2026-11-13 · **Status**: Accepted (user: "go with M3 Expressive")
- **Context**: M3 Expressive says to "stick to one or two hero moments", chosen by asking whether an interaction is emotionally impactful and whether it is a key interaction. M3 also says shapes and type should be "in harmony", and M3 shapes echo the roundness of Google Sans Flex.
- **Decision**:
  1. Hero moments: (a) the **balance overview on Home**, the key information users come for; (b) the **add-transaction flow** (FAB menu, form, satisfying save), the most frequent and important interaction. Everything else uses standard M3 patterns.
  2. Icons: **Material Symbols Rounded**, app-wide, to match the soft, rounded M3 Expressive shapes and type. (The style choice is our reading of the shape/type harmony principle. M3 itself allows outlined, rounded, or sharp.)
- **Consequences**: New hero treatments elsewhere need a new ADR. All icons use the Rounded style; how they are included in Compose is decided when the first icons are added (check REFERENCES first).

## ADR-011: Core dependencies and toolchain upgrade
- **Date**: 2026-11-13 · **Status**: Accepted
- **Context**: M0 needs DI, persistence, navigation, and settings. The template's Kotlin 2.2.10 was too old for current libraries (kotlinx-serialization 1.13 needs Kotlin 2.4; Hilt 2.60 is built with Kotlin 2.3). AGP 9.2.1 only supports compileSdk up to 37.0.
- **Decision**: Use the latest **stable** release of each library (material3 stays on its pre-release per ADR-006). Upgrade the toolchain to AGP 9.5.1, Gradle 9.9.1 (wrapper checksum verified against gradle.org), Kotlin 2.4.21, and KSP 2.3.13. Use **Room 3** (`androidx.room3`, stable 3.0.3) instead of Room 2.x, since it is Google's current major version, with `sqlite-bundled`. Annotation processing uses KSP only (no kapt).
- **Consequences**: Room 3 uses the `androidx.room3` package and plugin, so tutorials written for Room 2 need adapting. Toolchain upgrades are checked with a full build plus tests.

## ADR-012: Theme: brand colors, generation, dynamic color
- **Date**: 2026-11-13 · **Status**: Accepted (user chose the key colors and dynamic-by-default)
- **Context**: Material3 has no seed-to-scheme API at runtime, and M3 requires role-based colors that work in light, dark, and three contrast levels.
- **Decision**: Brand key colors are primary **#DEDACD** and secondary **#228B22**. Tertiary, neutral, and neutral-variant come from the TonalSpot variant of the primary. Schemes are generated with Google's `@material/material-color-utilities` 0.4.0 (the Material Theme Builder engine, color spec 2021) into `ui/theme/Color.kt`: six schemes (light/dark × standard/medium/high contrast). Dynamic color is the default on Android 12+, with a "use brand colors" toggle in Settings (M5). Type and shape stay at M3 defaults.
- **Consequences**: #DEDACD has very low chroma, so `primary` renders as a warm grey (#605F54 light, #CAC6BA dark) and the green secondary carries most of the color. If more color is wanted on primary actions, switch to the fidelity variant or pick a more saturated primary key, then regenerate. Never hand-edit Color.kt.

## ADR-013: Expressive-first, no deprecated APIs
- **Date**: 2026-11-13 · **Status**: Accepted (user guideline)
- **Context**: material3 ships both older components and their Expressive successors, so "prefer" left room for inconsistency.
- **Decision**: When an Expressive component or overload exists, using it is mandatory, and the older equivalent is forbidden. Deprecated APIs are never used. The old → new mapping lives in DESIGN_SYSTEM.md and is extended whenever a new pair is found.
- **Consequences**: Deprecation warnings must be fixed immediately. A non-Expressive choice (for example flat progress in a dense data view) needs its own ADR.

## ADR-014: Use the device's font
- **Date**: 2026-11-13 · **Status**: Accepted (user decision)
- **Decision**: No custom or bundled typeface. Material's default `Typography()` uses `FontFamily.SansSerif` for both its brand and plain typefaces (verified in material3 1.5.0-rc01 `TypefaceTokens`), which is the device's system font, so it is kept as-is.
- **Consequences**: Never set `fontFamily` on text. The app's text follows the user's device font (including OEM font styles).

