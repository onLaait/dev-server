package com.github.onlaait.fbw.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.MessageFormat

object MessageFormatAsStringSerializer : KSerializer<MessageFormat> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("messageformat", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MessageFormat) {
        encoder.encodeString(value.toPattern())
    }

    override fun deserialize(decoder: Decoder): MessageFormat {
        return MessageFormat(decoder.decodeString())
    }
}