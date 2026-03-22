package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.runtime.Composable
import io.ktor.client.engine.cio.CIO
import org.koin.dsl.koinConfiguration

@Composable
public fun RijksmuseumApp() {
    RijksmuseumApp { koinConfiguration { modules(AppModule(it, CIO)) } }
}
