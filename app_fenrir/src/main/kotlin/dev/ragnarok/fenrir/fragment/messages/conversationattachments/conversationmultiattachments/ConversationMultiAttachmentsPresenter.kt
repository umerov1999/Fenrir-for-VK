package dev.ragnarok.fenrir.fragment.messages.conversationattachments.conversationmultiattachments

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiPost
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.db.serialize.Serializers
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.Repository
import dev.ragnarok.fenrir.domain.interfaces.IAudioInteractor
import dev.ragnarok.fenrir.domain.interfaces.IMessagesRepository
import dev.ragnarok.fenrir.domain.interfaces.IWallsRepository
import dev.ragnarok.fenrir.fragment.base.PlaceSupportPresenter
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackService
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.Link
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoAlbum
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.model.TmpSource
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.parcel.ParcelFlags
import dev.ragnarok.fenrir.module.parcel.ParcelNative
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.intValueIn
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.dummy
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.hls.M3U8

class ConversationMultiAttachmentsPresenter(
    accountId: Long,
    private val peerID: Long,
    savedInstanceState: Bundle?
) : PlaceSupportPresenter<IConversationMultiAttachmentsView>(accountId, savedInstanceState) {
    private val mAudios: ArrayList<Audio> = ArrayList()
    private val mDocs: ArrayList<Document> = ArrayList()
    private val mLinks: ArrayList<Link> = ArrayList()
    private val mPhotoAlbums: ArrayList<PhotoAlbum> = ArrayList()
    private val mPhotos: ArrayList<Photo> = ArrayList()
    private val mVideos: ArrayList<Video> = ArrayList()
    private val mPosts: ArrayList<Post> = ArrayList()

    private val messagesRepository: IMessagesRepository = Repository.messages
    private val fInteractor: IWallsRepository = Repository.walls
    private val audioInteractor: IAudioInteractor = InteractorFactory.audio
    private val actualDataDisposable = CompositeJob()
    private var serviceStatusJob = CancelableJob()
    private var loaded = 0
    private var actualDataReceived = false
    private var endOfContent = false
    private var actualDataLoading = false
    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1

    @AttachmentConversationType
    private var currentPage: Int = AttachmentConversationType.PHOTO
    override fun onGuiCreated(viewHost: IConversationMultiAttachmentsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayAudioData(mAudios)
        viewHost.displayDocsData(mDocs)
        viewHost.displayLinksData(mLinks)
        viewHost.displayPhotoAlbumsData(mPhotoAlbums)
        viewHost.displayPhotoData(mPhotos)
        viewHost.displayVideoData(mVideos)
        viewHost.displayPostsData(mPosts)
        resolveToolbar()
        viewHost.resolveEmptyText(currentPage)
    }

    fun fireUpdateCurrentPage(@AttachmentConversationType currentPage: Int) {
        this.currentPage = currentPage
        view?.resolveEmptyText(currentPage)
        resolveToolbar()
    }

    private fun loadActualData(offset: Int) {
        actualDataLoading = true
        resolveRefreshingView()
        actualDataDisposable.add(
            messagesRepository.getPeerMessages(
                accountId = accountId,
                peerId = peerID,
                count = 100,
                offset = offset,
                startCmId = null,
                excludeStartMessage = false,
                cacheData = false, rev = false
            )
                .fromIOToMain({ data ->
                    onActualDataReceived(
                        offset,
                        data
                    )
                }) { t -> onActualDataGetError(t) })
    }

    private fun onActualDataGetError(t: Throwable) {
        actualDataLoading = false
        showError(t)
        resolveRefreshingView()
    }

    private fun updatePosts(data: List<Post>) {
        for (i in data) {
            i.attachments?.audios.nonNullNoEmpty {
                mAudios.addAll(it)
            }
            i.attachments?.docs.nonNullNoEmpty {
                mDocs.addAll(it)
            }
            i.attachments?.links.nonNullNoEmpty {
                mLinks.addAll(it)
            }
            i.attachments?.photoAlbums.nonNullNoEmpty {
                mPhotoAlbums.addAll(it)
            }
            i.attachments?.photos.nonNullNoEmpty {
                mPhotos.addAll(it)
            }
            i.attachments?.videos.nonNullNoEmpty {
                mVideos.addAll(it)
            }
            i.getCopyHierarchy()?.let {
                updatePosts(it)
            }
        }
    }

    private fun update(data: List<Message>, root: Boolean) {
        val origSizeAudios = mAudios.size
        val origSizeDocs = mDocs.size
        val origSizeLinks = mLinks.size
        val origSizePhotoAlbums = mPhotoAlbums.size
        val origSizePhotos = mPhotos.size
        val origSizeVideos = mVideos.size
        val origSizePosts = mPosts.size

        for (i in data) {
            i.attachments?.audios.nonNullNoEmpty {
                mAudios.addAll(it)
            }
            i.attachments?.docs.nonNullNoEmpty {
                mDocs.addAll(it)
            }
            i.attachments?.links.nonNullNoEmpty {
                mLinks.addAll(it)
            }
            i.attachments?.photoAlbums.nonNullNoEmpty {
                mPhotoAlbums.addAll(it)
            }
            i.attachments?.photos.nonNullNoEmpty {
                mPhotos.addAll(it)
            }
            i.attachments?.videos.nonNullNoEmpty {
                mVideos.addAll(it)
            }
            i.attachments?.posts.nonNullNoEmpty {
                mPosts.addAll(it)
                updatePosts(it)
            }
            i.fwd?.let {
                update(it, false)
            }
        }
        if (root) {
            if (mAudios.size - origSizeAudios > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.AUDIO,
                    origSizeAudios,
                    mAudios.size - origSizeAudios
                )
            }
            if (mDocs.size - origSizeDocs > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.DOC,
                    origSizeDocs,
                    mDocs.size - origSizeDocs
                )
            }
            if (mLinks.size - origSizeLinks > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.LINK,
                    origSizeLinks,
                    mLinks.size - origSizeLinks
                )
            }
            if (mPhotoAlbums.size - origSizePhotoAlbums > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.PHOTO_ALBUM,
                    origSizePhotoAlbums,
                    mPhotoAlbums.size - origSizePhotoAlbums
                )
            }
            if (mPhotos.size - origSizePhotos > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.PHOTO,
                    origSizePhotos,
                    mPhotos.size - origSizePhotos
                )
            }
            if (mVideos.size - origSizeVideos > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.VIDEO,
                    origSizeVideos,
                    mVideos.size - origSizeVideos
                )
            }
            if (mPosts.size - origSizePosts > 0) {
                view?.notifyDataAdded(
                    AttachmentConversationType.POST,
                    origSizePosts,
                    mPosts.size - origSizePosts
                )
            }
            view?.resolveEmptyText(currentPage)
        }
    }

    private fun clearAllData() {
        val origSizeAudios = mAudios.size
        val origSizeDocs = mDocs.size
        val origSizeLinks = mLinks.size
        val origSizePhotoAlbums = mPhotoAlbums.size
        val origSizePhotos = mPhotos.size
        val origSizeVideos = mVideos.size
        val origSizePostsWithComments = mPosts.size

        mAudios.clear()
        view?.notifyDataRemoved(AttachmentConversationType.AUDIO, 0, origSizeAudios)
        mDocs.clear()
        view?.notifyDataRemoved(AttachmentConversationType.DOC, 0, origSizeDocs)
        mLinks.clear()
        view?.notifyDataRemoved(AttachmentConversationType.LINK, 0, origSizeLinks)
        mPhotoAlbums.clear()
        view?.notifyDataRemoved(AttachmentConversationType.PHOTO_ALBUM, 0, origSizePhotoAlbums)
        mPhotos.clear()
        view?.notifyDataRemoved(AttachmentConversationType.PHOTO, 0, origSizePhotos)
        mVideos.clear()
        view?.notifyDataRemoved(AttachmentConversationType.VIDEO, 0, origSizeVideos)
        mPosts.clear()
        view?.notifyDataRemoved(
            AttachmentConversationType.POST,
            0,
            origSizePostsWithComments
        )

        view?.resolveEmptyText(currentPage)
    }

    private fun onActualDataReceived(offset: Int, data: List<Message>) {
        actualDataLoading = false
        endOfContent = data.isEmpty()
        actualDataReceived = true
        if (endOfContent) resumedView?.onSetLoadingStatus(2)
        if (offset == 0) {
            clearAllData()
            loaded = data.size
            update(data, true)
            resolveToolbar()
        } else {
            loaded += data.size
            update(data, true)
            resolveToolbar()
        }
        resolveRefreshingView()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        resumedView?.showRefreshing(actualDataLoading)
        if (!endOfContent) resumedView?.onSetLoadingStatus(
            if (actualDataLoading) 1 else 0
        )
    }

    fun firePlayAudio(context: Context, position: Int) {
        MusicPlaybackService.startForPlayList(context, mAudios, position)
        if (!Settings.get().main().isShow_mini_player) PlaceFactory.getPlayerPlace(accountId)
            .tryOpenWith(
                context
            )
    }

    private fun resolveToolbar() {
        view?.let {
            it.toolbarTitle(getString(R.string.attachments_in_chat))
            when (currentPage) {
                AttachmentConversationType.AUDIO -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.audios_count,
                            safeCountOf(mAudios)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }

                AttachmentConversationType.DOC -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.documents_count,
                            safeCountOf(mDocs)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }

                AttachmentConversationType.LINK -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.links_count,
                            safeCountOf(mLinks)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }

                AttachmentConversationType.PHOTO -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.photos_count,
                            safeCountOf(mPhotos)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }

                AttachmentConversationType.PHOTO_ALBUM -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.photo_albums_count,
                            safeCountOf(mPhotoAlbums)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }

                AttachmentConversationType.POST -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.posts_count,
                            safeCountOf(mPosts)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }

                AttachmentConversationType.VIDEO -> {
                    it.toolbarSubtitle(
                        getString(
                            R.string.videos_count,
                            safeCountOf(mVideos)
                        ) + " " + getString(R.string.messages_analized, loaded)
                    )
                }
            }
        }
    }

    fun firePhotoClick(position: Int) {
        if (FenrirNative.isNativeLoaded && Settings.get().main().isNative_parcel_photo) {
            view?.goToTempPhotosGallery(
                accountId,
                ParcelNative.createParcelableList(mPhotos, ParcelFlags.NULL_LIST),
                position
            )
        } else {
            val source = TmpSource(fireTempDataUsage(), 0)
            actualDataDisposable.add(
                Stores.instance
                    .tempStore()
                    .putTemporaryData(
                        source.ownerId,
                        source.sourceId,
                        mPhotos,
                        Serializers.PHOTOS_SERIALIZER
                    )
                    .fromIOToMain({
                        onPhotosSavedToTmpStore(
                            position,
                            source
                        )
                    }) { obj -> obj.printStackTrace() })
        }
    }

    private fun onPhotosSavedToTmpStore(index: Int, source: TmpSource) {
        view?.goToTempPhotosGallery(
            accountId,
            source,
            index
        )
    }

    override fun onDestroyed() {
        actualDataDisposable.cancel()
        super.onDestroyed()
    }

    fun fireScrollToEnd(button: Boolean): Boolean {
        if (!endOfContent && actualDataReceived && !actualDataLoading) {
            loadActualData(loaded)
            return false
        }
        if (button && endOfContent) resumedView?.onSetLoadingStatus(2)
        return true
    }

    fun fireRefresh() {
        actualDataDisposable.clear()
        actualDataLoading = false
        loadActualData(0)
    }

    fun firePostBodyClick(post: Post) {
        if (intValueIn(post.postType, VKApiPost.Type.SUGGEST, VKApiPost.Type.POSTPONE)) {
            view?.openPostEditor(
                accountId,
                post
            )
            return
        }
        firePostClick(post)
    }

    fun firePostRestoreClick(post: Post) {
        appendJob(
            fInteractor.restore(accountId, post.ownerId, post.vkid)
                .fromIOToMain(dummy()) { t ->
                    showError(t)
                })
    }

    fun fireLikeLongClick(post: Post) {
        view?.goToLikes(
            accountId,
            "post",
            post.ownerId,
            post.vkid
        )
    }

    fun fireShareLongClick(post: Post) {
        view?.goToReposts(
            accountId,
            "post",
            post.ownerId,
            post.vkid
        )
    }

    fun fireLikeClick(post: Post) {
        if (Settings.get().main().isDisable_likes || Utils.isHiddenAccount(
                accountId
            )
        ) {
            return
        }
        appendJob(
            fInteractor.like(accountId, post.ownerId, post.vkid, !post.isUserLikes)
                .fromIOToMain(dummy()) { t ->
                    showError(t)
                })
    }

    fun fireEditTrack(position: Int, artist: String, title: String?, audio: Audio) {
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.edit(
                accountId,
                audio.ownerId,
                audio.id,
                artist,
                title
            ).fromIOToMain({
                actualDataLoading = false
                resolveRefreshingView()
                if (position < mAudios.size && position >= 0) {
                    mAudios[position].setTitle(title).setArtist(artist)
                    view?.notifyAudioItemChanged(position)
                }
            }) {
                actualDataLoading = false
                resolveRefreshingView()

                showError(it)
            })
    }

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < mAudios.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    fun fireDelete(position: Int, audio: Audio) {
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.delete(accountId, audio.id, audio.ownerId).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()

                    if (position < mAudios.size && position >= 0) {
                        mAudios.removeAt(position)
                        view?.notifyAudioItemRemoved(position)
                    }

                    view?.customToast?.showToastSuccessBottom(R.string.deleted)
                }) {
                actualDataLoading = false
                resolveRefreshingView()

                view?.showThrowable(it)
            }
        )
    }

    fun fireAddTrack(audio: Audio) {
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.add(accountId, audio, null).fromIOToMain(
                {
                    actualDataLoading = false
                    resolveRefreshingView()
                    view?.customToast?.showToastSuccessBottom(R.string.added)
                }) {
                actualDataLoading = false
                resolveRefreshingView()
                view?.showThrowable(it)
            })
    }

    fun fireGetBitrate(audio: Audio) {
        actualDataLoading = true
        resolveRefreshingView()

        if (audio.needRefresh()) {
            appendJob(
                audioInteractor.getById(
                    accountId,
                    listOf(audio)
                ).fromIOToMain({
                    getBitrateTask(it[0])
                }) {
                    getBitrateTask(
                        audio
                    )
                })
        } else {
            getBitrateTask(audio)
        }
    }

    private fun getBitrateTask(audio: Audio) {
        val pUrl = audio.url
        if (pUrl.isNullOrEmpty()) {
            actualDataLoading = false
            resolveRefreshingView()
            return
        }
        appendJob(
            if (audio.isHLS) {
                M3U8(pUrl).length.fromIOToMain(
                    {
                        actualDataLoading = false
                        resolveRefreshingView()

                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                }
            } else {
                getLength(pUrl).fromIOToMain(
                    {
                        actualDataLoading = false
                        resolveRefreshingView()

                        view?.onShowBitrate(audio.duration, it)
                    }
                ) {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                }
            })
    }

    fun fireGetLyrics(audio: Audio) {
        actualDataLoading = true
        resolveRefreshingView()

        appendJob(
            audioInteractor.getLyrics(accountId, audio)
                .fromIOToMain({
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.onAudioLyricsReceived(
                        it,
                        audio
                    )
                }) {
                    actualDataLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
    }

    fun firePrepareDownload(position: Int, audio: Audio) {
        tmpDownloadPos = position
        tmpDownloadAudio = audio
        view?.onDownloadAudioNextStage()
    }

    fun continueDownload() {
        val position = tmpDownloadPos
        val audio = tmpDownloadAudio
        if (position < 0 || position > mAudios.size - 1 || audio == null) {
            return
        }
        view?.onStartAudioDownload(accountId, position, audio)
        tmpDownloadPos = -1
        tmpDownloadAudio = null
    }

    init {
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }

        loadActualData(0)
    }
}
