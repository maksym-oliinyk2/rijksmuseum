package io.github.kotlin

import android.app.Application
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.os.StrictMode.setThreadPolicy
import android.os.StrictMode.setVmPolicy
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig

public class RijksmuseumApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.Debug) {
            setupStrictAppPolicies()
        }
    }
}

private fun setupStrictAppPolicies() {
    setThreadPolicy(
        ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )

    setVmPolicy(
        VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()
    )
}
