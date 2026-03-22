package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import androidx.navigation3.runtime.NavKey
import app.cash.turbine.turbineScope
import arrow.core.left
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.core.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.NonEmptyString
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toException
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.nav.Navigator
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.domain.ArtworkUseCase
import io.github.oliinyk.maksym.rijksmuseum.feature.artworks.data.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.res.Res
import io.github.oliinyk.maksym.rijksmuseum.res.exception_unknown
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
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.mock.declare
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ArtworkDetailsViewModelTest : KoinTest {

    private val artwork = Artwork(
        url = UrlFrom("https://data.rijksmuseum.nl/api/en/collection/1"),
        title = NonEmptyString("Title 1"),
        primaryImage = null,
        linguisticObjects = emptyList()
    )

    private val destination = ArtworkDetailsDestination(artwork)

    private val testModule = module {
        single<RijksmuseumApi> {
            TestRijksmuseumApi(
                artworksDetails = mapOf(artwork.url to artwork.right())
            )
        }
        single<ArtworkRepository> { ArtworkRepositoryImpl(get()) }
        single { ArtworkUseCase(get()) }
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
    fun when_initialized_then_displays_initial_artwork() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val states = viewModel(flowOf())

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())
            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun when_OnRefresh_then_refreshes_artwork() = runTest {
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
    fun when_OnReload_then_reloads_artwork() = runTest {
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
    fun when_OnBack_then_navigates_back() = runTest {
        val backStack = mutableListOf<NavKey>(ArtworkDetailsDestination(artwork))

        declare {
            Navigator(backStack)
        }

        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val messages = MutableSharedFlow<Message>()
        val states = viewModel(messages)

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            messages.emit(Message.OnBack)

            assertTrue(backStack.isEmpty())

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun when_OnDataLoaded_failure_then_exception() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val messages = MutableSharedFlow<Message>()
        val states = viewModel(messages)

        turbineScope {
            val actualStates = states.testIn(this)

            assertEquals(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), actualStates.awaitItem())

            val error = AppException(Res.string.exception_unknown)
            messages.emit(Message.OnDataLoaded(error.left()))

            assertEquals(
                ArtworkDetailsViewState(Loadable.idleSingle(artwork).toException(error)),
                actualStates.awaitItem()
            )

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }
}
