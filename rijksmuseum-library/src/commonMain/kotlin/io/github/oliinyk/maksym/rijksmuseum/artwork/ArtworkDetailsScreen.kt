package io.github.oliinyk.maksym.rijksmuseum.artwork

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun ArtworkDetailsScreen(
    key: ArtworkNavEntry,
    modifier: Modifier = Modifier,
) {
    // Note: We need a new ViewModel for every new RouteB instance. Usually
    // we would need to supply a `key` String that is unique to the
    // instance, however, the ViewModelStoreNavEntryDecorator (supplied
    // above) does this for us, using `NavEntry.contentKey` to uniquely
    // identify the viewModel.
    //
    // tl;dr: Make sure you use rememberViewModelStoreNavEntryDecorator()
    // if you want a new ViewModel for each new navigation key instance.
    ScreenB(modifier = modifier, viewModel = koinViewModel { parametersOf(key) })
}

@Composable
internal fun ScreenB(modifier: Modifier, viewModel: ArtworkDetailsViewModel) {
    Text(modifier = modifier, text = "Route id: ${viewModel.key.id} ")
}
