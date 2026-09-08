package kotlinx.serialization.json.internal

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.internal.lexer.BATCH_SIZE
import kotlinx.serialization.json.internal.lexer.BufferedJsonLexer
import kotlinx.serialization.serializer

internal annotation class JsonFriendModuleApi

@JsonFriendModuleApi
interface InternalJsonWriter {
    fun writeLong(value: Long)
    fun writeChar(char: Char)
    fun write(text: String)
    fun writeQuoted(text: String)
    fun release()

    companion object {
        inline fun doWriteEscaping(
            text: String,
            writeImpl: (text: String, startIndex: Int, endIndex: Int) -> Unit
        ) {
            var lastPos = 0
            for (i in text.indices) {
                val c = text[i].code
                if (c < ESCAPE_STRINGS.size && ESCAPE_STRINGS[c] != null) {
                    writeImpl(text, lastPos, i) // flush prev
                    val escape = ESCAPE_STRINGS[c] ?: return
                    writeImpl(escape, 0, escape.length)
                    lastPos = i + 1
                }
            }
            writeImpl(text, lastPos, text.length)
        }
    }
}

@JsonFriendModuleApi
interface InternalJsonReader {
    fun read(buffer: CharArray, bufferOffset: Int, count: Int): Int
}

@JsonFriendModuleApi
fun <T> encodeByWriter(
    json: Json,
    writer: InternalJsonWriter,
    serializer: SerializationStrategy<T>,
    value: T
) {
    val encoder = StreamingJsonEncoder(
        writer, json,
        LexerMode.OBJ,
        arrayOfNulls(LexerMode.entries.size)
    )
    encoder.encodeSerializableValue(serializer, value)
}

@JsonFriendModuleApi
fun <T> decodeByReader(
    json: Json,
    deserializer: DeserializationStrategy<T>,
    reader: InternalJsonReader
): T {
    val lexer = BufferedJsonLexer(json, reader)
    try {
        val input = StreamingJsonDecoder(json, LexerMode.OBJ, lexer, deserializer.descriptor, null)
        val result = input.decodeSerializableValue(deserializer)
        lexer.expectEof()
        return result
    } finally {
        lexer.release()
    }
}

@JsonFriendModuleApi
@ExperimentalSerializationApi
fun <T> decodeToSequenceByReader(
    json: Json,
    reader: InternalJsonReader,
    deserializer: DeserializationStrategy<T>,
    format: DecodeSequenceMode = DecodeSequenceMode.AUTO_DETECT
): Sequence<T> {
    val lexer = BufferedJsonLexer(
        json,
        reader,
        CharArray(BATCH_SIZE)
    ) // Unpooled buffer due to lazy nature of sequence
    val iter = JsonIterator(format, json, lexer, deserializer)
    return Sequence { iter }.constrainOnce()
}

@JsonFriendModuleApi
@ExperimentalSerializationApi
inline fun <reified T> decodeToSequenceByReader(
    json: Json,
    reader: InternalJsonReader,
    format: DecodeSequenceMode = DecodeSequenceMode.AUTO_DETECT
): Sequence<T> = decodeToSequenceByReader(json, reader, json.serializersModule.serializer(), format)
