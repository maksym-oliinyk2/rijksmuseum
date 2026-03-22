package com.oliinyk.maksym.rijksmuseum.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.oliinyk.maksym.rijksmuseum.app.RijksmuseumApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.RijksmuseumTheme)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RijksmuseumApp()
        }
    }
}
