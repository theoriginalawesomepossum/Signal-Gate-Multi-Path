# SignalGate-MultiPoint: Migration Guide - Part 2
## Step-by-Step Migration Instructions (Bare React Native)

**Estimated Time:** 3-4 hours  
**Difficulty:** Intermediate (Network engineering background sufficient)  
**Prerequisites:** Node.js 18+, Android SDK, Java 11+

---

## DECISION: Which Path?

### Path A: Bare React Native (RECOMMENDED)
- **Pros:** Keeps React/TypeScript, adds minimal native code, easiest migration
- **Cons:** Requires Android build setup
- **Time:** 3-4 hours
- **Use this if:** You want to keep React for UI

### Path B: Full Native (Kotlin + Jetpack Compose)
- **Pros:** Pure native, best performance
- **Cons:** Requires rewriting all UI in Kotlin
- **Time:** 20+ hours
- **Use this if:** You want maximum control

**RECOMMENDATION: Use Path A (Bare React Native)**

---

## PATH A: BARE REACT NATIVE MIGRATION

### Phase 1: Project Setup (30 minutes)

#### Step 1.1: Create new bare React Native project

```bash
# Create the new bare RN project
npx react-native init SignalGateMultiPoint --template react-native-template-typescript

cd SignalGateMultiPoint
```

#### Step 1.2: Install dependencies

```bash
npm install --save \
  @react-navigation/native \
  @react-navigation/bottom-tabs \
  react-native-screens \
  react-native-safe-area-context \
  react-native-gesture-handler \
  react-native-reanimated \
  react-native-svg \
  nativewind \
  tailwindcss \
  @react-native-async-storage/async-storage \
  @tanstack/react-query \
  @trpc/client \
  @trpc/react-query \
  @trpc/server \
  drizzle-orm \
  better-sqlite3 \
  react-native-blur \
  react-native-haptics \
  @react-native-community/hooks \
  axios \
  clsx \
  tailwind-merge \
  zod \
  superjson
```

#### Step 1.3: Install dev dependencies

```bash
npm install --save-dev \
  @types/react \
  @types/react-native \
  @types/node \
  typescript \
  tailwindcss \
  postcss \
  autoprefixer \
  prettier \
  eslint \
  @react-native/eslint-config \
  vitest \
  drizzle-kit
```

---

### Phase 2: Copy Business Logic (15 minutes)

#### Step 2.1: Create directory structure

```bash
mkdir -p src/lib/{services,db}
mkdir -p src/constants
mkdir -p src/shared/_core
mkdir -p src/hooks
mkdir -p tests
```

#### Step 2.2: Copy all services (NO CHANGES NEEDED)

```bash
# From your Expo project
cp /home/ubuntu/signalgate-poc/lib/services/*.ts src/lib/services/
cp /home/ubuntu/signalgate-poc/lib/services/*.test.ts tests/

# Copy database schema
cp /home/ubuntu/signalgate-poc/lib/db/schema.ts src/lib/db/
cp /home/ubuntu/signalgate-poc/drizzle/schema.ts src/lib/db/
cp /home/ubuntu/signalgate-poc/drizzle/relations.ts src/lib/db/

# Copy utilities
cp /home/ubuntu/signalgate-poc/lib/utils.ts src/lib/
cp /home/ubuntu/signalgate-poc/lib/prediction-modeling.ts src/lib/
cp /home/ubuntu/signalgate-poc/lib/prediction-modeling.test.ts tests/

# Copy constants
cp /home/ubuntu/signalgate-poc/constants/*.ts src/constants/
cp /home/ubuntu/signalgate-poc/shared/*.ts src/shared/
cp /home/ubuntu/signalgate-poc/shared/_core/*.ts src/shared/_core/
```

#### Step 2.3: Verify all tests still pass

```bash
npm test

# Expected output: 367 tests passing
```

---

### Phase 3: Copy and Adapt UI Components (45 minutes)

#### Step 3.1: Create UI directory structure

```bash
mkdir -p src/components/ui
mkdir -p src/screens/{tabs,modals}
mkdir -p src/hooks
mkdir -p src/lib/_core
```

