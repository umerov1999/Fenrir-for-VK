package dev.ragnarok.fenrir.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.slidr.Slidr
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig
import dev.ragnarok.fenrir.listener.ActivityFuturesListener
import dev.ragnarok.fenrir.module.ZXingWrapper
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.toColor
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsResultAbs
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain

class CameraScanActivity : NoMainActivity(), ActivityFuturesListener {
    private lateinit var viewFinder: PreviewView
    private var camera: Camera? = null
    private var finder: FinderView? = null
    private var flashButton: FloatingActionButton? = null
    private val reader: ZXingWrapper = ZXingWrapper()
    private var isFlash = false
    private var mDecorView: View? = null
    private var mFocusView: View? = null
    private var focusHideJob = CancelableJob()
    private var imageAnalysisPointer: ImageAnalysis? = null

    init {
        reader.readerOptions.apply {
            formats = setOf(
                ZXingWrapper.Format.QR_CODE,
                ZXingWrapper.Format.EAN_8,
                ZXingWrapper.Format.EAN_13,
                ZXingWrapper.Format.CODE_128,
                ZXingWrapper.Format.DATA_MATRIX
            )
            tryHarder = false
            tryInvert = false
            tryRotate = false
            tryDownscale = false
            isPure = false
        }
    }

    private val requestCameraPermission = requestPermissionsResultAbs(
        arrayOf(
            Manifest.permission.CAMERA
        ), {
            startCamera()
        }, { finish() })

