package dev.ragnarok.fenrir.activity.shortvideopager

import android.os.Bundle
import dev.ragnarok.fenrir.App.Companion.instance
import dev.ragnarok.fenrir.Includes.storyPlayerFactory
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.IVideosInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.media.story.IStoryPlayer
import dev.ragnarok.fenrir.media.story.IStoryPlayer.IStatusChangeListener
import dev.ragnarok.fenrir.model.Commented
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.model.VideoSize
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain

class ShortVideoPagerPresenter(
    accountId: Long,
    private val ownerId: Long?,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IShortVideoPagerView>(accountId, savedInstanceState),
    IStatusChangeListener, IStoryPlayer.IVideoSizeChangeListener,
    IStoryPlayer.IPositionChangeListener {
    private var mShortVideoPlayer: IStoryPlayer? = null
    private val mShortVideos: ArrayList<Video> = ArrayList()
    private val shortVideosInteractor = InteractorFactory.story
    private val interactor: IVideosInteractor = InteractorFactory.video
    private var mCurrentIndex = -1
    private var nextFrom: String? = null
    private var isEndContent: Boolean = false
    private var loadingNow = false
    private var currentPosition = 0L
    private var currentSpeed = 1f

    override fun onGuiCreated(viewHost: IShortVideoPagerView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mShortVideos.size, mCurrentIndex)
        viewHost.displayListLoading(loadingNow)
        resolveToolbarTitle()
        resolvePlayerDisplay()
        resolveAspectRatio()
        resolvePreparingProgress()
        resolveToolbarSubtitle()
        resolveDuration()
        resolveSpeed()
    }

    fun fireTogglePlaybackSpeed() {
        mShortVideoPlayer?.togglePlaybackSpeed()
        currentSpeed = mShortVideoPlayer?.playbackSpeed ?: 1f
        resolveSpeed()
    }

    private fun receiveShortVideos() {
        loadingNow = true
        view?.displayListLoading(loadingNow)
        appendJob(
            shortVideosInteractor.getShortVideos(accountId, ownerId, nextFrom, 25).fromIOToMain(
                { (first, second) -> onActualShortVideosReceived(nextFrom, first, second) },
                {
                    loadingNow = false
                    view?.displayListLoading(loadingNow)
                    view?.showThrowable(it)
                })
        )
    }

    private fun onActualShortVideosReceived(
        startFrom: String?,
        shortVideos: List<Video>,
        nextFrom: String?
    ) {
        loadingNow = false
        view?.displayListLoading(loadingNow)
        this.nextFrom = nextFrom
        isEndContent = nextFrom.isNullOrEmpty()
        if (startFrom.isNullOrEmpty()) {
            if (shortVideos.isEmpty()) {
                return
            }
            mCurrentIndex = 0
            mShortVideos.clear()
            mShortVideos.addAll(shortVideos)
            view?.updateCount(mShortVideos.size)
            view?.notifyDataSetChanged()
            initShortVideosPlayer()
            resolveToolbarTitle()
            resolveToolbarSubtitle()
            resolveDuration()
        } else {
            val startSize = mShortVideos.size
            mShortVideos.addAll(shortVideos)
            view?.updateCount(mShortVideos.size)
            view?.notifyDataAdded(
                startSize,
                shortVideos.size
            )
            resolveToolbarTitle()
            resolveToolbarSubtitle()
            resolveDuration()
        }
    }

    fun fireSurfaceCreated(adapterPosition: Int) {
        if (mCurrentIndex == adapterPosition) {
            resolvePlayerDisplay()
        }
    }

    private fun resolveToolbarTitle() {
        view?.setToolbarTitle(
            R.string.image_number,
            mCurrentIndex + 1,
            mShortVideos.size
        )
    }

    private fun resolvePlayerDisplay() {
        if (guiIsReady) {
            view?.attachDisplayToPlayer(
                mCurrentIndex,
                mShortVideoPlayer
            )
        } else {
            mShortVideoPlayer?.setVideoTextureView(null)
        }
    }

    private fun initShortVideosPlayer() {
        val update: Boolean = mShortVideoPlayer != null
        val shortVideo = mShortVideos.getOrNull(mCurrentIndex)
        val url = firstNonEmptyString(
            shortVideo?.mp4link2160, shortVideo?.mp4link1440,
            shortVideo?.mp4link1080, shortVideo?.mp4link720, shortVideo?.mp4link480,
            shortVideo?.mp4link360, shortVideo?.mp4link240
        )
        if (url == null) {
            view?.showError(R.string.unable_to_play_file)
            return
        }
        if (!update) {
            mShortVideoPlayer = storyPlayerFactory.createStoryPlayer(url, false)
            mShortVideoPlayer?.playbackSpeed = currentSpeed
            mShortVideoPlayer?.addPositionChangeListener(this)
            mShortVideoPlayer?.addStatusChangeListener(this)
            mShortVideoPlayer?.addVideoSizeChangeListener(this)
        } else {
            mShortVideoPlayer?.updateSource(url)
        }
        try {
            mShortVideoPlayer?.play()
        } catch (_: Exception) {
            view?.showError(R.string.unable_to_play_file)
        }
    }

    private fun selectPage(position: Int) {
        if (mCurrentIndex == position) {
            return
        }
        mCurrentIndex = position
        initShortVideosPlayer()
    }

    private fun resolveAspectRatio() {
        if (mShortVideoPlayer == null) {
            return
        }
        val size = mShortVideoPlayer?.videoSize
        if (size != null) {
            view?.setAspectRatioAt(
                mCurrentIndex,
                size.width.coerceAtLeast(1),
                size.height.coerceAtLeast(1)
            )
        } else {
            view?.setAspectRatioAt(
                mCurrentIndex,
                1,
                1
            )
        }
    }

    private fun resolvePreparingProgress() {
        val preparing =
            mShortVideoPlayer != null && mShortVideoPlayer?.playerStatus == IStoryPlayer.IStatus.PREPARING
        view?.setPreparingProgressVisible(
            mCurrentIndex,
            preparing
        )
    }

    private fun resolveToolbarSubtitle() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        view?.setToolbarSubtitle(
            curVideo,
            accountId
        )
    }

    private fun resolveDuration() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        view?.onUpdateDuration(
            0L.coerceAtLeast(curVideo.duration - currentPosition)
        )
    }

    private fun resolveSpeed() {
        view?.onUpdateSpeed(
            mShortVideoPlayer?.playbackSpeed ?: 1f
        )
    }

    fun firePageSelected(position: Int) {
        if (mCurrentIndex == position) {
            return
        }
        selectPage(position)
        resolveToolbarTitle()
        resolveToolbarSubtitle()
        resolveDuration()
        resolvePreparingProgress()
        if (mCurrentIndex >= mShortVideos.size - 1 && !isEndContent && !loadingNow) {
            receiveShortVideos()
        }
    }

    fun fireHolderCreate(adapterPosition: Int) {
        val isProgress =
            adapterPosition == mCurrentIndex && (mShortVideoPlayer == null || mShortVideoPlayer?.playerStatus == IStoryPlayer.IStatus.PREPARING)
        val isPlaying =
            adapterPosition == mCurrentIndex && mShortVideoPlayer?.playerStatus == IStoryPlayer.IStatus.PREPARED
        var size = if (mShortVideoPlayer == null) null else mShortVideoPlayer?.videoSize
        if (size == null) {
            size = DEF_SIZE
        }
        if (size.width <= 0) {
            size.setWidth(1)
        }
        if (size.height <= 0) {
            size.setHeight(1)
        }
        view?.configHolder(
            adapterPosition,
            isProgress,
            isPlaying,
            size.width,
            size.width
        )
    }

    fun getVideo(pos: Int): Video? {
        return mShortVideos.getOrNull(pos)
    }

    fun fireCommentsClick() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        val commented = Commented.from(curVideo)
        view?.showComments(
            accountId,
            commented
        )
    }

    private fun onLikesResponse(count: Int, userLikes: Boolean, index: Int) {
        mShortVideos.getOrNull(index)?.let {
            it.setLikesCount(count)
            it.setUserLikes(userLikes)
            view?.displayLikes(
                count,
                userLikes
            )
        }
    }

    fun fireLikeClick() {
        if (Settings.get().main().isDisable_likes || Utils.isHiddenAccount(
                accountId
            )
        ) {
            return
        }
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        val add = !curVideo.isUserLikes
        appendJob(
            interactor.likeOrDislike(
                accountId,
                curVideo.ownerId,
                curVideo.id,
                curVideo.accessKey,
                add
            )
                .fromIOToMain(
                    { pair ->
                        onLikesResponse(
                            pair.first,
                            pair.second,
                            mCurrentIndex
                        )
                    }
                ) { showError(it) })
    }

    fun fireLikeLongClick() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        view?.goToLikes(
            accountId,
            "video",
            curVideo.ownerId,
            curVideo.id
        )
    }

    fun fireShareButtonClick() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        view?.onShare(curVideo, accountId)
    }

    fun fireDownloadButtonClick() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        if (!hasReadWriteStoragePermission(instance)) {
            view?.requestWriteExternalStoragePermission()
            return
        }
        downloadImpl(curVideo)
    }

    private fun onWritePermissionResolved() {
        val curVideo = mShortVideos.getOrNull(mCurrentIndex) ?: return
        if (hasReadWriteStoragePermission(instance)) {
            downloadImpl(curVideo)
        }
    }

    fun fireWritePermissionResolved() {
        onWritePermissionResolved()
    }

    override fun onGuiPaused() {
        super.onGuiPaused()
        mShortVideoPlayer?.pause()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        if (mShortVideoPlayer != null) {
            try {
                mShortVideoPlayer?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyed() {
        if (mShortVideoPlayer != null) {
            mShortVideoPlayer?.release()
        }
        super.onDestroyed()
    }

    private fun downloadImpl(shortVideo: Video) {
        val url = firstNonEmptyString(
            shortVideo.mp4link2160, shortVideo.mp4link1440,
            shortVideo.mp4link1080, shortVideo.mp4link720, shortVideo.mp4link480,
            shortVideo.mp4link360, shortVideo.mp4link240
        )
        shortVideo.setTitle(shortVideo.optionalOwner?.fullName)
        url.nonNullNoEmpty {
            view?.downloadVideo(shortVideo, it, "ShortVideo")
        }
    }

    override fun onPlayerStatusChange(
        player: IStoryPlayer,
        previousStatus: Int,
        currentStatus: Int
    ) {
        if (mShortVideoPlayer === player) {
            if (currentStatus == IStoryPlayer.IStatus.ENDED) {
                view?.onNext()
                return
            }
            resolvePreparingProgress()
            resolvePlayerDisplay()
        }
    }

    override fun onVideoSizeChanged(player: IStoryPlayer, size: VideoSize) {
        if (mShortVideoPlayer === player) {
            resolveAspectRatio()
        }
    }

    override fun onPlayerPositionChange(
        player: IStoryPlayer,
        previousPosition: Long,
        currentPosition: Long
    ) {
        if (mShortVideoPlayer === player) {
            this.currentPosition = currentPosition
            resolveDuration()
        }
    }

    companion object {
        private val DEF_SIZE = VideoSize(1, 1)
    }

    init {
        receiveShortVideos()
    }
}
