# Production-Grade Project Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the fresh Kotlin Multiplatform / Compose Multiplatform `HEM` template into a production-grade, package-by-feature scaffold — DI, networking, local storage, type-safe navigation, theme, and correctly-migrated design assets — with every feature route wired to a placeholder screen so the app builds and runs end-to-end on Android (iOS is scaffolded but not compiled here — no macOS toolchain on this machine).

**Architecture:** Package-by-feature inside the existing `shared` module. `core/` holds cross-cutting infrastructure (DI, network, storage, navigation, theme, shared UI components); `feature/<name>/presentation/` holds one placeholder screen per feature today, ready for `data/`/`domain/` to be added when real logic lands. Koin is bootstrapped once at process start (`Application.onCreate` on Android, `MainViewController()` on iOS) and consumed via `KoinContext` inside `App.kt`.

**Tech Stack:** Kotlin 2.4.10, Compose Multiplatform 1.11.1, Koin 4.2.1, Ktor client 3.5.1, kotlinx.serialization 1.11.0, kotlinx.coroutines 1.11.0, AndroidX Navigation Compose (multiplatform) 2.9.2, AndroidX DataStore (multiplatform) 1.2.1, Okio 3.17.0, ktlint Gradle plugin 14.2.0.

**Spec:** `docs/superpowers/specs/2026-08-26-production-grade-scaffold-design.md`

## Global Constraints

- Package-by-feature inside the single `shared` module — no Gradle multi-module split.
- No real business screens — every route renders a placeholder wired to real navigation + DI.
- No live backend endpoints — networking layer is scaffolded only (`HttpClientFactory` + `ApiResult<T>` + `NetworkException`).
- Asset migration must produce exactly 26 files in `shared/src/commonMain/composeResources/drawable/` (17 SVG + 9 PNG), all valid snake_case resource ids, with the old `shared/src/commonMain/kotlin/org/nha/project/assets/` folder removed afterward.
- Verification is Android + common only (`./gradlew :androidApp:assembleDebug`) — do not attempt iOS compilation on this Windows machine.
- All new Kotlin files use package root `org.nha.project`.

---

### Task 1: Dependency & plugin wiring

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `shared/build.gradle.kts`
- Modify: `androidApp/build.gradle.kts`

**Interfaces:**
- Produces: version catalog accessors (`libs.koin.core`, `libs.koin.compose`, `libs.koin.compose.viewmodel`, `libs.koin.android`, `libs.navigation.compose`, `libs.ktor.client.core`, `libs.ktor.client.contentNegotiation`, `libs.ktor.client.logging`, `libs.ktor.serialization.kotlinxJson`, `libs.ktor.client.okhttp`, `libs.ktor.client.darwin`, `libs.kotlinx.serialization.json`, `libs.kotlinx.coroutines.core`, `libs.androidx.datastore.preferences.core`, `libs.okio`, `libs.plugins.kotlinSerialization`, `libs.plugins.ktlint`) consumed by every later task.

- [ ] **Step 1: Replace `gradle/libs.versions.toml` with the full merged catalog**

