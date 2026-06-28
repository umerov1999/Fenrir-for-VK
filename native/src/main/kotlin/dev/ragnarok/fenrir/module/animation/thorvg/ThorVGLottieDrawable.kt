package dev.ragnarok.fenrir.module.animation.thorvg

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.RawRes
import androidx.core.graphics.createBitmap
import dev.ragnarok.fenrir.module.BufferWriteNative
import dev.ragnarok.fenrir.module.BuildConfig
import dev.ragnarok.fenrir.module.FenrirNative.appContext
import dev.ragnarok.fenrir.module.FenrirNative.density
import dev.ragnarok.fenrir.module.animation.LoadedFrom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import kotlin.math.ceil
import kotlin.time.Duration.Companion.milliseconds

class ThorVGLottieDrawable : Drawable, Animatable {
    @IntDef(RESTART, REVERSE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class RepeatMode

    private var lottieState: LottieDrawableState

    private var listener: LottieAnimationListener? = null

    private var running = false

    private var ended = false

    private var started = false

    private var repeated = 0

    private var frame = 0

    private val tmpPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    private var mutated = false

    private var startTime = -1L

    private var nextFrameJob: Job? = null

    constructor(
        filePath: String,
        canDeleteError: Boolean,
        colorReplacement: IntArray?,
        useMoveColor: Boolean
    ) {
        lottieState = LottieDrawableState()
        lottieState.composition =
            LottieComposition().init(filePath, canDeleteError, colorReplacement, useMoveColor)
        lottieState.updateFrameInterval()
    }

    constructor(
        @RawRes rawRes: Int,
        colorReplacement: IntArray?,
        useMoveColor: Boolean
    ) {
        lottieState = LottieDrawableState()
        lottieState.composition = LottieComposition().init(rawRes, colorReplacement, useMoveColor)
        lottieState.updateFrameInterval()
    }

    internal constructor(state: LottieDrawableState) {
        lottieState = state
    }

    fun release() {
        lottieState.releaseLottie()
    }

    protected fun finalize() {
        try {
            release()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
        }
    }

    override fun mutate(): Drawable {
        if (!mutated && super.mutate() === this) {
            lottieState = LottieDrawableState(lottieState)
            mutated = true
        }
        return this
    }

    override fun draw(canvas: Canvas) {
        if (!ended && lottieState.valid() && running) {
            if (!started) {
                started = true
                dispatchAnimationStart()
            }

            if (startTime < 0L) {
                startTime = System.nanoTime()
            }

            if (lottieState.repeatCount != INFINITE && repeated >= lottieState.repeatCount) {
                if (!ended) {
                    ended = true
                    dispatchAnimationEnd()
                }
                getFrame(if (lottieState.framesPerUpdate >= 0) lottieState.getLastFrameInternal() else lottieState.firstFrame)?.let {
                    canvas.drawBitmap(it, 0f, 0f, tmpPaint)
                }
            } else {
                getFrame(frame)?.let {
                    canvas.drawBitmap(it, 0f, 0f, tmpPaint)
                }

                var resetFrame = false
                frame += lottieState.framesPerUpdate
                if (frame > lottieState.getLastFrameInternal()) {
                    if (repeatMode == REVERSE) {
                        lottieState.framesPerUpdate = -1
                        frame--
                    } else {
                        frame = lottieState.firstFrame
                    }
                    resetFrame = true
                } else if (frame < lottieState.firstFrame) {
                    if (repeatMode == REVERSE) {
                        lottieState.framesPerUpdate = 1
                        frame++
                    } else {
                        frame = lottieState.getLastFrameInternal()
                    }
                    resetFrame = true
                }
                if (resetFrame) {
                    repeated++
                    dispatchAnimationRepeat()
                }
                val elapsedMs = (System.nanoTime() - startTime) / 1_000_000
                startTime = -1L

                nextFrameJob = CoroutineScope(Dispatchers.Main).launch {
                    delay((lottieState.frameInterval - elapsedMs).coerceAtLeast(0L).milliseconds)
                    if (running) {
                        startTime = System.nanoTime()
                        invalidateSelf()
                    }
                }
            }
        } else if (lottieState.valid()) {
            getFrame(frame)?.let {
                canvas.drawBitmap(it, 0f, 0f, tmpPaint)
            }
        }
    }

    fun getFrame(frame: Int): Bitmap? {
        return lottieState.getLottieBuffer(frame)
    }

    override fun setAlpha(alpha: Int) {
        tmpPaint.alpha = alpha
        startTime = System.nanoTime()
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        tmpPaint.colorFilter = colorFilter
        startTime = System.nanoTime()
        invalidateSelf()
    }

    @Deprecated("", ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat"))
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicWidth(): Int {
        return lottieState.composition?.width ?: 0
    }

    override fun getIntrinsicHeight(): Int {
        return lottieState.composition?.height ?: 0
    }

    var repeatCount: Int
        get() = lottieState.repeatCount
        set(count) {
            lottieState.repeatCount = count
            repeated = 0
        }

    @RepeatMode
    var repeatMode: Int
        get() = lottieState.repeatMode
        set(@RepeatMode mode) {
            lottieState.repeatMode = mode
        }

    var firstFrame: Int
        get() = lottieState.firstFrame
        set(frame) {
            lottieState.setFirstFrameInternal(frame)
        }

    var lastFrame: Int
        get() = lottieState.getLastFrameInternal()
        set(frame) {
            lottieState.setLastFrameInternal(frame)
        }

    val duration: Long
        get() = if (lottieState.valid()) lottieState.composition?.duration ?: 0L else 0L

    val animationHeight: Int
        get() = lottieState.composition?.height ?: 0

    val animationWidth: Int
        get() = lottieState.composition?.width ?: 0

    var speed: Float
        @FloatRange(from = 0.0)
        get() = lottieState.speed
        set(@FloatRange(from = 0.0) value) {
            lottieState.setSpeedInternal(value)
        }

    fun setSize(width: Int, height: Int) {
        require(width > 0) { "LottieDrawable requires width > 0" }
        require(height > 0) { "LottieDrawable requires height > 0" }
        lottieState.setCompositionSize(width, height)

        startTime = System.nanoTime()
        invalidateSelf()
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun start() {
        nextFrameJob?.cancel()

        running = true
        ended = false
        started = false
        repeated = 0
        frame = lottieState.firstFrame

        startTime = System.nanoTime()
        invalidateSelf()
    }

    override fun stop() {
        running = false

        nextFrameJob?.cancel()
    }

    fun pause() {
        running = false

        nextFrameJob?.cancel()
    }

    fun resume() {
        running = true

        startTime = System.nanoTime()
        invalidateSelf()
    }

    fun setAnimationListener(listener: LottieAnimationListener?) {
        this.listener = listener
    }

    fun dispatchAnimationStart() {
        listener?.onAnimationStart()
    }

    fun dispatchAnimationRepeat() {
        listener?.onAnimationRepeat()
    }

    fun dispatchAnimationEnd() {
        listener?.onAnimationEnd()
    }

    internal class LottieDrawableState : ConstantState {
        internal var composition: LottieComposition? = null

        internal var repeatMode = RESTART

        internal var repeatCount = 1

        internal var speed = 1.0f

        internal var firstFrame = 0
        internal var lastFrame = -1

        internal var frameInterval: Long = 0

        internal var framesPerUpdate = 1

        internal fun getLastFrameInternal(): Int {
            if (lastFrame < 0) {
                return composition?.frameCount ?: 0
            }
            return lastFrame
        }

        constructor(copy: LottieDrawableState?) {
            if (copy != null) {
                composition = LottieComposition(copy.composition)
                repeatCount = copy.repeatCount
                repeatMode = copy.repeatMode
                framesPerUpdate = copy.framesPerUpdate
                speed = copy.speed
                updateFrameInterval()
            }
        }

        internal fun releaseLottie() {
            composition?.destroy()
            composition = null
        }

        internal fun valid(): Boolean {
            composition?.let {
                return it.nativePtr != 0L
            }
            return false
        }

        internal fun setCompositionSize(width: Int, height: Int) {
            composition?.let {
                if (width != it.width || height != it.height || it.buffer == null) {
                    it.width = width
                    it.height = height
                    it.setBufferSize(width, height)
                }
            }
        }

        internal fun getLottieBuffer(frame: Int): Bitmap? {
            return composition?.getBuffer(frame)
        }

        internal fun setSpeedInternal(@FloatRange(from = 0.0) speed: Float) {
            this.speed = speed
            updateFrameInterval()
        }

        internal fun setFirstFrameInternal(frame: Int) {
            firstFrame = frame.coerceAtLeast(0).coerceAtMost(getLastFrameInternal())
            updateFrameInterval()
        }

        internal fun setLastFrameInternal(frame: Int) {
            val lastFrameTmp = composition?.frameCount ?: 0
            lastFrame =
                frame.coerceAtLeast(0).coerceAtMost(if (lastFrameTmp > 0) lastFrameTmp - 1 else 0)

            updateFrameInterval()
        }

        internal fun updateFrameInterval() {
            val currentComposition = composition ?: return
            val totalFrames = getLastFrameInternal() - firstFrame + 1
            frameInterval = when {
                totalFrames <= 0 -> 0L
                speed <= 0f -> 0L
                else -> (currentComposition.duration / totalFrames / speed).toLong()
            }
        }

        internal constructor()

        override fun newDrawable(): Drawable {
            return ThorVGLottieDrawable(this)
        }

        override fun getChangingConfigurations(): Int {
            return 0
        }
    }

    internal class LottieComposition {
        internal var nativePtr: Long = 0
        internal var frameCount = 0
        internal var duration: Long = 0
        internal var width: Int = 0
        internal var height: Int = 0
        internal var buffer: Bitmap? = null
        private var colorReplacementTmp: IntArray? = null
        private var useMoveColorTmp: Boolean = false

        @LoadedFrom
        private var loadedFrom: Int = LoadedFrom.NO
        private var filePathTmp: String? = null

        @RawRes
        private var rawResTmp: Int? = null

        constructor()

        internal constructor(copy: LottieComposition?) {
            if (copy == null || copy.loadedFrom == LoadedFrom.NO) {
                nativePtr = 0
                loadedFrom = LoadedFrom.NO
                return
            }
            filePathTmp = copy.filePathTmp
            rawResTmp = copy.rawResTmp
            loadedFrom = copy.loadedFrom
            useMoveColorTmp = copy.useMoveColorTmp
            colorReplacementTmp = copy.colorReplacementTmp
            when (copy.loadedFrom) {
                LoadedFrom.RES -> {
                    rawResTmp?.let {
                        init(it, colorReplacementTmp, useMoveColorTmp)
                    }
                }

                LoadedFrom.FILE -> {
                    filePathTmp?.let {
                        init(it, false, colorReplacementTmp, useMoveColorTmp)
                    }
                }
            }
            if (width != copy.width || height != copy.height) {
                width = copy.width
                height = copy.height
            }
            if (copy.buffer != null) {
                setBufferSize(width, height)
            }
        }

        @SuppressLint("SyntheticAccessor")
        internal fun init(
            filePath: String,
            canDeleteError: Boolean,
            colorReplacement: IntArray?,
            useMoveColor: Boolean
        ): LottieComposition {
            nativePtr = 0
            val file = File(filePath)
            if (!file.exists()) {
                return this
            }
            val outValues = IntArray(LOTTIE_INFO_COUNT)
            nativePtr = ThorVGLottieNativeBindings.nLoadFromFile(
                filePath,
                outValues,
                colorReplacement,
                useMoveColor
            )
            if (nativePtr == 0L) {
                if (canDeleteError) {
                    file.delete()
                }
                return this
            }
            loadedFrom = LoadedFrom.FILE
            filePathTmp = file.absolutePath
            colorReplacementTmp = colorReplacement
            useMoveColorTmp = useMoveColor
            frameCount = outValues[LOTTIE_INFO_FRAME_COUNT]
            duration = outValues[LOTTIE_INFO_DURATION].toLong()

            width = dp(outValues[LOTTIE_INFO_WIDTH])
            height = dp(outValues[LOTTIE_INFO_HEIGHT])
            return this
        }

        @SuppressLint("SyntheticAccessor")
        internal fun init(
            @RawRes rawRes: Int,
            colorReplacement: IntArray?,
            useMoveColor: Boolean
        ): LottieComposition {
            nativePtr = 0
            val jsonString = readRes(rawRes) ?: return this

            val outValues = IntArray(LOTTIE_INFO_COUNT)
            nativePtr =
                ThorVGLottieNativeBindings.nLoadFromMemory(
                    jsonString.pointer,
                    outValues,
                    colorReplacement,
                    useMoveColor
                )
            if (nativePtr == 0L) {
                return this
            }
            loadedFrom = LoadedFrom.RES
            rawResTmp = rawRes
            colorReplacementTmp = colorReplacement
            useMoveColorTmp = useMoveColor
            frameCount = outValues[LOTTIE_INFO_FRAME_COUNT]
            duration = outValues[LOTTIE_INFO_DURATION].toLong()

            width = dp(outValues[LOTTIE_INFO_WIDTH])
            height = dp(outValues[LOTTIE_INFO_HEIGHT])
            return this
        }

        @SuppressLint("SyntheticAccessor")
        internal fun destroy() {
            if (buffer != null) {
                buffer?.recycle()
                buffer = null
            }
            ThorVGLottieNativeBindings.nDestroy(nativePtr)
        }

        @SuppressLint("SyntheticAccessor")
        internal fun setBufferSize(width: Int, height: Int) {
            buffer = createBitmap(width, height, Bitmap.Config.ARGB_8888)
            buffer?.let {
                ThorVGLottieNativeBindings.nSetBufferSize(
                    nativePtr,
                    it,
                    width.toFloat(),
                    height.toFloat()
                )
            }
        }

        @SuppressLint("SyntheticAccessor")
        internal fun getBuffer(frame: Int): Bitmap? {
            buffer?.let { ThorVGLottieNativeBindings.nGetFrame(nativePtr, it, frame) }
            return buffer
        }
    }

    interface LottieAnimationListener {
        fun onAnimationStart()
        fun onAnimationEnd()
        fun onAnimationRepeat()
    }

    companion object {
        const val INFINITE = -1
        const val RESTART: Int = 1
        const val REVERSE: Int = 2
        private const val LOTTIE_INFO_FRAME_COUNT = 0
        private const val LOTTIE_INFO_DURATION = 1
        private const val LOTTIE_INFO_WIDTH = 2
        private const val LOTTIE_INFO_HEIGHT = 3
        private const val LOTTIE_INFO_COUNT = 4

        internal fun dp(value: Int): Int {
            return if (value == 0) {
                0
            } else ceil((density * value.toFloat()).toDouble())
                .toInt()
        }

        internal fun readRes(@RawRes rawRes: Int): BufferWriteNative? {
            var inputStream: InputStream? = null
            return try {
                inputStream = appContext.resources.openRawResource(rawRes)
                val res = BufferWriteNative.fromStreamEndlessNull(inputStream)
                if (res.bufferSize() <= 0) {
                    inputStream.close()
                    return null
                }
                res
            } catch (_: Throwable) {
                return null
            } finally {
                try {
                    inputStream?.close()
                } catch (_: Throwable) {
                }
            }
        }
    }
}

internal object ThorVGLottieNativeBindings {
    external fun nLoadFromFile(
        src: String?,
        params: IntArray?,
        colorReplacement: IntArray?,
        useMoveColor: Boolean
    ): Long

    external fun nLoadFromMemory(
        json: Long,
        params: IntArray?,
        colorReplacement: IntArray?,
        useMoveColor: Boolean
    ): Long

    external fun nSetBufferSize(
        ptr: Long,
        bitmap: Bitmap,
        width: Float,
        height: Float
    )

    external fun nGetFrame(ptr: Long, bitmap: Bitmap, frame: Int)
    external fun nDestroy(ptr: Long)
}