package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import dev.ragnarok.fenrir.App.Companion.instance
import dev.ragnarok.fenrir.Includes.gifPlayerFactory
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.media.gif.IGifPlayer
import dev.ragnarok.fenrir.media.gif.IGifPlayer.IStatusChangeListener
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoSize
import dev.ragnarok.fenrir.model.Story
import dev.ragnarok.fenrir.model.VideoSize
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IStoryPagerView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.requireNonNull
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadPhoto
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadVideo
import dev.ragnarok.fenrir.util.DownloadWorkUtils.makeLegalFilename
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import java.io.File
import java.util.*
import kotlin.math.abs

class StoryPagerPresenter(
    accountId: Int,
    private val mStories: ArrayList<Story>,
    index: Int,
    private val context: Context,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IStoryPagerView>(accountId, savedInstanceState),
    IStatusChangeListener, IGifPlayer.IVideoSizeChangeListener {
    private var mGifPlayer: IGifPlayer? = null
    private var mCurrentIndex = 0
    fun isStoryIsVideo(pos: Int): Boolean {
        return mStories[pos].photo == null && mStories[pos].video != null
    }

    fun getStory(pos: Int): Story {
        return mStories[pos]
    }

    override fun saveState(outState: Bundle) {
        super.saveState(outState)
        outState.putInt(SAVE_PAGER_INDEX, mCurrentIndex)
    }

    override fun onGuiCreated(viewHost: IStoryPagerView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mStories.size, mCurrentIndex)
        resolveToolbarTitle()
        resolvePlayerDisplay()
        resolveAspectRatio()
        resolvePreparingProgress()
        resolveToolbarSubtitle()
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
            mStories.size
        )
    }

    private fun resolvePlayerDisplay() {
        if (guiIsReady) {
            view?.attachDisplayToPlayer(
                mCurrentIndex,
                mGifPlayer
            )
        } else {
            mGifPlayer?.setDisplay(null)
        }
    }

    private fun initGifPlayer() {
        if (mGifPlayer != null) {
            val old: IGifPlayer? = mGifPlayer
            mGifPlayer = null
            old?.release()
        }
        val story = mStories[mCurrentIndex]
        if (story.video == null) {
            return
        }
        val url = firstNonEmptyString(
            story.video?.mp4link2160, story.video?.mp4link1440,
            story.video?.mp4link1080, story.video?.mp4link720, story.video?.mp4link480,
            story.video?.mp4link360, story.video?.mp4link240
        )
        if (url == null) {
            view?.showError(R.string.unable_to_play_file)
            return
        }
        mGifPlayer = gifPlayerFactory.createGifPlayer(url, false)
        mGifPlayer?.addStatusChangeListener(this)
        mGifPlayer?.addVideoSizeChangeListener(this)
        try {
            mGifPlayer?.play()
        } catch (e: Exception) {
            view?.showError(R.string.unable_to_play_file)
        }
    }

    private fun selectPage(position: Int) {
        if (mCurrentIndex == position) {
            return
        }
        mCurrentIndex = position
        initGifPlayer()
    }

    private val isMy: Boolean
        get() = mStories[mCurrentIndex].ownerId == accountId

    private fun resolveAspectRatio() {
        if (mGifPlayer == null) {
            return
        }
        val size = mGifPlayer?.videoSize
        if (size != null) {
            view?.setAspectRatioAt(
                mCurrentIndex,
                size.width,
                size.height
            )
        }
    }

    private fun resolvePreparingProgress() {
        val preparing =
            mGifPlayer != null && mGifPlayer?.playerStatus == IGifPlayer.IStatus.PREPARING
        view?.setPreparingProgressVisible(
            mCurrentIndex,
            preparing
        )
    }

    private fun resolveToolbarSubtitle() {
        view?.setToolbarSubtitle(
            mStories[mCurrentIndex],
            accountId
        )
    }

    fun firePageSelected(position: Int) {
        if (mCurrentIndex == position) {
            return
        }
        selectPage(position)
        resolveToolbarTitle()
        resolveToolbarSubtitle()
        resolvePreparingProgress()
    }

    fun fireHolderCreate(adapterPosition: Int) {
        if (!isStoryIsVideo(adapterPosition)) return
        val isProgress =
            adapterPosition == mCurrentIndex && (mGifPlayer == null || mGifPlayer?.playerStatus == IGifPlayer.IStatus.PREPARING)
        var size = if (mGifPlayer == null) null else mGifPlayer?.videoSize
        if (size == null) {
            size = DEF_SIZE
        }
        val finalSize = size
        view?.configHolder(
            adapterPosition,
            isProgress,
            finalSize.width,
            finalSize.width
        )
    }

    fun fireShareButtonClick() {
        val story = mStories[mCurrentIndex]
        view?.onShare(story, accountId)
    }

    fun fireDownloadButtonClick() {
        if (!hasReadWriteStoragePermission(instance)) {
            view?.requestWriteExternalStoragePermission()
            return
        }
        downloadImpl()
    }

    private fun onWritePermissionResolved() {
        if (hasReadWriteStoragePermission(instance)) {
            downloadImpl()
        }
    }

    fun fireWritePermissionResolved() {
        onWritePermissionResolved()
    }

    public override fun onGuiPaused() {
        super.onGuiPaused()
        mGifPlayer?.pause()
    }

    public override fun onGuiResumed() {
        super.onGuiResumed()
        if (mGifPlayer != null) {
            try {
                mGifPlayer?.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyed() {
        if (mGifPlayer != null) {
            mGifPlayer?.release()
        }
        super.onDestroyed()
    }

    private fun downloadImpl() {
        val story = mStories[mCurrentIndex]
        if (story.photo != null) doSaveOnDrive(story)
        if (story.video != null) {
            val url = firstNonEmptyString(
                story.video?.mp4link2160, story.video?.mp4link1440,
                story.video?.mp4link1080, story.video?.mp4link720, story.video?.mp4link480,
                story.video?.mp4link360, story.video?.mp4link240
            )
            story.video?.setTitle(story.owner?.fullName)
            url.nonNullNoEmpty {
                story.video.requireNonNull { s ->
                    doDownloadVideo(context, s, it, "Story")
                }
            }
        }
    }

    private fun doSaveOnDrive(photo: Story) {
        val dir = File(Settings.get().other().photoDir)
        if (!dir.isDirectory) {
            val created = dir.mkdirs()
            if (!created) {
                view?.showError("Can't create directory $dir")
                return
            }
        } else dir.setLastModified(Calendar.getInstance().time.time)
        photo.photo?.let {
            downloadResult(photo.owner?.fullName?.let { it1 ->
                makeLegalFilename(
                    it1,
                    null
                )
            }, dir, it)
        }
    }

    private fun transform_owner(owner_id: Int): String {
        return if (owner_id < 0) "club" + abs(owner_id) else "id$owner_id"
    }

    private fun downloadResult(Prefix: String?, dirF: File, photo: Photo) {
        var dir = dirF
        if (Prefix != null && Settings.get().other().isPhoto_to_user_dir) {
            val dir_final = File(dir.absolutePath + "/" + Prefix)
            if (!dir_final.isDirectory) {
                val created = dir_final.mkdirs()
                if (!created) {
                    view?.showError("Can't create directory $dir_final")
                    return
                }
            } else dir_final.setLastModified(Calendar.getInstance().time.time)
            dir = dir_final
        }
        val url = photo.getUrlForSize(PhotoSize.W, true)
        if (url != null) {
            doDownloadPhoto(
                context,
                url,
                dir.absolutePath,
                (if (Prefix != null) Prefix + "_" else "") + transform_owner(photo.ownerId) + "_" + photo.getObjectId()
            )
        }
    }

    override fun onPlayerStatusChange(player: IGifPlayer, previousStatus: Int, currentStatus: Int) {
        if (mGifPlayer === player) {
            if (currentStatus == IGifPlayer.IStatus.ENDED) {
                view?.onNext()
                return
            }
            resolvePreparingProgress()
            resolvePlayerDisplay()
        }
    }

    override fun onVideoSizeChanged(player: IGifPlayer, size: VideoSize) {
        if (mGifPlayer === player) {
            resolveAspectRatio()
        }
    }

    companion object {
        private const val SAVE_PAGER_INDEX = "save_pager_index"
        private val DEF_SIZE = VideoSize(1, 1)
    }

    init {
        mCurrentIndex = savedInstanceState?.getInt(SAVE_PAGER_INDEX) ?: index
        initGifPlayer()
    }
}