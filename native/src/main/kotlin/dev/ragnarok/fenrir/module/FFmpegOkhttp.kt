package dev.ragnarok.fenrir.module

import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.math.min

class FFmpegOkhttp(val pUrl: String, val pHeaders: String?) {
    var response: Response? = null
    var mimeType: String? = null
    var position: Long = 0
    var contentSize: Long = Long.MAX_VALUE
    var seekable: Boolean = true

    @Throws(IOException::class)
    fun okhttpClose() {
        try {
            response?.close()
            response = null
            position = 0L
            contentSize = Long.MAX_VALUE
        } catch (_: IOException) {
        }
    }

    fun okhttpGetMime(): String? {
        return mimeType
    }

    @Throws(IOException::class)
    fun okhttpOpen(avOptions: MutableMap<String, String>?): Int {
        val client = getOkHttpClient()
        return if (client == null || isLockNetwork) {
            -5
        } else {
            try {
                val httpHeaders = splitHttpHeaders(pHeaders)
                if (seekable) {
                    httpHeaders["Range"] = "bytes=$position-"
                }
                setOptions(avOptions, httpHeaders)

                response = client.newCall(
                    Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
                ).execute()
                if (response?.code == 416) {
                    okhttpClose()
                    httpHeaders.remove("Range")
                    seekable = false
                    response = client.newCall(
                        Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
                    ).execute()
                }
                if (response?.isSuccessful != true) {
                    okhttpClose()
                    -5
                } else {
                    mimeType = response?.body?.contentType()?.toString()
                    contentSize = response?.body?.contentLength() ?: 0
                    if (contentSize <= 0) {
                        contentSize = Long.MAX_VALUE
                    }
                    0
                }
            } catch (_: IOException) {
                okhttpClose()
                -5
            }
        }
    }

    @Throws(IOException::class)
    fun okhttpRead(bArr: ByteArray, bufferSize: Int): Int {
        return if (isLockNetwork) {
            okhttpClose()
            -1
        } else {
            try {
                if (response != null && bufferSize >= 0) {
                    val read = response?.body?.source()?.read(bArr, 0, min(bufferSize, 8192))
                    if (read == null) {
                        -1
                    } else if (read > 0) {
                        position += read.toLong()
                        read
                    } else if (read < 0 && contentSize == Long.MAX_VALUE) {
                        contentSize = position
                        -1
                    } else {
                        -1
                    }
                } else {
                    -3
                }
            } catch (_: IOException) {
                -1
            } catch (_: ArrayIndexOutOfBoundsException) {
                -1
            } catch (_: IllegalStateException) {
                -1
            }
        }
    }

    @Throws(IOException::class)
    private fun okhttpSeek(off: Long): Long {
        val client = getOkHttpClient()
        return if (client == null) {
            -5
        } else if (isLockNetwork) {
            okhttpClose()
            -5
        } else {
            okhttpClose()
            try {
                val httpHeaders: HashMap<String, String> = splitHttpHeaders(pHeaders)
                httpHeaders["Range"] = "bytes=$off-"
                response = client.newCall(
                    Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
                ).execute()
                if (response?.code == 416) {
                    okhttpClose()
                    httpHeaders.remove("Range")
                    seekable = false
                    response = client.newCall(
                        Request.Builder().url(pUrl).headers(httpHeaders.toHeaders()).build()
                    ).execute()
                }
                if (response?.isSuccessful == true) {
                    off
                } else {
                    okhttpClose()
                    -5L
                }
            } catch (_: IOException) {
                okhttpClose()
                -5L
            }
        }
    }

    fun setOptions(map: MutableMap<String, String>?, map2: MutableMap<String, String>) {
        if (map == null) {
            return
        }
        if (map.containsKey("seekable")) {
            val str = map["seekable"]
            seekable = str != null && str != "-1"
        }
        if (map.containsKey("offset")) {
            var range: String = "bytes=" + map["offset"] + "-"
            if (map.containsKey("end_offset")) {
                range += map["end_offset"]
            }
            map2["Range"] = range
        }
    }

    @Throws(IOException::class)
    fun okhttpSeek(off: Long, whence: Int): Long {
        return if (response == null || isLockNetwork) {
            -5L
        } else if (whence == 65536) {
            if (contentSize == Long.MAX_VALUE) position else contentSize
        } else if (whence == 0) {
            position = off
            okhttpSeek(off)
        } else if (whence == 1) {
            position += off
            okhttpSeek(position)
        } else if (whence != 2) {
            -4L
        } else {
            position = contentSize
            okhttpSeek(position)
        }
    }

    companion object {
        private var okHttpBuilder: OnFFmpegOkhttpCreate? = null
        private var okHttpClient: OkHttpClient? = null

        @Volatile
        private var isLockNetwork = false

        fun setOnFFmpegOkhttpCreate(client: () -> OkHttpClient.Builder) {
            okHttpBuilder = object : OnFFmpegOkhttpCreate {
                override fun createOkHttpClient(): OkHttpClient = client().build()
            }
        }

        fun releaseOkHttpClient() {
            synchronized(this) {
                okHttpClient = null
            }
        }

        internal fun getOkHttpClient(): OkHttpClient? {
            synchronized(this) {
                if (okHttpClient == null && okHttpBuilder != null) {
                    okHttpClient = okHttpBuilder?.createOkHttpClient()
                }
                return okHttpClient
            }
        }

        fun setLockNetwork(isLockNetwork: Boolean) {
            this.isLockNetwork = isLockNetwork
        }

        internal fun splitHttpHeaders(str: String?): HashMap<String, String> {
            if (str.isNullOrEmpty()) {
                return HashMap()
            }
            val strArrSplit =
                str.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val map = HashMap<String, String>()
            for (str2 in strArrSplit) {
                val strArrSplit2: Array<String> =
                    str2.split("=".toRegex(), limit = 2).toTypedArray()
                if (strArrSplit2.size >= 2) {
                    map[strArrSplit2[0]] = strArrSplit2[1]
                }
            }
            return map
        }
    }

    interface OnFFmpegOkhttpCreate {
        fun createOkHttpClient(): OkHttpClient
    }
}