```toml
[versions]
agp = "9.2.1"
android-compileSdk = "36"
android-minSdk = "24"
android-targetSdk = "36"
androidx-activity = "1.13.0"
androidx-appcompat = "1.7.1"
androidx-core = "1.19.0"
androidx-espresso = "3.7.0"
androidx-lifecycle = "2.11.0-beta01"
androidx-testExt = "1.3.0"
composeMultiplatform = "1.11.1"
datastore = "1.2.1"
junit = "4.13.2"
koin = "4.2.1"
kotlin = "2.4.10"
kotlinxCoroutines = "1.11.0"
kotlinxSerialization = "1.11.0"
ktlint = "14.2.0"
ktor = "3.5.1"
material3 = "1.11.0-alpha07"
navigationCompose = "2.9.2"
okio = "3.17.0"

[libraries]
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlin-testJunit = { module = "org.jetbrains.kotlin:kotlin-test-junit", version.ref = "kotlin" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidx-core" }
androidx-testExt-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-testExt" }
androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "androidx-espresso" }
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "androidx-appcompat" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }
compose-uiTooling = { module = "org.jetbrains.compose.ui:ui-tooling", version.ref = "composeMultiplatform" }
androidx-lifecycle-viewmodelCompose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidx-lifecycle" }
androidx-lifecycle-runtimeCompose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }
compose-runtime = { module = "org.jetbrains.compose.runtime:runtime", version.ref = "composeMultiplatform" }
compose-foundation = { module = "org.jetbrains.compose.foundation:foundation", version.ref = "composeMultiplatform" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3", version.ref = "material3" }
compose-ui = { module = "org.jetbrains.compose.ui:ui", version.ref = "composeMultiplatform" }
compose-components-resources = { module = "org.jetbrains.compose.components:components-resources", version.ref = "composeMultiplatform" }
compose-uiToolingPreview = { module = "org.jetbrains.compose.ui:ui-tooling-preview", version.ref = "composeMultiplatform" }
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-contentNegotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-serialization-kotlinxJson = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinxCoroutines" }
androidx-datastore-preferences-core = { module = "androidx.datastore:datastore-preferences-core", version.ref = "datastore" }
okio = { module = "com.squareup.okio:okio", version.ref = "okio" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidMultiplatformLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

- [ ] **Step 2: Add the new plugins as `apply false` in the root `build.gradle.kts`**

```kotlin
plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktlint) apply false
}
```

- [ ] **Step 3: Apply `kotlinSerialization` and `ktlint`, and add all new dependencies, in `shared/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktlint)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "org.nha.project.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.datastore.preferences.core)
            implementation(libs.okio)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
```

- [ ] **Step 4: Apply `ktlint` and add `koin-android` in `androidApp/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ktlint)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "org.nha.project"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.nha.project"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
```

- [ ] **Step 5: Verify Gradle resolves the new dependency graph**

Run: `./gradlew :shared:dependencies --configuration commonMainImplementation`
Expected: `BUILD SUCCESSFUL`, output lists `io.insert-koin:koin-core`, `io.ktor:ktor-client-core`, `org.jetbrains.androidx.navigation:navigation-compose`, `androidx.datastore:datastore-preferences-core`, `com.squareup.okio:okio` resolved (no `FAILED` markers).

- [ ] **Step 6: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts shared/build.gradle.kts androidApp/build.gradle.kts
git commit -m "build: add Koin, Ktor, Navigation, DataStore, ktlint dependencies"
```

---

### Task 2: Asset migration

**Files:**
- Create: 26 files under `shared/src/commonMain/composeResources/drawable/`
- Delete: `shared/src/commonMain/kotlin/org/nha/project/assets/` (entire folder, PNG + SVGs subfolders)

**Interfaces:**
- Produces: Compose-generated resource accessors `Res.drawable.<name>` for all 26 names below (package `hem.shared.generated.resources`, same generator already producing `Res.drawable.compose_multiplatform`) — consumed by Task 7 (`AppNavHost`) and Task 8 (feature screens).

- [ ] **Step 1: Copy the 17 SVGs (preferred over their PNG duplicates), renamed to snake_case**

Run from the repo root:

```bash
SRC_SVG="shared/src/commonMain/kotlin/org/nha/project/assets/SVGs"
DEST="shared/src/commonMain/composeResources/drawable"
mkdir -p "$DEST"

cp "$SRC_SVG/Alert red.svg" "$DEST/alert_red.svg"
cp "$SRC_SVG/Alert.svg" "$DEST/alert.svg"
cp "$SRC_SVG/Back arrow.svg" "$DEST/back_arrow.svg"
cp "$SRC_SVG/Device location.svg" "$DEST/device_location.svg"
cp "$SRC_SVG/Empanelled Hospitals (2).svg" "$DEST/empanelled_hospitals.svg"
cp "$SRC_SVG/Hospitals.svg" "$DEST/hospitals.svg"
cp "$SRC_SVG/Image.svg" "$DEST/image.svg"
cp "$SRC_SVG/In Progress Hospitals (1).svg" "$DEST/in_progress_hospitals.svg"
cp "$SRC_SVG/Location Accuracy.svg" "$DEST/location_accuracy.svg"
cp "$SRC_SVG/Location.svg" "$DEST/location.svg"
cp "$SRC_SVG/Login.svg" "$DEST/login.svg"
cp "$SRC_SVG/Onboarding screen background.svg" "$DEST/onboarding_screen_background.svg"
cp "$SRC_SVG/Phone.svg" "$DEST/phone.svg"
cp "$SRC_SVG/rejected.svg" "$DEST/rejected.svg"
cp "$SRC_SVG/Right arrow.svg" "$DEST/right_arrow.svg"
cp "$SRC_SVG/Success.svg" "$DEST/success.svg"
cp "$SRC_SVG/Zoom .svg" "$DEST/zoom.svg"
```

