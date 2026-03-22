package io.github.oliinyk.maksym.rijksmuseum.artworks.data

import io.github.oliinyk.maksym.rijksmuseum.domain.UrlFrom
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object GettyAatTypeSerializer : KSerializer<GettyAatType?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GettyAatType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): GettyAatType? {
        val id = decoder.decodeString()
        return GettyAatType.fromId(UrlFrom(id))
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: GettyAatType?) {
        if (value != null) {
            encoder.encodeString(value.id.toString())
        } else {
            encoder.encodeNull()
        }
    }
}
