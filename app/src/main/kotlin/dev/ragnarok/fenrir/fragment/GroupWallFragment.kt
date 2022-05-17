package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textview.MaterialTextView
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarSubtitle
import dev.ragnarok.fenrir.activity.ActivityUtils.setToolbarTitle
import dev.ragnarok.fenrir.activity.LoginActivity.Companion.createIntent
import dev.ragnarok.fenrir.activity.LoginActivity.Companion.extractGroupTokens
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter
import dev.ragnarok.fenrir.fragment.search.SearchContentType
import dev.ragnarok.fenrir.fragment.search.criteria.PeopleSearchCriteria
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.DocsListPresenter
import dev.ragnarok.fenrir.mvp.presenter.GroupWallPresenter
import dev.ragnarok.fenrir.mvp.view.IGroupWallView
import dev.ragnarok.fenrir.mvp.view.IGroupWallView.IOptionMenuView
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation
import dev.ragnarok.fenrir.place.PlaceFactory.getCommunityControlPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getDialogsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getDocumentsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getGroupChatsPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getMarketAlbumPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getMarketPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getShowComunityInfoPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getShowComunityLinksInfoPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleTabSearchPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getSingleURLPhotoPlace
import dev.ragnarok.fenrir.place.PlaceFactory.getTopicsPlace
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.Utils.dp
import dev.ragnarok.fenrir.util.Utils.getVerifiedColor
import dev.ragnarok.fenrir.util.Utils.setBackgroundTint
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import kotlin.math.abs