- [ ] **Step 2: Copy the 9 PNG-only images, using the highest-resolution (`@3x`, or base if no variant exists) file**

```bash
SRC_PNG="shared/src/commonMain/kotlin/org/nha/project/assets/PNG"
DEST="shared/src/commonMain/composeResources/drawable"

cp "$SRC_PNG/Add more@3x.png" "$DEST/add_more.png"
cp "$SRC_PNG/Alert 3@3x.png" "$DEST/alert_3.png"
cp "$SRC_PNG/Capture@3x.png" "$DEST/capture.png"
cp "$SRC_PNG/Guildlines@3x.png" "$DEST/guidelines.png"
cp "$SRC_PNG/HeroBanner.png" "$DEST/hero_banner.png"
cp "$SRC_PNG/HeroBanner2.png" "$DEST/hero_banner_2.png"
cp "$SRC_PNG/nha_logo.png" "$DEST/nha_logo.png"
cp "$SRC_PNG/pmjay_logo.png" "$DEST/pmjay_logo.png"
cp "$SRC_PNG/retake@3x.png" "$DEST/retake.png"
```

- [ ] **Step 3: Verify exactly 26 files landed, then delete the old assets folder**

Run: `ls shared/src/commonMain/composeResources/drawable | wc -l`
Expected: `27` (the 26 migrated files plus the pre-existing `compose_multiplatform.xml`/`.webp` template asset already in that folder — if the count is `26` instead, the template asset was never there and that's fine too; the key check is that all 26 names from Steps 1–2 are present).

```bash
rm -rf "shared/src/commonMain/kotlin/org/nha/project/assets"
```

- [ ] **Step 4: Confirm the resource generator picks up the new files**

Run: `./gradlew :shared:generateComposeResClass`
Expected: `BUILD SUCCESSFUL`, and the generated file (under `shared/build/generated/compose/resourceGenerator/kotlin/`) contains a `val login: DrawableResource` and `val hospitals: DrawableResource` accessor — grep for them:

Run: `grep -r "val login" shared/build/generated/compose/resourceGenerator/ ; grep -r "val hospitals" shared/build/generated/compose/resourceGenerator/`
Expected: both greps print a matching line.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/composeResources/drawable
git add -A shared/src/commonMain/kotlin/org/nha/project/assets
git commit -m "chore: migrate design assets into composeResources, dedupe SVG/PNG, drop density suffixes"
```

---

### Task 3: Core network layer

**Files:**
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/network/ApiResult.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/network/HttpClientFactory.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/di/NetworkModule.kt`

**Interfaces:**
- Produces: `sealed class ApiResult<out T>` with `Success(data: T)` / `Error(exception: NetworkException)`; `sealed class NetworkException`; `fun createHttpClient(engine: HttpClientEngine): HttpClient`; `val networkModule: Module` (Koin, provides `single<HttpClient>`, requires `HttpClientEngine` from the platform module — consumed by Task 5).
- Consumes: `HttpClientEngine` (from `io.ktor.client.engine`), to be provided by Task 5's `platformModule`.

- [ ] **Step 1: Create `ApiResult.kt`**

```kotlin
package org.nha.project.core.network

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: NetworkException) : ApiResult<Nothing>()
}

sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NoConnection(cause: Throwable? = null) : NetworkException("No network connection", cause)
    class Timeout(cause: Throwable? = null) : NetworkException("Request timed out", cause)
    class ServerError(val code: Int, serverMessage: String) : NetworkException("Server error $code: $serverMessage")
    class Unknown(cause: Throwable? = null) : NetworkException(cause?.message ?: "Unknown network error", cause)
}
```

- [ ] **Step 2: Create `HttpClientFactory.kt`**

```kotlin
package org.nha.project.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            },
        )
    }

    install(Logging) {
        level = LogLevel.INFO
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 15_000
    }
}
```

- [ ] **Step 3: Create `NetworkModule.kt`**

```kotlin
package org.nha.project.core.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.dsl.module
import org.nha.project.core.network.createHttpClient

val networkModule = module {
    single<HttpClient> { createHttpClient(get<HttpClientEngine>()) }
}
```

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew :shared:compileKotlinAndroid`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/org/nha/project/core/network shared/src/commonMain/kotlin/org/nha/project/core/di/NetworkModule.kt
git commit -m "feat: scaffold core network layer (Ktor client factory, ApiResult, NetworkModule)"
```

---

### Task 4: Core storage layer

**Files:**
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/storage/DataStoreFactory.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/storage/TokenStorage.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/di/StorageModule.kt`

**Interfaces:**
- Produces: `const val DATA_STORE_FILE_NAME`; `fun createDataStore(producePath: () -> String): DataStore<Preferences>` (consumed by Task 5's platform modules); `class TokenStorage(dataStore: DataStore<Preferences>)` with `val authToken: Flow<String?>`, `suspend fun saveToken(token: String)`, `suspend fun clearToken()`; `val storageModule: Module` (provides `single { TokenStorage(get()) }`, requires `DataStore<Preferences>` from the platform module).
- Consumes: `DataStore<Preferences>`, to be provided by Task 5's `platformModule`.

- [ ] **Step 1: Create `DataStoreFactory.kt`**

```kotlin
package org.nha.project.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

const val DATA_STORE_FILE_NAME = "hem.preferences_pb"

fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() },
    )
