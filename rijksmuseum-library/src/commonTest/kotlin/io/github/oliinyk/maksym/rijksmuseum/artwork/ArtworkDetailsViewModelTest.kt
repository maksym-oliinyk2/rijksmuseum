package io.github.oliinyk.maksym.rijksmuseum.artwork

import app.cash.turbine.turbineScope
import arrow.core.right
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepository
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ArtworkRepositoryImpl
import io.github.oliinyk.maksym.rijksmuseum.artwork.data.ValueHolder
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.GetArtworkUseCase
import io.github.oliinyk.maksym.rijksmuseum.artwork.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.artworks.data.RijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.artworks.list.TestRijksmuseumApi
import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.ui.model.Loadable
import io.github.xlopec.tea.core.ShareOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
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

    private val destination = ArtworkDetailsDestination(artwork.url)

    private val testModule = module {
        single {
            TestRijksmuseumApi(
                artworksDetails = mapOf(artwork.url to artwork.right())
            )
        } bind RijksmuseumApi::class
        single(named<Artwork>()) { ValueHolder(artwork) }
        single { ArtworkRepositoryImpl(get(), get(named<Artwork>())) } bind ArtworkRepository::class
        single { GetArtworkUseCase(get()) }
        single { ShareOptions(SharingStarted.Lazily, 1u) }
        factory { params ->
            val dest = params.get<ArtworkDetailsDestination>()
            ArtworkDetailsViewModel(ArtworkDetailsViewState.Initial(dest.id), get())
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
    fun test_loads_artwork_initially_given_cached_artwork() = runTest {
        val viewModel = get<ArtworkDetailsViewModel> { parametersOf(destination) }
        val states = viewModel(flowOf())

        turbineScope {
            val actualStates = states.testIn(this)
            val expectedStates = listOf(
                ArtworkDetailsViewState(destination.id, Loadable.loadingSingle()),
                ArtworkDetailsViewState(destination.id, Loadable.idleSingle(artwork)),
            )

            expectedStates.forEach { state ->
                assertEquals(state, actualStates.awaitItem())
            }

            actualStates.cancelAndConsumeRemainingEvents()
        }
    }
}
