package io.github.oliinyk.maksym.rijksmuseum.core.data.dto

import arrow.core.toNonEmptyListOrNull
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Artwork
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Description
import io.github.oliinyk.maksym.rijksmuseum.core.domain.GettyAatType
import io.github.oliinyk.maksym.rijksmuseum.core.domain.LinguisticObject
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Title
import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object NonEmptyListSerializer : KSerializer<List<Description>> {
    private val delegate = ListSerializer(String.serializer())
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): List<Description> =
        decoder.decodeSerializableValue(delegate).toNonEmptyListOrNull()
            ?: throw IllegalArgumentException("Empty list is not allowed for NonEmptyList")

    override fun serialize(encoder: Encoder, value: List<Description>) {
        encoder.encodeSerializableValue(delegate, value)
    }
}

@Serializable
private data class LinguisticObjectSurrogate(
    @Serializable(with = GettyAatTypeSerializer::class)
    val type: GettyAatType?,
    @Serializable(with = NonEmptyListSerializer::class)
    val descriptions: List<Description>,
)

internal object LinguisticObjectSerializer : KSerializer<LinguisticObject> {
    override val descriptor: SerialDescriptor = LinguisticObjectSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): LinguisticObject {
        val surrogate = decoder.decodeSerializableValue(LinguisticObjectSurrogate.serializer())
        return LinguisticObject(
            type = surrogate.type ?: throw IllegalArgumentException("Type cannot be null"),
            descriptions = surrogate.descriptions
        )
    }

    override fun serialize(encoder: Encoder, value: LinguisticObject) {
        encoder.encodeSerializableValue(
            LinguisticObjectSurrogate.serializer(),
            LinguisticObjectSurrogate(value.type, value.descriptions)
        )
    }
}

@Serializable
internal data class ArtworkSurrogate(
    @Serializable(with = UrlSerializer::class)
    val url: Url,
    val title: Title,
    @Serializable(with = UrlSerializer::class)
    val primaryImage: Url?,
    val linguisticObjects: List<
        @Serializable(with = LinguisticObjectSerializer::class)
        LinguisticObject
        >,
)

internal object ArtworkSerializer : KSerializer<Artwork> {
    override val descriptor: SerialDescriptor = ArtworkSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Artwork {
        val surrogate = decoder.decodeSerializableValue(ArtworkSurrogate.serializer())
        return Artwork(
            url = surrogate.url,
            title = surrogate.title,
            primaryImage = surrogate.primaryImage,
            linguisticObjects = surrogate.linguisticObjects
        )
    }

    override fun serialize(encoder: Encoder, value: Artwork) {
        encoder.encodeSerializableValue(
            ArtworkSurrogate.serializer(),
            ArtworkSurrogate(value.url, value.title, value.primaryImage, value.linguisticObjects)
        )
    }
}
