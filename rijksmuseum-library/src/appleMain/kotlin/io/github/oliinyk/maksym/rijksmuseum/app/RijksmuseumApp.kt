package io.github.oliinyk.maksym.rijksmuseum.app

import io.ktor.client.engine.darwin.Darwin
import org.koin.core.context.startKoin

public fun startKoinApp() {
    startKoin {
        modules(AppModule(Darwin))
    }
}