#### Step 3.2: Copy components (with minimal changes)

**Copy these as-is:**
```bash
cp /home/ubuntu/signalgate-poc/components/screen-container.tsx src/components/
cp /home/ubuntu/signalgate-poc/components/data-source-card.tsx src/components/
cp /home/ubuntu/signalgate-poc/components/themed-view.tsx src/components/
cp /home/ubuntu/signalgate-poc/components/ui/collapsible.tsx src/components/ui/
cp /home/ubuntu/signalgate-poc/components/ui/icon-symbol.tsx src/components/ui/
```

**Adapt these (see detailed changes below):**
```bash
# These need Expo → React Native changes
cp /home/ubuntu/signalgate-poc/components/frosted-overlay.tsx src/components/
cp /home/ubuntu/signalgate-poc/components/haptic-tab.tsx src/components/
```

#### Step 3.3: Update frosted-overlay.tsx

**Change this line:**
```typescript
// FROM:
import { BlurView } from 'expo-blur';

// TO:
import { BlurView } from '@react-native-blur/blur-view';
```

**Change this line:**
```typescript
// FROM:
import * as Haptics from 'expo-haptics';

// TO:
import RNHaptics from 'react-native-haptics';
```

**Change haptic calls:**
```typescript
// FROM:
Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);

// TO:
RNHaptics.trigger('impactLight');
```

#### Step 3.4: Update haptic-tab.tsx

```typescript
// FROM:
import * as Haptics from 'expo-haptics';

// TO:
import RNHaptics from 'react-native-haptics';

// Change all Haptics calls to RNHaptics.trigger()
```

#### Step 3.5: Copy theme and styling

```bash
cp /home/ubuntu/signalgate-poc/theme.config.js src/
cp /home/ubuntu/signalgate-poc/tailwind.config.js src/
cp /home/ubuntu/signalgate-poc/global.css src/
```

#### Step 3.6: Copy hooks

```bash
cp /home/ubuntu/signalgate-poc/hooks/*.ts src/hooks/
```

---

### Phase 4: Copy and Adapt Screens (60 minutes)

#### Step 4.1: Copy all screen files

```bash
cp /home/ubuntu/signalgate-poc/app/permission-wizard.tsx src/screens/
cp /home/ubuntu/signalgate-poc/app/\(tabs\)/*.tsx src/screens/tabs/
```

#### Step 4.2: Update imports in all screens

**In each screen file, change:**
```typescript
// FROM:
import { ScreenContainer } from "@/components/screen-container";

// TO:
import { ScreenContainer } from "../components/screen-container";
```

**Change all Expo imports:**
```typescript
// FROM:
import { useColorScheme } from 'expo-appearance';

// TO:
import { useColorScheme } from 'react-native';
```

#### Step 4.3: Create root navigation

**Create `src/navigation/RootNavigator.tsx`:**

```typescript
import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

// Import screens
import HomeScreen from '../screens/tabs/index';
import CallLogScreen from '../screens/tabs/call-log';
import BlockAllowListScreen from '../screens/tabs/block-allow-list';
import PatternRulesScreen from '../screens/tabs/pattern-rules';
import SourcesScreen from '../screens/tabs/sources';
import AdvancedSettingsScreen from '../screens/tabs/advanced-settings';
import PredictionDashboardScreen from '../screens/tabs/prediction-dashboard';
import GlowCustomizationScreen from '../screens/tabs/glow-customization';
import PermissionWizardScreen from '../screens/permission-wizard';

const Tab = createBottomTabNavigator();
const Stack = createNativeStackNavigator();

function TabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: '#0a7ea4',
      }}
    >
      <Tab.Screen name="Home" component={HomeScreen} />
      <Tab.Screen name="Sources" component={SourcesScreen} />
      <Tab.Screen name="CallLog" component={CallLogScreen} />
      <Tab.Screen name="BlockAllow" component={BlockAllowListScreen} />
      <Tab.Screen name="Patterns" component={PatternRulesScreen} />
      <Tab.Screen name="Predictions" component={PredictionDashboardScreen} />
      <Tab.Screen name="Glow" component={GlowCustomizationScreen} />
      <Tab.Screen name="Settings" component={AdvancedSettingsScreen} />
    </Tab.Navigator>
  );
}

export function RootNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerShown: false,
        }}
      >
        <Stack.Screen name="PermissionWizard" component={PermissionWizardScreen} />
        <Stack.Screen name="MainApp" component={TabNavigator} />
      </Stack.Navigator>
    </NavigationContainer>
  );
}
```

