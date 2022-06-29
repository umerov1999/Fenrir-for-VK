package dev.ragnarok.fenrir.util.serializeble.msgpack

import dev.ragnarok.fenrir.util.serializeble.msgpack.internal.*
import dev.ragnarok.fenrir.util.serializeble.msgpack.stream.toMsgPackBuffer
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

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

    final override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val decoder = MsgPackDecoder(
            BasicMsgPackDecoder(
                configuration,
                serializersModule,
                bytes.toMsgPackBuffer(),
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
