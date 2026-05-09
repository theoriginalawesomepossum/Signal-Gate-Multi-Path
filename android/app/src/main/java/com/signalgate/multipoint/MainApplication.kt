package com.signalgate.multipoint

import android.app.Application
import android.os.Process
import android.util.Log

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->

            Log.e(
                "SIGNALGATE_CRASH",
                "Uncaught exception in thread: ${thread.name}",
                throwable
            )

            throwable.printStackTrace()

            Process.killProcess(Process.myPid())
            System.exit(1)
        }
    }
}
