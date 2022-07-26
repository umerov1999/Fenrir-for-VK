package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils
import dev.ragnarok.fenrir.media.gif.IGifPlayer
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.GifPagerPresenter
import dev.ragnarok.fenrir.mvp.view.IGifPagerView
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.Utils.createPageTransform
import dev.ragnarok.fenrir.util.Utils.dp
import dev.ragnarok.fenrir.view.CircleCounterButton
import dev.ragnarok.fenrir.view.ExpandableSurfaceView
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import java.lang.ref.WeakReference

class GifPagerFragment : AbsDocumentPreviewFragment<GifPagerPresenter, IGifPagerView>(),
    IGifPagerView {
    private val mHolderSparseArray = SparseArray<WeakReference<Holder>>()
    private var mViewPager: ViewPager2? = null
    private var mToolbar: Toolbar? = null
    private var mButtonsRoot: View? = null
    private var mButtonAddOrDelete: CircleCounterButton? = null
    private var mFullscreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mFullscreen = savedInstanceState.getBoolean("mFullscreen")
        }
    }

    override val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        lazyPresenter {
            fireWritePermissionResolved(
                requireActivity(),
                requireView()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_gif_pager, container, false)
        mToolbar = root.findViewById(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(mToolbar)
        mButtonsRoot = root.findViewById(R.id.buttons)
        mButtonAddOrDelete = root.findViewById(R.id.button_add_or_delete)
        mButtonAddOrDelete?.setOnClickListener {
            presenter?.fireAddDeleteButtonClick()
        }
        mViewPager = root.findViewById(R.id.view_pager)
        mViewPager?.offscreenPageLimit = 1
        mViewPager?.setPageTransformer(
            createPageTransform(
                Settings.get().main().viewpager_page_transform
            )
        )
        mViewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                presenter?.firePageSelected(
                    position
                )
            }
        })
        root.findViewById<View>(R.id.button_share).setOnClickListener {
            presenter?.fireShareButtonClick()
        }
        root.findViewById<View>(R.id.button_download).setOnClickListener {
            presenter?.fireDownloadButtonClick(
                requireActivity(),
                requireView()
            )
        }
        resolveFullscreenViews()
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("mFullscreen", mFullscreen)
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

    private fun toggleFullscreen() {
        mFullscreen = !mFullscreen
        resolveFullscreenViews()
    }

    private fun resolveFullscreenViews() {
        mToolbar?.visibility = if (mFullscreen) View.GONE else View.VISIBLE
        mButtonsRoot?.visibility = if (mFullscreen) View.GONE else View.VISIBLE
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<GifPagerPresenter> {
        return object : IPresenterFactory<GifPagerPresenter> {
            override fun create(): GifPagerPresenter {
                val aid = requireArguments().getInt(Extra.ACCOUNT_ID)
                val index = requireArguments().getInt(Extra.INDEX)
                val documents: ArrayList<Document> =
                    requireArguments().getParcelableArrayList(Extra.DOCS)!!
                return GifPagerPresenter(aid, documents, index, saveInstanceState)
            }
        }
    }

    override fun displayData(pageCount: Int, selectedIndex: Int) {
        if (mViewPager != null) {
            val adapter = Adapter(pageCount)
            mViewPager?.adapter = adapter
            mViewPager?.setCurrentItem(selectedIndex, false)
        }
    }

    override fun setAspectRatioAt(position: Int, w: Int, h: Int) {
        val holder = findByPosition(position)
        holder?.mSurfaceView?.setAspectRatio(w, h)
    }

    override fun setPreparingProgressVisible(position: Int, preparing: Boolean) {
        for (i in 0 until mHolderSparseArray.size()) {
            val key = mHolderSparseArray.keyAt(i)
            val holder = findByPosition(key)
            if (holder != null) {
                val isCurrent = position == key
                val progressVisible = isCurrent && preparing
                holder.setProgressVisible(progressVisible)
                holder.mSurfaceView.visibility =
                    if (isCurrent && !preparing) View.VISIBLE else View.GONE
            }
        }
    }

    override fun setupAddRemoveButton(addEnable: Boolean) {
        mButtonAddOrDelete?.setIcon(if (addEnable) R.drawable.plus else R.drawable.ic_outline_delete)
    }

    override fun attachDisplayToPlayer(adapterPosition: Int, gifPlayer: IGifPlayer?) {
        val holder = findByPosition(adapterPosition)
        if (holder != null && gifPlayer != null && holder.isSurfaceReady) {
            gifPlayer.setDisplay(holder.mSurfaceHolder)
        }
    }

    override fun configHolder(
        adapterPosition: Int,
        progress: Boolean,
        aspectRatioW: Int,
        aspectRatioH: Int
    ) {
        val holder = findByPosition(adapterPosition)
        if (holder != null) {
            holder.setProgressVisible(progress)
            holder.mSurfaceView.setAspectRatio(aspectRatioW, aspectRatioH)
            holder.mSurfaceView.visibility =
                if (progress) View.GONE else View.VISIBLE
        }
    }

    private fun fireHolderCreate(holder: Holder) {
        presenter?.fireHolderCreate(
            holder.bindingAdapterPosition
        )
    }

    private fun findByPosition(position: Int): Holder? {
        val weak = mHolderSparseArray[position]
        return weak?.get()
    }

    inner class Holder internal constructor(rootView: View) : RecyclerView.ViewHolder(rootView),
        SurfaceHolder.Callback {
        val mSurfaceView: ExpandableSurfaceView = rootView.findViewById(R.id.videoSurface)
        val mSurfaceHolder: SurfaceHolder = mSurfaceView.holder
        private val mProgressBar: RLottieImageView
        var isSurfaceReady = false
        override fun surfaceCreated(holder: SurfaceHolder) {
            isSurfaceReady = true
            presenter?.fireSurfaceCreated(
                bindingAdapterPosition
            )
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            isSurfaceReady = false
        }

        fun setProgressVisible(visible: Boolean) {
            mProgressBar.visibility = if (visible) View.VISIBLE else View.GONE
            if (visible) {
                mProgressBar.fromRes(
                    dev.ragnarok.fenrir_common.R.raw.loading,
                    dp(100f),
                    dp(100f),
                    intArrayOf(
                        0x000000,
                        CurrentTheme.getColorPrimary(requireActivity()),
                        0x777777,
                        CurrentTheme.getColorSecondary(requireActivity())
                    )
                )
                mProgressBar.playAnimation()
            } else {
                mProgressBar.clearAnimationDrawable()
            }
        }

        init {
            mSurfaceHolder.addCallback(this)
            mProgressBar = rootView.findViewById(R.id.preparing_progress_bar)
            mSurfaceView.setOnClickListener { toggleFullscreen() }
        }
    }

    override fun toolbarTitle(@StringRes titleRes: Int, vararg params: Any?) {
        ActivityUtils.supportToolbarFor(this)?.title = getString(titleRes, *params)
    }

    override fun toolbarSubtitle(@StringRes titleRes: Int, vararg params: Any?) {
        ActivityUtils.supportToolbarFor(this)?.subtitle = getString(titleRes, *params)
    }

    private inner class Adapter(val mPageCount: Int) :
        RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(container: ViewGroup, viewType: Int): Holder {
            return Holder(
                LayoutInflater.from(container.context)
                    .inflate(R.layout.content_gif_page, container, false)
            )
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {}
        override fun getItemCount(): Int {
            return mPageCount
        }

        override fun onViewDetachedFromWindow(holder: Holder) {
            super.onViewDetachedFromWindow(holder)
            mHolderSparseArray.remove(holder.bindingAdapterPosition)
        }

        override fun onViewAttachedToWindow(holder: Holder) {
            super.onViewAttachedToWindow(holder)
            mHolderSparseArray.put(holder.bindingAdapterPosition, WeakReference(holder))
            fireHolderCreate(holder)
        }

        init {
            mHolderSparseArray.clear()
        }
    }

    companion object {
        fun newInstance(args: Bundle?): GifPagerFragment {
            val fragment = GifPagerFragment()
            fragment.arguments = args
            return fragment
        }

        fun buildArgs(aid: Int, documents: ArrayList<Document>, index: Int): Bundle {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, aid)
            args.putInt(Extra.INDEX, index)
            args.putParcelableArrayList(Extra.DOCS, documents)
            return args
        }
    }
}