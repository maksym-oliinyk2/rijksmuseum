package io.github.oliinyk.maksym.rijksmuseum.core.data

import io.github.oliinyk.maksym.rijksmuseum.core.domain.Url
import io.github.oliinyk.maksym.rijksmuseum.core.domain.UrlFrom
import io.github.oliinyk.maksym.rijksmuseum.core.domain.toStringValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object UrlSerializer : KSerializer<Url> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Url", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Url = UrlFrom(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Url) {
        encoder.encodeString(value.toStringValue())
    }
}
