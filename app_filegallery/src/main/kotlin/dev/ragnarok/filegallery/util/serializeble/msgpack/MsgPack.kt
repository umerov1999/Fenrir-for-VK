package dev.ragnarok.filegallery.util.serializeble.msgpack

import dev.ragnarok.filegallery.util.serializeble.json.JsonElement
import dev.ragnarok.filegallery.util.serializeble.msgpack.internal.*
import dev.ragnarok.filegallery.util.serializeble.msgpack.stream.toMsgPackArrayBuffer
import dev.ragnarok.filegallery.util.serializeble.msgpack.stream.toMsgPackBufferedSource
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import okio.BufferedSource

/**
 * Main entry point of library
 *
 *
 * ## Examples of usage
 * ```
 * val msgPack = MsgPack(...)
 *
 * @Serializer
 * class Message(val id: Int, val data: String)
 *
 * // parsing from [ByteArray] to an object
 * msgPack.parse(Message.serializer(), binaryData)
 * ```
 *
 * @see MsgPack.Default The instance using default configurations.
 */
open class MsgPack @JvmOverloads constructor(
    private val configuration: MsgPackConfiguration = MsgPackConfiguration.default,
    final override val serializersModule: SerializersModule = SerializersModule {
        contextual(Any::class, MsgPackDynamicSerializer)
    },
    private val inlineEncoders: Map<SerialDescriptor, (InlineEncoderHelper) -> Encoder> = mapOf(),
    private val inlineDecoders: Map<SerialDescriptor, (InlineDecoderHelper) -> Decoder> = mapOf()
) : BinaryFormat {
    companion object Default : MsgPack()

    @Serializable(with = InternalJsonElementAdapter::class)
    internal class InternalJsonElement(jsonElement: JsonElement) {
        var element: JsonElement = jsonElement
    }

    internal class InternalJsonElementAdapter : KSerializer<InternalJsonElement> {
        override val descriptor: SerialDescriptor =
            buildClassSerialDescriptor("InternalJsonElement")

        override fun deserialize(decoder: Decoder): InternalJsonElement {
            require(decoder is BasicMsgPackDecoder)
            return InternalJsonElement(decoder.decodeMsgPackElement())
        }

        override fun serialize(encoder: Encoder, value: InternalJsonElement) {
            throw UnsupportedOperationException()
        }
    }

    final override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val decoder = MsgPackDecoder(
            BasicMsgPackDecoder(
                configuration,
                serializersModule,
                bytes.toMsgPackArrayBuffer(),
                inlineDecoders = inlineDecoders
            )
        )
        return decoder.decodeSerializableValue(deserializer)
    }

    fun parseToJsonElement(bytes: ByteArray): JsonElement {
        return decodeFromByteArray(InternalJsonElement.serializer(), bytes).element
    }

    fun parseToJsonElement(bytes: BufferedSource): JsonElement {
        return decodeFromOkioStream(InternalJsonElement.serializer(), bytes).element
    }

    @ExperimentalSerializationApi
    inline fun <reified T> decodeFromOkioStream(bytes: BufferedSource): T =
        decodeFromOkioStream(serializersModule.serializer(), bytes)

    fun <T> decodeFromOkioStream(
        deserializer: DeserializationStrategy<T>,
        bytes: BufferedSource
    ): T {
        val decoder = MsgPackDecoder(
            BasicMsgPackDecoder(
                configuration,
                serializersModule,
                bytes.toMsgPackBufferedSource(),
                inlineDecoders = inlineDecoders
            )
        )
        return decoder.decodeSerializableValue(deserializer)
    }

    final override fun <T> encodeToByteArray(
        serializer: SerializationStrategy<T>,
        value: T
    ): ByteArray {
        val encoder = MsgPackEncoder(
            BasicMsgPackEncoder(
                configuration,
                serializersModule,
                inlineEncoders = inlineEncoders
            )
        )
        kotlin.runCatching {
            encoder.encodeSerializableValue(serializer, value)
        }.fold(
            onSuccess = { return encoder.result.toByteArray() },
            onFailure = {
                throw it
            }
        )
    }
}
