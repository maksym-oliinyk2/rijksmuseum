package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.koinConfiguration
import platform.UIKit.UIViewController

@ObjCName("appController")
public fun AppController(): UIViewController = ComposeUIViewController {
    RijksmuseumApp { koinConfiguration { modules(AppModule(it, Darwin)) } }
}
