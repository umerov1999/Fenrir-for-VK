package dev.ragnarok.fenrir.util.msgpack.stream

interface MsgPackDataBuffer {
    fun toByteArray(): ByteArray
}

class MsgPackDataOutputBuffer : MsgPackDataBuffer {
    private val bytes = mutableListOf<Byte>()

    fun add(byte: Byte) = bytes.add(byte)
    fun addAll(bytes: List<Byte>) = this.bytes.addAll(bytes)
    fun addAll(bytes: ByteArray) = this.bytes.addAll(bytes.toList())

    override fun toByteArray() = bytes.toByteArray()
}

class MsgPackDataInputBuffer(private val byteArray: ByteArray) : MsgPackDataBuffer {
    var index = 0
        private set

    fun skip(bytes: Int) {
        index += bytes
    }

    fun peek(): Byte = byteArray.getOrNull(index) ?: throw Exception("End of stream")
    fun peekSafely(): Byte? = byteArray.getOrNull(index)

    // Increases index only if next byte is not null
    fun nextByteOrNull(): Byte? = byteArray.getOrNull(index)?.also { index++ }

    fun requireNextByte(): Byte = nextByteOrNull() ?: throw Exception("End of stream")

    fun takeNext(next: Int): ByteArray {
        require(next > 0) { "Number of bytes to take must be greater than 0!" }
        val result = ByteArray(next)
        (0 until next).forEach {
            result[it] = requireNextByte()
        }
        return result
    }

    override fun toByteArray() = byteArray
}

internal fun ByteArray.toMsgPackBuffer() = MsgPackDataInputBuffer(this)
