package dev.ragnarok.fenrir.util

import dev.ragnarok.fenrir.util.brotli.BrotliInputStream
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.asResponseBody
import okhttp3.internal.http.promisesBody
import okio.GzipSource
import okio.buffer
import okio.source

object BrotliInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        if (chain.request().header("Accept-Encoding") == null && Utils.isCompressTraffic()) {
            val request = chain.request().newBuilder()
                .header("Accept-Encoding", "br,gzip")
                .build()

            val response = chain.proceed(request)
            uncompress(response)
        } else {
            chain.proceed(chain.request())
        }

    private fun uncompress(response: Response): Response {
        if (!response.promisesBody()) {
            return response
        }
        val body = response.body ?: return response
        val encoding = response.header("Content-Encoding") ?: return response

        val decompressedSource = when {
            encoding.equals("br", ignoreCase = true) ->
                BrotliInputStream(body.source().inputStream()).source().buffer()
            encoding.equals("gzip", ignoreCase = true) ->
                GzipSource(body.source()).buffer()
            else -> return response
        }
        val builder: Response.Builder = response.newBuilder()
        response.header("Content-Length")
            ?.let { builder.addHeader("Compressed-Content-Length", it) }
        return builder
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedSource.asResponseBody(body.contentType(), -1))
            .build()
    }
}
