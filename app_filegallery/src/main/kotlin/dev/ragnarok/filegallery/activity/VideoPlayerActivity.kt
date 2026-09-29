package dev.ragnarok.filegallery.activity

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Rational
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.customview.widget.ViewDragHelper
import dev.ragnarok.fenrir.module.FFmpegOkhttp
import dev.ragnarok.filegallery.R
import dev.ragnarok.filegallery.activity.slidr.Slidr
import dev.ragnarok.filegallery.activity.slidr.model.SlidrConfig
import dev.ragnarok.filegallery.activity.slidr.model.SlidrListener
import dev.ragnarok.filegallery.activity.slidr.model.SlidrPosition
import dev.ragnarok.filegallery.getParcelableCompat
import dev.ragnarok.filegallery.getParcelableExtraCompat
import dev.ragnarok.filegallery.media.video.ExoVideoPlayer
import dev.ragnarok.filegallery.media.video.IVideoPlayer
import dev.ragnarok.filegallery.media.video.IVideoPlayer.IVideoSizeChangeListener
import dev.ragnarok.filegallery.model.Video
import dev.ragnarok.filegallery.orZero
import dev.ragnarok.filegallery.settings.CurrentTheme.getColorPrimary
import dev.ragnarok.filegallery.settings.Settings.get
import dev.ragnarok.filegallery.settings.theme.ThemesController.currentStyle
import dev.ragnarok.filegallery.toColor
import dev.ragnarok.filegallery.util.Logger
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.util.toast.CustomToast
import dev.ragnarok.filegallery.view.ExpandableSurfaceView
import dev.ragnarok.filegallery.view.VideoControllerView

