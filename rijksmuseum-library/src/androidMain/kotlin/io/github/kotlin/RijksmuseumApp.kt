package io.github.kotlin

import android.app.Application
import io.github.oliinyk.maksym.rijksmuseum.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

public class RijksmuseumApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RijksmuseumApp)
            modules(appModule)
        }
    }
}