class GroupWallFragment : AbsWallFragment<IGroupWallView, GroupWallPresenter>(), IGroupWallView {
    private val requestCommunity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val tokens = extractGroupTokens(
                result.data ?: return@registerForActivityResult
            )
            lazyPresenter {
                fireGroupTokensReceived(tokens ?: return@lazyPresenter)
            }
        }
    }
    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        presenter?.fireShowQR(requireActivity())
    }
    private val ownerLinkAdapter: OwnerLinkSpanFactory.ActionListener =
        object : LinkActionAdapter() {
            override fun onOwnerClick(ownerId: Int) {
                presenter?.fireOwnerClick(
                    ownerId
                )
            }
        }
    private var mHeaderHolder: GroupHeaderHolder? = null
    override fun displayBaseCommunityData(community: Community, details: CommunityDetails) {
        if (mHeaderHolder == null) return
        mHeaderHolder?.tvName?.text = community.fullName
        details.getCover()?.getImages().ifNonNullNoEmpty({
            var def = 0
            var url: String? = null
            for (i in it) {
                if (i.getWidth() * i.getHeight() > def) {
                    def = i.getWidth() * i.getHeight()
                    url = i.getUrl()
                }
            }
            displayCommunityCover(url)
        }, {
            displayCommunityCover(community.maxSquareAvatar)
        })
        val statusText: String? = if (details.getStatusAudio() != null) {
            details.getStatusAudio()?.artistAndTitle
        } else {
            details.getStatus()
        }
        mHeaderHolder?.tvStatus?.text = statusText
        mHeaderHolder?.tvAudioStatus?.visibility =
            if (details.getStatusAudio() != null) View.VISIBLE else View.GONE
        val domain =
            if (community.domain.nonNullNoEmpty()) "@" + community.domain else ("club" + community.id)
        mHeaderHolder?.tvDomain?.text = domain
        mHeaderHolder?.tvName?.setTextColor(
            getVerifiedColor(
                requireActivity(),
                community.isVerified
            )
        )
        mHeaderHolder?.tvDomain?.setTextColor(
            getVerifiedColor(
                requireActivity(),
                community.isVerified
            )
        )
        val donate_anim = Settings.get().other().donate_anim_set
        if (donate_anim > 0 && community.isDonated) {
            mHeaderHolder?.bDonate?.visibility = View.VISIBLE
            mHeaderHolder?.bDonate?.setAutoRepeat(true)
            if (donate_anim == 2) {
                val cur = Settings.get().ui().mainThemeKey
                if ("fire" == cur || "yellow_violet" == cur) {
                    mHeaderHolder?.tvName?.setTextColor(Color.parseColor("#df9d00"))
                    mHeaderHolder?.tvDomain?.setTextColor(Color.parseColor("#df9d00"))
                    setBackgroundTint(mHeaderHolder?.ivVerified, Color.parseColor("#df9d00"))
                    mHeaderHolder?.bDonate?.fromRes(R.raw.donater_fire, dp(100f), dp(100f))
                } else {
                    mHeaderHolder?.tvName?.setTextColor(CurrentTheme.getColorPrimary(requireActivity()))
                    mHeaderHolder?.tvDomain?.setTextColor(
                        CurrentTheme.getColorPrimary(
                            requireActivity()
                        )
                    )
                    setBackgroundTint(
                        mHeaderHolder?.ivVerified,
                        CurrentTheme.getColorPrimary(requireActivity())
                    )
                    mHeaderHolder?.bDonate?.fromRes(
                        R.raw.donater_fire,
                        dp(100f),
                        dp(100f),
                        intArrayOf(0xFF812E, CurrentTheme.getColorPrimary(requireActivity())),
                        true
                    )
                }
            } else {
                mHeaderHolder?.bDonate?.fromRes(
                    R.raw.donater,
                    dp(100f),
                    dp(100f),
                    intArrayOf(
                        0xffffff,
                        CurrentTheme.getColorPrimary(requireActivity()),
                        0x777777,
                        CurrentTheme.getColorSecondary(requireActivity())
                    )
                )
            }
            mHeaderHolder?.bDonate?.playAnimation()
        } else {
            mHeaderHolder?.bDonate?.setImageDrawable(null)
            mHeaderHolder?.bDonate?.visibility = View.GONE
        }
        mHeaderHolder?.ivVerified?.visibility =
            if (community.isVerified) View.VISIBLE else View.GONE
        if (!details.isCanMessage()) mHeaderHolder?.fabMessage?.setImageResource(R.drawable.close) else mHeaderHolder?.fabMessage?.setImageResource(
            R.drawable.email
        )
        val photoUrl = community.maxSquareAvatar
        if (photoUrl.nonNullNoEmpty()) {
            mHeaderHolder?.ivAvatar?.let {
                with()
                    .load(photoUrl).transform(CurrentTheme.createTransformationForAvatar())
                    .tag(Constants.PICASSO_TAG)
                    .into(it)
            }
        }
        mHeaderHolder?.ivAvatar?.setOnClickListener {
            val cmt = presenter?.community
                ?: return@setOnClickListener
            getSingleURLPhotoPlace(
                cmt.originalAvatar,
                cmt.fullName,
                "club" + abs(cmt.id)
            ).tryOpenWith(requireActivity())
        }
        mHeaderHolder?.ivAvatar?.setOnLongClickListener {
            presenter?.fireMentions()
            true
        }
    }

    private fun displayCommunityCover(resource: String?) {
        if (!Settings.get().other().isShow_wall_cover) return
        if (resource.nonNullNoEmpty()) {
            mHeaderHolder?.vgCover?.let {
                with()
                    .load(resource)
                    .transform(BlurTransformation(6f, requireActivity()))
                    .into(it)
            }
        }
    }

    override fun InvalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<GroupWallPresenter> {
        return object : IPresenterFactory<GroupWallPresenter> {
            override fun create(): GroupWallPresenter {
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                val ownerId = requireArguments().getInt(Extra.OWNER_ID)
                val wrapper: ParcelableOwnerWrapper? = requireArguments().getParcelable(Extra.OWNER)
                return GroupWallPresenter(
                    accountId,
                    ownerId,
                    wrapper?.get() as Community?,
                    requireActivity(),
                    saveInstanceState
                )
            }
        }
    }

    override fun headerLayout(): Int {
        return R.layout.header_group
    }

    override fun onHeaderInflated(headerRootView: View) {
        mHeaderHolder = GroupHeaderHolder(headerRootView)
        setupPaganContent(mHeaderHolder?.Runes, mHeaderHolder?.paganSymbol)
    }

    override fun setupPrimaryButton(@StringRes title: Int?) {
        if (mHeaderHolder != null) {
            if (title != null) {
                mHeaderHolder?.primaryActionButton?.setText(title)
                mHeaderHolder?.primaryActionButton?.visibility = View.VISIBLE
            } else {
                mHeaderHolder?.primaryActionButton?.visibility = View.GONE
            }
        }
    }

    override fun setupSecondaryButton(@StringRes title: Int?) {
        if (mHeaderHolder != null) {
            if (title != null) {
                mHeaderHolder?.secondaryActionButton?.setText(title)
                mHeaderHolder?.secondaryActionButton?.visibility = View.VISIBLE
            } else {
                mHeaderHolder?.secondaryActionButton?.visibility = View.GONE
            }
        }
    }

    override fun openTopics(accountId: Int, ownerId: Int, owner: Owner?) {
        getTopicsPlace(accountId, ownerId)
            .withParcelableExtra(Extra.OWNER, owner)
            .tryOpenWith(requireActivity())
    }

    override fun openCommunityMembers(accountId: Int, groupId: Int) {
        val criteria = PeopleSearchCriteria("")
            .setGroupId(groupId)
        getSingleTabSearchPlace(accountId, SearchContentType.PEOPLE, criteria).tryOpenWith(
            requireActivity()
        )
    }

    override fun openDocuments(accountId: Int, ownerId: Int, owner: Owner?) {
        getDocumentsPlace(accountId, ownerId, DocsListPresenter.ACTION_SHOW)
            .withParcelableExtra(Extra.OWNER, owner)
            .tryOpenWith(requireActivity())
    }

    override fun displayWallFilters(filters: MutableList<PostFilter>) {
        mHeaderHolder?.mFiltersAdapter?.setItems(filters)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        super.onCreateMenu(menu, menuInflater)
        menuInflater.inflate(R.menu.menu_community_wall, menu)
        val optionMenuView = OptionMenuView()
        presenter?.fireOptionMenuViewCreated(
            optionMenuView
        )
        menu.add(R.string.mutual_friends).setOnMenuItemClickListener {
            presenter?.fireMutualFriends()
            true
        }
        if (!optionMenuView.pIsSubscribed) {
            menu.add(R.string.notify_wall_added).setOnMenuItemClickListener {
                presenter?.fireSubscribe()
                true
            }
        } else {
            menu.add(R.string.unnotify_wall_added).setOnMenuItemClickListener {
                presenter?.fireUnSubscribe()
                true
            }
        }
        if (!optionMenuView.pIsFavorite) {
            menu.add(R.string.add_to_bookmarks).setOnMenuItemClickListener {
                presenter?.fireAddToBookmarksClick()
                true
            }
        } else {
            menu.add(R.string.remove_from_bookmarks).setOnMenuItemClickListener {
                presenter?.fireRemoveFromBookmarks()
                true
            }
        }
        menu.add(R.string.mentions).setOnMenuItemClickListener {
            presenter?.fireMentions()
            true
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_community_control) {
            presenter?.fireCommunityControlClick()
            return true
        }
        if (menuItem.itemId == R.id.action_community_messages) {
            presenter?.fireCommunityMessagesClick()
            return true
        }
        if (menuItem.itemId == R.id.action_show_qr) {
            if (!hasReadWriteStoragePermission(requireActivity())) {
                requestWritePermission.launch()
            } else {
                presenter?.fireShowQR(
                    requireActivity()
                )
            }
            return true
        }
        return super.onMenuItemSelected(menuItem)
    }

    override fun notifyWallFiltersChanged() {
        mHeaderHolder?.mFiltersAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(this, R.string.community)
        setToolbarSubtitle(this, null)
    }

    override fun goToCommunityControl(
        accountId: Int,
        community: Community,
        settings: GroupSettings?
    ) {
        getCommunityControlPlace(accountId, community, settings).tryOpenWith(requireActivity())
    }

    override fun goToShowCommunityInfo(accountId: Int, community: Community) {
        getShowComunityInfoPlace(accountId, community).tryOpenWith(requireActivity())
    }

    override fun goToShowCommunityLinksInfo(accountId: Int, community: Community) {
        getShowComunityLinksInfoPlace(accountId, community).tryOpenWith(requireActivity())
    }

    override fun goToShowCommunityAboutInfo(accountId: Int, details: CommunityDetails) {
        if (details.getDescription().isNullOrEmpty()) {
            return
        }
        val root = View.inflate(requireActivity(), R.layout.dialog_selectable_text, null)
        val tvText: MaterialTextView = root.findViewById(R.id.selectable_text)
        val subtitle =
            OwnerLinkSpanFactory.withSpans(
                details.getDescription(),
                owners = true,
                topics = false,
                listener = ownerLinkAdapter
            )
        tvText.setText(subtitle, TextView.BufferType.SPANNABLE)
        tvText.movementMethod = LinkMovementMethod.getInstance()
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.description_hint)
            .setView(root)
            .setPositiveButton(R.string.button_ok, null)
            .show()
    }

    override fun goToGroupChats(accountId: Int, community: Community) {
        getGroupChatsPlace(accountId, abs(community.id)).tryOpenWith(requireActivity())
    }

    override fun goToMutualFriends(accountId: Int, community: Community) {
        CommunityFriendsFragment.newInstance(accountId, community.id)
            .show(childFragmentManager, "community_friends")
    }

    override fun startLoginCommunityActivity(groupId: Int) {
        val intent = createIntent(
            requireActivity(),
            "2685278",
            "messages,photos,docs,manage",
            listOf(groupId)
        )
        requestCommunity.launch(intent)
    }

    override fun openCommunityDialogs(accountId: Int, groupId: Int, subtitle: String?) {
        getDialogsPlace(accountId, -groupId, subtitle).tryOpenWith(requireActivity())
    }

    override fun displayCounters(
        members: Int,
        topics: Int,
        docs: Int,
        photos: Int,
        audio: Int,
        video: Int,
        articles: Int,
        products: Int,
        chats: Int,
        products_services: Int,
        narratives: Int
    ) {
        if (mHeaderHolder == null) return
        setupCounter(mHeaderHolder?.bTopics, topics)
        setupCounter(mHeaderHolder?.bMembers, members)
        setupCounter(mHeaderHolder?.bDocuments, docs)
        setupCounter(mHeaderHolder?.bPhotos, photos)
        setupCounter(mHeaderHolder?.bAudios, audio)
        setupCounter(mHeaderHolder?.bVideos, video)
        setupCounter(mHeaderHolder?.bArticles, articles)
        setupCounter(mHeaderHolder?.bProducts, products)
        setupCounter(mHeaderHolder?.bProductServices, products_services)
        setupCounter(mHeaderHolder?.bNarratives, narratives)
        setupCounterFlow(mHeaderHolder?.bChats, mHeaderHolder?.bChatsContainer, chats)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        val optionMenuView = OptionMenuView()
        presenter?.fireOptionMenuViewCreated(
            optionMenuView
        )
        menu.findItem(R.id.action_community_control).isVisible = optionMenuView.pControlVisible
    }

    override fun openProducts(accountId: Int, ownerId: Int, owner: Owner?) {
        getMarketAlbumPlace(accountId, ownerId).tryOpenWith(requireActivity())
    }

    override fun openProductServices(accountId: Int, ownerId: Int) {
        getMarketPlace(accountId, ownerId, 0, true).tryOpenWith(requireActivity())
    }

    private class OptionMenuView : IOptionMenuView {
        var pControlVisible = false
        var pIsFavorite = false
        var pIsSubscribed = false
        override fun setControlVisible(visible: Boolean) {
            pControlVisible = visible
        }

        override fun setIsFavorite(favorite: Boolean) {
            pIsFavorite = favorite
        }

        override fun setIsSubscribed(subscribed: Boolean) {
            pIsSubscribed = subscribed
        }
    }

    private inner class GroupHeaderHolder(root: View) {
        val vgCover: ImageView = root.findViewById(R.id.cover)
        val ivAvatar: ImageView = root.findViewById(R.id.header_group_avatar)
        val ivVerified: ImageView = root.findViewById(R.id.item_verified)
        val bDonate: RLottieImageView = root.findViewById(R.id.donated_anim)
        val tvName: TextView = root.findViewById(R.id.header_group_name)
        val tvStatus: TextView = root.findViewById(R.id.header_group_status)
        val tvAudioStatus: ImageView = root.findViewById(R.id.fragment_group_audio)
        val tvDomain: TextView = root.findViewById(R.id.header_group_id)
        val bTopics: TextView = root.findViewById(R.id.header_group_btopics)
        val bArticles: TextView = root.findViewById(R.id.header_group_barticles)
        val bChats: TextView = root.findViewById(R.id.header_group_bchats)
        val bChatsContainer: ViewGroup = root.findViewById(R.id.header_group_chats_container)
        val bProducts: TextView = root.findViewById(R.id.header_group_bproducts)
        val bProductServices: TextView = root.findViewById(R.id.header_group_bservices_products)
        val bNarratives: TextView = root.findViewById(R.id.header_group_bnarratives)
        val bMembers: TextView = root.findViewById(R.id.header_group_bmembers)
        val bDocuments: TextView = root.findViewById(R.id.header_group_bdocuments)
        val bPhotos: TextView = root.findViewById(R.id.header_group_bphotos)
        val bAudios: TextView = root.findViewById(R.id.header_group_baudios)
        val bVideos: TextView = root.findViewById(R.id.header_group_bvideos)
        val primaryActionButton: MaterialButton =
            root.findViewById(R.id.header_group_primary_button)
        val secondaryActionButton: MaterialButton =
            root.findViewById(R.id.header_group_secondary_button)
        val fabMessage: FloatingActionButton = root.findViewById(R.id.header_group_fab_message)
        val mFiltersAdapter: HorizontalOptionsAdapter<PostFilter>
        val paganSymbol: RLottieImageView = root.findViewById(R.id.pagan_symbol)
        val Runes: View = root.findViewById(R.id.runes_container)

        init {
            val filterList: RecyclerView = root.findViewById(R.id.post_filter_recyclerview)
            filterList.layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            mFiltersAdapter = HorizontalOptionsAdapter(mutableListOf())
            mFiltersAdapter.setListener(object : HorizontalOptionsAdapter.Listener<PostFilter> {
                override fun onOptionClick(entry: PostFilter) {
                    presenter?.fireFilterEntryClick(entry)
                }
            })
            filterList.adapter = mFiltersAdapter
            tvStatus.setOnClickListener {
                presenter?.fireHeaderStatusClick()
            }
            fabMessage.setOnClickListener {
                presenter?.fireChatClick()
            }
            secondaryActionButton.setOnClickListener {
                presenter?.fireSecondaryButtonClick()
            }
            primaryActionButton.setOnClickListener {
                presenter?.firePrimaryButtonClick()
            }
            root.findViewById<View>(R.id.header_group_photos_container)
                .setOnClickListener {
                    presenter?.fireHeaderPhotosClick()
                }
            root.findViewById<View>(R.id.header_group_videos_container)
                .setOnClickListener {
                    presenter?.fireHeaderVideosClick()
                }
            root.findViewById<View>(R.id.header_group_members_container)
                .setOnClickListener {
                    presenter?.fireHeaderMembersClick()
                }
            root.findViewById<View>(R.id.horiz_scroll).clipToOutline = true
            root.findViewById<View>(R.id.header_group_topics_container)
                .setOnClickListener {
                    presenter?.fireHeaderTopicsClick()
                }
            root.findViewById<View>(R.id.header_group_documents_container)
                .setOnClickListener {
                    presenter?.fireHeaderDocsClick()
                }
            root.findViewById<View>(R.id.header_group_audios_container)
                .setOnClickListener {
                    presenter?.fireHeaderAudiosClick()
                }
            root.findViewById<View>(R.id.header_group_articles_container)
                .setOnClickListener {
                    presenter?.fireHeaderArticlesClick()
                }
            root.findViewById<View>(R.id.header_group_products_container)
                .setOnClickListener {
                    presenter?.fireHeaderProductsClick()
                }
            root.findViewById<View>(R.id.header_group_products_services_container)
                .setOnClickListener {
                    presenter?.fireHeaderProductServicesClick()
                }
            root.findViewById<View>(R.id.header_group_contacts_container)
                .setOnClickListener {
                    presenter?.fireShowCommunityInfoClick()
                }
            root.findViewById<View>(R.id.header_group_links_container)
                .setOnClickListener {
                    presenter?.fireShowCommunityLinksInfoClick()
                }
            root.findViewById<View>(R.id.header_group_about_container)
                .setOnClickListener {
                    presenter?.fireShowCommunityAboutInfoClick()
                }
            root.findViewById<View>(R.id.header_group_narratives_container)
                .setOnClickListener {
                    presenter?.fireNarrativesClick()
                }
            bChatsContainer.setOnClickListener {
                presenter?.fireGroupChatsClick()
            }
        }
    }
}