class VideoPlayerActivity : AppCompatActivity(),
    VideoControllerView.MediaPlayerControl, IVideoSizeChangeListener {
    private var mDecorView: View? = null
    private var mPlaySpeed: ImageView? = null
    private var mControllerView: VideoControllerView? = null
    private var mSurfaceView: ExpandableSurfaceView? = null
    private var mPlayer: IVideoPlayer? = null
    private var video: Video? = null
    private var onStopCalled = false
    private var seekSave: Long = -1
    private var isLandscape = false
    override fun onStop() {
        onStopCalled = true
        super.onStop()
    }

    override fun finish() {
        finishAndRemoveTask()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.d(VideoPlayerActivity::class.simpleName, "onNewIntent, intent: $intent")
        handleIntent(intent, true)
    }

    private fun handleIntent(intent: Intent?, update: Boolean) {
        if (intent == null) {
            return
        }
        video = intent.getParcelableExtraCompat(EXTRA_VIDEO)
        val actionBar = supportActionBar
        actionBar?.title = video?.title
        actionBar?.subtitle = video?.description
        if (update) {
            val url = video?.link
            mPlayer?.updateSource(this, url)
            mPlayer?.play()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(Utils.updateActivityContext(newBase))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("video", video)
        outState.putLong("seek", mPlayer?.currentPosition ?: -1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentStyle())
        Utils.prepareDensity(this)
        super.onCreate(savedInstanceState)
        FFmpegOkhttp.setLockNetwork(true)
        setContentView(R.layout.activity_video)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mDecorView = window.decorView
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.videoSurfaceContainer)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val insets2 =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            if (Utils.isLandscape(this)) {
                toolbar.setPadding(insets.left, insets.top, insets.right, 0)
                v.setPadding(
                    insets.left, 0,
                    insets.right,
                    insets.bottom
                )
            } else {
                toolbar.setPadding(
                    insets2.left,
                    insets2.top,
                    insets2.right,
                    0
                )
                v.setPadding(
                    insets2.left, 0,
                    insets2.right,
                    insets2.bottom
                )
            }
            WindowInsetsCompat.CONSUMED
        }

        Utils.applyEdgeToEdgeActivity(this, true)

        setSupportActionBar(toolbar)
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left)
            toolbar.setNavigationOnClickListener { finish() }
        }
        if (savedInstanceState == null) {
            handleIntent(intent, false)
        } else {
            video = savedInstanceState.getParcelableCompat("video")
            seekSave = savedInstanceState.getLong("seek")
            val actionBar = supportActionBar
            actionBar?.title = video?.title
            actionBar?.subtitle = video?.description
        }
        mControllerView = VideoControllerView(this)
        val surfaceContainer = findViewById<ViewGroup>(R.id.videoSurfaceContainer)
        mSurfaceView = findViewById(R.id.videoSurface)
        if (get().main().isVideo_swipes) {
            Slidr.attach(
                this,
                SlidrConfig.Builder().setAlphaForView(false).fromFromBlackToNormalNavigation(true)
                    .position(SlidrPosition.LEFT)
                    .listener(object : SlidrListener {
                        override fun onSlideStateChanged(state: Int) {
                            if (state != ViewDragHelper.STATE_IDLE) {
                                if (supportActionBar?.isShowing == true) {
                                    resolveControlsVisibility()
                                }
                            }
                        }

                        @SuppressLint("Range")
                        override fun onSlideChange(percent: Float) {
                            var tmp = 1f - percent
                            tmp *= 4
                            tmp = Utils.clamp(1f - tmp, 0f, 1f)
                            surfaceContainer?.setBackgroundColor(Color.argb(tmp, 0f, 0f, 0f))
                            mControllerView?.alpha = tmp
                            toolbar?.alpha = tmp
                            mSurfaceView?.alpha = Utils.clamp(percent, 0f, 1f)
                        }

                        override fun onSlideOpened() {

                        }

                        override fun onSlideClosed(): Boolean {
                            Utils.finishActivityImmediate(this@VideoPlayerActivity)
                            return true
                        }

                    }).build()
            )
        }
        surfaceContainer.setOnClickListener { resolveControlsVisibility() }
        val scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    mSurfaceView?.doScale(detector.getScaleFactor())
                    return true
                }
            })
        var isScaleMode = false
        @SuppressLint("ClickableViewAccessibility")
        surfaceContainer.setOnTouchListener { _, event ->
            if (event.pointerCount >= 2 || isScaleMode) {
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    isScaleMode = true
                } else if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                    isScaleMode = false
                }
                scaleGestureDetector.onTouchEvent(event)
            } else {
                false
            }
        }
        //val videoHolder = mSurfaceView?.holder
        //videoHolder?.addCallback(this)
        resolveControlsVisibility()
        mPlayer = createPlayer()
        mPlayer?.setTextureView(mSurfaceView)
        mPlayer?.addVideoSizeChangeListener(this)
        mPlayer?.play()
        if (seekSave > 0) {
            mPlayer?.seekTo(seekSave)
        }
        mPlaySpeed = findViewById(R.id.toolbar_play_speed)
        Utils.setTint(
            mPlaySpeed,
            if (mPlayer?.isPlaybackSpeed == true) getColorPrimary(this) else "#ffffff".toColor()
        )
        mPlaySpeed?.setOnClickListener {
            mPlayer?.togglePlaybackSpeed()
            Utils.setTint(
                mPlaySpeed,
                if (mPlayer?.isPlaybackSpeed == true) getColorPrimary(this) else "#ffffff".toColor()
            )
        }
        mControllerView?.setMediaPlayer(this)
        if (get().main().isVideo_controller_to_decor) {
            mControllerView?.setAnchorView(mDecorView as ViewGroup?, true)
        } else {
            mControllerView?.setAnchorView(findViewById(R.id.panel), false)
        }
        mControllerView?.updatePip(
            packageManager.hasSystemFeature(
                PackageManager.FEATURE_PICTURE_IN_PICTURE
            ) && hasPipPermission()
        )
    }

    private fun createPlayer(): IVideoPlayer {
        val url = video?.link
        return ExoVideoPlayer(this, url, object : IVideoPlayer.IUpdatePlayListener {
            override fun onPlayChanged(isPause: Boolean) {
                mControllerView?.updatePausePlay()
            }
        })
    }

    internal fun resolveControlsVisibility() {
        val actionBar = supportActionBar ?: return
        if (actionBar.isShowing) {
            actionBar.hide()
            mControllerView?.hide()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mDecorView?.layoutParams =
                    WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
            }

            WindowCompat.setDecorFitsSystemWindows(window, false)
            mDecorView?.let {
                WindowInsetsControllerCompat(window, it).let { controller ->
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            actionBar.show()
            mControllerView?.show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mDecorView?.layoutParams =
                    WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT)
            }
            WindowCompat.setDecorFitsSystemWindows(window, true)
            mDecorView?.let {
                WindowInsetsControllerCompat(
                    window,
                    it
                ).show(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    override fun onDestroy() {
        mPlayer?.release()
        FFmpegOkhttp.setLockNetwork(false)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        onStopCalled = false
        val actionBar = supportActionBar
        if (actionBar != null && actionBar.isShowing) {
            actionBar.hide()
            mControllerView?.hide()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mDecorView?.layoutParams =
                WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        mDecorView?.let {
            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        val actionBar = supportActionBar ?: return
        if (isInPictureInPictureMode) {
            actionBar.hide()
            mControllerView?.hide()
        } else {
            if (onStopCalled) {
                finish()
            } else {
                actionBar.show()
                mControllerView?.show()
            }
        }
    }

    private fun canVideoPause(): Boolean {
        return !isInPictureInPictureMode
    }

    override fun onPause() {
        if (canVideoPause()) {
            mPlayer?.pause()
            mControllerView?.updatePausePlay()
        }
        super.onPause()
    }

    /*
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceCreated(holder: SurfaceHolder) {
        mPlayer?.setSurfaceHolder(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

     */
    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override val bufferPercentage: Int
        get() = mPlayer?.bufferPercentage.orZero()
    override val currentPosition: Long
        get() = mPlayer?.currentPosition.orZero()
    override val bufferPosition: Long
        get() = mPlayer?.bufferPosition.orZero()
    override val duration: Long
        get() = mPlayer?.duration.orZero()
    override val isPlaying: Boolean
        get() = mPlayer?.isPlaying == true

    override fun pause() {
        mPlayer?.pause()
    }

    override fun seekTo(pos: Long) {
        mPlayer?.seekTo(pos)
    }

    override fun start() {
        mPlayer?.play()
    }

    override fun doRotateDisplay() {
        try {
            requestedOrientation =
                if (isLandscape) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } catch (_: Exception) {
            CustomToast.createCustomToast(this, mSurfaceView)?.setDuration(Toast.LENGTH_LONG)
                ?.showToastError(R.string.not_supported)
        }
    }

    private fun hasPipPermission(): Boolean {
        val appsOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager?
        return appsOps?.checkOpNoThrow(
            AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
            Process.myUid(),
            packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    override fun toPIPScreen() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            && hasPipPermission()
        ) if (!isInPictureInPictureMode) {
            val aspectRatio = Rational(mSurfaceView?.width.orZero(), mSurfaceView?.height.orZero())
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build()
            )
        }
    }

    override fun hideActionBar() {
        val actionBar = supportActionBar ?: return
        actionBar.hide()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mDecorView?.layoutParams =
                WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        mDecorView?.let {
            WindowInsetsControllerCompat(window, it).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false
        }
    }

    override fun onVideoSizeChanged(player: IVideoPlayer, w: Int, h: Int) {
        mSurfaceView?.setAspectRatio(w, h)
    }

    companion object {
        const val EXTRA_VIDEO = "video"

        fun newInstance(context: Context, args: Bundle): Intent {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtras(args)
            return intent
        }

        fun buildArgs(video: Video?): Bundle {
            val args = Bundle()
            args.putParcelable(EXTRA_VIDEO, video)
            return args
        }
    }
}
