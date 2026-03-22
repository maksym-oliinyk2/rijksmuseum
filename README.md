# Rijksmuseum

## What is it?

A [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) application for Android and iOS that lets users browse artworks
from the [Rijksmuseum](https://www.rijksmuseum.nl/) collection. The app fetches artwork data from the Rijksmuseum API, supports paginated
browsing, and provides a detail view for each artwork.

The shared library (`rijksmuseum-library`) contains all business logic, data layer, and Compose Multiplatform UI, while `android-app` and
`ios-app` are thin platform entry points.

---

## Modules

### `android-app`

The Android application entry point. Contains `MainActivity`, which bootstraps the shared `RijksmuseumApp` composable from
`rijksmuseum-library`. Has no business logic of its own — all UI and logic lives in the shared library.

### `ios-app`

The iOS application entry point (Swift/SwiftUI). Embeds the shared library as `RijksmuseumLib` framework and hosts the Compose Multiplatform
UI via a `UIViewController` returned by `App_appleKt.appController()`. Has no business logic of its own.

### `rijksmuseum-library`

The core Kotlin Multiplatform library shared between Android and iOS. Contains:

- **`app/`** — App-level wiring: Koin DI module, HTTP client setup, navigation
- **`core/data/`** — Ktor-based `RijksmuseumApi` and JSON DTOs/serializers for the Rijksmuseum REST API.
- **`core/domain/`** — Domain models (`Artwork`, `Page`, `Url`, `GettyAatType`, `AppException`).
- **`core/presentation/`** — Shared UI utilities: `Loadable`/`Paginateable` state wrappers, `Navigator`, theme (colors, typography,
  paddings, shapes), and reusable composables (`ProgressIndicator`, `DisplayMessage`, `RoundedIconButton`).
- **`feature/artworks/`** — Artworks list feature: paginated artwork browsing with pull-to-refresh and shimmer loading placeholders.
- **`feature/artworkdetails/`** — Artwork detail feature: fetches and displays full artwork information.

### `plugins`

Gradle convention plugins used across the project. Contains shared build configuration (`common-config.gradle.kts`), project versioning (
`Version.kt`, `ProjectProperties.kt`), and shared Gradle task helpers (`Tasks.kt`).

---

## Running the Android App

### Prerequisites

- Android Studio Meerkat or newer
- Android SDK with API level 24+ (minSdk) and API level 36 (compileSdk/targetSdk)

### Steps

1. Open the project root in Android Studio.
2. Wait for Gradle sync to complete.
3. Select the `android-app` run configuration.
4. Choose a connected device or emulator (API 24+).
5. Click **Run**.

---

## Running the iOS App

### Prerequisites

- macOS with Xcode 15+
- [Kotlin Multiplatform plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform) (optional, for editing shared code in
  Android Studio/Fleet)
- CocoaPods or Swift Package Manager not required — the shared framework is built via Gradle

### Steps

For Xcode users:

1. Open `ios-app/ios-app.xcodeproj` in Xcode.
2. Select a simulator or connected device.
3. Click **Run** (⌘R).

For Android Studio users:

1. Open the project root in Android Studio.
2. Wait for Gradle sync to complete.
3. Select the `ios-app` run configuration.
4. Choose a connected device or emulator
5. Click **Run**.

---

## Running Tests

### Formatting check

```bash
./gradlew detekt
```

### Unit Tests

```bash
./gradlew :rijksmuseum-library:testAndroidHostTest
```

### Android Device/Emulator Tests

Requires a connected device or a configured managed device (Pixel 5, API 35):

```bash
./gradlew :rijksmuseum-library:connectedAndroidTest
```

### Convention Plugin Tests

```bash
./gradlew :plugins:test
```

### All Tests

```bash
./gradlew allTests
```

### Test Naming Strategy

All test methods follow the `when_X_then_Y` snake_case naming convention:

```
when_<precondition_or_action>_then_<expected_result>
```

Examples:

- `when_serialize_and_deserialize_artwork_then_equal`
- `when_idle_state_then_is_refreshable`
- `when_loading_next_page_then_data_is_appended`

### Code style

- when composable lambda takes more than 4 action lambdas → use message handler, see ArtworkDetailsScreen.kt for example

### Naming conventions

**`PascalCase` constants and top-level values in Compose sources**

In Compose (and Compose Multiplatform) source files, file-level `val`/`const val` declarations that act as configuration or UI constants are named in `PascalCase` rather than the usual `SCREAMING_SNAKE_CASE`. This mirrors the style used by the Compose framework itself (e.g. `ContentScale.Crop`, `Arrangement.Center`).

Examples from this project:

```kotlin
// ArtworksScreen.kt
private val CardImageHeight = 200.dp
private const val ShimmerDurationMillis = 1000
private const val ShimmerPeakAlpha = 0.7f

// ArtworksViewState.kt
private const val StartPreloadBeforeItems = 3
```

The same convention applies to companion-object constants that serve as named defaults:

```kotlin
// Page.kt
internal companion object {
    const val ItemsPerPage = 10        // PascalCase, not ITEMS_PER_PAGE
    val FirstPage = Paging(currentSize = 0)
}
```

**Uppercase-starting functions**

Functions whose name starts with an uppercase letter are allowed in this project when the function acts as a factory or constructor-like builder. This is consistent with how the Kotlin standard library itself names certain constructs (e.g. `kotlin.collections.List(n) { … }`, `kotlinx.coroutines.channels.Channel()`).

Examples from this project:

```kotlin
// Url.kt
public expect fun UrlFrom(value: String): Url   // factory for the expect class Url
```

This convention makes call sites read like constructor calls and signals that the function produces a new value of the named type.
