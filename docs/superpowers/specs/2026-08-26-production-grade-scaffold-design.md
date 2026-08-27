# Production-grade project scaffold — design

Date: 2026-08-26
Status: Approved (chat), implementing

## 2026-08-27 addendum: real NHA login (captcha/OTP flow, crypto, session)

Onboarding is removed from the flow entirely: `Splash` ->
`LocationPermission` -> `Login` (or straight past it to `HospitalList`
if `SessionStorage.isLoggedIn` is already true). Login is now a real,
working integration against `https://apisbeta.nha.gov.in/pmjay/stgbis`,
ported from a reference Android-only app (`LoginApis`,
`EncryptDecrypt`/`EncryptDecrypt2`) into KMP. See `AuthApi`,
`LoginViewModel`, `IdamCrypto`/`SessionCrypto` for the implementation.
Notable findings:

- `dev.whyoleg.cryptography` (cryptography-core +
  provider-optimal) reproduces the reference app's
  PBKDF2WithHmacSHA1 + AES/CBC/PKCS5 (IDAM_KEY) and
  SHA-256-as-raw-AES-key + AES/ECB/PKCS5 (IDAM_KEY2) schemes on both
  Android and iOS — verified for real: `generateToken` and
  `generateCaptcha` both returned HTTP 200 from the live staging
  server, and the returned captcha image decoded and rendered
  correctly, meaning the crypto is byte-for-byte compatible with the
  server, not just "compiles."
- Needs `@OptIn(DelicateCryptographyApi::class)` for `encryptWithIv`/
  `decryptWithIv`/ECB and `dev.whyoleg.cryptography.BinarySize.Companion.bits`
  imported explicitly (not a bare top-level `bits`) for
  `PBKDF2.secretDerivation`'s `outputSize`.
- IDAM_KEY/IDAM_KEY2 live in `local.properties` (gitignored) and reach
  commonMain via a Gradle task (`generateAppSecrets` in
  `shared/build.gradle.kts`) that writes a generated `AppSecrets`
  object at build time — the KMP equivalent of the reference app's
  Android `BuildConfig` fields, since `local.properties` isn't
  otherwise reachable from Kotlin/Native.
- Decoding a base64 captcha PNG into something Compose can draw uses
  `org.jetbrains.compose.resources.decodeToImageBitmap` (already
  available via the `compose-components-resources` dependency we
  had) — no Skia/skiko import needed, and it works on both platforms.
- Ktor's `Logging` plugin silently no-ops on Android without an SLF4J
  provider on the classpath (`HttpClientFactory` now installs a plain
  `println`-based `Logger` instead of relying on `Logger.DEFAULT`,
  which is SLF4J-backed) — without this, network activity is
  invisible in logcat even though the requests are actually
  succeeding, which looked exactly like a silent failure until traced.
- `TokenEntity`/`TokenDao`/`TokenStorage` (the earlier minimal
  placeholder) are replaced by `SessionEntity`/`SessionDao`/
  `SessionStorage`, storing the full identity (userId, username,
  state, entityType, roleName, entityId, parentEntityId) plus both
  tokens. The Room schema JSON was regenerated from scratch rather
  than migrated, since the database was never shipped anywhere.
- Two login "types" (hospital vs. physical verifier) are not a UI
  choice — the server's `entityapprolelist` on the decrypted profile
  determines role; the app takes `entityapprolelist.first()` as the
  active role for now (no chooser UI for multi-role accounts yet).

## 2026-08-27 addendum: Navigation 3 + Room replace Navigation-Compose + DataStore

