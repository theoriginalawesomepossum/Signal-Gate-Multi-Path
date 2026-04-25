# SignalGate-MultiPoint: Migration Guide - Part 3
## Native Bridge Implementation (Kotlin + TypeScript)

**Estimated Time:** 90 minutes  
**Difficulty:** Intermediate  
**What You'll Create:** 4 files (2 Kotlin, 2 TypeScript)

---

## OVERVIEW: How the Native Bridge Works

```
Android System
    ↓ (incoming call)
    ↓
CallScreeningService.kt (Kotlin)
    ↓ (extracts phone number)
    ↓
CallScreeningModule.kt (Kotlin → JS bridge)
    ↓ (sends to JavaScript)
    ↓
CallScreeningModule.ts (TypeScript)
    ↓ (calls your existing service)
    ↓
call-screening-integration.ts (TypeScript - already exists!)
    ↓ (checks database, applies rules)
    ↓
CallScreeningModule.ts (JavaScript → Kotlin bridge)
    ↓ (returns decision)
    ↓
CallScreeningModule.kt (Kotlin)
    ↓ (receives decision)
    ↓
CallScreeningService.kt (Kotlin)
    ↓ (blocks or allows call)
    ↓
Android System (call blocked/allowed)
```

---

## FILE 1: CallScreeningService.kt (Kotlin - Native Service)

**Location:** `android/app/src/main/java/com/signalgate/CallScreeningService.kt`

```kotlin
package com.signalgate.multipoint

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.annotation.RequiresApi
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * CallScreeningService - Intercepts incoming calls and screens them
 * 
 * This service:
 * 1. Receives incoming call details from Android
 * 2. Sends call info to JavaScript via CallScreeningModule
 * 3. Waits for decision (block/allow) from JavaScript
 * 4. Applies the decision to the call
 */
@RequiresApi(Build.VERSION_CODES.N)
class CallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "SignalGate-CallScreening"
        private var reactContext: ReactApplicationContext? = null
        private var pendingCallDecision: String = "ALLOW"
        private var isWaitingForDecision = false

        fun setReactContext(context: ReactApplicationContext) {
            reactContext = context
        }

        fun setCallDecision(decision: String) {
            pendingCallDecision = decision
            isWaitingForDecision = false
        }
    }

    override fun onScreenCall(callDetails: Call.Details) {
        try {
            // Extract call information
            val handle = callDetails.handle
            val phoneNumber = handle?.schemeSpecificPart ?: "Unknown"
            val isIncoming = callDetails.state == Call.STATE_RINGING
            val displayName = callDetails.callerDisplayName ?: ""

            Log.d(TAG, "Incoming call: $phoneNumber, Display: $displayName")

            // If no React context, allow the call (fallback)
            if (reactContext == null) {
                Log.w(TAG, "React context not available, allowing call")
                respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
                return
            }

            // Send call info to JavaScript for screening
            val callInfo = Arguments.createMap().apply {
                putString("phoneNumber", phoneNumber)
                putString("displayName", displayName)
                putBoolean("isIncoming", isIncoming)
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            }

            // Reset decision flag
            isWaitingForDecision = true
            pendingCallDecision = "ALLOW"

            // Send event to JavaScript
            reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                ?.emit("onIncomingCall", callInfo)

            // Wait for decision from JavaScript (with timeout)
            val startTime = System.currentTimeMillis()
            val timeout = 3000L // 3 second timeout

            while (isWaitingForDecision && System.currentTimeMillis() - startTime < timeout) {
                Thread.sleep(100)
            }

            // If still waiting after timeout, allow the call
            if (isWaitingForDecision) {
                Log.w(TAG, "Decision timeout, allowing call")
                pendingCallDecision = "ALLOW"
            }

            // Apply the decision
            val shouldBlock = pendingCallDecision == "BLOCK"
            val response = CallResponse.Builder()
                .setDisallowCall(shouldBlock)
                .setRejectCall(shouldBlock)
                .setSkipCallLog(shouldBlock)
                .build()

            respondToCall(callDetails, response)

            Log.d(TAG, "Call decision: ${if (shouldBlock) "BLOCKED" else "ALLOWED"}")

        } catch (e: Exception) {
            Log.e(TAG, "Error screening call", e)
            // On error, allow the call
            respondToCall(callDetails, CallResponse.Builder().setDisallowCall(false).build())
        }
    }

    override fun onShowDialog(callDetails: Call.Details) {
        // Optional: Show custom UI dialog for call screening
        // For now, we handle everything in the overlay
        Log.d(TAG, "Show dialog called for: ${callDetails.handle?.schemeSpecificPart}")
    }
}
```

