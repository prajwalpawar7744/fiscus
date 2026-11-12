# Decision Log

Format: `## ADR-NNN: Title`, then Date, Status, Context, Decision, Consequences. Newest at the bottom.

## ADR-001: Jetpack Compose with Material 3 Expressive
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: A modern, expressive Android UI is a core goal.
- **Decision**: Compose-only UI using the material3 expressive APIs.
- **Consequences**: Some APIs are experimental and need opt-ins; the Compose BOM must stay current.

## ADR-002: Single module to start
- **Date**: 2026-11-12 · **Status**: Proposed
- **Context**: Early-stage solo project.
- **Decision**: Keep `:app` only, with package-level layering. Split later if needed.
- **Consequences**: Faster iteration; layer boundaries are kept by convention, not by the compiler.

## ADR-003: Money as Long minor units
- **Date**: 2026-11-12 · **Status**: Accepted
- **Context**: Floating-point causes rounding errors in currency math.
- **Decision**: Store and compute amounts as `Long` minor units with a currency code.
- **Consequences**: Formatting must account for each currency's fraction digits.

## ADR-004: Strict M3 Expressive-only UI
- **Date**: 2026-11-12 · **Status**: Accepted (user guideline)
- **Decision**: Use only `androidx.compose.material3` components. No third-party or custom look-alike components. Any gap is escalated to the user.
- **Consequences**: Charts and other gaps need an explicit decision.

## ADR-005: Semantic versioning
- **Date**: 2026-11-12 · **Status**: Accepted (user guideline)
- **Decision**: SemVer for versionName; versionCode = MAJOR*10000 + MINOR*100 + PATCH. See VERSIONING.md.

## ADR-006: material3 version for Expressive APIs
- **Date**: 2026-11-12 · **Status**: Accepted (user decision)
- **Context**: Stable 1.4.0 lacks or gates many Expressive APIs. The user wants every Expressive component and accepts experimental, bleeding-edge versions.
- **Decision**: Use the **newest available `androidx.compose.material3` release (alpha, beta, or RC)**, pinned explicitly in the version catalog and overriding the BOM. Opting into experimental APIs (`ExperimentalMaterial3ExpressiveApi`, `ExperimentalMaterial3Api`) is allowed.
- **Consequences**: APIs may be renamed or changed between versions. Check the release notes on each bump and record the changes in REFERENCES.md. Put opt-ins at file or function level, never module-wide.
