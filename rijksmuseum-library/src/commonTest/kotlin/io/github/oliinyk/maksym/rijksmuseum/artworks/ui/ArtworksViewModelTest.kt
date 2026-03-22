package io.github.oliinyk.maksym.rijksmuseum.artworks.ui

import androidx.navigation3.runtime.NavKey
import app.cash.turbine.turbineScope
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig
import io.github.oliinyk.maksym.rijksmuseum.artwork.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.artworks.list.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.ui.nav.Navigator
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArtworksViewModelTest : KoinTest {

    private val artwork = Artwork(
        url = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/1"),
        title = Title("Title 1"),
        primaryImage = null,
        linguisticObjects = listOf()
    )

    private val testModule = module {
        val api = TestRijksmuseumApi(
            artworksDetails = mapOf(artwork.url to artwork.right()),
            searchResponses = mapOf(
                BuildConfig.InitialPageUrl to PaginatedIds(
                    next = null,
                    ids = listOf(artwork.url)
                ).right()
            )
        )

        single { SearchUseCase(SearchRepositoryImpl(api = api)) }
        single { Navigator(mutableListOf()) }
        single { ShareOptions(SharingStarted.Lazily, 1u) }
        factory { ArtworksViewModel(ArtworksViewState.Initial(), get()) }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        startKoin {
            modules(testModule)
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun test_loads_artwork_from_use_case() = runTest {
        val viewModel = get<ArtworksViewModel>()
        val states = viewModel(flowOf())

        turbineScope {
            val actualStates = states.testIn(this)
            val expectedStates = listOf(
                ArtworksViewState(artworks = Paginateable.loadingList()),
                ArtworksViewState(artworks = Paginateable.idleList(listOf(artwork))),
            )

            expectedStates.forEach { state ->
                assertEquals(state, actualStates.awaitItem())
            }

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_navigation_to_details() = runTest {
        val viewState = ArtworksViewState(artworks = Paginateable.idleList(listOf(artwork)))

        declare {
            ArtworksViewModel(Initializer(viewState), get())
        }

        val viewModel = get<ArtworksViewModel>()
        // suspends until the view model handles a message
        val messages = Channel<Message>(0)

        turbineScope {
            val states = viewModel(messages.receiveAsFlow()).testIn(this)
            // view model emits the initial state
            assertEquals(viewState, states.awaitItem())

            messages.send(Message.OnNavigateToDetails(artwork))

            val navigator = get<Navigator>()

            assertEquals(listOf<NavKey>(ArtworkDetailsDestination(artwork)), navigator.toList())
            // state didn't change after navigation
            assertEquals(viewState, states.awaitItem())
            states.cancelAndConsumeRemainingEvents()
        }
    }
}
