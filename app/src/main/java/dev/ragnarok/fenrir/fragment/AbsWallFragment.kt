package dev.ragnarok.fenrir.fragment

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.adapter.WallAdapter
import dev.ragnarok.fenrir.adapter.WallAdapter.NonPublishedPostActionListener
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalStoryAdapter
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.WallSearchCriteria
import dev.ragnarok.fenrir.link.LinkHelper
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.mvp.presenter.AbsWallPresenter
import dev.ragnarok.fenrir.mvp.view.IVideosListView
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView
import dev.ragnarok.fenrir.mvp.view.IWallView
import dev.ragnarok.fenrir.place.PlaceFactory.getAudiosPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getOwnerArticles
import dev.ragnarok.fenrir.place.PlaceFactory.getPhotoAlbumGalleryPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVKPhotoAlbumsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getVideosPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getWallAttachmentsPlace
import dev.ragnarok.fenrir.place.PlaceUtil.goToPostCreation
import dev.ragnarok.fenrir.place.PlaceUtil.goToPostEditor
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.theme.ThemesController.paganSymbol
import dev.ragnarok.fenrir.util.AppTextUtils.getCounterWithK
import dev.ragnarok.fenrir.util.FindAttachmentType
import dev.ragnarok.fenrir.util.Utils.dp
import dev.ragnarok.fenrir.util.Utils.is600dp
import dev.ragnarok.fenrir.util.Utils.isLandscape
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper.Companion.createFrom
import dev.ragnarok.fenrir.view.UpEditFab
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView

abstract class AbsWallFragment<V : IWallView, P : AbsWallPresenter<V>> :
    PlaceSupportMvpFragment<P, V>(), IWallView, WallAdapter.ClickListener,
    NonPublishedPostActionListener {
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mWallAdapter: WallAdapter? = null
    private var mLoadMoreFooterHelper: LoadMoreFooterHelper? = null
    private var mStoryAdapter: HorizontalStoryAdapter? = null
    private var fabCreate: UpEditFab? = null
    protected fun setupPaganContent(Runes: View?, paganSymbol: RLottieImageView?) {
        Runes?.visibility = if (Settings.get()
                .other().isRunes_show
        ) View.VISIBLE else View.GONE
        val symbol = paganSymbol()
        paganSymbol?.visibility = if (symbol != 0) View.VISIBLE else View.GONE
        if (symbol == 0) {
            return
        }
        when (symbol) {
            2 -> paganSymbol?.setImageResource(R.drawable.ic_igdr)
            3 -> paganSymbol?.setImageResource(R.drawable.valknut)
            4 -> paganSymbol?.setImageResource(R.drawable.ic_mjolnir)
            5 -> paganSymbol?.setImageResource(R.drawable.ic_vegvisir)
            6 -> paganSymbol?.setImageResource(R.drawable.ic_vegvisir2)
            7 -> paganSymbol?.setImageResource(R.drawable.ic_celtic_knot)
            8 -> paganSymbol?.setImageResource(R.drawable.ic_celtic_flower)
            9 -> paganSymbol?.setImageResource(R.drawable.ic_slepnir)
            10 -> if (FenrirNative.isNativeLoaded) {
                paganSymbol?.fromRes(
                    R.raw.fenrir, dp(140f), dp(140f), intArrayOf(
                        0x333333,
                        CurrentTheme.getColorPrimary(requireActivity()),
                        0x777777,
                        CurrentTheme.getColorSecondary(requireActivity())
                    )
                )
                paganSymbol?.playAnimation()
            } else {
                paganSymbol?.setImageResource(R.drawable.ic_igdr)
            }
            11 -> paganSymbol?.setImageResource(R.drawable.ic_triskel)
            12 -> paganSymbol?.setImageResource(R.drawable.ic_hell)
            13 -> paganSymbol?.setImageResource(R.drawable.ic_odin)
            14 -> paganSymbol?.setImageResource(R.drawable.ic_viking)
            15 -> paganSymbol?.setImageResource(R.drawable.ic_raven)
            16 -> paganSymbol?.setImageResource(R.drawable.ic_fire)
            else -> paganSymbol?.setImageResource(R.drawable.ic_cat)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_wall, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.fireRefresh()
        }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        val manager: RecyclerView.LayoutManager = if (is600dp(requireActivity())) {
            val land = isLandscape(requireActivity())
            StaggeredGridLayoutManager(if (land) 2 else 1, StaggeredGridLayoutManager.VERTICAL)
        } else {
            LinearLayoutManager(requireActivity())
        }
        val recyclerView: RecyclerView = root.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = manager
        fabCreate = root.findViewById(R.id.fragment_user_profile_fab)
        fabCreate?.setOnClickListener {
            if (fabCreate?.isEdit == true) {
                presenter?.fireCreateClick()
            } else {
                recyclerView.scrollToPosition(0)
            }
        }
        recyclerView.addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
        fabCreate?.getRecyclerObserver(7)?.let { recyclerView.addOnScrollListener(it) }
        recyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
            override fun onScrollToLastElement() {
                presenter?.fireScrollToEnd()
            }
        })
        val headerView = inflater.inflate(headerLayout(), recyclerView, false)
        onHeaderInflated(headerView)
        val footerView = inflater.inflate(R.layout.footer_load_more, recyclerView, false)
        mLoadMoreFooterHelper = createFrom(footerView, object : LoadMoreFooterHelper.Callback {
            override fun onLoadMoreClick() {
                presenter?.fireLoadMoreClick()
            }
        })
        val headerStory = inflater.inflate(R.layout.header_story, recyclerView, false)
        val headerStoryRecyclerView: RecyclerView = headerStory.findViewById(R.id.header_story)
        headerStoryRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        mStoryAdapter = HorizontalStoryAdapter(mutableListOf())
        mStoryAdapter?.setListener(object : HorizontalStoryAdapter.Listener {
            override fun onOptionClick(item: Story, pos: Int) {
                openHistoryVideo(
                    Settings.get().accounts().current, ArrayList(
                        presenter?.stories ?: emptyList()
                    ), pos
                )
            }
        })
        headerStoryRecyclerView.adapter = mStoryAdapter
        mWallAdapter = WallAdapter(requireActivity(), mutableListOf(), this, this)
        mWallAdapter?.addHeader(headerView)
        mWallAdapter?.addHeader(headerStoryRecyclerView)
        mWallAdapter?.addFooter(footerView)
        mWallAdapter?.setNonPublishedPostActionListener(this)
        recyclerView.adapter = mWallAdapter
        return root
    }

    override fun onAvatarClick(ownerId: Int) {
        super.onOwnerClick(ownerId)
    }

    override fun showSnackbar(res: Int, isLong: Boolean) {
        view?.let {
            Snackbar.make(
                it,
                res,
                if (isLong) BaseTransientBottomBar.LENGTH_LONG else BaseTransientBottomBar.LENGTH_SHORT
            ).show()
        }
    }

    override fun openPhotoAlbum(
        accountId: Int,
        ownerId: Int,
        albumId: Int,
        photos: ArrayList<Photo>,
        position: Int
    ) {
        getPhotoAlbumGalleryPlace(
            accountId,
            albumId,
            ownerId,
            photos,
            position,
            false,
            Settings.get().other().isInvertPhotoRev
        )
            .tryOpenWith(requireActivity())
    }

    override fun goToWallSearch(accountId: Int, ownerId: Int) {
        val criteria = WallSearchCriteria("", ownerId)
        getSingleTabSearchPlace(accountId, SearchContentType.WALL, criteria).tryOpenWith(
            requireActivity()
        )
    }

    override fun goToConversationAttachments(accountId: Int, ownerId: Int) {
        val types = arrayOf(
            FindAttachmentType.TYPE_PHOTO,
            FindAttachmentType.TYPE_VIDEO,
            FindAttachmentType.TYPE_DOC,
            FindAttachmentType.TYPE_AUDIO,
            FindAttachmentType.TYPE_LINK,
            FindAttachmentType.TYPE_ALBUM,
            FindAttachmentType.TYPE_POST_WITH_COMMENT,
            FindAttachmentType.TYPE_POST_WITH_QUERY
        )
        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(OptionRequest(0, getString(R.string.photos), R.drawable.photo_album, true))
        menus.add(OptionRequest(1, getString(R.string.videos), R.drawable.video, true))
        menus.add(OptionRequest(2, getString(R.string.documents), R.drawable.book, true))
        menus.add(OptionRequest(3, getString(R.string.music), R.drawable.song, true))
        menus.add(OptionRequest(4, getString(R.string.links), R.drawable.web, true))
        menus.add(OptionRequest(5, getString(R.string.photo_album), R.drawable.album_photo, false))
        menus.add(
            OptionRequest(
                6,
                getString(R.string.posts_with_comment),
                R.drawable.comment,
                false
            )
        )
        menus.add(OptionRequest(7, getString(R.string.posts_with_query), R.drawable.magnify, false))
        menus.show(
            childFragmentManager,
            "attachments_select",
            object : ModalBottomSheetDialogFragment.Listener {
                override fun onModalOptionSelected(option: Option) {
                    getWallAttachmentsPlace(
                        accountId,
                        ownerId,
                        types[option.id]
                    ).tryOpenWith(requireActivity())
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                presenter?.fireRefresh()
                return true
            }
            R.id.action_edit -> {
                presenter?.fireEdit(requireActivity())
                return true
            }
            R.id.action_copy_url -> {
                presenter?.fireCopyUrlClick()
                return true
            }
            R.id.action_copy_id -> {
                presenter?.fireCopyIdClick()
                return true
            }
            R.id.action_add_to_shortcut -> {
                presenter?.fireAddToShortcutClick()
                return true
            }
            R.id.action_search -> {
                presenter?.fireSearchClick()
                return true
            }
            R.id.wall_attachments -> {
                presenter?.openConversationAttachments()
                return true
            }
            R.id.search_stories -> {
                val menus = ModalBottomSheetDialogFragment.Builder()
                menus.add(
                    OptionRequest(
                        R.id.button_ok,
                        getString(R.string.by_name),
                        R.drawable.pencil,
                        false
                    )
                )
                menus.add(
                    OptionRequest(
                        R.id.button_cancel,
                        getString(R.string.by_owner),
                        R.drawable.person,
                        false
                    )
                )
                menus.show(
                    requireActivity().supportFragmentManager,
                    "search_story_options",
                    object : ModalBottomSheetDialogFragment.Listener {
                        override fun onModalOptionSelected(option: Option) {
                            if (item.itemId == R.id.button_ok) {
                                presenter?.searchStory(true)
                            } else if (item.itemId == R.id.button_cancel) {
                                presenter?.searchStory(false)
                            }
                        }
                    })
                return true
            }
            R.id.action_open_url -> {
                val clipBoard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                if (clipBoard != null && clipBoard.primaryClip != null && clipBoard.primaryClip!!
                        .itemCount > 0
                ) {
                    val temp = clipBoard.primaryClip?.getItemAt(0)?.text.toString()
                    LinkHelper.openUrl(
                        requireActivity(),
                        presenter?.accountId ?: Settings.get().accounts().current,
                        temp, false
                    )
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_wall, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        val view = OptionView()
        presenter?.fireOptionViewCreated(view)
        val isDebug = Settings.get().other().isDeveloper_mode
        menu.findItem(R.id.action_open_url).isVisible = view.isMy
        menu.findItem(R.id.search_stories).isVisible = isDebug
        menu.findItem(R.id.action_edit).isVisible = view.isMy
        menu.findItem(R.id.action_add_to_shortcut).isVisible = !view.isMy
    }

    override fun copyToClipboard(label: String?, body: String?) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label, body)
        clipboard?.setPrimaryClip(clip)
        customToast.showToast(R.string.copied)
    }

    override fun goToPostCreation(accountId: Int, ownerId: Int, @EditingPostType postType: Int) {
        goToPostCreation(requireActivity(), accountId, ownerId, postType, null)
    }

    override fun showRefreshing(refreshing: Boolean) {
        mSwipeRefreshLayout?.isRefreshing = refreshing
    }

    @LayoutRes
    protected abstract fun headerLayout(): Int
    protected abstract fun onHeaderInflated(headerRootView: View)
    override fun displayWallData(data: MutableList<Post>) {
        mWallAdapter?.setItems(data)
    }

    override fun notifyWallDataSetChanged() {
        mWallAdapter?.notifyDataSetChanged()
    }

    override fun updateStory(stories: MutableList<Story>?) {
        mStoryAdapter?.setItems(stories ?: mutableListOf())
        mStoryAdapter?.notifyDataSetChanged()
    }

    override fun notifyWallItemChanged(position: Int) {
        mWallAdapter?.notifyItemChanged(position + (mWallAdapter?.headersCount ?: 0))
    }

    override fun notifyWallDataAdded(position: Int, count: Int) {
        mWallAdapter?.notifyItemRangeInserted(position + (mWallAdapter?.headersCount ?: 0), count)
    }

    override fun notifyWallItemRemoved(index: Int) {
        mWallAdapter?.notifyItemRemoved(index + (mWallAdapter?.headersCount ?: 0))
    }

    override fun onOwnerClick(ownerId: Int) {
        onOpenOwner(ownerId)
    }

    override fun onShareClick(post: Post) {
        presenter?.fireShareClick(post)
    }

    override fun onPostClick(post: Post) {
        presenter?.firePostBodyClick(post)
    }

    override fun onRestoreClick(post: Post) {
        presenter?.firePostRestoreClick(post)
    }

    override fun onCommentsClick(post: Post) {
        presenter?.fireCommentsClick(post)
    }

    override fun onLikeLongClick(post: Post) {
        presenter?.fireLikeLongClick(post)
    }

    override fun onShareLongClick(post: Post) {
        presenter?.fireShareLongClick(post)
    }

    override fun onLikeClick(post: Post) {
        presenter?.fireLikeClick(post)
    }

    override fun openPostEditor(accountId: Int, post: Post) {
        goToPostEditor(requireActivity(), accountId, post)
    }

    override fun setupLoadMoreFooter(@LoadMoreState state: Int) {
        mLoadMoreFooterHelper?.switchToState(state)
    }

    override fun openPhotoAlbums(accountId: Int, ownerId: Int, owner: Owner?) {
        getVKPhotoAlbumsPlace(
            accountId,
            ownerId,
            IVkPhotosView.ACTION_SHOW_PHOTOS,
            ParcelableOwnerWrapper.wrap(owner)
        )
            .tryOpenWith(requireActivity())
    }

    override fun openVideosLibrary(accountId: Int, ownerId: Int, owner: Owner?) {
        getVideosPlace(accountId, ownerId, IVideosListView.ACTION_SHOW)
            .withParcelableExtra(Extra.OWNER, owner)
            .tryOpenWith(requireActivity())
    }

    override fun openAudios(accountId: Int, ownerId: Int, owner: Owner?) {
        getAudiosPlace(accountId, ownerId)
            .withParcelableExtra(Extra.OWNER, owner)
            .tryOpenWith(requireActivity())
    }

    override fun openArticles(accountId: Int, ownerId: Int, owner: Owner?) {
        getOwnerArticles(accountId, ownerId)
            .withParcelableExtra(Extra.OWNER, owner)
            .tryOpenWith(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onClearSelection()
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onButtonRemoveClick(post: Post) {
        presenter?.fireButtonRemoveClick(post)
    }

    protected class OptionView : IWallView.IOptionView {
        var isMy = false
        var isBlacklistedByMe = false
        var isFavorite = false
        var isSubscribed = false
        override fun setIsBlacklistedByMe(blocked: Boolean) {
            isBlacklistedByMe = blocked
        }

        override fun setIsMy(my: Boolean) {
            isMy = my
        }

        override fun setIsFavorite(favorite: Boolean) {
            isFavorite = favorite
        }

        override fun setIsSubscribed(subscribed: Boolean) {
            isSubscribed = subscribed
        }
    }

    companion object {
        fun buildArgs(accountId: Int, ownerId: Int, owner: Owner?): Bundle {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putInt(Extra.OWNER_ID, ownerId)
            args.putParcelable(Extra.OWNER, ParcelableOwnerWrapper(owner))
            return args
        }

        fun newInstance(args: Bundle): Fragment {
            val fragment: Fragment = if (args.getInt(Extra.OWNER_ID) > 0) {
                UserWallFragment()
            } else {
                GroupWallFragment()
            }
            fragment.arguments = args
            return fragment
        }


        fun setupCounter(view: TextView?, count: Int) {
            view?.text = if (count > 0) getCounterWithK(count) else "-"
            view?.isEnabled = count > 0
        }


        fun setupCounterFlow(view: TextView?, container: ViewGroup?, count: Int) {
            view?.text = if (count > 0) getCounterWithK(count) else "-"
            view?.isEnabled = count > 0
            container?.visibility = if (count > 0) View.VISIBLE else View.GONE
        }


        @SuppressLint("SetTextI18n")
        fun setupCounterWith(view: TextView?, count: Int, with: Int) {
            view?.text =
                (if (count > 0) getCounterWithK(count) else "-") + if (with > 0) "/" + getCounterWithK(
                    with
                ) else ""
            view?.isEnabled = count > 0
        }
    }
}