    private fun updateFlashButton() {
        Utils.setColorFilter(
            flashButton,
            if (isFlash) CurrentTheme.getColorPrimary(this) else ContextCompat.getColor(
                this,
                com.google.android.material.R.color.m3_fab_efab_foreground_color_selector
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        focusHideJob.cancel()
    }

    @get:LayoutRes
    override val noMainContentView: Int
        get() = R.layout.activity_camera_scan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Slidr.attach(
            this,
            SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this))
                .fromFromBlackToNormalNavigation(true).build()
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item_root)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insets2 =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            if (Utils.isLandscape(this)) {
                v.setPadding(
                    insets.left, 0,
                    insets.right,
                    insets.bottom
                )
            } else {
                v.setPadding(
                    insets2.left, 0,
                    insets2.right,
                    insets2.bottom
                )
            }
            WindowInsetsCompat.CONSUMED
        }

        viewFinder = findViewById(R.id.preview)
        finder = findViewById(R.id.view_finder)
        flashButton = findViewById(R.id.flash_button)
        mFocusView = findViewById(R.id.focus)
        updateFlashButton()

        flashButton?.setOnClickListener {
            if (camera?.cameraInfo?.hasFlashUnit() == true) {
                isFlash = !isFlash
                camera?.cameraControl?.enableTorch(isFlash)
                updateFlashButton()
            }
        }

        if (AppPerms.hasCameraPermission(this)) {
            startCamera()
        } else {
            requestCameraPermission.launch()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mDecorView = window.decorView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mDecorView?.layoutParams =
                WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
        }

        mDecorView?.let {
            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }

        Utils.applyEdgeToEdgeActivity(this, true)
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(true)
            .build()
            .apply(this)
    }

    private fun getScreenRotation(): Int {
        try {
            var currentDisplay: Display? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                currentDisplay = display
            } else {
                val manager = getSystemService(WINDOW_SERVICE) as WindowManager?
                if (manager != null) {
                    @Suppress("deprecation")
                    currentDisplay = manager.defaultDisplay
                }
            }
            return currentDisplay?.rotation ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
            return -1
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalysisPointer?.targetRotation = getScreenRotation()
    }

    private fun detect(
        image: ImageProxy,
        left: Int,
        top: Int,
        aspect: Int,
        flip: Boolean
    ): String? {
        val yPlane = image.getPlanes()[0]
        var tmpRotationDegrees = image.imageInfo.rotationDegrees
        if (tmpRotationDegrees % 90 != 0) {
            tmpRotationDegrees = 0
        }
        val result = try {
            reader.readYBuffer(
                yPlane.getBuffer(),
                yPlane.pixelStride,
                Rect(left, top, left + aspect, top + aspect),
                image.width,
                image.height,
                tmpRotationDegrees,
                flip
            )
        } catch (e: Exception) {
            finder?.invalidate()
            return e.localizedMessage
        }
        finder?.possibleResultPoints?.clear()

        for ((_, text, _, position) in result.orEmpty()) {
            finder?.pushPoints(position.topLeft)
            finder?.pushPoints(position.topRight)
            finder?.pushPoints(position.bottomLeft)
            finder?.pushPoints(position.bottomRight)
            if (text.nonNullNoEmpty()) {
                finder?.invalidate()
                return text
            }
        }
        finder?.invalidate()
        return null
    }

    @SuppressLint("ClickableViewAccessibility", "UnsafeOptInUsageError")
    private fun startCamera() {
        val resolution = ResolutionSelector.Builder()
            .setResolutionStrategy(
                ResolutionStrategy(
                    Size(1280, 960),
                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            ).setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val preview = Preview.Builder().setResolutionSelector(resolution).build()
        preview.surfaceProvider = viewFinder.surfaceProvider
        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setResolutionSelector(resolution)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(
            ContextCompat.getMainExecutor(this)
        ) { imageProxy ->
            if (ImageFormat.YUV_420_888 != imageProxy.getFormat() || imageProxy.planes.size < 1) {
                imageProxy.close()
                return@setAnalyzer
            }

            val imageWidth = imageProxy.width
            val imageHeight = imageProxy.height

            val aspect = imageWidth.coerceAtMost(imageHeight)
            finder?.updatePreviewSize(aspect, aspect)

            val left = (imageWidth - aspect) / 2
            val top = (imageHeight - aspect) / 2

            val data = detect(
                imageProxy,
                left,
                top,
                aspect,
                cameraSelector.lensFacing == CameraSelector.LENS_FACING_FRONT
            )
            if (data.nonNullNoEmpty()) {
                val retIntent = Intent()
                retIntent.putExtra(Extra.URL, data)
                setResult(RESULT_OK, retIntent)
                imageProxy.close()
                finish()
                return@setAnalyzer
            }
            imageProxy.close()
        }

        imageAnalysisPointer = imageAnalysis

        // request a ProcessCameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // verify that initialization succeeded when View was created
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector,
                    imageAnalysis, preview
                )
                camera?.let { cam ->
                    val camera2 = Camera2CameraControl.from(cam.cameraControl)
                    camera2.setCaptureRequestOptions(
                        CaptureRequestOptions.Builder()
                            .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, 1600)
                            .setCaptureRequestOption(
                                CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION,
                                -8
                            )
                            .build()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))

        val zoomListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scale =
                    camera?.cameraInfo?.zoomState?.value?.zoomRatio.orZero() * detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(scale)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this, zoomListener)
        var isScaleMode = false
        viewFinder.setOnTouchListener { _, event ->
            if (event.pointerCount >= 2 || isScaleMode) {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    isScaleMode = true
                } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    isScaleMode = false
                }
                scaleGestureDetector.onTouchEvent(event)
            } else {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        focusHideJob.cancel()
                        mFocusView?.visibility = View.VISIBLE
                        mFocusView?.x = event.x
                        mFocusView?.y = event.y
                        focusHideJob += delayTaskFlow(1000).toMain {
                            mFocusView?.visibility = View.GONE
                        }

                        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                            viewFinder.width.toFloat(), viewFinder.height.toFloat()
                        )
                        val autoFocusPoint = factory.createPoint(event.x, event.y)
                        try {
                            camera?.cameraControl?.startFocusAndMetering(
                                FocusMeteringAction.Builder(
                                    autoFocusPoint,
                                    FocusMeteringAction.FLAG_AF
                                ).apply {
                                    //focus only when the user tap the preview
                                    disableAutoCancel()
                                }.build()
                            )
                        } catch (e: CameraInfoUnavailableException) {
                            Log.d("ERROR", "cannot access camera", e)
                        }
                        true
                    }

                    else -> false // Unhandled event.
                }
            }
        }
    }

    override fun hideMenu(hide: Boolean) {}
    override fun openMenu(open: Boolean) {}

    open class FinderView(context: Context, attrs: AttributeSet?) :
        View(context, attrs) {
        private val frame: Rect = Rect()
        private val rectTmp: RectF = RectF()
        private val path = Path()
        private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val cornerPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val laserPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val pointPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val POINT_SIZE = 6f
        private var scannerAlpha = 0
        private val SCANNER_ALPHA = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
        private val ANIMATION_DELAY = 80L
        private var previewSize = Size(0, 0)
        var possibleResultPoints: ArrayList<Point> = ArrayList()

        fun updatePreviewSize(width: Int, height: Int) {
            if (previewSize.height != height || previewSize.width != width) {
                previewSize = Size(width, height)
                invalidate()
            }
        }

        fun pushPoints(p: Point) {
            possibleResultPoints.add(p)
            if (possibleResultPoints.size > 20) {
                possibleResultPoints.clear()
            }
        }

        init {
            cornerPaint.color = "#ffffff".toColor()
            paint.color = "#88000000".toColor()
            laserPaint.color = CurrentTheme.getColorInActive(context)
            pointPaint.color = CurrentTheme.getColorPrimary(context)
        }

        private fun aroundPoint(x: Int, y: Int, r: Int): RectF {
            rectTmp.set((x - r).toFloat(), (y - r).toFloat(), (x + r).toFloat(), (y + r).toFloat())
            return rectTmp
        }

        private fun lerp(a: Int, b: Int, f: Float): Int {
            return (a + f * (b - a)).toInt()
        }

        override fun onDraw(canvas: Canvas) {
            val s = width.coerceAtMost(height) - Utils.dp(10f)
            frame.left = (width - s) / 2
            frame.top = (height - s) / 2
            frame.bottom = frame.top + s
            frame.right = frame.left + s
            val width: Int = width
            val height: Int = height

            canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
            canvas.drawRect(
                0f,
                frame.top.toFloat(),
                frame.left.toFloat(),
                (frame.bottom + 1).toFloat(),
                paint
            )
            canvas.drawRect(
                (frame.right + 1).toFloat(),
                frame.top.toFloat(),
                width.toFloat(),
                (frame.bottom + 1).toFloat(),
                paint
            )
            canvas.drawRect(
                0f,
                (frame.bottom + 1).toFloat(),
                width.toFloat(),
                height.toFloat(),
                paint
            )
            val lineWidth =
                lerp(0, Utils.dp(4f), 1f)
            val halfLineWidth = lineWidth / 2
            val lineLength = lerp(
                (frame.right - frame.left).coerceAtMost(frame.bottom - frame.top),
                Utils.dp(20f),
                1f
            )
            path.reset()
            path.arcTo(aroundPoint(frame.left, frame.top + lineLength, halfLineWidth), 0f, 180f)
            path.arcTo(
                aroundPoint(
                    (frame.left + lineWidth * 1.5f).toInt(),
                    (frame.top + lineWidth * 1.5f).toInt(), lineWidth * 2
                ), 180f, 90f
            )
            path.arcTo(aroundPoint(frame.left + lineLength, frame.top, halfLineWidth), 270f, 180f)
            path.lineTo(
                (frame.left + halfLineWidth).toFloat(),
                (frame.top + halfLineWidth).toFloat()
            )
            path.arcTo(
                aroundPoint(
                    (frame.left + lineWidth * 1.5f).toInt(),
                    (frame.top + lineWidth * 1.5f).toInt(), lineWidth
                ), 270f, -90f
            )
            path.close()
            canvas.drawPath(path, cornerPaint)
            path.reset()
            path.arcTo(aroundPoint(frame.right, frame.top + lineLength, halfLineWidth), 180f, -180f)
            path.arcTo(
                aroundPoint(
                    (frame.right - lineWidth * 1.5f).toInt(),
                    (frame.top + lineWidth * 1.5f).toInt(), lineWidth * 2
                ), 0f, -90f
            )
            path.arcTo(aroundPoint(frame.right - lineLength, frame.top, halfLineWidth), 270f, -180f)
            path.arcTo(
                aroundPoint(
                    (frame.right - lineWidth * 1.5f).toInt(),
                    (frame.top + lineWidth * 1.5f).toInt(), lineWidth
                ), 270f, 90f
            )
            path.close()
            canvas.drawPath(path, cornerPaint)
            path.reset()
            path.arcTo(aroundPoint(frame.left, frame.bottom - lineLength, halfLineWidth), 0f, -180f)
            path.arcTo(
                aroundPoint(
                    (frame.left + lineWidth * 1.5f).toInt(),
                    (frame.bottom - lineWidth * 1.5f).toInt(), lineWidth * 2
                ), 180f, -90f
            )
            path.arcTo(
                aroundPoint(frame.left + lineLength, frame.bottom, halfLineWidth),
                90f,
                -180f
            )
            path.arcTo(
                aroundPoint(
                    (frame.left + lineWidth * 1.5f).toInt(),
                    (frame.bottom - lineWidth * 1.5f).toInt(), lineWidth
                ), 90f, 90f
            )
            path.close()
            canvas.drawPath(path, cornerPaint)
            path.reset()
            path.arcTo(
                aroundPoint(frame.right, frame.bottom - lineLength, halfLineWidth),
                180f,
                180f
            )
            path.arcTo(
                aroundPoint(
                    (frame.right - lineWidth * 1.5f).toInt(),
                    (frame.bottom - lineWidth * 1.5f).toInt(), lineWidth * 2
                ), 0f, 90f
            )
            path.arcTo(
                aroundPoint(frame.right - lineLength, frame.bottom, halfLineWidth),
                90f,
                180f
            )
            path.arcTo(
                aroundPoint(
                    (frame.right - lineWidth * 1.5f).toInt(),
                    (frame.bottom - lineWidth * 1.5f).toInt(), lineWidth
                ), 90f, -90f
            )
            path.close()
            canvas.drawPath(path, cornerPaint)

            laserPaint.alpha = SCANNER_ALPHA[scannerAlpha]
            if (scannerAlpha == 0) {
                possibleResultPoints.clear()
            }
            scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size

            val middle = frame.height() / 2 + frame.top
            canvas.drawRect(
                (frame.left + 2).toFloat(),
                (middle - 1).toFloat(),
                (frame.right - 1).toFloat(),
                (middle + 2).toFloat(),
                laserPaint
            )

            if (previewSize.width > 0 && previewSize.height > 0) {
                val scaleX: Float = frame.width() / previewSize.width.toFloat()
                val scaleY: Float = frame.height() / previewSize.height.toFloat()

                // draw current possible result points
                if (possibleResultPoints.isNotEmpty()) {
                    for (point in possibleResultPoints) {
                        canvas.drawCircle(
                            frame.left + point.x * scaleX,
                            frame.top + point.y * scaleY,
                            POINT_SIZE, pointPaint
                        )
                    }
                }
            }

            postInvalidateDelayed(
                ANIMATION_DELAY,
                (frame.left - POINT_SIZE).toInt(),
                (frame.top - POINT_SIZE).toInt(),
                (frame.right + POINT_SIZE).toInt(),
                (frame.bottom + POINT_SIZE).toInt()
            )
        }
    }

    companion object {
        private fun alphaBlendWithWhiteColor(source: Bitmap?): Bitmap? {
            if (source == null) {
                return null
            }
            var workBitmap = source
            var needClean = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && source.config == Bitmap.Config.HARDWARE) {
                workBitmap = source.copy(Bitmap.Config.ARGB_8888, true)
                needClean = true
            }
            val bitmapWidth = workBitmap.width
            val bitmapHeight = workBitmap.height

            val output: Bitmap = createBitmap(
                bitmapWidth,
                bitmapHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            paint.shader = BitmapShader(workBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

            canvas.drawColor(Color.WHITE)
            canvas.drawRect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat(), paint)
            if (needClean) {
                workBitmap.recycle()
            }
            return output
        }

        fun decodeFromBitmap(bitmap: Bitmap?): String? {
            val finalBitmap = alphaBlendWithWhiteColor(bitmap) ?: return "error"
            val reader = ZXingWrapper()
            reader.readerOptions.apply {
                formats = setOf(
                    ZXingWrapper.Format.QR_CODE,
                    ZXingWrapper.Format.EAN_8,
                    ZXingWrapper.Format.EAN_13,
                    ZXingWrapper.Format.CODE_128,
                    ZXingWrapper.Format.DATA_MATRIX
                )
                tryHarder = false
                tryInvert = true
                tryRotate = true
                tryDownscale = false
            }

            val result = try {
                reader.readBitmap(finalBitmap)
            } catch (e: Exception) {
                finalBitmap.recycle()
                return e.localizedMessage
            }
            finalBitmap.recycle()
            for ((_, text) in result) {
                if (text.nonNullNoEmpty()) {
                    return text
                }
            }
            return null
        }
    }
}