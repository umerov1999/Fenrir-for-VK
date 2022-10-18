package dev.ragnarok.fenrir.fragment.gifpager

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
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
import dev.ragnarok.fenrir.fragment.absdocumentpreview.AbsDocumentPreviewFragment
import dev.ragnarok.fenrir.fragment.base.core.IPresenterFactory
import dev.ragnarok.fenrir.getParcelableArrayListCompat
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.PhotoSize
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.createPageTransform
import dev.ragnarok.fenrir.view.CircleCounterButton
import dev.ragnarok.fenrir.view.TouchImageView

class GifPagerFragment : AbsDocumentPreviewFragment<GifPagerPresenter, IGifPagerView>(),
    IGifPagerView {
    private var mViewPager: ViewPager2? = null
    private var mToolbar: Toolbar? = null
    private var mButtonsRoot: View? = null
    private var mButtonAddOrDelete: CircleCounterButton? = null
    private var mFullscreen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFullscreen = savedInstanceState?.getBoolean("mFullscreen") ?: false
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
                presenter?.selectPage(
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

    internal fun toggleFullscreen() {
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
                    requireArguments().getParcelableArrayListCompat(Extra.DOCS)!!
                return GifPagerPresenter(aid, documents, index, saveInstanceState)
            }
        }
    }

    override fun displayData(mDocuments: List<Document>, selectedIndex: Int) {
        if (mViewPager != null) {
            val adapter = Adapter(mDocuments)
            mViewPager?.adapter = adapter
            mViewPager?.setCurrentItem(selectedIndex, false)
        }
    }

    override fun setupAddRemoveButton(addEnable: Boolean) {
        mButtonAddOrDelete?.setIcon(if (addEnable) R.drawable.plus else R.drawable.ic_outline_delete)
    }

    inner class Holder internal constructor(rootView: View) : RecyclerView.ViewHolder(rootView) {
        val mGifView: TouchImageView = rootView.findViewById(R.id.gif_view)

        init {
            mGifView.setOnClickListener { toggleFullscreen() }
        }
    }

    override fun toolbarTitle(@StringRes titleRes: Int, vararg params: Any?) {
        ActivityUtils.supportToolbarFor(this)?.title = getString(titleRes, *params)
    }

    override fun toolbarSubtitle(@StringRes titleRes: Int, vararg params: Any?) {
        ActivityUtils.supportToolbarFor(this)?.subtitle = getString(titleRes, *params)
    }

    private inner class Adapter(private var data: List<Document>) :
        RecyclerView.Adapter<Holder>() {
        @SuppressLint("ClickableViewAccessibility")
        override fun onCreateViewHolder(container: ViewGroup, viewType: Int): Holder {
            val ret = Holder(
                LayoutInflater.from(container.context)
                    .inflate(R.layout.content_gif_page, container, false)
            )
            ret.mGifView.setOnTouchListener { view: View, event: MotionEvent ->
                if (event.pointerCount >= 2 || view.canScrollHorizontally(1) && view.canScrollHorizontally(
                        -1
                    )
                ) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                            container.requestDisallowInterceptTouchEvent(true)
                            return@setOnTouchListener false
                        }
                        MotionEvent.ACTION_UP -> {
                            container.requestDisallowInterceptTouchEvent(false)
                            return@setOnTouchListener true
                        }
                    }
                }
                true
            }
            return ret
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.mGifView.fromNet(
                data[position].ownerId.toString() + "_" + data[position].id.toString(),
                data[position].videoPreview?.src,
                data[position].getPreviewWithSize(PhotoSize.W, false), Utils.createOkHttp(5, true)
            )
        }

        override fun getItemCount(): Int {
            return data.size
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
