package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.ui.window.ComposeUIViewController
import io.ktor.client.engine.darwin.Darwin
import org.koin.dsl.koinConfiguration
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("appController")
public fun AppController(): UIViewController = ComposeUIViewController {
    App { koinConfiguration { modules(AppModule(it, Darwin)) } }
}
