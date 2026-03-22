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