```

- [ ] **Step 2: Create `TokenStorage.kt`**

```kotlin
package org.nha.project.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenStorage(private val dataStore: DataStore<Preferences>) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    val authToken: Flow<String?> = dataStore.data.map { it[authTokenKey] }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[authTokenKey] = token }
    }

    suspend fun clearToken() {
        dataStore.edit { it.remove(authTokenKey) }
    }
}
```

- [ ] **Step 3: Create `StorageModule.kt`**

```kotlin
package org.nha.project.core.di

import org.koin.dsl.module
import org.nha.project.core.storage.TokenStorage

val storageModule = module {
    single { TokenStorage(get()) }
}
```

- [ ] **Step 4: Verify it compiles**

Run: `./gradlew :shared:compileKotlinAndroid`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add shared/src/commonMain/kotlin/org/nha/project/core/storage shared/src/commonMain/kotlin/org/nha/project/core/di/StorageModule.kt
git commit -m "feat: scaffold core storage layer (DataStore factory, TokenStorage, StorageModule)"
```

---

### Task 5: Platform DI modules, AppModule, initKoin

**Files:**
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/di/PlatformModule.kt`
- Create: `shared/src/androidMain/kotlin/org/nha/project/core/di/PlatformModule.android.kt`
- Create: `shared/src/iosMain/kotlin/org/nha/project/core/di/PlatformModule.ios.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/di/AppModule.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/di/KoinInit.kt`

**Interfaces:**
- Consumes: `networkModule` (Task 3), `storageModule` (Task 4), `createDataStore` + `DATA_STORE_FILE_NAME` (Task 4).
- Produces: `expect val platformModule: Module` (provides `single<HttpClientEngine>` and `single<DataStore<Preferences>>` per platform); `val appModules: List<Module>`; `fun initKoin(platformDeclaration: KoinAppDeclaration? = null, extraModules: List<Module> = emptyList())` — consumed by Task 6 (`HemApplication`, `MainViewController`).

- [ ] **Step 1: Create the `expect` declaration**

```kotlin
package org.nha.project.core.di

import org.koin.core.module.Module

expect val platformModule: Module
```

- [ ] **Step 2: Create the Android `actual`**

```kotlin
package org.nha.project.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.storage.DATA_STORE_FILE_NAME
import org.nha.project.core.storage.createDataStore

actual val platformModule: Module = module {
    single<HttpClientEngine> { OkHttp.create() }
    single<DataStore<Preferences>> {
        val context = get<Context>()
        createDataStore { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
    }
}
```

- [ ] **Step 3: Create the iOS `actual`**

```kotlin
package org.nha.project.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.storage.DATA_STORE_FILE_NAME
import org.nha.project.core.storage.createDataStore
import platform.Foundation.NSHomeDirectory

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single<DataStore<Preferences>> {
        createDataStore { NSHomeDirectory() + "/$DATA_STORE_FILE_NAME" }
    }
}
```

- [ ] **Step 4: Create `AppModule.kt`**

```kotlin
package org.nha.project.core.di

