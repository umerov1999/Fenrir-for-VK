package dev.ragnarok.fenrir.util

import android.content.Context
import dev.ragnarok.fenrir.R
import io.reactivex.rxjava3.core.Single
import okhttp3.Request

object Mp3InfoHelper {
    @JvmStatic
    fun getLength(url: String): Single<Long> {
        return Single.create {
            val builder = Utils.createOkHttp(60)
            val request: Request = Request.Builder()
                .url(url)
                .build()
            val response = builder.build().newCall(request).execute()
            if (!response.isSuccessful) {
                it.onError(
                    Exception(
                        "Server return " + response.code +
                                " " + response.message
                    )
                )
            } else {
                val length = response.header("Content-Length")
                response.body?.close()
                if (Utils.isEmpty(length)) {
                    it.onError(Exception("Empty content length!"))
                }
                length?.let { o ->
                    it.onSuccess(o.toLong())
                }
                length ?: it.onSuccess(0)
            }
        }
    }

    @JvmStatic
    fun getBitrate(duration: Int, size: Long): Int {
        return ((((size / duration) * 8)) / 1000).toInt()
    }

    @JvmStatic
    fun getBitrate(context: Context, duration: Int, size: Long): String {
        return context.getString(
            R.string.bitrate,
            ((((size / duration) * 8)) / 1000).toInt(),
            Utils.BytesToSize(size)
        )
    }
}