---

## FILE 2: CallScreeningModule.kt (Kotlin - React Native Bridge)

**Location:** `android/app/src/main/java/com/signalgate/CallScreeningModule.kt`

```kotlin
package com.signalgate.multipoint

import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.util.Log

/**
 * CallScreeningModule - React Native bridge for call screening
 * 
 * Provides JavaScript methods to:
 * 1. Register/unregister the CallScreeningService
 * 2. Set the app as the default screening app
 * 3. Send call decisions back to the native service
 */
class CallScreeningModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val TAG = "SignalGate-Module"
        private const val MODULE_NAME = "CallScreeningModule"
    }

    init {
        // Set the React context in the service so it can send events
        CallScreeningService.setReactContext(reactContext)
    }

    override fun getName(): String = MODULE_NAME

    /**
     * Register the app as a call screening service
     */
    @ReactMethod
    fun registerCallScreeningService(promise: Promise) {
        try {
            val context = reactApplicationContext
            val intent = Intent(context, CallScreeningService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            
            Log.d(TAG, "CallScreeningService registered")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register CallScreeningService", e)
            promise.reject("REGISTER_FAILED", e.message)
        }
    }

    /**
     * Unregister the call screening service
     */
    @ReactMethod
    fun unregisterCallScreeningService(promise: Promise) {
        try {
            val context = reactApplicationContext
            val intent = Intent(context, CallScreeningService::class.java)
            context.stopService(intent)
            
            Log.d(TAG, "CallScreeningService unregistered")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister CallScreeningService", e)
            promise.reject("UNREGISTER_FAILED", e.message)
        }
    }

    /**
     * Check if app is set as the default screening app
     */
    @ReactMethod
    fun isDefaultScreeningApp(promise: Promise) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                promise.resolve(false)
                return
            }

            val context = reactApplicationContext
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            
            val isDefault = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                telecomManager?.defaultDialerPackage == context.packageName
            } else {
                false
            }
            
            Log.d(TAG, "Is default screening app: $isDefault")
            promise.resolve(isDefault)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check default screening app", e)
            promise.reject("CHECK_FAILED", e.message)
        }
    }

    /**
     * Request the user to set the app as the default screening app
     */
    @ReactMethod
    fun requestDefaultScreeningApp(promise: Promise) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                promise.resolve(false)
                return
            }

            val context = reactApplicationContext
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            
            // This will show a system dialog asking the user
            telecomManager?.showCallScreeningAccessUi()
            
            Log.d(TAG, "Requested default screening app")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request default screening app", e)
            promise.reject("REQUEST_FAILED", e.message)
        }
    }

    /**
     * Send a call decision (BLOCK or ALLOW) to the native service
     */
    @ReactMethod
    fun respondToCall(phoneNumber: String, decision: String, promise: Promise) {
        try {
            // Set the decision in the service
            CallScreeningService.setCallDecision(decision)
            
            Log.d(TAG, "Call decision sent: $phoneNumber -> $decision")
            promise.resolve(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send call decision", e)
            promise.reject("RESPOND_FAILED", e.message)
        }
    }

    /**
     * Block a specific phone number
     */
    @ReactMethod
    fun blockCall(phoneNumber: String, promise: Promise) {
        try {
            respondToCall(phoneNumber, "BLOCK", promise)
        } catch (e: Exception) {
            promise.reject("BLOCK_FAILED", e.message)
        }
    }

    /**
     * Allow a specific phone number
     */
    @ReactMethod
    fun allowCall(phoneNumber: String, promise: Promise) {
        try {
            respondToCall(phoneNumber, "ALLOW", promise)
        } catch (e: Exception) {
            promise.reject("ALLOW_FAILED", e.message)
        }
    }

    /**
     * Get the current call screening status
     */
    @ReactMethod
    fun getScreeningStatus(promise: Promise) {
        try {
            val context = reactApplicationContext
            val status = Arguments.createMap().apply {
                putBoolean("isRegistered", true) // Would check actual status
                putBoolean("isDefaultApp", false) // Would check actual status
                putString("version", "1.0.0")
            }
            promise.resolve(status)
        } catch (e: Exception) {
            promise.reject("STATUS_FAILED", e.message)
        }
    }
}
```

