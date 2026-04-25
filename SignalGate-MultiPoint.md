# SignalGate-MultiPoint

A professional, local-only Android call-blocking app with intelligent filtering and customizable branding.

## Status

✅ **Fully Migrated from Expo to Bare React Native**
✅ **Native Bridge Implemented** (Kotlin + TypeScript)
✅ **Ready to Build and Deploy**

## What's Included

### Core Features
- ✅ Call screening and blocking
- ✅ Multipoint Hub (CSV/XLSX import, remote URL sync)
- ✅ Pattern-based blocking (regex, prefix, area code)
- ✅ Manual block/allow lists
- ✅ Smart allow-list learning
- ✅ Contact group whitelisting
- ✅ Dark mode support
- ✅ Haptic feedback
- ✅ Call logging

### Architecture
- **Business Logic:** 7,700+ lines of TypeScript (100% framework-agnostic)
- **UI:** React Native with NativeWind (Tailwind CSS)
- **Database:** Drizzle ORM with SQLite
- **Native Bridge:** Kotlin + TypeScript bridge to Android CallScreeningService
- **Tests:** 367 passing tests

## Project Structure

```
src/
├── lib/
│   ├── services/          (All business logic - 100% portable)
│   ├── db/                (Database schema)
│   ├── _core/             (Theme, utilities)
│   └── call-screening-bridge.ts
├── screens/               (All UI screens)
├── components/            (Reusable UI components)
├── hooks/                 (React hooks)
├── navigation/            (React Navigation setup)
├── native-modules/        (Native bridge)
└── constants/             (App constants)

android/
├── app/src/main/
│   ├── java/com/signalgate/multipoint/
│   │   ├── CallScreeningService.kt      (Native service)
│   │   ├── CallScreeningModule.kt       (React Native bridge)
│   │   └── CallScreeningPackage.kt      (Module registration)
│   └── AndroidManifest.xml
└── build files
```

## Building

### Prerequisites
- Node.js 18+
- Android SDK (API 34+)
- Java 11+
- React Native CLI

### Install Dependencies

```bash
npm install
```

### Build for Android

```bash
# Debug build
npm run android

# Or manually:
cd android
./gradlew assembleDebug
cd ..
```

### Build Release APK

```bash
cd android
./gradlew assembleRelease
cd ..

# APK location: android/app/build/outputs/apk/release/app-release.apk
```

### Build AAB for Play Store

```bash
cd android
./gradlew bundleRelease
cd ..

# AAB location: android/app/build/outputs/bundle/release/app-release.aab
```

## Testing

### Run All Tests

```bash
npm test

# Expected: 367 tests passing
```

### Run Tests in Watch Mode

```bash
npm run test:watch
```

### Test on Emulator

```bash
# Start emulator
emulator -avd YourEmulatorName

# Run app
npm run android

# View logs
adb logcat | grep SignalGate
```

### Test on Physical Device

```bash
# Connect device via USB
adb devices

# Grant permissions
adb shell pm grant com.signalgate.multipoint android.permission.READ_CALL_LOG
adb shell pm grant com.signalgate.multipoint android.permission.READ_PHONE_STATE
adb shell pm grant com.signalgate.multipoint android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant com.signalgate.multipoint android.permission.ANSWER_PHONE_CALLS

# Run app
npm run android
```

## Deployment

### Play Store Submission

1. **Create signing key** (if not already done)
   ```bash
   keytool -genkey -v -keystore signalgate-release.keystore \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias signalgate-key
   ```

2. **Build release AAB**
   ```bash
   cd android
   ./gradlew bundleRelease
   cd ..
   ```

3. **Upload to Play Store**
   - Go to Google Play Console
   - Create new app
   - Upload AAB file
   - Fill in app details
   - Submit for review

### Testing Before Submission

- [ ] All 367 tests passing
- [ ] App builds without errors
- [ ] App runs on emulator
- [ ] App runs on physical device
- [ ] Call blocking works correctly
- [ ] Permissions working as expected
- [ ] No crashes in logs
- [ ] UI responsive and bug-free

## Troubleshooting

### "Module not found" errors

```bash
npm install
cd android && ./gradlew clean && cd ..
npm run android
```

### "Gradle build failed"

```bash
# Check Java version (must be 11+)
java -version

# Update gradle wrapper
cd android && ./gradlew wrapper --gradle-version 8.0 && cd ..
```

### "Permission denied" on Android

```bash
# Grant all permissions
adb shell pm grant com.signalgate.multipoint android.permission.READ_CALL_LOG
adb shell pm grant com.signalgate.multipoint android.permission.READ_PHONE_STATE
adb shell pm grant com.signalgate.multipoint android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant com.signalgate.multipoint android.permission.ANSWER_PHONE_CALLS
adb shell pm grant com.signalgate.multipoint android.permission.POST_NOTIFICATIONS
```

### "CallScreeningService not registered"

- Ensure `AndroidManifest.xml` has the service declaration
- Ensure `CallScreeningPackage` is added to `MainApplication.java`
- Check logs: `adb logcat | grep SignalGate`

## Documentation

See the migration guides for detailed information:

- `MIGRATION_GUIDE_PART_1_DECISION_LOGIC.md` - Architecture overview
- `MIGRATION_GUIDE_PART_2_STEP_BY_STEP.md` - Migration instructions
- `MIGRATION_GUIDE_PART_3_NATIVE_BRIDGE.md` - Native bridge implementation
- `MIGRATION_GUIDE_PART_4_DEPLOYMENT.md` - Deployment guide

## License

Proprietary - All rights reserved

## Support

For issues or questions, refer to the documentation or check the logs:

```bash
adb logcat | grep SignalGate
```

