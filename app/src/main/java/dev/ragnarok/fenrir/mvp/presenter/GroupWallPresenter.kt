package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.domain.*
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.domain.Repository.walls
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.criteria.WallCriteria
import dev.ragnarok.fenrir.mvp.view.IGroupWallView
import dev.ragnarok.fenrir.mvp.view.IGroupWallView.IOptionMenuView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.PlaceFactory.getMentionsPlace
import dev.ragnarok.fenrir.settings.ISettings.IAccountsSettings
import dev.ragnarok.fenrir.util.RxUtils.applyCompletableIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.ignore
import dev.ragnarok.fenrir.util.ShortcutUtils.createWallShortcutRx
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.singletonArrayList
import kotlin.math.abs

class GroupWallPresenter(
    accountId: Int,
    ownerId: Int,
    pCommunity: Community?,
    private val context: Context,
    savedInstanceState: Bundle?
) : AbsWallPresenter<IGroupWallView>(accountId, ownerId, savedInstanceState) {
    var community: Community
    private val settings: IAccountsSettings
    private val faveInteractor: IFaveInteractor
    private val ownersRepository: IOwnersRepository
    private val communitiesInteractor: ICommunitiesInteractor
    private val wallsRepository: IWallsRepository
    private val filters: MutableList<PostFilter>
    private var details: CommunityDetails
    private fun resolveBaseCommunityViews() {
        view?.displayBaseCommunityData(
            community, details
        )
    }

    private fun resolveMenu() {
        view?.InvalidateOptionsMenu()
    }

    private fun resolveCounters() {
        view?.displayCounters(
            community.membersCount,
            details.topicsCount,
            details.docsCount,
            details.photosCount,
            details.audiosCount,
            details.videosCount,
            details.articlesCount,
            details.productsCount,
            details.chatsCount
        )
    }

    private fun refreshInfo() {
        val accountId = accountId
        appendDisposable(
            ownersRepository.getFullCommunityInfo(
                accountId,
                abs(ownerId),
                IOwnersRepository.MODE_CACHE
            )
                .compose(applySingleIOToMainSchedulers())
                .subscribe({
                    onFullInfoReceived(it.first, it.second)
                    requestActualFullInfo()
                }, ignore())
        )
    }

    private fun requestActualFullInfo() {
        val accountId = accountId
        appendDisposable(ownersRepository.getFullCommunityInfo(
            accountId,
            abs(ownerId),
            IOwnersRepository.MODE_NET
        )
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                onFullInfoReceived(
                    it.first,
                    it.second
                )
            }) { t: Throwable -> onDetailsGetError(t) })
    }

    private fun onFullInfoReceived(community: Community?, details: CommunityDetails?) {
        if (community != null) {
            this.community = community
        }
        if (details != null) {
            this.details = details
        }
        filters.clear()
        filters.addAll(createPostFilters())
        syncFiltersWithSelectedMode()
        syncFilterCounters()
        view?.notifyWallFiltersChanged()
        resolveActionButtons()
        resolveCounters()
        resolveBaseCommunityViews()
        resolveMenu()
    }

    private fun onDetailsGetError(t: Throwable) {
        showError(getCauseIfRuntime(t))
    }

    private fun createPostFilters(): List<PostFilter> {
        val filters: MutableList<PostFilter> = ArrayList()
        filters.add(PostFilter(WallCriteria.MODE_ALL, getString(R.string.all_posts)))
        filters.add(PostFilter(WallCriteria.MODE_OWNER, getString(R.string.owner_s_posts)))
        filters.add(PostFilter(WallCriteria.MODE_SUGGEST, getString(R.string.suggests)))
        if (isAdmin) {
            filters.add(PostFilter(WallCriteria.MODE_SCHEDULED, getString(R.string.scheduled)))
        }
        return filters
    }

    private val isAdmin: Boolean
        get() = community.isAdmin

    private fun syncFiltersWithSelectedMode() {
        for (filter in filters) {
            filter.setActive(filter.mode == wallFilter)
        }
    }

    private fun syncFilterCounters() {
        for (filter in filters) {
            when (filter.mode) {
                WallCriteria.MODE_ALL -> filter.count = details.allWallCount
                WallCriteria.MODE_OWNER -> filter.count = details.ownerWallCount
                WallCriteria.MODE_SCHEDULED -> filter.count = details.postponedWallCount
                WallCriteria.MODE_SUGGEST -> filter.count = details.suggestedWallCount
            }
        }
    }

    override fun onGuiCreated(viewHost: IGroupWallView) {
        super.onGuiCreated(viewHost)
        viewHost.displayWallFilters(filters)
        resolveBaseCommunityViews()
        resolveMenu()
        resolveCounters()
        resolveActionButtons()
    }

    fun firePrimaryButtonClick() {
        if (community.memberStatus == VKApiCommunity.MemberStatus.IS_MEMBER || community.memberStatus == VKApiCommunity.MemberStatus.SENT_REQUEST) {
            leaveCommunity()
        } else {
            joinCommunity()
        }
    }

    fun fireSecondaryButtonClick() {
        if (community.memberStatus == VKApiCommunity.MemberStatus.INVITED) {
            leaveCommunity()
        }
    }

    private fun leaveCommunity() {
        val accountid = accountId
        val groupId = abs(ownerId)
        appendDisposable(communitiesInteractor.leave(accountid, groupId)
            .compose(applyCompletableIOToMainSchedulers())
            .subscribe({ onLeaveResult() }) { t: Throwable? ->
                showError(getCauseIfRuntime(t))
            })
    }

    private fun joinCommunity() {
        val accountid = accountId
        val groupId = abs(ownerId)
        appendDisposable(communitiesInteractor.join(accountid, groupId)
            .compose(applyCompletableIOToMainSchedulers())
            .subscribe({ onJoinResult() }) { t: Throwable? ->
                showError(getCauseIfRuntime(t))
            })
    }

    fun fireHeaderPhotosClick() {
        view?.openPhotoAlbums(
            accountId,
            ownerId,
            community
        )
    }

    fun fireHeaderAudiosClick() {
        view?.openAudios(
            accountId,
            ownerId,
            community
        )
    }

    fun fireHeaderArticlesClick() {
        view?.openArticles(
            accountId,
            ownerId,
            community
        )
    }

    fun fireHeaderProductsClick() {
        view?.openProducts(
            accountId,
            ownerId,
            community
        )
    }

    fun fireHeaderVideosClick() {
        view?.openVideosLibrary(
            accountId,
            ownerId,
            community
        )
    }

    fun fireHeaderMembersClick() {
        view?.openCommunityMembers(
            accountId,
            abs(ownerId)
        )
    }

    fun fireHeaderTopicsClick() {
        view?.openTopics(
            accountId,
            ownerId,
            community
        )
    }

    fun fireHeaderDocsClick() {
        view?.openDocuments(
            accountId,
            ownerId,
            community
        )
    }

    fun fireShowCommunityInfoClick() {
        view?.goToShowCommunityInfo(
            accountId,
            community
        )
    }

    fun fireShowCommunityLinksInfoClick() {
        view?.goToShowCommunityLinksInfo(
            accountId,
            community
        )
    }

    fun fireShowCommunityAboutInfoClick() {
        view?.goToShowCommunityAboutInfo(
            accountId,
            details
        )
    }

    fun fireGroupChatsClick() {
        view?.goToGroupChats(
            accountId,
            community
        )
    }

    fun fireHeaderStatusClick() {
        if (details.statusAudio != null) {
            view?.playAudioList(
                accountId, 0, singletonArrayList(
                    details.statusAudio
                )
            )
        }
    }

    private fun resolveActionButtons() {
        @StringRes var primaryText: Int? = null
        @StringRes var secondaryText: Int? = null
        when (community.memberStatus) {
            VKApiCommunity.MemberStatus.IS_NOT_MEMBER -> when (community.type) {
                VKApiCommunity.Type.GROUP -> when (community.closed) {
                    VKApiCommunity.Status.CLOSED -> primaryText = R.string.community_send_request
                    VKApiCommunity.Status.OPEN -> primaryText = R.string.community_join
                }
                VKApiCommunity.Type.PAGE -> primaryText = R.string.community_follow
                VKApiCommunity.Type.EVENT -> primaryText = R.string.community_to_go
            }
            VKApiCommunity.MemberStatus.IS_MEMBER -> when (community.type) {
                VKApiCommunity.Type.GROUP -> primaryText = R.string.community_leave
                VKApiCommunity.Type.PAGE -> primaryText = R.string.community_unsubscribe_from_news
                VKApiCommunity.Type.EVENT -> primaryText = R.string.community_not_to_go
            }
            VKApiCommunity.MemberStatus.NOT_SURE -> primaryText = R.string.community_leave
            VKApiCommunity.MemberStatus.DECLINED_INVITATION -> primaryText =
                R.string.community_send_request
            VKApiCommunity.MemberStatus.SENT_REQUEST -> primaryText = R.string.cancel_request
            VKApiCommunity.MemberStatus.INVITED -> {
                primaryText = R.string.community_join
                secondaryText = R.string.cancel_invitation
            }
        }
        val finalPrimaryText = primaryText
        val finalSecondaryText = secondaryText
        view?.let {
            it.setupPrimaryButton(finalPrimaryText)
            it.setupSecondaryButton(finalSecondaryText)
        }
    }

    private fun onLeaveResult() {
        var resultMessage: Int? = null
        when (community.memberStatus) {
            VKApiCommunity.MemberStatus.IS_MEMBER -> {
                community.memberStatus = VKApiCommunity.MemberStatus.IS_NOT_MEMBER
                community.isMember = false
                when (community.type) {
                    VKApiCommunity.Type.GROUP, VKApiCommunity.Type.EVENT -> resultMessage =
                        R.string.community_leave_success
                    VKApiCommunity.Type.PAGE -> resultMessage =
                        R.string.community_unsubscribe_from_news_success
                }
            }
            VKApiCommunity.MemberStatus.SENT_REQUEST -> if (community.type == VKApiCommunity.Type.GROUP) {
                community.memberStatus = VKApiCommunity.MemberStatus.IS_NOT_MEMBER
                community.isMember = false
                resultMessage = R.string.request_canceled
            }
            VKApiCommunity.MemberStatus.INVITED -> if (community.type == VKApiCommunity.Type.GROUP) {
                community.isMember = false
                community.memberStatus = VKApiCommunity.MemberStatus.IS_NOT_MEMBER
                resultMessage = R.string.invitation_has_been_declined
            }
        }
        resolveActionButtons()
        if (resultMessage != null) {
            val finalResultMessage: Int = resultMessage
            view?.showSnackbar(
                finalResultMessage,
                true
            )
        }
    }

    private fun onJoinResult() {
        var resultMessage: Int? = null
        when (community.memberStatus) {
            VKApiCommunity.MemberStatus.IS_NOT_MEMBER -> when (community.type) {
                VKApiCommunity.Type.GROUP -> when (community.closed) {
                    VKApiCommunity.Status.CLOSED -> {
                        community.isMember = false
                        community.memberStatus = VKApiCommunity.MemberStatus.SENT_REQUEST
                        resultMessage = R.string.community_send_request_success
                    }
                    VKApiCommunity.Status.OPEN -> {
                        community.isMember = true
                        community.memberStatus = VKApiCommunity.MemberStatus.IS_MEMBER
                        resultMessage = R.string.community_join_success
                    }
                }
                VKApiCommunity.Type.PAGE, VKApiCommunity.Type.EVENT -> {
                    community.isMember = true
                    community.memberStatus = VKApiCommunity.MemberStatus.IS_MEMBER
                    resultMessage = R.string.community_follow_success
                }
            }
            VKApiCommunity.MemberStatus.DECLINED_INVITATION -> if (community.type == VKApiCommunity.Type.GROUP) {
                community.isMember = false
                community.memberStatus = VKApiCommunity.MemberStatus.SENT_REQUEST
                resultMessage = R.string.community_send_request_success
            }
            VKApiCommunity.MemberStatus.INVITED -> if (community.type == VKApiCommunity.Type.GROUP) {
                community.isMember = true
                community.memberStatus = VKApiCommunity.MemberStatus.IS_MEMBER
                resultMessage = R.string.community_join_success
            }
        }
        resolveActionButtons()
        if (resultMessage != null) {
            val finalResultMessage: Int = resultMessage
            view?.showSnackbar(
                finalResultMessage,
                true
            )
        }
    }

    fun fireFilterEntryClick(entry: PostFilter) {
        if (changeWallFilter(entry.mode)) {
            syncFiltersWithSelectedMode()
            view?.notifyWallFiltersChanged()
        }
    }

    fun fireCommunityControlClick() {
        view?.goToCommunityControl(
            accountId,
            community,
            null
        )

        /*final int accountId = super.getAccountId();
        final int grouId = Math.abs(ownerId);

        IGroupSettingsInteractor interactor = new GroupSettingsInteractor(Includes.getNetworkInterfaces(), Includes.getStores().owners());
        appendDisposable(interactor.getGroupSettings(accountId, grouId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onSettingsReceived, throwable -> {
                    callView(v -> showError(v, getCauseIfRuntime(throwable)));
                }));*/
    }

    //private void onSettingsReceived(GroupSettings settings) {
    //    callView(view -> view.goToCommunityControl(getAccountId(), owner, settings));
    //}
    fun fireCommunityMessagesClick() {
        if (settings.getAccessToken(ownerId).nonNullNoEmpty()) {
            openCommunityMessages()
        } else {
            val groupId = abs(ownerId)
            view?.startLoginCommunityActivity(
                groupId
            )
        }
    }

    private fun openCommunityMessages() {
        val groupId = abs(ownerId)
        val accountId = accountId
        val subtitle = community.fullName
        view?.openCommunityDialogs(
            accountId,
            groupId,
            subtitle
        )
    }

    fun fireGroupTokensReceived(tokens: ArrayList<Token>) {
        for (token in tokens) {
            settings.registerAccountId(token.ownerId, false)
            settings.storeAccessToken(token.ownerId, token.accessToken)
        }
        if (tokens.size == 1) {
            openCommunityMessages()
        }
    }

    fun fireMutualFriends() {
        view?.goToMutualFriends(
            accountId,
            community
        )
    }

    fun fireSubscribe() {
        val accountId = accountId
        appendDisposable(wallsRepository.subscribe(accountId, ownerId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ onExecuteComplete() }) { t: Throwable -> onExecuteError(t) })
    }

    fun fireUnSubscribe() {
        val accountId = accountId
        appendDisposable(wallsRepository.unsubscribe(accountId, ownerId)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ onExecuteComplete() }) { t: Throwable -> onExecuteError(t) })
    }

    fun fireAddToBookmarksClick() {
        val accountId = accountId
        appendDisposable(faveInteractor.addPage(accountId, ownerId)
            .compose(applyCompletableIOToMainSchedulers())
            .subscribe({ onExecuteComplete() }) { t: Throwable -> onExecuteError(t) })
    }

    fun fireRemoveFromBookmarks() {
        val accountId = accountId
        appendDisposable(faveInteractor.removePage(accountId, ownerId, false)
            .compose(applyCompletableIOToMainSchedulers())
            .subscribe({ onExecuteComplete() }) { t: Throwable -> onExecuteError(t) })
    }

    fun fireMentions() {
        getMentionsPlace(accountId, ownerId).tryOpenWith(context)
    }

    private fun onExecuteError(t: Throwable) {
        showError(getCauseIfRuntime(t))
    }

    private fun onExecuteComplete() {
        onRefresh()
        view?.customToast?.showToast(R.string.success)
    }

    override fun onRefresh() {
        requestActualFullInfo()
    }

    fun fireOptionMenuViewCreated(view: IOptionMenuView) {
        view.setControlVisible(isAdmin)
        view.setIsFavorite(details.isSetFavorite)
        view.setIsSubscribed(details.isSetSubscribed)
    }

    fun fireChatClick() {
        val peer = Peer(ownerId).setTitle(
            community.fullName
        ).setAvaUrl(community.maxSquareAvatar)
        val accountId = accountId
        view?.openChatWith(
            accountId,
            accountId,
            peer
        )
    }

    override fun fireAddToShortcutClick() {
        appendDisposable(
            createWallShortcutRx(context, accountId, community)
                .compose(applyCompletableIOToMainSchedulers()).subscribe({
                    view?.showSnackbar(
                        R.string.success,
                        true
                    )
                }) { t: Throwable ->
                    view?.showError(t.localizedMessage)
                })
    }

    override fun searchStory(ByName: Boolean) {
        appendDisposable(ownersRepository.searchStory(
            accountId,
            if (ByName) community.fullName else null,
            if (ByName) null else ownerId
        )
            .compose(applySingleIOToMainSchedulers())
            .subscribe({
                if (it.nonNullNoEmpty()) {
                    stories.clear()
                    stories.addAll(it)
                    view?.updateStory(
                        stories
                    )
                }
            }) { })
    }

    init {
        community = pCommunity ?: Community(abs(ownerId))
        details = CommunityDetails()
        ownersRepository = owners
        faveInteractor = InteractorFactory.createFaveInteractor()
        communitiesInteractor = InteractorFactory.createCommunitiesInteractor()
        settings = Includes.settings.accounts()
        wallsRepository = walls
        filters = ArrayList()
        filters.addAll(createPostFilters())
        syncFiltersWithSelectedMode()
        syncFilterCounters()
        refreshInfo()
    }
}