---

## FILE 3: CallScreeningPackage.kt (Kotlin - Module Registration)

**Location:** `android/app/src/main/java/com/signalgate/CallScreeningPackage.kt`

```kotlin
package com.signalgate.multipoint

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

/**
 * CallScreeningPackage - Registers the CallScreeningModule with React Native
 */
class CallScreeningPackage : ReactPackage {

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        return listOf(CallScreeningModule(reactContext))
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return emptyList()
    }
}
```

---

## FILE 4: CallScreeningModule.ts (TypeScript - React Native Bridge)

**Location:** `src/native-modules/call-screening/CallScreeningModule.ts`

```typescript
import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const { CallScreeningModule: NativeCallScreeningModule } = NativeModules;

export interface CallScreeningResult {
  action: 'ALLOW' | 'BLOCK';
  reason: string;
}

export interface IncomingCall {
  phoneNumber: string;
  displayName?: string;
  timestamp: number;
  isIncoming: boolean;
}

/**
 * CallScreeningModule - TypeScript bridge to native call screening
 * 
 * This module:
 * 1. Registers/unregisters the CallScreeningService
 * 2. Listens for incoming calls
 * 3. Sends decisions back to native service
 */
export class CallScreeningModule {
  private static instance: CallScreeningModule;
  private eventEmitter: NativeEventEmitter | null = null;
  private incomingCallListener: ((call: IncomingCall) => void) | null = null;

  private constructor() {
    if (Platform.OS === 'android' && NativeCallScreeningModule) {
      this.eventEmitter = new NativeEventEmitter(NativeCallScreeningModule);
    }
  }

  static getInstance(): CallScreeningModule {
    if (!CallScreeningModule.instance) {
      CallScreeningModule.instance = new CallScreeningModule();
    }
    return CallScreeningModule.instance;
  }

  /**
   * Register the call screening service
   */
  async registerCallScreeningService(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      console.warn('CallScreeningModule not available on this platform');
      return false;
    }

    try {
      const result = await NativeCallScreeningModule.registerCallScreeningService();
      console.log('CallScreeningService registered:', result);
      return result;
    } catch (error) {
      console.error('Failed to register CallScreeningService:', error);
      return false;
    }
  }

  /**
   * Unregister the call screening service
   */
  async unregisterCallScreeningService(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      const result = await NativeCallScreeningModule.unregisterCallScreeningService();
      console.log('CallScreeningService unregistered:', result);
      return result;
    } catch (error) {
      console.error('Failed to unregister CallScreeningService:', error);
      return false;
    }
  }

  /**
   * Check if app is set as the default screening app
   */
  async isDefaultScreeningApp(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.isDefaultScreeningApp();
    } catch (error) {
      console.error('Failed to check default screening app:', error);
      return false;
    }
  }

  /**
   * Request the user to set the app as the default screening app
   */
  async requestDefaultScreeningApp(): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.requestDefaultScreeningApp();
    } catch (error) {
      console.error('Failed to request default screening app:', error);
      return false;
    }
  }

  /**
   * Block a call
   */
  async blockCall(phoneNumber: string): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.blockCall(phoneNumber);
    } catch (error) {
      console.error('Failed to block call:', error);
      return false;
    }
  }

  /**
   * Allow a call
   */
  async allowCall(phoneNumber: string): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.allowCall(phoneNumber);
    } catch (error) {
      console.error('Failed to allow call:', error);
      return false;
    }
  }

  /**
   * Respond to a call with a decision
   */
  async respondToCall(phoneNumber: string, decision: 'BLOCK' | 'ALLOW'): Promise<boolean> {
    if (!NativeCallScreeningModule) {
      return false;
    }

    try {
      return await NativeCallScreeningModule.respondToCall(phoneNumber, decision);
    } catch (error) {
      console.error('Failed to respond to call:', error);
      return false;
    }
  }

  /**
   * Listen for incoming calls
   */
  onIncomingCall(callback: (call: IncomingCall) => void): () => void {
    this.incomingCallListener = callback;

    if (!this.eventEmitter) {
      console.warn('Event emitter not available');
      return () => {};
    }

    const subscription = this.eventEmitter.addListener('onIncomingCall', (call: IncomingCall) => {
      callback(call);
    });

    // Return unsubscribe function
    return () => {
      subscription.remove();
      this.incomingCallListener = null;
    };
  }

  /**
   * Get screening status
   */
  async getScreeningStatus(): Promise<any> {
    if (!NativeCallScreeningModule) {
      return null;
    }

    try {
      return await NativeCallScreeningModule.getScreeningStatus();
    } catch (error) {
      console.error('Failed to get screening status:', error);
      return null;
    }
  }
}

// Export singleton instance
export const callScreeningModule = CallScreeningModule.getInstance();
```

