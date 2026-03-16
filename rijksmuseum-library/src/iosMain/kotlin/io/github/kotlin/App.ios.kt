package io.github.kotlin

import androidx.compose.ui.window.ComposeUIViewController
import io.github.oliinyk.maksym.rijksmuseum.ui.App
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("appController")
public fun AppController(): UIViewController = ComposeUIViewController {
    App()
}