Superseding the original DI/navigation/storage decisions below:
`org.jetbrains.androidx.navigation:navigation-compose` was replaced with
`org.jetbrains.androidx.navigation3:navigation3-ui` (1.1.1), and
`androidx.datastore` was replaced with Room 3.0.2
(`androidx.room3:room3-*`, KSP, `androidx.sqlite:sqlite-bundled`).
`TokenStorage`'s public interface is unchanged. Also added: splash
screen and a custom (non-OS) location-permission screen, both using the
real brand teal `#1A7275` found embedded in the design assets (replacing
the earlier placeholder `HemPrimary` guess). Notable implementation
findings, in case this needs redoing or debugging later:

- Room's `@Database` class needs `@ConstructedBy(XConstructor::class)` +
  a commonMain `expect object XConstructor : RoomDatabaseConstructor<X>`
  (with `@Suppress("KotlinNoActualForExpect")`, no manual actuals — KSP
  generates them) whenever any non-Android target is compiled. Skipping
  it compiles fine on Android alone and fails only on `kspKotlinIosArm64`
  / `kspKotlinIosSimulatorArm64`, so it's easy to miss if Android is the
  only target checked.
- `Dispatchers.IO` is JVM-internal, not available in commonMain — use
  `Dispatchers.Default` for Room's `setQueryCoroutineContext`.
- `rememberNavBackStack(Route.X)` (single-arg) compiles on Android but
  fails on iOS/Native — Nav3 requires an explicit `SavedStateConfiguration`
  with a `polymorphic(...) { subclass(...) }` entry per sealed subtype to
  save/restore state without JVM reflection. **Register it under
  `NavKey::class`, not `Route::class`** — the runtime encoder
  (`NavBackStackSerializer`) is statically typed to
  `SnapshotStateList<NavKey>`, so a lookup keyed on `Route` compiles fine
  everywhere (including both iOS targets) but throws at runtime on first
  composition: `Serializer for subclass 'Splash' is not found in the
  polymorphic scope of 'NavKey'`. This was caught only by actually
  installing and running the APK, not by any compile/lint check.
- **Compose Multiplatform's `painterResource` does not support raw
  `.svg` files on Android at all** — throws
  `IllegalStateException: Android platform doesn't support SVG format.`
  (JetBrains/compose-multiplatform#4670, still open as of this writing).
  Android needs either raster (PNG/JPG/WEBP) or its own XML
  vector-drawable format; SVG only renders on iOS/Desktop/Web. This
  reverses the "prefer SVG over PNG when both exist" rule from the
  original asset migration above — **prefer PNG for any icon that will
  render on Android**, and only use `.svg` for assets Android will never
  load. All 9 remaining SVGs in this project were converted to PNG (8
  recovered from the original `@3x` exports in the first commit;
  `image.svg`, which had no PNG alternative and no code reference, was
  dropped rather than left as a landmine).
- **Compiling is not verifying.** Both bugs above compiled clean on
  Android and both iOS targets, with zero warnings — they only surfaced
  by installing the APK on a running emulator (`adb install` +
  `am start`) and checking logcat for `FATAL EXCEPTION`. On this
  Windows/GPU-emulation setup, `adb screencap` reliably produces a
  solid-black PNG regardless of what's actually on screen (consistent
  with the GFXSTREAM/EGL warnings in logcat) — that is a capture
  artifact, not a rendering bug. To verify real UI content when
  screenshots are black, use
  `adb shell uiautomator dump /sdcard/ui.xml` and check the `text="..."`
  attributes against what the screen should show.
- This project's Kotlin/Native iOS targets (`compileKotlinIosArm64`,
  `compileKotlinIosSimulatorArm64`) compile and type-check fine on
  Windows with no Xcode/macOS — only linking a real .ipa needs a Mac.
  Treat iOS compile errors as real bugs, not something to skip.
- The Gradle daemon can serve a stale `generateComposeResClass` /
  `prepareComposeResourcesTaskFor*` output across unrelated commands in
  the same session even with `--no-configuration-cache`; when in doubt
  after touching composeResources, run the specific leaf task
  (`generateResourceAccessorsForCommonMain`) rather than trusting the
  aggregator task's UP-TO-DATE claim.

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