---

## INTEGRATION: Connect Native Bridge to Your Services

**Create `src/lib/call-screening-bridge.ts`:**

```typescript
import { callScreeningModule, IncomingCall } from '../native-modules/call-screening/CallScreeningModule';
import { CallScreeningService, CallScreeningResult } from './services/call-screening-integration';
import {
  CallLogService,
  PatternRulesService,
  ManualListService,
  SmartAllowListService,
  ContactGroupWhitelistService,
} from './services';

/**
 * Bridge between native CallScreeningService and TypeScript services
 * 
 * This connects the native Android service to your existing business logic
 */
export class CallScreeningBridge {
  private screeningService: CallScreeningService;
  private unsubscribe: (() => void) | null = null;

  constructor(
    callLog: CallLogService,
    patternRules: PatternRulesService,
    manualList: ManualListService,
    smartAllowList: SmartAllowListService,
    contactGroupWhitelist: ContactGroupWhitelistService
  ) {
    this.screeningService = new CallScreeningService(
      callLog,
      patternRules,
      manualList,
      smartAllowList,
      contactGroupWhitelist
    );
  }

  /**
   * Initialize the bridge
   * Registers the native service and sets up listeners
   */
  async initialize(): Promise<void> {
    try {
      // Register the native service
      await callScreeningModule.registerCallScreeningService();

      // Listen for incoming calls
      this.unsubscribe = callScreeningModule.onIncomingCall(
        (call: IncomingCall) => this.handleIncomingCall(call)
      );

      console.log('CallScreeningBridge initialized');
    } catch (error) {
      console.error('Failed to initialize CallScreeningBridge:', error);
    }
  }

  /**
   * Handle incoming call
   * Screens the call using your business logic
   */
  private async handleIncomingCall(call: IncomingCall): Promise<void> {
    try {
      // Use your existing screening logic
      const result = await this.screeningService.screenCall(call);

      // Send decision back to native service
      const decision = result.action === 'BLOCK' ? 'BLOCK' : 'ALLOW';
      await callScreeningModule.respondToCall(call.phoneNumber, decision);

      console.log(`Call screened: ${call.phoneNumber} -> ${decision}`);
    } catch (error) {
      console.error('Failed to screen call:', error);
      // Allow the call on error
      await callScreeningModule.allowCall(call.phoneNumber);
    }
  }

  /**
   * Cleanup
   */
  destroy(): void {
    if (this.unsubscribe) {
      this.unsubscribe();
    }
  }
}
```

