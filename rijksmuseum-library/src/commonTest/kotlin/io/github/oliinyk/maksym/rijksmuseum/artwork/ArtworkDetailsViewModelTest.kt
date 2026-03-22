package io.github.oliinyk.maksym.rijksmuseum.artwork

import app.cash.turbine.turbineScope
import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.AppException
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.list.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.ui.model.toException
import io.github.xlopec.tea.core.ShareOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkDetailsViewModelTest : KoinTest {

    private val artwork = Artwork(
        url = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/1"),
        title = Title("Title 1"),
        primaryImage = null,
        linguisticObjects = emptyList()
    )

    private val destination = ArtworkDetailsDestination(artwork)

    private val testModule = module {
        single {
            TestRijksmuseumApi(
                artworksDetails = mapOf(artwork.url to artwork.right())
            )
        } bind RijksmuseumApi::class
        single { ArtworkRepositoryImpl(get()) } bind ArtworkRepository::class
        single { GetArtworkUseCase(get()) }
        single { ShareOptions(SharingStarted.Lazily, 1u) }
        factory { params ->
            val dest = params.get<ArtworkDetailsDestination>()
            ArtworkDetailsViewModel(ArtworkDetailsViewState.Initial(dest.artwork), get())
        }
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
    fun test_view_model_displays_initial_artwork() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val states = viewModel(flowOf())

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())
            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_view_model_refreshes_artwork() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val messages = MutableSharedFlow<Message>()
        val states = viewModel(messages)

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            messages.emit(Message.OnRefresh)

            assertEquals(ArtworkDetailsViewState(Loadable(artwork, Loadable.Refreshing)), actualStates.awaitItem())
            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_view_model_reloads_artwork() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val messages = MutableSharedFlow<Message>()
        val states = viewModel(messages)

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            messages.emit(Message.OnReload)

            assertEquals(ArtworkDetailsViewState(Loadable(artwork, Loadable.Loading)), actualStates.awaitItem())
            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun test_view_model_handles_data_loaded_error() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val messages = MutableSharedFlow<Message>()
        val states = viewModel(messages)

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            val error = AppException("Error")
            messages.emit(Message.OnDataLoaded(error.left()))

            assertEquals(
                ArtworkDetailsViewState(Loadable.idleSingle(artwork).toException(error)),
                actualStates.awaitItem()
            )

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }
}
