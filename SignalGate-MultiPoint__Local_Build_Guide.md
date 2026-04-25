# SignalGate-MultiPoint: Local Build Guide

**Status:** Project ready for local development  
**Tested:** All 367 tests passing ✅  
**Next Step:** Build on your local machine

---

## Why Local Build?

The cloud sandbox environment doesn't include Android SDK, which is required to build native Android apps. You'll need to set up Android development on your local machine.

**Good news:** The setup is straightforward and takes 30-60 minutes.

---

## Prerequisites

### System Requirements
- **macOS:** 10.14 or later
- **Windows:** Windows 10 or later
- **Linux:** Ubuntu 18.04 or later
- **RAM:** 8GB minimum (16GB recommended)
- **Disk Space:** 20GB minimum

### Software Requirements
- **Node.js:** 18+ (check: `node --version`)
- **npm:** 9+ (check: `npm --version`)
- **Java:** 11+ (check: `java -version`)
- **Git:** Latest version

---

## Step 1: Download the Project

### Option A: Download from Cloud

```bash
# The project is available at:
# /home/ubuntu/SignalGateMultiPoint-migrated.tar.gz (98 MB)

# Download and extract:
tar -xzf SignalGateMultiPoint-migrated.tar.gz
cd SignalGateMultiPoint
```

### Option B: Clone from Git (if you have a repo)

```bash
git clone <your-repo-url>
cd SignalGateMultiPoint
```

---

## Step 2: Install Android SDK

### macOS (using Homebrew)

```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Android SDK
brew install android-sdk

# Set environment variables
export ANDROID_HOME=/usr/local/share/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### Windows (using Android Studio)

1. Download [Android Studio](https://developer.android.com/studio)
2. Run the installer
3. During installation, select:
   - Android SDK
   - Android SDK Platform-Tools
   - Android SDK Build-Tools
   - Android API 33 (or higher)
4. Set environment variable:
   - `ANDROID_HOME` = `C:\Users\YourUsername\AppData\Local\Android\Sdk`
   - Add to `PATH`: `%ANDROID_HOME%\platform-tools`

### Linux (Ubuntu/Debian)

```bash
# Install Java
sudo apt-get update
sudo apt-get install openjdk-11-jdk

# Download Android SDK
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip
unzip commandlinetools-linux-10406996_latest.zip

# Extract and setup
mkdir -p ~/Android/Sdk
mv cmdline-tools ~/Android/Sdk/

# Set environment variables
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

---

## Step 3: Verify Android SDK Installation

```bash
# Check Android SDK location
echo $ANDROID_HOME

# Should output something like:
# /usr/local/share/android-sdk (macOS)
# C:\Users\YourUsername\AppData\Local\Android\Sdk (Windows)
# /home/username/Android/Sdk (Linux)

# Verify adb is available
adb version

# Should output version info
```

---

## Step 4: Install Project Dependencies

```bash
cd /path/to/SignalGateMultiPoint

# Install npm dependencies
npm install

# Expected output:
# added 690 packages, audited 690 packages in XXs
```

---

## Step 5: Run Tests

```bash
# Run all tests
npm test

# Expected output:
# Test Files  13 passed (13)
# Tests  367 passed (367)
```

---

## Step 6: Build for Android

### Option A: Build Debug APK

```bash
# Build debug APK
npm run android

# Or manually:
cd android
./gradlew assembleDebug
cd ..

# Output location:
# android/app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Build Release APK

```bash
# Build release APK
cd android
./gradlew assembleRelease
cd ..

# Output location:
# android/app/build/outputs/apk/release/app-release.apk
```

### Option C: Build AAB for Play Store

```bash
# Build Android App Bundle
cd android
./gradlew bundleRelease
cd ..

# Output location:
# android/app/build/outputs/bundle/release/app-release.aab
```

---

## Step 7: Test on Emulator

### Create Android Emulator

```bash
# List available emulators
emulator -list-avds

# Create a new emulator (if needed)
avdmanager create avd -n SignalGateEmulator -k "system-images;android-33;google_apis;arm64-v8a"

# Start the emulator
emulator -avd SignalGateEmulator
```

### Install and Run App

```bash
# Install debug APK on emulator
adb install android/app/build/outputs/apk/debug/app-debug.apk

# Or use npm script
npm run android

# View logs
adb logcat | grep SignalGate
```

---

## Step 8: Test on Physical Device

### Enable Developer Mode

1. Go to **Settings** → **About Phone**
2. Tap **Build Number** 7 times
3. Go back to **Settings** → **Developer Options**
4. Enable **USB Debugging**

### Connect Device

```bash
# Connect via USB
# Check connection
adb devices