import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    platformModule,
    networkModule,
    storageModule,
)
```

- [ ] **Step 5: Create `KoinInit.kt`**

```kotlin
package org.nha.project.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(platformDeclaration: KoinAppDeclaration? = null, extraModules: List<Module> = emptyList()) {
    startKoin {
        platformDeclaration?.invoke(this)
        modules(appModules + extraModules)
    }
}
```

- [ ] **Step 6: Verify Android side compiles (iOS side is not compiled on this machine)**

Run: `./gradlew :shared:compileKotlinAndroid`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 7: Commit**

```bash
git add shared/src/commonMain/kotlin/org/nha/project/core/di shared/src/androidMain/kotlin/org/nha/project/core/di shared/src/iosMain/kotlin/org/nha/project/core/di
git commit -m "feat: wire Koin platform modules, AppModule, and initKoin bootstrap"
```

---

### Task 6: App entry-point wiring (Android Application, iOS MainViewController)

**Files:**
- Create: `androidApp/src/main/kotlin/org/nha/project/HemApplication.kt`
- Modify: `androidApp/src/main/AndroidManifest.xml`
- Modify: `shared/src/iosMain/kotlin/org/nha/project/MainViewController.kt`

**Interfaces:**
- Consumes: `initKoin()` (Task 5).

- [ ] **Step 1: Create `HemApplication.kt`**

```kotlin
package org.nha.project

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.nha.project.core.di.initKoin

class HemApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@HemApplication)
        }
    }
}
```

- [ ] **Step 2: Register it in `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".HemApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:exported="true"
            android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 3: Call `initKoin()` from the iOS entry point**

```kotlin
package org.nha.project

import androidx.compose.ui.window.ComposeUIViewController
import org.nha.project.core.di.initKoin

fun MainViewController() = run {
    initKoin()
    ComposeUIViewController { App() }
}
```

- [ ] **Step 4: Verify Android side compiles and the manifest merges cleanly**

Run: `./gradlew :androidApp:assembleDebug`
Expected: `BUILD SUCCESSFUL` (this task's App.kt is still the old template — Task 8 replaces it — so this step confirms manifest + Application wiring only; if App.kt (pre-Task 8) fails to reference anything removed by this task, that's a real bug — Task 6 touches no App.kt symbols, so it must pass).

- [ ] **Step 5: Commit**

```bash
git add androidApp/src/main/kotlin/org/nha/project/HemApplication.kt androidApp/src/main/AndroidManifest.xml shared/src/iosMain/kotlin/org/nha/project/MainViewController.kt
git commit -m "feat: bootstrap Koin from Android Application and iOS MainViewController"
```

---

### Task 7: Theme, PlaceholderScreen, navigation routes

**Files:**
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/ui/theme/Color.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/ui/theme/Type.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/ui/theme/Theme.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/ui/components/PlaceholderScreen.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/navigation/Route.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/core/navigation/AppNavHost.kt`

**Interfaces:**
- Consumes: `Res.drawable.*` (Task 2 asset migration).
- Produces: `@Composable fun HemTheme(darkTheme: Boolean, content: @Composable () -> Unit)`; `@Composable fun PlaceholderScreen(title: String, imageRes: DrawableResource, onContinue: (() -> Unit)?, modifier: Modifier)`; `sealed interface Route` with `@Serializable data object Onboarding/Login/HospitalList/Capture/Status`; `@Composable fun AppNavHost()` — all consumed by Task 8 and Task 9's `App.kt` rewrite.

- [ ] **Step 1: Create `Color.kt`**

```kotlin
package org.nha.project.core.ui.theme

import androidx.compose.ui.graphics.Color

val HemPrimary = Color(0xFF0B6E4F)
val HemOnPrimary = Color(0xFFFFFFFF)
val HemSecondary = Color(0xFF1B5E20)
val HemBackground = Color(0xFFF7F9F8)
val HemSurface = Color(0xFFFFFFFF)
val HemError = Color(0xFFB3261E)
```

- [ ] **Step 2: Create `Type.kt`**

```kotlin
package org.nha.project.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val HemTypography = Typography(
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)
```

- [ ] **Step 3: Create `Theme.kt`**

```kotlin
package org.nha.project.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HemLightColors = lightColorScheme(
    primary = HemPrimary,
    onPrimary = HemOnPrimary,
    secondary = HemSecondary,
    background = HemBackground,
    surface = HemSurface,
    error = HemError,
)

