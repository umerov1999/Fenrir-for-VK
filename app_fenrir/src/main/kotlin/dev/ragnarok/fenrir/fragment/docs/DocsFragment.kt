package dev.ragnarok.fenrir.fragment.docs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.DualTabPhotoActivity.Companion.createIntent
import dev.ragnarok.fenrir.activity.MainActivity
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.dialog.PostShareDialog.Methods
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.fragment.base.MenuAdapter
import dev.ragnarok.fenrir.fragment.base.RecyclerBindableAdapter
import dev.ragnarok.fenrir.fragment.base.horizontal.HorizontalOptionsAdapter
import dev.ragnarok.fenrir.getParcelableArrayListExtraCompat
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.DocFilter
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.EditingPostType
import dev.ragnarok.fenrir.model.LocalPhoto
import dev.ragnarok.fenrir.model.PhotoSize
import dev.ragnarok.fenrir.model.Text
import dev.ragnarok.fenrir.model.menu.Item
import dev.ragnarok.fenrir.model.menu.options.DocsOption
import dev.ragnarok.fenrir.model.selection.FileManagerSelectableSource
import dev.ragnarok.fenrir.model.selection.LocalPhotosSelectableSource
import dev.ragnarok.fenrir.model.selection.Sources
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory.getDocPreviewPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getGifPagerPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerWallPlace
import dev.ragnarok.fenrir.place.PlaceUtil.goToPostCreation
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.shareLink
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.view.navigation.AbsNavigationView
import kotlin.math.max

