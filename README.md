# Signal Gate Multi-Path

A native Android application built with Kotlin for high-performance call screening and blocking. This project has been migrated from React Native to pure Android to ensure maximum reliability and minimal APK size.

## Project Structure

```text
.
├── android
│   ├── app
│   │   ├── build.gradle            # App-level config (Room, KSP, Minification)
│   │   └── src
│   │       └── main
│   │           ├── AndroidManifest.xml
│   │           ├── java/com/signalgate/multipoint
│   │           │   ├── CallScreeningService.kt  # Core blocking logic
│   │           │   ├── MainActivity.kt         # Entry point
│   │           │   ├── MainApplication.kt      # App initialization
│   │           │   └── db
│   │           │       ├── AppDatabase.kt      # Room Database definition
│   │           │       └── BlockEntry.kt       # Entity & DAO for blocklist
│   │           └── res/            # Resources (icons, strings, themes)
│   └── build.gradle                # Project-level config
└── README.md
```

## Features

- **Native CallScreeningService**: Implements `android.telecom.CallScreeningService` for direct system-level call interception.
- **Local Persistence**: Uses **Room Database** for high-performance, local storage of blocked numbers and patterns.
- **Smart Filtering**: Supports both exact number matching and prefix/pattern-based blocking.
- **Optimized Performance**: 
  - **R8/ProGuard**: Enabled for code shrinking and obfuscation.
  - **Resource Shrinking**: Removes unused resources to minimize APK size.
  - **Asynchronous Processing**: Uses Kotlin Coroutines for non-blocking database operations.

## Setup & Build

1. Open the `android` folder in Android Studio.
2. Ensure you have the Android SDK for API 34 installed.
3. Build the project using `./gradlew assembleRelease` to generate the optimized APK.

## Permissions

The app requires the following permissions to function as a default call screening app:
- `android.permission.BIND_SCREENING_SERVICE`
- `android.permission.READ_PHONE_STATE`
- `android.permission.READ_CALL_LOG`
