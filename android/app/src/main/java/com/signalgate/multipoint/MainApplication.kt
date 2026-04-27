package com.signalgate.multipoint

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.flipper.ReactNativeFlipper

class MainApplication : Application(), ReactApplication {
    override val reactNativeHost: ReactNativeHost =
        object : ReactNativeHost(this) {
            override fun getPackages(): List<com.facebook.react.ReactPackage> =
                PackageList(this).packages.apply {
                    // Add the CallScreeningPackage
                    add(CallScreeningPackage())
                }

            override fun getJSMainModuleName(): String = "index"

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(this.applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, false)
        if (BuildConfig.DEBUG) {
            ReactNativeFlipper.initializeFlipper(this, reactNativeHost.reactInstanceManager)
        }
        load()
    }
}

// SoLoader stub for native libraries
object SoLoader {
    fun init(context: android.content.Context, native: Boolean) {
        // Initialize native libraries if needed
    }
}
