# Production-grade project scaffold — design

Date: 2026-08-26
Status: Approved (chat), implementing

## Context

`HEM` is a fresh Kotlin Multiplatform / Compose Multiplatform template
(package `org.nha.project`, module layout `androidApp` / `iosApp` /
`shared`) targeting Android + iOS, for a field-agent app that verifies
and onboards hospitals for PM-JAY empanelment (login → capture hospital
docs/location → track empanelment status: empanelled / in-progress /
rejected). Only the JetBrains default scaffold code exists so far
(`Greeting`, `Platform`, a counter-demo `App.kt`).

Design assets (PNG + SVG, Figma-exported with iOS-style `@2x`/`@3x`
density suffixes) were dropped into
`shared/src/commonMain/kotlin/org/nha/project/assets/` — inside the
Kotlin source root, which is not a valid Compose Multiplatform resource
location.

No backend API exists yet. No git repository exists in this directory
(spec is not committed; kept as a local file for reference).

## Decisions

- **Structure**: package-by-feature inside the existing `shared`
  module (not a Gradle multi-module split). Each feature gets
  `data/domain/presentation`; cross-cutting code lives under `core/`.
- **Screens**: scaffold only. Every feature route renders a shared
  `PlaceholderScreen`, wired into real navigation + DI, so the app
  builds and runs end-to-end. No real business screens are built.
- **DI**: Koin 4.2.1 (`koin-core`, `koin-compose`, `koin-compose-viewmodel`).
- **Navigation**: `org.jetbrains.androidx.navigation:navigation-compose`
  2.9.2, type-safe `@Serializable` routes.
- **Networking**: Ktor client 3.5.1, scaffolded only — `HttpClientFactory`
  + `ApiResult<T>` wrapper + `NetworkException`, no live endpoints.
- **Serialization**: kotlinx.serialization 1.11.0.
- **Local storage**: AndroidX DataStore (multiplatform) 1.2.1, for
  prefs/token storage.
- **Coroutines**: kotlinx.coroutines 1.11.0, added explicitly.
- **Lint**: ktlint Gradle plugin.

## Folder layout

```
shared/src/commonMain/kotlin/org/nha/project/
  core/
    di/          AppModule.kt, NetworkModule.kt, StorageModule.kt
    network/     HttpClientFactory.kt, ApiResult.kt, NetworkException.kt
    storage/     AppPreferences.kt, TokenStorage.kt
    navigation/  Routes.kt, AppNavHost.kt
    ui/theme/    Color.kt, Type.kt, Theme.kt (HemTheme)
    ui/components/ PlaceholderScreen.kt
  feature/
    auth/{data,domain,presentation}
    onboarding/{data,domain,presentation}
    hospital/{data,domain,presentation}
    capture/{data,domain,presentation}
  App.kt

shared/src/commonMain/composeResources/drawable/   migrated assets
shared/src/androidMain/kotlin/org/nha/project/di/PlatformModule.android.kt
shared/src/iosMain/kotlin/org/nha/project/di/PlatformModule.ios.kt
androidApp/src/main/kotlin/org/nha/project/HemApplication.kt  (new)
```

## Asset migration

Source: `shared/src/commonMain/kotlin/org/nha/project/assets/{PNG,SVGs}/`
Target: `shared/src/commonMain/composeResources/drawable/`

Rules:
1. Where both a PNG and an SVG exist for the same image, keep the SVG
   (resolution-independent; avoids a resource-id collision between
   `x.png` and `x.svg`). Applies to: alert, alert_red, back_arrow,
   device_location, empanelled_hospitals, hospitals,
   in_progress_hospitals, location, location_accuracy, login,
   onboarding_screen_background, phone, rejected, right_arrow,
   success, zoom.
2. PNG-only images keep the `@3x` variant's bytes (highest resolution),
   renamed without the density suffix: add_more, alert_3, capture,
   guidelines (typo-fixed from "Guildlines"), hero_banner,
   hero_banner_2, nha_logo, pmjay_logo, retake.
3. `Image.svg` (no PNG counterpart) kept as `image.svg`.
4. All filenames normalized to valid Compose resource ids: lowercase
   snake_case, no spaces/parentheses/trailing characters.
5. The stray `SVGs/retake.png` (a PNG file mislabeled into the SVG
   folder, duplicate of `PNG/retake.png`) is dropped in favor of the
   PNG-group `retake.png` (kept as PNG — no SVG counterpart exists for
   it; folded into rule 2's PNG-only list above).
6. Original `assets/` folder under the Kotlin source root is removed
   once migration is verified.

Result: 26 files in `composeResources/drawable/` (17 SVG + 9 PNG).

**Correction found during implementation:** rule 1 ("prefer SVG when both
exist") assumed the SVGs were lightweight vector icons. Inspecting the
actual files showed 8 of the 16 SVG/PNG-overlap images are not vector
art at all — they're multi-MB stock photos wrapped in an `<svg>` tag
via an embedded `<image>` element (`alert`, `alert_red`, `hospitals`,
`location`, `onboarding_screen_background`, `phone`, `rejected`,
`success`; e.g. `hospitals.svg` was 9.6 MB vs. a 33 KB PNG for the same
image). Those 8 were switched to PNG instead, cutting total asset
weight from ~40 MB to ~8.5 MB. The remaining 8 overlap images
(`back_arrow`, `device_location`, `empanelled_hospitals`,
`in_progress_hospitals`, `location_accuracy`, `login`, `right_arrow`,
`zoom`) are genuine small vector paths and stayed as SVG, along with
`image.svg` (no PNG alternative exists). Final mix: **9 SVG + 17 PNG**
(counts flip from the original rule, total file count unchanged at 26).

## Verification

`./gradlew :shared:compileKotlin :androidApp:assembleDebug` (Android +
common only — iOS targets require Xcode/macOS, unavailable on this
Windows machine, and are unaffected by this change).
