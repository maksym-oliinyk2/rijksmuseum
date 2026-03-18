package io.github.kotlin

import android.app.Application
import io.github.oliinyk.maksym.rijksmuseum.di.AppModule
import io.ktor.client.engine.cio.CIO
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

public class RijksmuseumApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RijksmuseumApp)
            modules(AppModule(CIO))
        }
    }
}
