package dev.ragnarok.fenrir.view.natives.animation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.RawRes
import androidx.core.content.withStyledAttributes
import com.google.android.material.imageview.ShapeableImageView
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.animation.AnimatedFileDrawable
import dev.ragnarok.fenrir.module.animation.LoadedFrom
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.isActive
import kotlinx.coroutines.flow.flow
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

open class AnimatedShapeableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ShapeableImageView(context, attrs) {
    private val cache: AnimationNetworkCache = AnimationNetworkCache(context)
    private var defaultWidth: Int = 100
    private var defaultHeight: Int = 100
    private var animatedDrawable: AnimatedFileDrawable? = null
    private var attachedToWindow = false
    private var decoderCallback: OnDecoderInit? = null
    private var mDisposable: CancelableJob? = CancelableJob()

    @LoadedFrom
    private var loadedFrom = LoadedFrom.NO
    private var filePathTmp: String? = null
    private var keyTmp: String? = null

    @RawRes
    private var rawResTmp: Int? = null
    private var isPlaying: Boolean? = null
    private var tmpFade: Boolean? = null

    fun setDecoderCallback(decoderCallback: OnDecoderInit?) {
        this.decoderCallback = decoderCallback
    }

    private fun setAnimationByUrlCache(key: String, autoPlay: Boolean, fade: Boolean) {
        if (!FenrirNative.isNativeLoaded) {
            decoderCallback?.onLoaded(false)
            return
        }
        val ch = cache.fetch(key)
        if (ch == null) {
            decoderCallback?.onLoaded(false)
            return
        }
        if (filePathTmp == ch.absolutePath && loadedFrom == LoadedFrom.FILE) {
            return
        }
        clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = false)
        loadedFrom = LoadedFrom.FILE
        filePathTmp = ch.absolutePath
        tmpFade = fade
        isPlaying = autoPlay

