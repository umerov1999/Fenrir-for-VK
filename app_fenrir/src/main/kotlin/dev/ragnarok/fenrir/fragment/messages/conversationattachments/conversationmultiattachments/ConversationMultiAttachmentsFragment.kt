package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationmultiattachments

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity.Companion.startForSendAttachments
import dev.ragnarok.fenrir.fragment.audio.audios.AudiosAdapter
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.fragment.docs.DocsAdapter
import dev.ragnarok.fenrir.fragment.fave.favephotos.FavePhotosAdapter
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria
import dev.ragnarok.fenrir.fragment.videos.VideosAdapter
import dev.ragnarok.fenrir.fragment.wall.WallAdapter
import dev.ragnarok.fenrir.fragment.wall.wallattachments.wallmultiattachments.LinksAdapter
import dev.ragnarok.fenrir.fragment.wall.wallattachments.wallmultiattachments.PhotoAlbumsAdapter
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.canPlayAfterCurrent
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.playAfterCurrent
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoAlbum
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.model.TmpSource
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.model.menu.options.AudioOption
import dev.ragnarok.fenrir.module.StringHash
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.place.PlaceFactory.SearchByAudioPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getArtistPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosInAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.place.PlaceUtil.goToPostEditor
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadAudio
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getBitrate
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.util.toast.CustomSnackbars
import dev.ragnarok.fenrir.view.RecyclerViewSavedScroll

