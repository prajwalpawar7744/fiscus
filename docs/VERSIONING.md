# Versioning

Fiscus follows **Semantic Versioning 2.0.0** (https://semver.org/) for `versionName`.

## versionName: `MAJOR.MINOR.PATCH[-prerelease]`
- **MAJOR**: incompatible changes. Examples: a DB migration that drops data compatibility, backup-format breaks, or major UX overhauls.
- **MINOR**: new features that are backward compatible.
- **PATCH**: bug fixes and polish only.
- Pre-releases use `-alpha.N`, `-beta.N`, or `-rc.N` (for example, `0.3.0-beta.1`).
- Until the first public release, stay on `0.y.z`. `1.0.0` marks the first stable Play release.

## versionCode
Per https://developer.android.com/studio/publish/versioning:
- It is a positive integer and must increase with **every** release. Google Play rejects a versionCode that was already used.
- Its maximum value is 2,100,000,000.
- Fiscus derives it from the version number: `MAJOR*10000 + MINOR*100 + PATCH` (for example, 1.4.2 → 10402). This requires MINOR and PATCH to stay ≤ 99.

## Where versions are set
- Single source of truth: `app/build.gradle.kts` → `defaultConfig { versionCode; versionName }`. Never set them in the manifest. (Moving them to a `version.properties` file or a catalog entry would be a separate ADR.)
- Each release gets a git tag `vX.Y.Z` and an entry in `CHANGELOG.md` (Keep a Changelog format) once releases begin.

## Build types
- **debug**: applicationId `<id>.debug`, versionName `X.Y.Z-debug`. Installs side by side with release.
- **release**: no suffix. The applicationId must never change after the app is published.
- Output files are named `fiscus-v<versionName>-<buildType>.apk` (set via `base.archivesName` in `app/build.gradle.kts`). Unsigned release builds end in `-release-unsigned` until signing is configured.

## Current
- `0.1.0` (versionCode 100).