private val HemDarkColors = darkColorScheme(
    primary = HemPrimary,
    onPrimary = HemOnPrimary,
    secondary = HemSecondary,
    error = HemError,
)

@Composable
fun HemTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) HemDarkColors else HemLightColors,
        typography = HemTypography,
        content = content,
    )
}
```

- [ ] **Step 4: Create `PlaceholderScreen.kt`**

```kotlin
package org.nha.project.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlaceholderScreen(
    title: String,
    imageRes: DrawableResource,
    onContinue: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(imageRes), contentDescription = title)
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        if (onContinue != null) {
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("Continue")
            }
        }
    }
}
```

- [ ] **Step 5: Create `Route.kt`**

```kotlin
package org.nha.project.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object HospitalList : Route

    @Serializable
    data object Capture : Route

    @Serializable
    data object Status : Route
}
```

- [ ] **Step 6: Create `AppNavHost.kt`**

```kotlin
package org.nha.project.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.nha.project.feature.auth.presentation.LoginScreen
import org.nha.project.feature.capture.presentation.CaptureScreen
import org.nha.project.feature.hospital.presentation.HospitalListScreen
import org.nha.project.feature.hospital.presentation.HospitalStatusScreen
import org.nha.project.feature.onboarding.presentation.OnboardingScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Route.Onboarding) {
        composable<Route.Onboarding> {
            OnboardingScreen(onContinue = { navController.navigate(Route.Login) })
        }
        composable<Route.Login> {
            LoginScreen(onContinue = { navController.navigate(Route.HospitalList) })
        }
        composable<Route.HospitalList> {
            HospitalListScreen(onContinue = { navController.navigate(Route.Capture) })
        }
        composable<Route.Capture> {
            CaptureScreen(onContinue = { navController.navigate(Route.Status) })
        }
        composable<Route.Status> {
            HospitalStatusScreen()
        }
    }
}
```

Note: `AppNavHost.kt` references the four feature screen composables created in Task 8. That's expected — Task 7 and Task 8 are compiled together; Task 7's own verification step (below) only checks the files that don't yet depend on Task 8, and full compilation is verified at the end of Task 8.

- [ ] **Step 7: Verify the theme and component files compile in isolation**

Run: `./gradlew :shared:compileKotlinAndroid`
Expected: **FAILS** at this point with unresolved references to `org.nha.project.feature.*` screens (expected — Task 8 creates them next). Confirm the *only* errors are "unresolved reference" for `LoginScreen`, `CaptureScreen`, `HospitalListScreen`, `HospitalStatusScreen`, `OnboardingScreen` in `AppNavHost.kt` — if any other file fails, fix it before proceeding.

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/org/nha/project/core/ui shared/src/commonMain/kotlin/org/nha/project/core/navigation
git commit -m "feat: add HemTheme, PlaceholderScreen, and type-safe navigation graph"
```

---

### Task 8: Feature placeholder screens + App.kt rewrite

**Files:**
- Create: `shared/src/commonMain/kotlin/org/nha/project/feature/onboarding/presentation/OnboardingScreen.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/feature/auth/presentation/LoginScreen.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/feature/hospital/presentation/HospitalListScreen.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/feature/hospital/presentation/HospitalStatusScreen.kt`
- Create: `shared/src/commonMain/kotlin/org/nha/project/feature/capture/presentation/CaptureScreen.kt`
- Modify: `shared/src/commonMain/kotlin/org/nha/project/App.kt`

**Interfaces:**
- Consumes: `PlaceholderScreen` (Task 7), `Res.drawable.onboarding_screen_background/login/hospitals/empanelled_hospitals/capture` (Task 2), `AppNavHost` + `HemTheme` (Task 7).
- Produces: `@Composable fun OnboardingScreen(onContinue: () -> Unit)`, `@Composable fun LoginScreen(onContinue: () -> Unit)`, `@Composable fun HospitalListScreen(onContinue: () -> Unit)`, `@Composable fun HospitalStatusScreen()`, `@Composable fun CaptureScreen(onContinue: () -> Unit)` — these are exactly the symbols `AppNavHost.kt` (Task 7) imports.

- [ ] **Step 1: Create `OnboardingScreen.kt`**

```kotlin
package org.nha.project.feature.onboarding.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.onboarding_screen_background
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Onboarding",
        imageRes = Res.drawable.onboarding_screen_background,
        onContinue = onContinue,
    )
}
```

- [ ] **Step 2: Create `LoginScreen.kt`**

