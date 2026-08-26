This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It's organized package-by-feature:
  - `core/` — cross-cutting infrastructure: `di` (Koin modules), `network` (Ktor client + `ApiResult`),
    `storage` (DataStore-backed `TokenStorage`), `navigation` (type-safe routes + `AppNavHost`),
    `ui/theme` and `ui/components` (shared theme + `PlaceholderScreen`).
  - `feature/<name>/presentation` — one package per feature (`auth`, `onboarding`, `hospital`, `capture`).
    Each currently renders a placeholder screen wired into real navigation and DI; add `data/` and `domain/`
    subpackages as real business logic lands.
  - `composeResources/drawable` — design assets (PNG/SVG), migrated from Figma exports.
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Code style

Run `./gradlew ktlintFormat` before committing; CI (once configured) should run `./gradlew ktlintCheck`.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
