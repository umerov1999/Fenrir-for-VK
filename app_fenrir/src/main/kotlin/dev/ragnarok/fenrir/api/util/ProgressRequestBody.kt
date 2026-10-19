package dev.ragnarok.fenrir.api.util

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.Source
import java.io.IOException

class ProgressRequestBody(
    private val source: Source,
    private val size: Long,
    private val listener: UploadCallbacks?,
    private val mediaType: MediaType?
) : RequestBody() {
    override fun contentType(): MediaType? {
        return mediaType
    }

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return size
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val buffer = Buffer()
        var uploaded = 0L
        try {
            source.use { v ->
                while (true) {
                    val read = v.read(buffer, DEFAULT_BUFFER_SIZE)
                    if (read == -1L) break
                    sink.write(buffer, read)
                    uploaded += read
                    listener?.onProgressUpdate((100 * uploaded.toDouble() / size.toDouble()).toInt())
                    buffer.clear()
                }
            }
            listener?.onProgressUpdate(100)
        } catch (e: Exception) {
            if (e is IOException) {
                throw e
            } else {
                throw IOException(e)
            }
        } finally {
            source.close()
        }
    }

    interface UploadCallbacks {
        fun onProgressUpdate(percentage: Int)
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192L
    }
}