class ConversationMultiAttachmentsFragment :
    PlaceSupportMvpFragment<ConversationMultiAttachmentsPresenter, IConversationMultiAttachmentsView>(),
    IConversationMultiAttachmentsView, WallAdapter.ClickListener, DocsAdapter.ActionListener,
    LinksAdapter.ActionListener, PhotoAlbumsAdapter.ClickListener,
    FavePhotosAdapter.PhotoSelectionListener, VideosAdapter.VideoOnClickListener {
    private var mEmpty: TextView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private var mAudiosAdapter: AudiosAdapter? = null
    private var mDocsAdapter: DocsAdapter? = null
    private var mLinksAdapter: LinksAdapter? = null
    private var mPhotoAlbumAdapter: PhotoAlbumsAdapter? = null
    private var mPhotoAdapter: FavePhotosAdapter? = null
    private var mVideoAdapter: VideosAdapter? = null
    private var mPostsAdapter: WallAdapter? = null

    private var mLoadMore: FloatingActionButton? = null
    private var frameAdapter: FrameAdapter? = null

    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.continueDownload()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(
            R.layout.fragment_wall_conversation_attachments_multi,
            container,
            false
        )
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        val viewpager: ViewPager2 = root.findViewById(R.id.viewpager)
        viewpager.setPageTransformer(
            Utils.createPageTransform(
                Settings.get().main().viewpager_page_transform
            )
        )
        frameAdapter = FrameAdapter(
            listOf(
                AttachmentConversationType.PHOTO,
                AttachmentConversationType.PHOTO_ALBUM,
                AttachmentConversationType.VIDEO,
                AttachmentConversationType.DOC,
                AttachmentConversationType.LINK,
                AttachmentConversationType.POST,
                AttachmentConversationType.AUDIO
            )
        )
        viewpager.adapter = frameAdapter
        TabLayoutMediator(
            root.findViewById(R.id.tablayout),
            viewpager
        ) { tab, position ->
            frameAdapter?.getTitle(position)
                ?.let { tab.setText(it) }
        }.attach()
        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                frameAdapter?.getItem(position)?.let { presenter?.fireUpdateCurrentPage(it) }
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        mEmpty = root.findViewById(R.id.empty)
        mLoadMore = root.findViewById(R.id.goto_button)

        mLoadMore?.setOnClickListener {
            presenter?.fireScrollToEnd(true)
        }
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            mSwipeRefreshLayout?.isRefreshing = false
            CustomSnackbars.createCustomSnackbars(view, null, true)
                ?.setDurationSnack(Snackbar.LENGTH_LONG)?.defaultSnack(R.string.do_update, false)
                ?.setAction(R.string.button_yes) {
                    presenter?.fireRefresh()
                }?.show()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)

        mAudiosAdapter = AudiosAdapter(
            mutableListOf(),
            notShowMy = false,
            iSSelectMode = false
        )
        mAudiosAdapter?.setClickListener(object : AudiosAdapter.ClickListener {
            override fun onClick(position: Int, audio: Audio) {
                presenter?.firePlayAudio(
                    requireActivity(),
                    position
                )
            }

            override fun onUrlPhotoOpen(url: String, prefix: String, photoPrefix: String) {
                getSingleURLPhotoPlace(url, prefix, photoPrefix).tryOpenWith(requireActivity())
            }

            override fun openMenu(position: Int, audio: Audio) {
                presenter?.fireOpenMenu(position, audio)
            }

            override fun onDownload(position: Int, audio: Audio) {
                presenter?.firePrepareDownload(position, audio)
            }
        })

        mDocsAdapter = DocsAdapter(mutableListOf())
        mDocsAdapter?.setActionListener(this)

        mLinksAdapter = LinksAdapter(mutableListOf())
        mLinksAdapter?.setActionListener(this)

        mPhotoAlbumAdapter = PhotoAlbumsAdapter(emptyList())
        mPhotoAlbumAdapter?.setClickListener(this)

        mPhotoAdapter = FavePhotosAdapter(requireActivity(), emptyList())
        mPhotoAdapter?.setPhotoSelectionListener(this)

        mVideoAdapter = VideosAdapter(emptyList())
        mVideoAdapter?.setVideoOnClickListener(this)

        mPostsAdapter = WallAdapter(requireActivity(), mutableListOf(), this, this)

        return root
    }

    private val requestPhotoUpdate = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && (result.data
                ?: return@registerForActivityResult)
                .extras != null
        ) {
            val ps = ((result.data ?: return@registerForActivityResult).extras
                ?: return@registerForActivityResult).getInt(Extra.POSITION)
            mPhotoAdapter?.updateCurrentPosition(ps)
            mPhotoAdapter?.attachedRecyclerView?.scrollToPosition(ps)
        }
    }

    override fun onPhotoClicked(position: Int, photo: Photo) {
        presenter?.firePhotoClick(
            position
        )
    }

    override fun goToTempPhotosGallery(accountId: Long, source: TmpSource, index: Int) {
        PlaceFactory.getTmpSourceGalleryPlace(accountId, source, index).setActivityResultLauncher(
            requestPhotoUpdate
        ).tryOpenWith(requireActivity())
    }

    override fun goToTempPhotosGallery(accountId: Long, ptr: Long, index: Int) {
        PlaceFactory.getTmpSourceGalleryPlace(
            accountId,
            ptr,
            index
        ).setActivityResultLauncher(requestPhotoUpdate).tryOpenWith(requireActivity())
    }

    override fun onAlbumClick(index: Int, album: PhotoAlbum) {
        presenter?.firePhotoAlbumClick(
            album
        )
    }

    override fun onDocClick(index: Int, doc: Document) {
        presenter?.fireDocClick(
            doc
        )
    }

    override fun onLinkClick(index: Int, doc: Link) {
        presenter?.fireLinkClick(
            doc
        )
    }

    override fun onDocLongClick(index: Int, doc: Document): Boolean {
        return false
    }

    override fun onVideoClick(position: Int, video: Video) {
        presenter?.fireVideoOpen(
            video
        )
    }

    override fun onVideoLongClick(position: Int, video: Video): Boolean {
        return false
    }

    override fun resolveEmptyText(@AttachmentConversationType type: Int) {
        when (type) {
            AttachmentConversationType.AUDIO -> {
                mEmpty?.visibility =
                    if (mAudiosAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }

            AttachmentConversationType.DOC -> {
                mEmpty?.visibility = if (mDocsAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }

            AttachmentConversationType.LINK -> {
                mEmpty?.visibility = if (mLinksAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }

            AttachmentConversationType.PHOTO -> {
                mEmpty?.visibility = if (mPhotoAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }

            AttachmentConversationType.PHOTO_ALBUM -> {
                mEmpty?.visibility =
                    if (mPhotoAlbumAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }

            AttachmentConversationType.POST -> {
                mEmpty?.visibility =
                    if (mPostsAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }

            AttachmentConversationType.VIDEO -> {
                mEmpty?.visibility = if (mVideoAdapter?.itemCount == 0) View.VISIBLE else View.GONE
            }
        }
    }

    override fun displayAudioData(posts: MutableList<Audio>) {
        mAudiosAdapter?.setItems(posts)
    }

    override fun displayDocsData(docs: MutableList<Document>) {
        mDocsAdapter?.setItems(docs)
    }

    override fun displayLinksData(links: MutableList<Link>) {
        mLinksAdapter?.setItems(links)
    }

    override fun displayPhotoAlbumsData(photoAlbums: MutableList<PhotoAlbum>) {
        mPhotoAlbumAdapter?.setData(photoAlbums)
    }

    override fun displayPhotoData(photos: MutableList<Photo>) {
        mPhotoAdapter?.setData(photos)
    }

    override fun displayVideoData(videos: MutableList<Video>) {
        mVideoAdapter?.setData(videos)
    }

    override fun displayPostsData(posts: MutableList<Post>) {
        mPostsAdapter?.setItems(posts)
    }

    override fun notifyDataRemoved(
        @AttachmentConversationType type: Int,
        position: Int,
        count: Int
    ) {
        when (type) {
            AttachmentConversationType.AUDIO -> {
                mAudiosAdapter?.notifyItemBindableRangeRemoved(position, count)
            }

            AttachmentConversationType.DOC -> {
                mDocsAdapter?.notifyItemBindableRangeRemoved(position, count)
            }

            AttachmentConversationType.LINK -> {
                mLinksAdapter?.notifyItemBindableRangeRemoved(position, count)
            }

            AttachmentConversationType.PHOTO -> {
                mPhotoAdapter?.notifyItemRangeRemoved(position, count)
            }

            AttachmentConversationType.PHOTO_ALBUM -> {
                mPhotoAlbumAdapter?.notifyItemRangeRemoved(position, count)
            }

            AttachmentConversationType.POST -> {
                mPostsAdapter?.notifyItemBindableRangeRemoved(position, count)
            }

            AttachmentConversationType.VIDEO -> {
                mVideoAdapter?.notifyItemRangeRemoved(position, count)
            }
        }
    }

    override fun notifyDataChanged(type: Int, position: Int, count: Int) {
        when (type) {
            AttachmentConversationType.AUDIO -> {
                mAudiosAdapter?.notifyItemBindableRangeChanged(position, count)
            }

            AttachmentConversationType.DOC -> {
                mDocsAdapter?.notifyItemBindableRangeChanged(position, count)
            }

            AttachmentConversationType.LINK -> {
                mLinksAdapter?.notifyItemBindableRangeChanged(position, count)
            }

            AttachmentConversationType.PHOTO -> {
                mPhotoAdapter?.notifyItemRangeRemoved(position, count)
            }

            AttachmentConversationType.PHOTO_ALBUM -> {
                mPhotoAlbumAdapter?.notifyItemRangeRemoved(position, count)
            }

            AttachmentConversationType.POST -> {
                mPostsAdapter?.notifyItemBindableRangeChanged(position, count)
            }

            AttachmentConversationType.VIDEO -> {
                mVideoAdapter?.notifyItemRangeRemoved(position, count)
            }
        }
    }

    override fun notifyDataAdded(@AttachmentConversationType type: Int, position: Int, count: Int) {
        when (type) {
            AttachmentConversationType.AUDIO -> {
                mAudiosAdapter?.notifyItemBindableRangeInserted(position, count)
            }

            AttachmentConversationType.DOC -> {
                mDocsAdapter?.notifyItemBindableRangeInserted(position, count)
            }

            AttachmentConversationType.LINK -> {
                mLinksAdapter?.notifyItemBindableRangeInserted(position, count)
            }

            AttachmentConversationType.PHOTO -> {
                mPhotoAdapter?.notifyItemRangeInserted(position, count)
            }

            AttachmentConversationType.PHOTO_ALBUM -> {
                mPhotoAlbumAdapter?.notifyItemRangeInserted(position, count)
            }

            AttachmentConversationType.POST -> {
                mPostsAdapter?.notifyItemBindableRangeInserted(position, count)
            }

            AttachmentConversationType.VIDEO -> {
                mVideoAdapter?.notifyItemRangeInserted(position, count)
            }
        }
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        ConversationMultiAttachmentsPresenter(
            requireArguments().getLong(Extra.ACCOUNT_ID),
            requireArguments().getLong(Extra.PEER_ID),
            saveInstanceState
        )

    override fun toolbarTitle(title: String) {
        supportToolbarFor(this)?.title = title
    }

    override fun toolbarSubtitle(subtitle: String) {
        supportToolbarFor(this)?.subtitle = subtitle
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .build()
            .apply(requireActivity())
    }

    override fun onSetLoadingStatus(isLoad: Int) {
        when (isLoad) {
            1 -> mLoadMore?.setImageResource(R.drawable.audio_died)
            2 -> mLoadMore?.setImageResource(R.drawable.view)
            else -> mLoadMore?.setImageResource(R.drawable.ic_arrow_down)
        }
    }

    override fun onAvatarClick(ownerId: Long) {
        onOwnerClick(ownerId)
    }

    override fun onShareClick(post: Post) {
        presenter?.fireShareClick(
            post
        )
    }

    override fun onPostClick(post: Post) {
        presenter?.firePostBodyClick(
            post
        )
    }

    override fun onRestoreClick(post: Post) {
        presenter?.firePostRestoreClick(
            post
        )
    }

    override fun onCommentsClick(post: Post) {
        presenter?.fireCommentsClick(
            post
        )
    }

    override fun onLikeLongClick(post: Post) {
        presenter?.fireLikeLongClick(
            post
        )
    }

    override fun onShareLongClick(post: Post) {
        presenter?.fireShareLongClick(
            post
        )
    }

    override fun onLikeClick(post: Post) {
        presenter?.fireLikeClick(
            post
        )
    }

    override fun openPostEditor(accountId: Long, post: Post) {
        goToPostEditor(requireActivity(), accountId, post)
    }

    inner class FrameAdapter(@param:AttachmentConversationType private var data: List<Int>) :
        RecyclerView.Adapter<FrameAdapter.Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(
                LayoutInflater.from(requireActivity())
                    .inflate(R.layout.item_wall_attachments_recycler, parent, false)
            )
        }

        @AttachmentConversationType
        fun getItem(position: Int): Int {
            return data[position]
        }

        @StringRes
        fun getTitle(position: Int): Int {
            return when (data[position]) {
                AttachmentConversationType.AUDIO -> {
                    R.string.audios
                }

                AttachmentConversationType.DOC -> {
                    R.string.documents
                }

                AttachmentConversationType.LINK -> {
                    R.string.links
                }

                AttachmentConversationType.PHOTO -> {
                    R.string.photos
                }

                AttachmentConversationType.PHOTO_ALBUM -> {
                    R.string.photo_album
                }

                AttachmentConversationType.POST -> {
                    R.string.posts
                }

                AttachmentConversationType.VIDEO -> {
                    R.string.videos
                }

                else -> {
                    R.string.unknown_error
                }
            }
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val item = data[position]
            holder.recyclerView.clearOnScrollListeners()
            when (item) {
                AttachmentConversationType.AUDIO -> {
                    holder.recyclerView.adapter = null
                    holder.recyclerView.layoutManager =
                        LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                    holder.recyclerView.adapter = mAudiosAdapter
                }

                AttachmentConversationType.DOC -> {
                    holder.recyclerView.adapter = null
                    holder.recyclerView.layoutManager =
                        LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                    holder.recyclerView.adapter = mDocsAdapter
                }

                AttachmentConversationType.LINK -> {
                    holder.recyclerView.adapter = null
                    holder.recyclerView.layoutManager =
                        LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                    holder.recyclerView.adapter = mLinksAdapter
                }

                AttachmentConversationType.PHOTO -> {
                    holder.recyclerView.adapter = null
                    val columns = resources.getInteger(R.integer.photos_column_count)
                    holder.recyclerView.layoutManager =
                        GridLayoutManager(requireActivity(), columns)
                    holder.recyclerView.adapter = mPhotoAdapter
                }

                AttachmentConversationType.PHOTO_ALBUM -> {
                    holder.recyclerView.adapter = null
                    val columnCount = resources.getInteger(R.integer.photos_albums_column_count)
                    holder.recyclerView.layoutManager =
                        GridLayoutManager(requireActivity(), columnCount)
                    holder.recyclerView.adapter = mPhotoAlbumAdapter
                }

                AttachmentConversationType.POST -> {
                    holder.recyclerView.adapter = null
                    holder.recyclerView.layoutManager =
                        LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
                    holder.recyclerView.adapter = mPostsAdapter
                }

                AttachmentConversationType.VIDEO -> {
                    holder.recyclerView.adapter = null
                    val columns = resources.getInteger(R.integer.videos_column_count)
                    holder.recyclerView.layoutManager =
                        GridLayoutManager(requireActivity(), columns)
                    holder.recyclerView.adapter = mVideoAdapter
                }
            }
            holder.recyclerView.updateUid(
                StringHash.calculateCRC32(TAG + "_" + item)
            )
            PicassoPauseOnScrollListener.addListener(holder.recyclerView)
            holder.recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
                override fun onScrollToLastElement() {
                    presenter?.fireScrollToEnd(false)
                }
            })
        }

        override fun getItemCount(): Int {
            return data.size
        }

        inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val recyclerView: RecyclerViewSavedScroll =
                itemView.findViewById(R.id.wall_attachments_recycle)
        }
    }

    override fun onServiceBindEvent(@PlayerStatus status: Int) {
        mAudiosAdapter?.onServiceBindEvent(status)
    }

    override fun notifyAudioItemRemoved(index: Int) {
        mAudiosAdapter?.notifyItemBindableRangeRemoved(index, 1)
    }

    override fun notifyAudioItemChanged(index: Int) {
        mAudiosAdapter?.notifyItemBindableRangeChanged(index, 1)
    }

    override fun onDownloadAudioNextStage() {
        if (!hasReadWriteStoragePermission(requireActivity())) {
            requestWritePermission.launch()
            return
        }
        presenter?.continueDownload()
    }

    override fun onStartAudioDownload(
        accountId: Long,
        position: Int,
        audio: Audio
    ) {
        audio.downloadIndicator = 1
        mAudiosAdapter?.notifyItemBindableRangeChanged(position, 1)
        val ret = doDownloadAudio(
            requireActivity(),
            audio,
            accountId,
            Force = false,
            isLocal = false
        )
        when (ret) {
            0 -> customToast?.showToastBottom(R.string.saved_audio)

            1, 2 -> {
                CustomSnackbars.createCustomSnackbars(view)
                    ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                    ?.themedSnack(if (ret == 1) R.string.audio_force_download else R.string.audio_force_download_pc)
                    ?.setAction(
                        R.string.button_yes
                    ) {
                        doDownloadAudio(
                            requireActivity(),
                            audio,
                            accountId,
                            Force = true,
                            isLocal = false
                        )
                    }
                    ?.show()
            }

            else -> {
                audio.downloadIndicator = 0
                mAudiosAdapter?.notifyItemBindableRangeChanged(position, 1)
                customToast?.showToastError(R.string.error_audio)
            }
        }
    }

    override fun onShowBitrate(duration: Int, size: Long) {
        CustomSnackbars.createCustomSnackbars(view)
            ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
            ?.themedSnack(getBitrate(requireActivity(), duration, size))
            ?.show()
    }

    override fun onSelectToSendAttachment(audios: ArrayList<Audio>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra(
            Extra.ATTACHMENTS, audios
        )
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

    override fun onOpenMenu(accountId: Long, position: Int, audio: Audio) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(
            OptionRequest(
                AudioOption.play_item_audio,
                getString(R.string.play),
                R.drawable.play,
                true
            )
        )
        if (canPlayAfterCurrent()) {
            menus.add(
                OptionRequest(
                    AudioOption.play_item_after_current_audio,
                    getString(R.string.play_audio_after_current),
                    R.drawable.play_next,
                    false
                )
            )
        }
        if (audio.ownerId != accountId) {
            menus.add(
                OptionRequest(
                    AudioOption.add_item_audio,
                    getString(R.string.action_add),
                    R.drawable.list_add,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    AudioOption.add_and_download_button,
                    getString(R.string.add_and_download_button),
                    R.drawable.add_download,
                    false
                )
            )
        } else {
            menus.add(
                OptionRequest(
                    AudioOption.add_item_audio,
                    getString(R.string.delete),
                    R.drawable.ic_outline_delete,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    AudioOption.edit_track,
                    getString(R.string.edit),
                    R.drawable.about_writed,
                    true
                )
            )
        }
        menus.add(
            OptionRequest(
                AudioOption.share_button,
                getString(R.string.share),
                R.drawable.ic_outline_share,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.save_item_audio,
                getString(R.string.save),
                R.drawable.save,
                true
            )
        )
        if (audio.albumId != 0) menus.add(
            OptionRequest(
                AudioOption.open_album,
                getString(R.string.open_album),
                R.drawable.audio_album,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.get_recommendation_by_audio,
                getString(R.string.get_recommendation_by_audio),
                R.drawable.music_mic,
                false
            )
        )
        if (audio.main_artists.nonNullNoEmpty()) menus.add(
            OptionRequest(
                AudioOption.goto_artist,
                getString(R.string.audio_goto_artist),
                R.drawable.artist_icon,
                false
            )
        )
        if (audio.lyricsId != 0) menus.add(
            OptionRequest(
                AudioOption.get_lyrics_menu,
                getString(R.string.get_lyrics_menu),
                R.drawable.lyric,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.bitrate_item_audio,
                getString(R.string.get_bitrate),
                R.drawable.high_quality,
                false
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.search_by_artist,
                getString(R.string.search_by_artist),
                R.drawable.magnify,
                true
            )
        )
        menus.add(
            OptionRequest(
                AudioOption.copy_url,
                getString(R.string.copy_url),
                R.drawable.content_copy,
                false
            )
        )
        menus.header(
            Utils.firstNonEmptyString(audio.artist, " ") + " - " + audio.title,
            R.drawable.song,
            audio.thumb_image_little
        )
        menus.columns(2)
        menus.show(
            childFragmentManager,
            "audio_options"
        ) { _, option ->
            when (option.id) {
                AudioOption.play_item_audio -> {
                    presenter?.firePlayAudio(
                        requireActivity(),
                        position
                    )
                }

                AudioOption.play_item_after_current_audio -> playAfterCurrent(
                    audio
                )

                AudioOption.edit_track -> {
                    val root = View.inflate(requireActivity(), R.layout.entry_audio_info, null)
                    root.findViewById<TextInputEditText>(R.id.edit_artist).setText(audio.artist)
                    root.findViewById<TextInputEditText>(R.id.edit_title).setText(audio.title)
                    MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.enter_audio_info)
                        .setCancelable(true)
                        .setView(root)
                        .setPositiveButton(R.string.button_ok) { _, _ ->
                            presenter?.fireEditTrack(
                                position,
                                root.findViewById<TextInputEditText>(R.id.edit_artist).text?.trim()
                                    .toString(),
                                root.findViewById<TextInputEditText>(R.id.edit_title).text?.trim()
                                    .toString(),
                                audio
                            )
                        }
                        .setNegativeButton(R.string.button_cancel, null)
                        .show()
                }

                AudioOption.share_button -> startForSendAttachments(
                    requireActivity(),
                    accountId,
                    audio
                )

                AudioOption.search_by_artist -> getSingleTabSearchPlace(
                    accountId,
                    SearchContentType.AUDIOS,
                    AudioSearchCriteria(
                        audio.artist, by_artist = true, in_main_page = false
                    )
                ).tryOpenWith(requireActivity())

                AudioOption.get_lyrics_menu -> presenter?.fireGetLyrics(audio)
                AudioOption.get_recommendation_by_audio -> SearchByAudioPlace(
                    accountId, audio.ownerId, audio.id
                ).tryOpenWith(requireActivity())

                AudioOption.open_album -> getAudiosInAlbumPlace(
                    accountId,
                    audio.album_owner_id,
                    audio.albumId,
                    audio.album_access_key
                ).tryOpenWith(requireActivity())

                AudioOption.copy_url -> {
                    val clipboard =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    val clip = ClipData.newPlainText("response", audio.url)
                    clipboard?.setPrimaryClip(clip)
                    customToast?.showToast(R.string.copied)
                }

                AudioOption.add_item_audio -> {
                    val myAudio = audio.ownerId == accountId
                    if (myAudio) {
                        presenter?.fireDelete(position, audio)
                    } else {
                        presenter?.fireAddTrack(audio)
                    }
                }

                AudioOption.add_and_download_button -> {
                    if (audio.ownerId != accountId) {
                        presenter?.fireAddTrack(audio)
                    }
                    presenter?.firePrepareDownload(position, audio)
                }

                AudioOption.save_item_audio -> {
                    presenter?.firePrepareDownload(position, audio)
                }

                AudioOption.bitrate_item_audio -> presenter?.fireGetBitrate(audio)

                AudioOption.goto_artist -> {
                    val artists = Utils.getArrayFromHash(
                        audio.main_artists
                    )
                    if (audio.main_artists?.keys?.size.orZero() > 1) {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setItems(artists[1]) { _, which ->
                                getArtistPlace(
                                    accountId,
                                    artists[0][which]
                                ).tryOpenWith(requireActivity())
                            }
                            .show()
                    } else {
                        getArtistPlace(
                            accountId,
                            artists[0][0]
                        ).tryOpenWith(requireActivity())
                    }
                }
            }
        }
    }

    override fun onAudioLyricsReceived(lyricText: String, audio: Audio) {
        val title = audio.artistAndTitle
        MaterialAlertDialogBuilder(requireActivity())
            .setIcon(R.drawable.dir_song)
            .setMessage(lyricText)
            .setTitle(title)
            .setPositiveButton(R.string.button_ok, null)
            .setNeutralButton(R.string.copy_text) { _, _ ->
                val clipboard = requireActivity().getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager?
                val clip = ClipData.newPlainText("response", lyricText)
                clipboard?.setPrimaryClip(clip)
                customToast?.showToast(R.string.copied_to_clipboard)
            }
            .setCancelable(true)
            .show()
    }

    companion object {
        private val TAG = ConversationMultiAttachmentsFragment::class.simpleName.orEmpty()
    }
}