```kotlin
package org.nha.project.feature.auth.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.login
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun LoginScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Login",
        imageRes = Res.drawable.login,
        onContinue = onContinue,
    )
}
```

- [ ] **Step 3: Create `HospitalListScreen.kt`**

```kotlin
package org.nha.project.feature.hospital.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.hospitals
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun HospitalListScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Hospitals",
        imageRes = Res.drawable.hospitals,
        onContinue = onContinue,
    )
}
```

- [ ] **Step 4: Create `HospitalStatusScreen.kt`**

```kotlin
package org.nha.project.feature.hospital.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.empanelled_hospitals
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun HospitalStatusScreen() {
    PlaceholderScreen(
        title = "Empanelment Status",
        imageRes = Res.drawable.empanelled_hospitals,
        onContinue = null,
    )
}
```

- [ ] **Step 5: Create `CaptureScreen.kt`**

```kotlin
package org.nha.project.feature.capture.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.capture
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun CaptureScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Capture Documents",
        imageRes = Res.drawable.capture,
        onContinue = onContinue,
    )
}
```

- [ ] **Step 6: Replace `App.kt`**

```kotlin
package org.nha.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.nha.project.core.navigation.AppNavHost
import org.nha.project.core.ui.theme.HemTheme

@Composable
@Preview
fun App() {
    KoinContext {
        HemTheme {
            AppNavHost()
        }
    }
}
```

- [ ] **Step 7: Full build verification**

Run: `./gradlew :androidApp:assembleDebug`
Expected: `BUILD SUCCESSFUL`. This compiles `shared` for Android (including the new Koin/Ktor/Navigation/DataStore wiring, the migrated composeResources, and all feature placeholder screens) and assembles the debug APK.

- [ ] **Step 8: Commit**

```bash
git add shared/src/commonMain/kotlin/org/nha/project/feature shared/src/commonMain/kotlin/org/nha/project/App.kt
git commit -m "feat: add feature placeholder screens wired into navigation, replace demo App.kt"
```

---

### Task 9: ktlint and final cleanup

**Files:**
- Modify: `README.md`

**Interfaces:**
- None (final polish task).

- [ ] **Step 1: Run ktlint format across the project**

Run: `./gradlew ktlintFormat`
Expected: `BUILD SUCCESSFUL`. Review the diff it produces (formatting-only changes) before committing.

- [ ] **Step 2: Run ktlint check to confirm a clean baseline**

Run: `./gradlew ktlintCheck`
Expected: `BUILD SUCCESSFUL` with no violations reported.

- [ ] **Step 3: Update `README.md` with the new structure**

```markdown
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
```

- [ ] **Step 4: Final full build verification**

Run: `./gradlew :androidApp:assembleDebug ktlintCheck`
Expected: `BUILD SUCCESSFUL` for both.

- [ ] **Step 5: Commit**

```bash
git add . -- ':!local.properties'
git commit -m "chore: apply ktlint formatting, document the package-by-feature structure"
```

---

## Self-Review Notes

- **Spec coverage:** DI (Koin, Task 5) ✓, navigation (Task 7) ✓, networking scaffold (Task 3) ✓, local storage (Task 4) ✓, coroutines/serialization (Task 1 deps, used throughout) ✓, ktlint (Task 9) ✓, asset migration with the corrected 26-file/dedup rules (Task 2) ✓, folder layout (Tasks 3–8) ✓, Android+common-only verification (every task's verify step) ✓.
- **Type consistency checked:** `PlaceholderScreen(title, imageRes, onContinue, modifier)` signature is identical everywhere it's called (Task 7 definition, Task 8's five call sites). `DATA_STORE_FILE_NAME` and `createDataStore(producePath: () -> String)` (Task 4) match their usage in both platform actuals (Task 5). `appModules` (Task 5) lists exactly `platformModule`, `networkModule` (Task 3), `storageModule` (Task 4) — all three exist by the time Task 5 runs. `Route` sealed interface members (Task 7) match `composable<Route.X>` calls in the same file and `navController.navigate(Route.X)` calls in Task 8's nav host — no mismatches.
- **No git repo exists yet** in this project. Every task's commit step will fail until `git init` happens — that's expected and out of scope for this plan; the executor should run the build/verify steps regardless and skip (or defer) the `git commit` steps if no repository is present, flagging this to the user rather than silently dropping work.