class DocsFragment : BaseMvpFragment<DocsListPresenter, IDocListView>(), IDocListView,
    DocsAdapter.ActionListener, DocsUploadAdapter.ActionListener,
    DocsAsImagesAdapter.ActionListener {
    private val requestFile = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val file = result.data?.getStringExtra(Extra.PATH)
            val photos: ArrayList<LocalPhoto>? =
                result.data?.getParcelableArrayListExtraCompat(Extra.PHOTOS)
            if (file.nonNullNoEmpty()) {
                lazyPresenter {
                    fireFileForUploadSelected(file)
                }
            } else if (photos.nonNullNoEmpty()) {
                lazyPresenter {
                    fireLocalPhotosForUploadSelected(photos)
                }
            }
        }
    }
    private val requestReadPermission =
        requestPermissionsAbs(
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            lazyPresenter { fireReadPermissionResolved() }
        }
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mDocsAdapter: RecyclerBindableAdapter<Document, *>? = null
    private var mUploadAdapter: DocsUploadAdapter? = null
    private var mFiltersAdapter: HorizontalOptionsAdapter<DocFilter>? = null
    private var mHeaderView: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var mUploadRoot: View? = null
    private var mImagesOnly = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_docs, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        val buttonAdd: FloatingActionButton = root.findViewById(R.id.add_button)
        buttonAdd.setOnClickListener {
            presenter?.fireButtonAddClick()
        }
        if (requireActivity() is MainActivity) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
                WindowInsetsCompat.CONSUMED
            }
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                val insets =
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                val imeFixedBottom =
                    if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                        windowInsets.getInsets(
                            WindowInsetsCompat.Type.ime()
                        ).bottom, insets.bottom
                    ) else insets.bottom
                root.findViewById<View>(R.id.actionbar)
                    ?.setPadding(insets.left, 0, insets.right, 0)
                root.findViewById<View>(R.id.toolbar)
                    ?.setPadding(0, insets.top, 0, 0)
                mRecyclerView?.setPadding(insets.left, 0, insets.right, imeFixedBottom)
                mUploadRoot?.setPadding(insets.left, 0, insets.right, imeFixedBottom)

                (buttonAdd.layoutParams as? CoordinatorLayout.LayoutParams)?.bottomMargin =
                    imeFixedBottom + Utils.dp(16f)
                (buttonAdd.layoutParams as? CoordinatorLayout.LayoutParams)?.rightMargin =
                    insets.right + Utils.dp(16f)
                WindowInsetsCompat.CONSUMED
            }
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        mRecyclerView = root.findViewById(R.id.recycler_view)

        // тут, значит, некая многоходовочка
        // Так как мы не знаем, какой тип данных мы показываем (фото или просто документы),
        // то при создании view мы просим presenter уведомить об этом типе.
        // Предполагается, что presenter НЕЗАМЕДЛИТЕЛЬНО вызовет у view метод setAdapterType(boolean imagesOnly)
        presenter?.pleaseNotifyViewAboutAdapterType()
        // и мы дальше по коду можем использовать переменную mImagesOnly
        mRecyclerView?.layoutManager = createLayoutManager(mImagesOnly)
        mDocsAdapter = createAdapter(mImagesOnly, mutableListOf())
        val uploadRecyclerView: RecyclerView = root.findViewById(R.id.uploads_recycler_view)
        uploadRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        mUploadAdapter = DocsUploadAdapter(emptyList(), this)
        uploadRecyclerView.adapter = mUploadAdapter
        mHeaderView = View.inflate(requireActivity(), R.layout.header_feed, null)
        val headerRecyclerView: RecyclerView? = mHeaderView?.findViewById(R.id.header_list)
        headerRecyclerView?.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        mFiltersAdapter = HorizontalOptionsAdapter(mutableListOf())
        mFiltersAdapter?.setListener(object : HorizontalOptionsAdapter.Listener<DocFilter> {
            override fun onOptionClick(entry: DocFilter) {
                presenter?.fireFilterClick(
                    entry
                )
            }
        })
        headerRecyclerView?.adapter = mFiltersAdapter
        mHeaderView?.let {
            mDocsAdapter?.addHeader(it)
        }
        mRecyclerView?.adapter = mDocsAdapter
        mUploadRoot = root.findViewById(R.id.uploads_root)
        PicassoPauseOnScrollListener.addListener(mRecyclerView)
        return root
    }

    private fun createLayoutManager(asImages: Boolean): RecyclerView.LayoutManager {
        return if (asImages) {
            val columnCount = resources.getInteger(R.integer.local_gallery_column_count)
            GridLayoutManager(requireActivity(), columnCount)
        } else {
            LinearLayoutManager(requireActivity())
        }
    }

    override fun displayData(documents: MutableList<Document>, asImages: Boolean) {
        mImagesOnly = asImages
        if (mRecyclerView == null) {
            return
        }
        if (asImages && mDocsAdapter is DocsAsImagesAdapter) {
            (mDocsAdapter as DocsAsImagesAdapter).setItems(documents)
            return
        }
        if (!asImages && mDocsAdapter is DocsAdapter) {
            (mDocsAdapter as DocsAdapter).setItems(documents)
            return
        }
        mDocsAdapter = if (asImages) {
            val docsAsImagesAdapter = DocsAsImagesAdapter(documents)
            docsAsImagesAdapter.setActionListener(this)
            docsAsImagesAdapter
        } else {
            val docsAdapter = DocsAdapter(documents)
            docsAdapter.setActionListener(this)
            docsAdapter
        }
        mRecyclerView?.layoutManager = createLayoutManager(asImages)
        mDocsAdapter = createAdapter(asImages, documents)
        mHeaderView?.let { mDocsAdapter?.addHeader(it) }
        mRecyclerView?.adapter = mDocsAdapter
    }

    private fun createAdapter(
        asImages: Boolean,
        documents: MutableList<Document>
    ): RecyclerBindableAdapter<Document, *> {
        return if (asImages) {
            val docsAsImagesAdapter = DocsAsImagesAdapter(documents)
            docsAsImagesAdapter.setActionListener(this)
            docsAsImagesAdapter
        } else {
            val docsAdapter = DocsAdapter(documents)
            docsAdapter.setActionListener(this)
            docsAdapter
        }
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.post { mSwipeRefreshLayout?.isRefreshing = refreshing }
    }

    override fun notifyDataSetChanged() {
        mDocsAdapter?.notifyDataSetChanged()
    }

    override fun notifyDataAdd(position: Int, count: Int) {
        mDocsAdapter?.notifyItemBindableRangeInserted(position, count)
    }

    override fun notifyDataRemoved(position: Int) {
        mDocsAdapter?.notifyItemBindableRemoved(position)
    }

    override fun openDocument(accountId: Long, document: Document) {
        getDocPreviewPlace(accountId, document).tryOpenWith(requireActivity())
    }

    override fun returnSelection(docs: ArrayList<Document>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, docs)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    override fun goToGifPlayer(accountId: Long, gifs: ArrayList<Document>, selected: Int) {
        getGifPagerPlace(accountId, gifs, selected).tryOpenWith(requireActivity())
    }

    override fun requestReadExternalStoragePermission() {
        requestReadPermission.launch()
    }

    override fun startSelectUploadFileActivity(accountId: Long) {
        val sources = Sources()
            .with(FileManagerSelectableSource())
            .with(LocalPhotosSelectableSource())
        val intent = createIntent(requireActivity(), 10, sources)
        requestFile.launch(intent)
    }

    override fun setUploadDataVisible(visible: Boolean) {
        mUploadRoot?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun displayUploads(data: List<Upload>) {
        mUploadAdapter?.setData(data)
    }

    override fun notifyUploadItemsAdded(position: Int, count: Int) {
        mUploadAdapter?.notifyItemRangeInserted(position, count)
    }

    override fun notifyUploadItemChanged(position: Int) {
        mUploadAdapter?.notifyItemChanged(position)
    }

    override fun notifyUploadItemRemoved(position: Int) {
        mUploadAdapter?.notifyItemRemoved(position)
    }

    override fun notifyUploadProgressChanged(position: Int, progress: Int, smoothly: Boolean) {
        mUploadAdapter?.changeUploadProgress(position, progress, smoothly)
    }

    override fun displayFilterData(filters: MutableList<DocFilter>) {
        mFiltersAdapter?.setItems(filters)
    }

    override fun notifyFiltersChanged() {
        mFiltersAdapter?.notifyDataSetChanged()
    }

    override fun setAdapterType(imagesOnly: Boolean) {
        mImagesOnly = imagesOnly
    }

    private fun onMenuSelect(index: Int, doc: Document, option: Option) {
        when (option.id) {
            DocsOption.open_item_doc -> presenter?.fireDocClick(doc)
            DocsOption.share_item_doc -> presenter?.fireDocumentShare(doc)
            DocsOption.add_item_doc -> {
                presenter?.fireAddDocument(doc)
            }

            DocsOption.delete_item_doc -> MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.remove_confirm)
                .setMessage(R.string.doc_remove_confirm_message)
                .setPositiveButton(R.string.button_yes) { _, _ ->
                    presenter?.fireRemove(
                        doc,
                        index
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()

            DocsOption.go_to_owner_doc -> presenter?.fireOpenDocumentOwnerWall(doc.ownerId)
        }
    }

    override fun onMenuClick(index: Int, doc: Document, isMy: Boolean) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                DocsOption.open_item_doc,
                getString(R.string.open),
                R.drawable.view,
                true
            )
        )
        menus.add(
            OptionRequest(
                DocsOption.share_item_doc,
                getString(R.string.share),
                R.drawable.share,
                true
            )
        )
        menus.add(
            OptionRequest(
                DocsOption.go_to_owner_doc,
                getString(R.string.goto_user),
                R.drawable.person,
                false
            )
        )
        if (isMy) {
            menus.add(
                OptionRequest(
                    DocsOption.delete_item_doc,
                    getString(R.string.delete),
                    R.drawable.ic_outline_delete,
                    true
                )
            )
        } else {
            menus.add(
                OptionRequest(
                    DocsOption.add_item_doc,
                    getString(R.string.action_add),
                    R.drawable.plus,
                    true
                )
            )
        }
        menus.header(doc.title, R.drawable.book, doc.getPreviewWithSize(PhotoSize.X, true))
        menus.columns(2)
        menus.show(
            childFragmentManager,
            "docs_options"
        ) { _, option ->
            onMenuSelect(index, doc, option)
        }
    }

    private fun postToMyWall(accountId: Long, document: Document) {
        val models: List<AbsModel> = listOf(document)
        goToPostCreation((context as Activity), accountId, accountId, EditingPostType.TEMP, models)
    }

    override fun onShareDocument(
        accountId: Long,
        document: Document
    ) {
        val items: MutableList<Item> = ArrayList()
        items.add(Item(Methods.SHARE_LINK, Text(R.string.share_link)).setIcon(R.drawable.web))
        items.add(
            Item(
                Methods.SEND_MESSAGE,
                Text(R.string.repost_send_message)
            ).setIcon(R.drawable.share)
        )
        items.add(
            Item(
                Methods.REPOST_YOURSELF,
                Text(R.string.repost_to_wall)
            ).setIcon(R.drawable.ic_outline_share)
        )

        val mAdapter = MenuAdapter(requireActivity(), items, true)
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.repost_document_title)
            .setAdapter(mAdapter) { _, which ->
                when (items[which].key) {
                    Methods.SHARE_LINK -> shareLink(
                        requireActivity(),
                        String.format(
                            "${Settings.get().main().domain}/doc%s_%s",
                            document.ownerId,
                            document.id
                        ),
                        document.title
                    )

                    Methods.SEND_MESSAGE -> startForSendAttachments(
                        requireActivity(),
                        accountId,
                        document
                    )

                    Methods.REPOST_YOURSELF -> postToMyWall(accountId, document)
                }
            }
            .setNegativeButton(R.string.button_cancel, null).show()
    }

    override fun onOpenDocumentOwnerWall(accountId: Long, ownerId: Long) {
        getOwnerWallPlace(
            accountId,
            ownerId,
            null
        ).tryOpenWith(requireActivity())
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) = DocsListPresenter(
        requireArguments().getLong(Extra.ACCOUNT_ID),
        requireArguments().getLong(Extra.OWNER_ID),
        requireArguments().getString(Extra.ACTION),
        saveInstanceState
    )

    override fun onResume() {
        super.onResume()
        Settings.get().ui().notifyPlaceResumed(Place.DOCS)
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.documents)
            actionBar.subtitle = null
        }
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(AbsNavigationView.SECTION_ITEM_DOCS)
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onDocClick(index: Int, doc: Document) {
        presenter?.fireDocClick(
            doc
        )
    }

    override fun onDocLongClick(index: Int, doc: Document): Boolean {
        presenter?.fireMenuClick(
            index,
            doc
        )
        return true
    }

    override fun onRemoveClick(upload: Upload) {
        presenter?.fireRemoveClick(
            upload
        )
    }

    companion object {
        fun buildArgs(accountId: Long, ownerId: Long, action: String?): Bundle {
            val args = Bundle()
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putLong(Extra.OWNER_ID, ownerId)
            args.putString(Extra.ACTION, action)
            return args
        }

        fun newInstance(args: Bundle?): DocsFragment {
            val fragment = DocsFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(accountId: Long, ownerId: Long, action: String?): DocsFragment {
            return newInstance(buildArgs(accountId, ownerId, action))
        }
    }
}