        if (attachedToWindow) {
            createAnimationDrawable()
        }
    }

    private fun setAnimationByResCache(@RawRes res: Int, autoPlay: Boolean, fade: Boolean) {
        if (!FenrirNative.isNativeLoaded) {
            decoderCallback?.onLoaded(false)
            return
        }
        val ch = cache.fetch(res)
        if (ch == null) {
            decoderCallback?.onLoaded(false)
            return
        }
        if (filePathTmp == ch.absolutePath && loadedFrom == LoadedFrom.FILE) {
            return
        }
        clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = false)
        loadedFrom = LoadedFrom.FILE
        filePathTmp = ch.absolutePath
        tmpFade = fade
        isPlaying = autoPlay

        if (attachedToWindow) {
            createAnimationDrawable()
        }
    }

    fun fromNet(key: String, url: String?, autoPlay: Boolean) {
        if (!FenrirNative.isNativeLoaded || url.isNullOrEmpty()) {
            if (loadedFrom == LoadedFrom.NET) {
                loadedFrom = LoadedFrom.NO
            }
            decoderCallback?.onLoaded(false)
            return
        }
        if (filePathTmp == url && keyTmp == key && loadedFrom == LoadedFrom.NET) {
            return
        }

        var client: OkHttpClient?
        if (cache.isCachedFile(key)) {
            setAnimationByUrlCache(key, autoPlay, true)
            return
        } else {
            clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = true)
            client = AnimationNetworkCache.getOkHttpClient()
            if (client == null) {
                return
            }
            loadedFrom = LoadedFrom.NET
            filePathTmp = url
            keyTmp = key
            isPlaying = autoPlay
            tmpFade = true
        }
        mDisposable?.set(flow {
            var call: Call? = null
            try {
                val request: Request = Request.Builder()
                    .url(url)
                    .build()
                call = client.newCall(request)
                val response: Response = call.execute()
                if (!response.isSuccessful) {
                    emit(false)
                    return@flow
                }
                if (isActive()) {
                    val rs = cache.writeTempCacheFile(key, response.body.source())
                    response.close()
                    emit(rs)
                } else {
                    response.close()
                    emit(false)
                }
            } catch (e: CancellationException) {
                call?.cancel()
                throw e
            }
        }.fromIOToMain({ u ->
            if (u) {
                setAnimationByUrlCache(key, autoPlay, true)
            } else {
                decoderCallback?.onLoaded(false)
            }
        }, {
            decoderCallback?.onLoaded(false)
        }))
    }

    fun fromRes(@RawRes resId: Int, autoPlay: Boolean) {
        if (!FenrirNative.isNativeLoaded || resId == -1) {
            if (loadedFrom == LoadedFrom.RES) {
                loadedFrom = LoadedFrom.NO
            }
            decoderCallback?.onLoaded(false)
            return
        }
        if (rawResTmp == resId && loadedFrom == LoadedFrom.RES) {
            return
        }

        if (cache.isCachedRes(resId)) {
            setAnimationByResCache(resId, autoPlay, true)
            return
        } else {
            clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = true)
            loadedFrom = LoadedFrom.RES
            rawResTmp = resId
            tmpFade = true
            isPlaying = autoPlay
        }
        mDisposable?.set(flow {
            if (isActive()) {
                val rs = cache.writeTempCacheFile(resId)
                emit(rs)
            } else {
                emit(false)
            }
        }.fromIOToMain {
            if (it) {
                setAnimationByResCache(resId, autoPlay, true)
            } else {
                decoderCallback?.onLoaded(false)
            }
        })
    }

    private fun createAnimationDrawable() {
        if (FenrirNative.isNativeLoaded && attachedToWindow && loadedFrom != LoadedFrom.NO && animatedDrawable == null && filePathTmp != null) {
            animatedDrawable = AnimatedFileDrawable(
                filePathTmp ?: "",
                0,
                defaultWidth,
                defaultHeight,
                tmpFade == true,
                object : AnimatedFileDrawable.DecoderListener {
                    override fun onError() {
                        clearAnimationDrawable(
                            callSuper = true,
                            clearState = true,
                            cancelTask = true
                        )
                        decoderCallback?.onLoaded(false)
                    }

                    override fun onSuccess() {
                        if (animatedDrawable?.isDecoded != true) {
                            clearAnimationDrawable(
                                callSuper = true,
                                clearState = true,
                                cancelTask = true
                            )
                            decoderCallback?.onLoaded(animatedDrawable?.isDecoded == true)
                            return
                        }
                        decoderCallback?.onLoaded(animatedDrawable?.isDecoded == true)
                        tmpFade = false
                        animatedDrawable?.setAllowDecodeSingleFrame(true)
                        setSuperImageDrawable(animatedDrawable)
                        if (isPlaying == true) {
                            playAnimation()
                        }
                    }

                })
        }
    }

    fun fromFile(file: File, autoPlay: Boolean) {
        if (!FenrirNative.isNativeLoaded || !file.exists()) {
            decoderCallback?.onLoaded(false)
            return
        }
        if (filePathTmp == file.absolutePath && loadedFrom == LoadedFrom.FILE) {
            return
        }
        clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = true)
        loadedFrom = LoadedFrom.FILE
        filePathTmp = file.absolutePath
        isPlaying = autoPlay
        tmpFade = false
        if (attachedToWindow) {
            createAnimationDrawable()
        }
    }

    fun fromFile(file: String, autoPlay: Boolean) {
        if (!FenrirNative.isNativeLoaded || file.isEmpty()) {
            decoderCallback?.onLoaded(false)
            return
        }
        if (filePathTmp == file && loadedFrom == LoadedFrom.FILE) {
            return
        }
        clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = true)
        loadedFrom = LoadedFrom.FILE
        filePathTmp = file
        isPlaying = autoPlay
        tmpFade = false
        if (attachedToWindow) {
            createAnimationDrawable()
        }
    }

    fun fromVKVideo(video: Video, autoPlay: Boolean) {
        if (!FenrirNative.isNativeLoaded) {
            decoderCallback?.onLoaded(false)
            return
        }
        val url = video.urlForPreviewInternal
        if (url.nonNullNoEmpty()) {
            fromFile(url, autoPlay)
            return
        }
        mDisposable?.set(
            InteractorFactory.video.getById(
                Settings.get().accounts().current,
                video.ownerId,
                video.id,
                video.accessKey,
                false
            )
                .fromIOToMain({
                    video.setHls(it.hls).setMp4link1080(it.mp4link1080)
                        .setMp4link1440(it.mp4link1440).setMp4link2160(it.mp4link2160)
                        .setMp4link720(it.mp4link720).setMp4link480(it.mp4link480)
                        .setMp4link360(it.mp4link360).setMp4link240(it.mp4link240)
                        .setTrailer(it.trailer)
                    val url2 = it.urlForPreviewInternal
                    if (url2.nonNullNoEmpty()) {
                        fromFile(url2, autoPlay)
                    } else {
                        decoderCallback?.onLoaded(false)
                    }
                }, {
                    decoderCallback?.onLoaded(false)
                })
        )
    }

    fun releaseAnimation() {
        clearAnimationDrawable(callSuper = true, clearState = true, cancelTask = true)
    }

    internal fun clearAnimationDrawable(
        callSuper: Boolean,
        clearState: Boolean,
        cancelTask: Boolean
    ) {
        if (cancelTask) {
            mDisposable?.cancel()
        }
        if (animatedDrawable != null) {
            animatedDrawable?.callback = null
            animatedDrawable?.recycle()
            animatedDrawable = null
        }
        if (callSuper) {
            super.setImageDrawable(null)
        }
        if (clearState) {
            isPlaying = false
            loadedFrom = LoadedFrom.NO
            filePathTmp = null
            rawResTmp = null
            tmpFade = null
        }
    }

    override fun onAttachedToWindow() {
        attachedToWindow = true
        super.onAttachedToWindow()
        when {
            loadedFrom == LoadedFrom.NET -> {
                val tmpFilePathTmp = filePathTmp
                tmpFilePathTmp?.let {
                    keyTmp?.let { s ->
                        filePathTmp = null
                        fromNet(
                            s,
                            it,
                            isPlaying == true,
                        )
                    }
                    return
                }
                clearAnimationDrawable(callSuper = false, clearState = true, cancelTask = false)
            }

            loadedFrom == LoadedFrom.RES -> {
                val tmpRawResTmp = rawResTmp
                tmpRawResTmp?.let {
                    rawResTmp = null
                    fromRes(
                        it,
                        isPlaying == true,
                    )
                    return
                }
                clearAnimationDrawable(callSuper = false, clearState = true, cancelTask = false)
            }

            loadedFrom != LoadedFrom.NO -> {
                createAnimationDrawable()
            }
        }
    }

    override fun onDetachedFromWindow() {
        attachedToWindow = false
        super.onDetachedFromWindow()
        if (loadedFrom != LoadedFrom.NO) {
            clearAnimationDrawable(callSuper = true, clearState = false, cancelTask = true)
        }
    }

    fun isPlaying(): Boolean {
        return animatedDrawable != null && animatedDrawable?.isRunning == true
    }

    override fun setImageDrawable(dr: Drawable?) {
        super.setImageDrawable(dr)
        clearAnimationDrawable(callSuper = false, clearState = true, cancelTask = true)
    }

    internal fun setSuperImageDrawable(dr: Drawable?) {
        super.setImageDrawable(dr)
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        clearAnimationDrawable(callSuper = false, clearState = true, cancelTask = true)
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        clearAnimationDrawable(callSuper = false, clearState = true, cancelTask = true)
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        clearAnimationDrawable(callSuper = false, clearState = true, cancelTask = true)
    }

    fun playAnimation() {
        animatedDrawable?.start()
        isPlaying = true
    }

    fun resetFrame() {
        animatedDrawable?.seekTo(0, true)
    }

    fun stopAnimation() {
        animatedDrawable?.let {
            it.stop()
            isPlaying = false
        }
    }

    protected override fun onSaveInstanceState(): Parcelable? {
        return super.onSaveInstanceState()
    }

    protected override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    interface OnDecoderInit {
        fun onLoaded(success: Boolean)
    }

    init {
        context.withStyledAttributes(attrs, R.styleable.AnimatedShapeableImageView) {
            defaultWidth =
                getDimension(R.styleable.AnimatedShapeableImageView_default_width, 100f).toInt()
            defaultHeight =
                getDimension(R.styleable.AnimatedShapeableImageView_default_height, 100f).toInt()
        }
    }
}
