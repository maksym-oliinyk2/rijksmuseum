package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import io.ktor.client.engine.cio.CIO
import org.koin.dsl.koinConfiguration

@Composable
public fun App() {
    App { koinConfiguration { modules(AppModule(it, CIO)) } }
}
