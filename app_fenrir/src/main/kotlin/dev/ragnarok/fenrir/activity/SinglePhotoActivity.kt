package dev.ragnarok.fenrir.activity

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso3.Callback
import com.squareup.picasso3.Rotatable
import dev.ragnarok.fenrir.App
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.StubAnimatorListener
import dev.ragnarok.fenrir.activity.slidr.Slidr.attach
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig
import dev.ragnarok.fenrir.activity.slidr.model.SlidrListener
import dev.ragnarok.fenrir.activity.slidr.model.SlidrPosition
import dev.ragnarok.fenrir.applyAlpha
import dev.ragnarok.fenrir.fragment.audio.AudioPlayerFragment
import dev.ragnarok.fenrir.listener.AppStyleable
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceProvider
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.CurrentTheme.getNavigationBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarColor
import dev.ragnarok.fenrir.settings.CurrentTheme.getStatusBarNonColored
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.DownloadWorkUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toMain
import dev.ragnarok.fenrir.util.toast.CustomToast
import dev.ragnarok.fenrir.view.CircleCounterButton
import dev.ragnarok.fenrir.view.TouchImageView
import dev.ragnarok.fenrir.view.natives.animation.ThorVGLottieView
import dev.ragnarok.fenrir.view.pager.WeakPicassoLoadCallback
import java.io.File
import java.lang.ref.WeakReference
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class SinglePhotoActivity : NoMainActivity(), PlaceProvider, AppStyleable {
    private var url: String? = null
    private var prefix: String? = null
    private var photo_prefix: String? = null
    private var mFullscreen = false
    private var mDownload: CircleCounterButton? = null
    private var mDecorView: View? = null
    private var canDownload = true

    @get:LayoutRes
    override val noMainContentView: Int
        get() = R.layout.activity_single_url_photo

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        mFullscreen = savedInstanceState?.getBoolean("mFullscreen") == true

        mDecorView = window.decorView
        mDownload = findViewById(R.id.button_download)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.photo_single_root)) { v, windowInsets ->
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
        url?.let {
            canDownload = !it.contains("content://") && !it.contains("file://")
            mDownload?.visibility =
                if (it.contains("content://") || it.contains("file://")) View.GONE else View.VISIBLE
        }
        url ?: run {
            canDownload = false
            mDownload?.visibility = View.GONE
        }
        val ret = PhotoViewHolder(this)
        ret.bindTo(url)
        val mContentRoot = findViewById<RelativeLayout>(R.id.photo_single_root)
        attach(
            this,
            SlidrConfig.Builder().setAlphaForView(false).fromUnColoredToColoredStatusBar(true)
                .position(SlidrPosition.VERTICAL)
                .listener(object : SlidrListener {
                    override fun onSlideStateChanged(state: Int) {

                    }

                    @SuppressLint("Range")
                    override fun onSlideChange(percent: Float) {
                        var tmp = 1f - percent
                        tmp *= 4
                        tmp = Utils.clamp(1f - tmp, 0f, 1f)
                        mContentRoot?.setBackgroundColor(Color.argb(tmp, 0f, 0f, 0f))
                        if (canDownload) {
                            mDownload?.alpha = tmp
                        }
                        ret.photo.alpha = Utils.clamp(percent, 0f, 1f)
                    }

                    override fun onSlideOpened() {
                    }

                    override fun onSlideClosed(): Boolean {
                        Utils.finishActivityImmediate(this@SinglePhotoActivity)
                        return true
                    }

                }).build()
        )
        ret.photo.setOnLongClickListener {
            val lCMode = Settings.get().main().longClickPhoto
            when (lCMode) {
                1 -> {
                    doSaveOnDrive(true)
                    true
                }

                2 if ret.photo.drawable is Rotatable -> {
                    var rot = (ret.photo.drawable as Rotatable).getRotation() + 45
                    if (rot >= 360f) {
                        rot = 0f
                    }
                    (ret.photo.drawable as Rotatable).rotate(rot)
                    ret.photo.fitImageToView()
                    ret.photo.invalidate()
                    true
                }

                else -> {
                    false
                }
            }
        }
        mDownload?.setOnClickListener { doSaveOnDrive(true) }
        resolveFullscreenViews()

        ret.photo.setOnTouchListener { view, event ->
            (event.pointerCount >= 2 || view is TouchImageView && view.isZoomed) && when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    mContentRoot?.requestDisallowInterceptTouchEvent(true)
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mContentRoot?.requestDisallowInterceptTouchEvent(false)
                    true
                }

                else -> false
            }
        }
    }

    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        doSaveOnDrive(false)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) {
            finish()
            return
        }
        if (Intent.ACTION_VIEW == intent.action) {
            val data = intent.data
            url = "full_$data"
            prefix = "tmp"
            photo_prefix = "tmp"
        } else {
            url = intent.extras?.getString(Extra.URL)
            prefix = DownloadWorkUtils.makeLegalFilenameFromArg(
                intent.extras?.getString(Extra.STATUS),
                null
            )
            photo_prefix = DownloadWorkUtils.makeLegalFilenameFromArg(
                intent.extras?.getString(Extra.KEY),
                null
            )
        }
    }

    override fun openPlace(place: Place) {
        val args = place.safeArguments()
        when (place.type) {
            Place.PLAYER -> {
                val player = supportFragmentManager.findFragmentByTag("audio_player")
                if (player is AudioPlayerFragment) player.dismiss()
                AudioPlayerFragment.newInstance(args).show(supportFragmentManager, "audio_player")
            }

            else -> Utils.openPlaceWithSwipebleActivity(this, place)
        }
    }

    private fun doSaveOnDrive(Request: Boolean) {
        if (Request) {
            if (!AppPerms.hasReadWriteStoragePermission(App.instance)) {
                requestWritePermission.launch()
            }
        }
        var dir = File(Settings.get().main().photoDir)
        if (!dir.isDirectory) {
            val created = dir.mkdirs()
            if (!created) {
                CustomToast.createCustomToast(this, null)
                    ?.showToastError("Can't create directory $dir")
                return
            }
        } else dir.setLastModified(System.currentTimeMillis())
        if (prefix != null && Settings.get().main().isPhoto_to_user_dir) {
            val dir_final = File(dir.absolutePath + "/" + prefix)
            if (!dir_final.isDirectory) {
                val created = dir_final.mkdirs()
                if (!created) {
                    CustomToast.createCustomToast(this, null)
                        ?.showToastError("Can't create directory $dir")
                    return
                }
            } else dir_final.setLastModified(System.currentTimeMillis())
            dir = dir_final
        }
        val DOWNLOAD_DATE_FORMAT: DateFormat =
            SimpleDateFormat("yyyyMMdd_HHmmss", Utils.appLocale)
        url?.let {
            DownloadWorkUtils.doDownloadPhoto(
                this,
                it,
                dir.absolutePath,
                Utils.firstNonEmptyString(prefix, "null") + "." + Utils.firstNonEmptyString(
                    photo_prefix,
                    "null"
                ) + ".profile." + DOWNLOAD_DATE_FORMAT.format(Date())
            )
        }
    }

    private inner class PhotoViewHolder(view: SinglePhotoActivity) : Callback {
        private val ref = WeakReference(view)
        val reload: FloatingActionButton
        private val mPicassoLoadCallback: WeakPicassoLoadCallback
        val photo: TouchImageView
        val progress: ThorVGLottieView
        var animationDispose = CancelableJob()
        private var mAnimationLoaded = false
        private var mLoadingNow = false
        fun bindTo(url: String?) {
            reload.setOnClickListener {
                reload.visibility = View.INVISIBLE
                if (url.nonNullNoEmpty()) {
                    loadImage(url)
                } else PicassoInstance.with().cancelRequest(photo)
            }
            if (url.nonNullNoEmpty()) {
                loadImage(url)
            } else {
                PicassoInstance.with().cancelRequest(photo)
                CustomToast.createCustomToast(ref.get(), null)?.showToast(R.string.empty_url)
            }
        }

        private fun resolveProgressVisibility(forceStop: Boolean) {
            animationDispose.cancel()
            if (mAnimationLoaded && !mLoadingNow && !forceStop) {
                mAnimationLoaded = false
                val k = ObjectAnimator.ofFloat(progress, View.ALPHA, 0.0f).setDuration(1000)
                k.addListener(object : StubAnimatorListener() {
                    override fun onAnimationEnd(animation: Animator) {
                        progress.releaseAnimation()
                        progress.visibility = View.GONE
                        progress.alpha = 1f
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        progress.releaseAnimation()
                        progress.visibility = View.GONE
                        progress.alpha = 1f
                    }
                })
                k.start()
            } else if (mAnimationLoaded && !mLoadingNow) {
                mAnimationLoaded = false
                progress.releaseAnimation()
                progress.visibility = View.GONE
            } else if (mLoadingNow) {
                animationDispose += delayTaskFlow(300).toMain {
                    mAnimationLoaded = true
                    progress.visibility = View.VISIBLE
                    progress.fromRes(
                        dev.ragnarok.fenrir_common.R.raw.loading,
                        intArrayOf(
                            0x000000,
                            CurrentTheme.getColorPrimary(ref.get()),
                            0x777777,
                            CurrentTheme.getColorSecondary(ref.get())
                        )
                    )
                    progress.startAnimation()
                }
            }
        }

        private fun loadImage(url: String?) {
            PicassoInstance.with().cancelRequest(photo)
            mLoadingNow = true
            resolveProgressVisibility(true)
            PicassoInstance.with()
                .load(url)
                .into(photo, mPicassoLoadCallback)
        }

        @IdRes
        private fun idOfImageView(): Int {
            return R.id.image_view
        }

        @IdRes
        private fun idOfProgressBar(): Int {
            return R.id.progress_bar
        }

        override fun onSuccess() {
            mLoadingNow = false
            resolveProgressVisibility(false)
            reload.visibility = View.INVISIBLE
        }

        override fun onError(t: Throwable) {
            mLoadingNow = false
            resolveProgressVisibility(true)
            reload.visibility = View.VISIBLE
        }

        init {
            photo = view.findViewById(idOfImageView())
            photo.maxZoom = 8f
            photo.doubleTapScale = 2f
            photo.doubleTapMaxZoom = 4f
            progress = view.findViewById(idOfProgressBar())
            reload = view.findViewById(R.id.goto_button)
            mPicassoLoadCallback = WeakPicassoLoadCallback(this)
            photo.setOnClickListener { toggleFullscreen() }
        }
    }

    internal fun toggleFullscreen() {
        mFullscreen = !mFullscreen
        resolveFullscreenViews()
    }

    private fun resolveFullscreenViews() {
        mDownload?.visibility = if (mFullscreen || !canDownload) View.GONE else View.VISIBLE

        if (mFullscreen) {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("mFullscreen", mFullscreen)
    }

    override fun hideMenu(hide: Boolean) {}
    override fun openMenu(open: Boolean) {}

    override fun setStatusbarColored(colored: Boolean, invertIcons: Boolean) {
        val statusBarColor = if (colored) getStatusBarColor(this) else getStatusBarNonColored(
            this
        )
        val navigationBarColor = if (colored) getNavigationBarColor(this) else Color.BLACK

        val statusBarStyle = if (invertIcons) SystemBarStyle.light(
            statusBarColor.applyAlpha(180),
            statusBarColor.applyAlpha(180)
        ) else SystemBarStyle.dark(statusBarColor.applyAlpha(180))
        val navigationBarStyle = if (invertIcons) SystemBarStyle.light(
            navigationBarColor.applyAlpha(180),
            navigationBarColor.applyAlpha(180)
        ) else SystemBarStyle.dark(navigationBarColor.applyAlpha(180))

        for (i in (window.decorView as ViewGroup)) {
            if (i is ProtectionLayout) {
                (window.decorView as ViewGroup).removeView(i)
            }
        }

        enableEdgeToEdge(statusBarStyle, navigationBarStyle)
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(true)
            .setBarsColored(colored = false, invertIcons = false)
            .build()
            .apply(this)
    }

    companion object {
        private const val ACTION_OPEN =
            "dev.ragnarok.fenrir.activity.SinglePhotoActivity"

        fun newInstance(context: Context, args: Bundle?): Intent {
            val ph = Intent(context, SinglePhotoActivity::class.java)
            val targetArgs = Bundle()
            targetArgs.putAll(args)
            ph.action = ACTION_OPEN
            ph.putExtras(targetArgs)
            return ph
        }

        fun buildArgs(url: String?, download_prefix: String?, photo_prefix: String?): Bundle {
            val args = Bundle()
            args.putString(Extra.URL, url)
            args.putString(Extra.STATUS, download_prefix)
            args.putString(Extra.KEY, photo_prefix)
            return args
        }
    }
}
