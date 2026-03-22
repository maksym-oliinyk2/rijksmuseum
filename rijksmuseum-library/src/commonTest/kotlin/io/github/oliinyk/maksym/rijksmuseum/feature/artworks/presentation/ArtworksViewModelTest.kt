package io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation

import androidx.navigation3.runtime.NavKey
import app.cash.turbine.turbineScope
import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.BuildConfig
import io.github.oliinyk.maksym.rijksmuseum.core.data.PaginatedIds
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Paging
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Paginateable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toLoading
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toRefreshing
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.Navigator
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.ArtworkDetailsDestination
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.SearchRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.domain.SearchUseCase
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.presentation.ArtworksCommand.LoadCommand
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.ShareOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworksViewModelTest : KoinTest {

    private val artwork = Artwork(
        url = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/1"),
        title = NonEmptyString("Title 1"),
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
        factory { ArtworksViewModel(Initializer(ArtworksViewState(), LoadCommand(Paging(0, 1))), get()) }
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

    @Test
    fun test_view_model_refreshes_artworks() = runTest {
        val initialState = ArtworksViewState(artworks = Paginateable.idleList())
        declare {
            ArtworksViewModel(Initializer(initialState), get())
        }
        val viewModel = get<ArtworksViewModel>()
        val messages = Channel<Message>(0)
        val states = viewModel(messages.receiveAsFlow())

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(initialState, actualStates.awaitItem())

            messages.send(Message.OnRefresh)

            assertEquals(initialState.copy(artworks = initialState.artworks.toRefreshing()), actualStates.awaitItem())
            assertEquals(ArtworksViewState(artworks = Paginateable.idleList(listOf(artwork))), actualStates.awaitItem())

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_view_model_reloads_artworks() = runTest {
        val initialState = ArtworksViewState(artworks = Paginateable.idleList(listOf(artwork)))
        declare {
            ArtworksViewModel(Initializer(initialState), get())
        }
        val viewModel = get<ArtworksViewModel>()
        val messages = Channel<Message>()
        val states = viewModel(messages.receiveAsFlow())

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(initialState, actualStates.awaitItem())

            messages.send(Message.OnReload)

            assertEquals(initialState.copy(artworks = initialState.artworks.toLoading()), actualStates.awaitItem())
            assertEquals(initialState, actualStates.awaitItem())

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_view_model_loads_next_page() = runTest {
        val artwork2 = artwork.copy(url = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/2"))
        val nextUrl = UrlFrom("https://data.rijksmuseum.nl/api/en/collection?page=2")

        val api = TestRijksmuseumApi(
            artworksDetails = mapOf(
                artwork.url to artwork.right(),
                artwork2.url to artwork2.right()
            ),
            searchResponses = mapOf(
                BuildConfig.InitialPageUrl to PaginatedIds(
                    next = nextUrl,
                    ids = listOf(artwork.url)
                ).right(),
                nextUrl to PaginatedIds(
                    next = null,
                    ids = listOf(artwork2.url)
                ).right()
            )
        )

        declare {
            SearchUseCase(SearchRepositoryImpl(api = api))
        }

        val viewModel = get<ArtworksViewModel>()
        val messages = Channel<Message>()
        val states = viewModel(messages.receiveAsFlow())

        turbineScope {
            val actualStates = states.testIn(this)

            // Initial load
            assertEquals(ArtworksViewState(Paginateable.loadingList()), actualStates.awaitItem())
            assertEquals(
                ArtworksViewState(Paginateable(listOf(artwork), Paginateable.Idle, hasMore = true)),
                actualStates.awaitItem()
            )
            messages.send(Message.OnLoadNext)

            // We expect LoadingNext state
            assertEquals(
                ArtworksViewState(Paginateable(listOf(artwork), Paginateable.LoadingNext, hasMore = true)),
                actualStates.awaitItem()
            )
            // Then Idle state with more data
            assertEquals(
                ArtworksViewState(Paginateable(listOf(artwork, artwork2), Paginateable.Idle, hasMore = false)),
                actualStates.awaitItem()
            )

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_view_model_handles_data_loaded_error() = runTest {
        val error = AppException("Error")

        declare {
            val api = TestRijksmuseumApi(
                artworksDetails = mapOf(),
                searchResponses = mapOf(BuildConfig.InitialPageUrl to error.left())
            )

            SearchUseCase(SearchRepositoryImpl(api = api))
        }

        val viewModel = get<ArtworksViewModel>()
        val messages = Channel<Message>()
        val states = viewModel(messages.receiveAsFlow())

        turbineScope {
            val actualStates = states.testIn(this)

            // Initial load
            assertEquals(ArtworksViewState(artworks = Paginateable.loadingList()), actualStates.awaitItem())
            // Exception
            assertEquals(
                ArtworksViewState(artworks = Paginateable(listOf(), Paginateable.Exception(error))),
                actualStates.awaitItem()
            )

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }
}