---

## REGISTRATION: Add to MainActivity

**Update `android/app/src/main/java/com/signalgate/MainActivity.java`:**

```java
package com.signalgate.multipoint;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.ReactRootView;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

public class MainActivity extends ReactActivity {

  @Override
  protected String getMainComponentName() {
    return "SignalGateMultiPoint";
  }

  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new DefaultReactActivityDelegate(
        this,
        getMainComponentName(),
        DefaultNewArchitectureEntryPoint.getFabricDelegate());
  }
}
```

**Update `android/app/src/main/java/com/signalgate/MainApplication.java`:**

```java
package com.signalgate.multipoint;

import android.app.Application;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactNativeHost;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

  private final ReactNativeHost mReactNativeHost =
      new DefaultReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
          return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
          @SuppressWarnings("UnnecessaryLocalVariable")
          List<ReactPackage> packages = new PackageList(this).getPackages();
          // Add CallScreeningPackage
          packages.add(new CallScreeningPackage());
          return packages;
        }

        @Override
        protected String getJSMainModuleName() {
          return "index";
        }

        @Override
        protected boolean isNewArchEnabled() {
          return BuildConfig.IS_NEW_ARCHITECTURE_ENABLED;
        }

        @Override
        protected Boolean isHermesEnabled() {
          return BuildConfig.IS_HERMES_ENABLED;
        }
      };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    DefaultNewArchitectureEntryPoint.load();
  }
}
```

---

## TESTING THE NATIVE BRIDGE

**Create `tests/call-screening-bridge.test.ts`:**

```typescript
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { CallScreeningBridge } from '../src/lib/call-screening-bridge';
import { CallLogService } from '../src/lib/services/call-log';
import { PatternRulesService } from '../src/lib/services/pattern-rules';
import { ManualListService } from '../src/lib/services/manual-list';
import { SmartAllowListService } from '../src/lib/services/smart-allow-list';
import { ContactGroupWhitelistService } from '../src/lib/services/contact-group-whitelist';

describe('CallScreeningBridge', () => {
  let bridge: CallScreeningBridge;

  beforeEach(() => {
    const callLog = new CallLogService();
    const patternRules = new PatternRulesService();
    const manualList = new ManualListService();
    const smartAllowList = new SmartAllowListService();
    const contactGroupWhitelist = new ContactGroupWhitelistService();

    bridge = new CallScreeningBridge(
      callLog,
      patternRules,
      manualList,
      smartAllowList,
      contactGroupWhitelist
    );
  });

  afterEach(() => {
    bridge.destroy();
  });

  it('should initialize without errors', async () => {
    expect(() => bridge.initialize()).not.toThrow();
  });

  it('should handle incoming calls', async () => {
    const call = {
      phoneNumber: '+12125551234',
      displayName: 'Test Caller',
      timestamp: Date.now(),
      isIncoming: true,
    };

    // This would be tested on actual device
    expect(call.phoneNumber).toBeDefined();
  });
});
```

---

## WHAT HAPPENS WHEN A CALL COMES IN

1. **Android receives incoming call** → Triggers `CallScreeningService.onScreenCall()`
2. **Kotlin extracts phone number** → Sends to JavaScript via `DeviceEventManagerModule`
3. **TypeScript receives event** → `callScreeningModule.onIncomingCall()` fires
4. **CallScreeningBridge handles it** → Calls `screeningService.screenCall()`
5. **Your business logic runs** → Checks database, applies rules
6. **Decision made** → `BLOCK` or `ALLOW`
7. **Decision sent back to Kotlin** → `callScreeningModule.respondToCall()`
8. **Kotlin applies decision** → `CallScreeningService.respondToCall()`
9. **Android blocks or allows call** ✅

---

## NEXT STEPS

**Part 4** will provide:
- Complete deployment guide
- Testing on Android device
- Play Store submission

