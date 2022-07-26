package dev.ragnarok.filegallery.picasso

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.StatFs
import com.squareup.picasso3.BitmapSafeResize
import com.squareup.picasso3.Picasso
import dev.ragnarok.filegallery.Constants
import dev.ragnarok.filegallery.settings.Settings
import dev.ragnarok.filegallery.util.CoverSafeResize
import dev.ragnarok.filegallery.util.Logger
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException

class PicassoInstance @SuppressLint("CheckResult") private constructor(
    private val app: Context
) {
    private var cache_data: Cache? = null

    @Volatile
    private var singleton: Picasso? = null

    private fun getSingleton(): Picasso {
        singleton ?: run {
            synchronized(this) {
                singleton = create()
            }
        }
        return singleton!!
    }

    private fun getCache_data() {
        if (cache_data == null) {
            val cache = File(app.cacheDir, "picasso-cache")
            if (!cache.exists()) {
                cache.mkdirs()
            }
            cache_data = Cache(cache, calculateDiskCacheSize(cache))
        }
    }

    private fun create(): Picasso {
        Logger.d(TAG, "Picasso singleton creation")
        getCache_data()
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
            .cache(cache_data).addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
                chain.proceed(chain.request()).newBuilder()
                    .header("Cache-Control", "max-age=86400").build()
            })
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", Constants.USER_AGENT).build()
                chain.proceed(request)
            })
        BitmapSafeResize.setMaxResolution(Settings.get().main().getMaxBitmapResolution())
        BitmapSafeResize.setHardwareRendering(Settings.get().main().getRendering_mode())
        CoverSafeResize.setMaxResolution(Settings.get().main().getMaxThumbResolution())
        return Picasso.Builder(app)
            .defaultBitmapConfig(Bitmap.Config.ARGB_8888)
            .client(builder.build())
            .addRequestHandler(PicassoMediaMetadataHandler(app))
            .build()
    }

    companion object {
        private val TAG = PicassoInstance::class.java.simpleName

        @SuppressLint("StaticFieldLeak")
        private var instance: PicassoInstance? = null

        fun init(context: Context) {
            instance = PicassoInstance(context.applicationContext)
        }

        fun with(): Picasso {
            return instance!!.getSingleton()
        }

        fun getCoversPath(context: Context): File {
            val cache = File(context.cacheDir, "covers-cache")
            //val cache = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "covers-cache")
            if (!cache.exists()) {
                cache.mkdirs()
            }
            return cache
        }

        @Throws(IOException::class)
        fun clear_cache() {
            instance?.getCache_data()
            instance?.cache_data?.evictAll()
        }

        // from picasso sources
        private fun calculateDiskCacheSize(dir: File): Long {
            var size = 5242880L
            try {
                val statFs = StatFs(dir.absolutePath)
                val blockCount = statFs.blockCountLong
                val blockSize = statFs.blockSizeLong
                val available = blockCount * blockSize
                size = available / 50L
            } catch (ignored: IllegalArgumentException) {
            }
            return size.coerceAtMost(52428800L).coerceAtLeast(5242880L)
        }
    }
}