# Should show your device:
# emulator-5554        device
# or your device serial number

# Grant permissions
adb shell pm grant com.signalgate.multipoint android.permission.READ_CALL_LOG
adb shell pm grant com.signalgate.multipoint android.permission.READ_PHONE_STATE
adb shell pm grant com.signalgate.multipoint android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant com.signalgate.multipoint android.permission.ANSWER_PHONE_CALLS
adb shell pm grant com.signalgate.multipoint android.permission.POST_NOTIFICATIONS

# Install app
adb install android/app/build/outputs/apk/debug/app-debug.apk

# Open app
adb shell am start -n com.signalgate.multipoint/.MainActivity
```

---

## Step 9: Test Call Blocking

### On Emulator

```bash
# Use telnet to simulate calls
telnet localhost 5554

# In telnet:
gsm call +12125551234
gsm accept
gsm hangup
```

### On Physical Device

1. Have someone call the test number
2. Verify the call is blocked/allowed based on rules
3. Check call log in app
4. Monitor logs: `adb logcat | grep SignalGate`

---

## Troubleshooting

### Issue: "ANDROID_HOME not set"

**Solution:**
```bash
# Set ANDROID_HOME
export ANDROID_HOME=/path/to/android/sdk

# Add to ~/.bashrc or ~/.zshrc for permanent setting
echo 'export ANDROID_HOME=/path/to/android/sdk' >> ~/.bashrc
source ~/.bashrc
```

### Issue: "SDK location not found"

**Solution:**
```bash
# Create local.properties in android directory
cd android
echo "sdk.dir=/path/to/android/sdk" > local.properties
cd ..
```

### Issue: "Gradle build failed"

**Solution:**
```bash
# Clean gradle cache
cd android
./gradlew clean
cd ..

# Retry build
npm run android
```

### Issue: "Module not found"

**Solution:**
```bash
# Reinstall dependencies
npm install
npm run android
```

### Issue: "Permission denied" on device

**Solution:**
```bash
# Grant all permissions
adb shell pm grant com.signalgate.multipoint android.permission.READ_CALL_LOG
adb shell pm grant com.signalgate.multipoint android.permission.READ_PHONE_STATE
adb shell pm grant com.signalgate.multipoint android.permission.SYSTEM_ALERT_WINDOW
adb shell pm grant com.signalgate.multipoint android.permission.ANSWER_PHONE_CALLS
adb shell pm grant com.signalgate.multipoint android.permission.POST_NOTIFICATIONS
```

### Issue: "App crashes on startup"

**Solution:**
```bash
# Check logs
adb logcat | grep SignalGate

# Common causes:
# 1. Missing permissions
# 2. Database initialization issue
# 3. Native module not loaded

# Check native module:
adb logcat | grep "CallScreeningModule"
```

---

## Build Commands Reference

```bash
# Development
npm install              # Install dependencies
npm test                 # Run all tests
npm run type-check       # TypeScript check
npm run lint             # Lint code
npm run format           # Format code

# Android builds
npm run android          # Build and run on emulator/device
cd android && ./gradlew assembleDebug && cd ..      # Debug APK
cd android && ./gradlew assembleRelease && cd ..    # Release APK
cd android && ./gradlew bundleRelease && cd ..      # Play Store AAB

# Debugging
adb logcat              # View device logs
adb devices             # List connected devices
adb shell pm grant ...  # Grant permissions
adb install <apk>       # Install APK
```

---

## Next Steps

1. **Set up Android SDK** on your local machine
2. **Download the project** from the cloud
3. **Install dependencies:** `npm install`
4. **Run tests:** `npm test` (verify 367 passing)
5. **Build for Android:** `npm run android`
6. **Test on emulator or device**
7. **Build release APK/AAB** for Play Store
8. **Submit to Play Store**

---

## Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review the Android documentation: https://developer.android.com/
3. Check React Native docs: https://reactnative.dev/
4. Review project README: `README.md`
5. Check migration guides in `/home/ubuntu/`

---

## Estimated Timeline

| Step | Time |
|------|------|
| Download project | 5 min |
| Install Android SDK | 30-60 min |
| Install dependencies | 5 min |
| Run tests | 2 min |
| Build for Android | 10-20 min |
| Test on emulator | 10 min |
| **Total** | **60-100 min** |

---

## You're Ready! 🚀

Everything is prepared for local development. Follow these steps and you'll have the app running on your device in about an hour.

Good luck! 💪