#### Step 4.4: Update App.tsx

```typescript
import React from 'react';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RootNavigator } from './navigation/RootNavigator';
import { ThemeProvider } from './lib/theme-provider';

const queryClient = new QueryClient();

export default function App() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <QueryClientProvider client={queryClient}>
          <ThemeProvider>
            <RootNavigator />
          </ThemeProvider>
        </QueryClientProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
```

---

### Phase 5: Android Configuration (45 minutes)

#### Step 5.1: Update AndroidManifest.xml

**Location:** `android/app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.signalgate.multipoint">

    <!-- Required permissions for call screening -->
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- CallScreeningService declaration -->
        <service
            android:name=".CallScreeningService"
            android:permission="android.permission.BIND_SCREENING_SERVICE"
            android:exported="true">
            <intent-filter>
                <action android:name="android.telecom.CallScreeningService" />
            </intent-filter>
        </service>

    </application>

</manifest>
```

#### Step 5.2: Update build.gradle (app level)

**Location:** `android/app/build.gradle`

```gradle
android {
    compileSdkVersion 34
    
    defaultConfig {
        applicationId "com.signalgate.multipoint"
        minSdkVersion 24
        targetSdkVersion 34
        versionCode 1
        versionName "1.0.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
}

dependencies {
    // Kotlin
    implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.9.0'
    
    // Android X
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.core:core:1.12.0'
    implementation 'androidx.telecom:telecom:1.0.0-alpha01'
    
    // React Native
    implementation project(':react-native')
}
```

---

### Phase 6: Create Native Bridge (90 minutes)

**This is covered in PART 3 of this guide.**

---

### Phase 7: Build and Test (30 minutes)

#### Step 7.1: Build the Android app

```bash
# Build debug APK
cd android
./gradlew assembleDebug

# Or build for emulator
./gradlew installDebug
```

#### Step 7.2: Run on emulator

```bash
npm run android
```

#### Step 7.3: Run tests

```bash
npm test

# Should show: 367 tests passing
```

---

## MIGRATION CHECKLIST

- [ ] Phase 1: Project setup complete
- [ ] Phase 2: All services copied and tests passing
- [ ] Phase 3: UI components copied and adapted
- [ ] Phase 4: Screens copied and navigation working
- [ ] Phase 5: Android configuration updated
- [ ] Phase 6: Native bridge implemented (see Part 3)
- [ ] Phase 7: Build successful and tests passing
- [ ] App runs on emulator
- [ ] Permission Wizard displays
- [ ] All tabs accessible
- [ ] Call screening service registers

---

## TROUBLESHOOTING

### Issue: "Module not found" errors

**Solution:**
```bash
# Clear cache and reinstall
npm install
cd android && ./gradlew clean && cd ..
npm run android
```

### Issue: "Gradle build failed"

**Solution:**
```bash
# Check Java version (must be 11+)
java -version

# Update gradle wrapper
cd android && ./gradlew wrapper --gradle-version 8.0 && cd ..
```

### Issue: "Permission denied" on Android

**Solution:**
- Ensure `AndroidManifest.xml` has all required permissions
- Grant permissions in app settings on device
- Restart app after granting permissions

### Issue: Tests failing

**Solution:**
```bash
# Ensure all services copied correctly
npm test -- --reporter=verbose

# Check for import path mismatches
grep -r "from '@/" src/lib/services/
```

---

## NEXT STEPS

**Part 3** will provide:
- Complete Kotlin native bridge code
- TypeScript bridge layer
- Integration with CallScreeningService

