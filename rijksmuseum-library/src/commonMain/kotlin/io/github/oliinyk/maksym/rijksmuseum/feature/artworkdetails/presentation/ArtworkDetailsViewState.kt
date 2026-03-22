package io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation

import arrow.core.Either
import io.github.oliinyk.maksym.rijksmuseum.core.domain.AppException
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.Loadable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.isRefreshable
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toException
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toIdle
import io.github.oliinyk.maksym.rijksmuseum.core.presentation.model.toRefreshing
import io.github.oliinyk.maksym.rijksmuseum.feature.artworkdetails.presentation.Command.LoadCommand
import io.github.xlopec.tea.core.Initializer
import io.github.xlopec.tea.core.Update
import io.github.xlopec.tea.core.command
import io.github.xlopec.tea.core.noCommand
import kotlin.jvm.JvmInline

internal sealed interface Message {
    data object OnReload : Message
    data object OnRefresh : Message
    data object OnBack : Message

    @JvmInline
    value class OnDataLoaded(
        val result: Either<AppException, Artwork>
    ) : Message
}

internal data class ArtworkDetailsViewState(
    val loadable: Loadable<Artwork>,
) {
    companion object {
        fun Initial(artwork: Artwork): Initializer<ArtworkDetailsViewState, Command> =
            Initializer(ArtworkDetailsViewState(Loadable.idleSingle(artwork)), setOf())
    }
}

internal sealed interface Command {
    data object OnBack : Command

    @JvmInline
    value class LoadCommand(
        val artworkId: Url,
    ) : Command
}

internal fun ArtworkDetailsViewState.update(message: Message): Update<ArtworkDetailsViewState, Command> {
    return when (message) {
        is Message.OnDataLoaded -> onLoaded(message.result)
        Message.OnRefresh -> onRefresh()
        Message.OnReload -> onReload()
        Message.OnBack -> command(Command.OnBack)
    }
}

private fun ArtworkDetailsViewState.onReload(): Update<ArtworkDetailsViewState, Command> {
    return copy(loadable = Loadable(loadable.data, Loadable.Loading))
        .command(LoadCommand(loadable.data.url))
}

private fun ArtworkDetailsViewState.onRefresh(): Update<ArtworkDetailsViewState, Command> {
    return if (loadable.isRefreshable) {
        copy(loadable = loadable.toRefreshing())
            .command(LoadCommand(loadable.data.url))
    } else {
        noCommand()
    }
}

private fun ArtworkDetailsViewState.onLoaded(
    result: Either<AppException, Artwork>
): Update<ArtworkDetailsViewState, LoadCommand> {
    val updated = result.fold(
        { loadable.toException(it) },
        { loadable.toIdle(it) }
    )
    return copy(loadable = updated).noCommand()
}
