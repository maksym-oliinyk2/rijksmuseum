package io.github.oliinyk.maksym.rijksmuseum.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.oliinyk.maksym.rijksmuseum.ui.theme.RijksmuseumTheme

@Composable
public fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
public fun GreetingPreview() {
    RijksmuseumTheme {
        Greeting("Android")
    }
}
