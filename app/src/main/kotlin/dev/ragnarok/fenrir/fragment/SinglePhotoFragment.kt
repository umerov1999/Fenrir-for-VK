package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso3.Callback
import dev.ragnarok.fenrir.App.Companion.instance
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.fragment.base.BaseFragment
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.listener.BackPressCallback
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadPhoto
import dev.ragnarok.fenrir.util.DownloadWorkUtils.makeLegalFilenameFromArg
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.rxutils.RxUtils
import dev.ragnarok.fenrir.view.CircleCounterButton
import dev.ragnarok.fenrir.view.TouchImageView
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import dev.ragnarok.fenrir.view.pager.GoBackCallback
import dev.ragnarok.fenrir.view.pager.WeakGoBackAnimationAdapter
import dev.ragnarok.fenrir.view.pager.WeakPicassoLoadCallback
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SinglePhotoFragment : BaseFragment(), GoBackCallback, BackPressCallback {
    private val mGoBackAnimationAdapter = WeakGoBackAnimationAdapter(this)
    private var url: String? = null
    private var prefix: String? = null
    private var photo_prefix: String? = null
    private var mFullscreen = false
    private var mDownload: CircleCounterButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mFullscreen = savedInstanceState.getBoolean("mFullscreen")
        }
        url = requireArguments().getString(Extra.URL)
        prefix = makeLegalFilenameFromArg(requireArguments().getString(Extra.STATUS), null)
        photo_prefix = makeLegalFilenameFromArg(requireArguments().getString(Extra.KEY), null)
    }

    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        doSaveOnDrive(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_single_url_photo, container, false)
        mDownload = root.findViewById(R.id.button_download)
        url?.let {
            mDownload?.visibility =
                if (it.contains("content://") || it.contains("file://")) View.GONE else View.VISIBLE
        }
        url ?: run {
            mDownload?.visibility = View.GONE
        }
        val ret = PhotoViewHolder(root)
        ret.bindTo(url)

        ret.photo.setOnLongClickListener {
            doSaveOnDrive(true)
            true
        }
        ret.photo.setOnTouchListener { view: View, event: MotionEvent ->
            if (event.pointerCount >= 2 || view.canScrollHorizontally(1) && view.canScrollHorizontally(
                    -1
                )
            ) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        container?.requestDisallowInterceptTouchEvent(true)
                        return@setOnTouchListener false
                    }
                    MotionEvent.ACTION_UP -> {
                        container?.requestDisallowInterceptTouchEvent(false)
                        return@setOnTouchListener true
                    }
                }
            }
            true
        }
        mDownload?.setOnClickListener { doSaveOnDrive(true) }
        return root
    }

    private fun doSaveOnDrive(Request: Boolean) {
        if (Request) {
            if (!AppPerms.hasReadWriteStoragePermission(instance)) {
                requestWritePermission.launch()
            }
        }
        var dir = File(Settings.get().other().photoDir)
        if (!dir.isDirectory) {
            val created = dir.mkdirs()
            if (!created) {
                CreateCustomToast(requireActivity()).showToastError("Can't create directory $dir")
                return
            }
        } else dir.setLastModified(Calendar.getInstance().time.time)
        if (prefix != null && Settings.get().other().isPhoto_to_user_dir) {
            val dir_final = File(dir.absolutePath + "/" + prefix)
            if (!dir_final.isDirectory) {
                val created = dir_final.mkdirs()
                if (!created) {
                    CreateCustomToast(requireActivity()).showToastError("Can't create directory $dir")
                    return
                }
            } else dir_final.setLastModified(Calendar.getInstance().time.time)
            dir = dir_final
        }
        val DOWNLOAD_DATE_FORMAT: DateFormat =
            SimpleDateFormat("yyyyMMdd_HHmmss", Utils.appLocale)
        url?.let {
            doDownloadPhoto(
                requireActivity(),
                it,
                dir.absolutePath,
                Utils.firstNonEmptyString(prefix, "null") + "." + Utils.firstNonEmptyString(
                    photo_prefix,
                    "null"
                ) + ".profile." + DOWNLOAD_DATE_FORMAT.format(Date())
            )
        }
    }

    override fun goBack() {
        if (isAdded) {
            if (canGoBack()) {
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                requireActivity().finish()
            }
        }
    }

    private fun canGoBack(): Boolean {
        return requireActivity().supportFragmentManager.backStackEntryCount > 1
    }

    override fun onBackPressed(): Boolean {
        val objectAnimatorPosition = ObjectAnimator.ofFloat(view, "translationY", -600f)
        val objectAnimatorAlpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimatorPosition, objectAnimatorAlpha)
        animatorSet.duration = 200
        animatorSet.addListener(mGoBackAnimationAdapter)
        animatorSet.start()
        return false
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(true)
            .setBarsColored(colored = false, invertIcons = false)
            .build()
            .apply(requireActivity())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("mFullscreen", mFullscreen)
    }

    private fun resolveFullscreenViews() {
        mDownload?.visibility = if (mFullscreen) View.GONE else View.VISIBLE
    }

    private fun toggleFullscreen() {
        mFullscreen = !mFullscreen
        resolveFullscreenViews()
    }

    private inner class PhotoViewHolder(view: View) : Callback {
        val reload: FloatingActionButton
        private val mPicassoLoadCallback: WeakPicassoLoadCallback
        val photo: TouchImageView
        val progress: RLottieImageView
        var animationDispose: Disposable = Disposable.disposed()
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
                CreateCustomToast(requireActivity()).showToast(R.string.empty_url)
            }
        }

        private fun resolveProgressVisibility(forceStop: Boolean) {
            animationDispose.dispose()
            if (mAnimationLoaded && !mLoadingNow && !forceStop) {
                mAnimationLoaded = false
                val k = ObjectAnimator.ofFloat(progress, View.ALPHA, 0.0f).setDuration(1000)
                k.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        progress.clearAnimationDrawable()
                        progress.visibility = View.GONE
                        progress.alpha = 1f
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        progress.clearAnimationDrawable()
                        progress.visibility = View.GONE
                        progress.alpha = 1f
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                })
                k.start()
            } else if (mAnimationLoaded && !mLoadingNow) {
                mAnimationLoaded = false
                progress.clearAnimationDrawable()
                progress.visibility = View.GONE
            } else if (mLoadingNow) {
                animationDispose = Completable.create {
                    it.onComplete()
                }.delay(300, TimeUnit.MILLISECONDS).fromIOToMain().subscribe({
                    mAnimationLoaded = true
                    progress.visibility = View.VISIBLE
                    progress.fromRes(
                        dev.ragnarok.fenrir_common.R.raw.loading,
                        Utils.dp(100F),
                        Utils.dp(100F),
                        intArrayOf(
                            0x000000,
                            CurrentTheme.getColorPrimary(requireActivity()),
                            0x777777,
                            CurrentTheme.getColorSecondary(requireActivity())
                        )
                    )
                    progress.playAnimation()
                }, RxUtils.ignore())
            }
        }

        private fun loadImage(url: String?) {
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

    companion object {


        fun newInstance(args: Bundle?): SinglePhotoFragment {
            val fragment = SinglePhotoFragment()
            fragment.arguments = args
            return fragment
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