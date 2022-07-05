package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import dev.ragnarok.fenrir.Includes.gifPlayerFactory
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.media.gif.IGifPlayer
import dev.ragnarok.fenrir.media.gif.IGifPlayer.IStatusChangeListener
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.model.VideoSize
import dev.ragnarok.fenrir.mvp.view.IGifPagerView
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.DownloadWorkUtils.doDownloadDoc
import dev.ragnarok.fenrir.util.toast.CustomSnackbars

class GifPagerPresenter(
    accountId: Int,
    private val mDocuments: ArrayList<Document>,
    index: Int,
    savedInstanceState: Bundle?
) : BaseDocumentPresenter<IGifPagerView>(accountId, savedInstanceState), IStatusChangeListener,
    IGifPlayer.IVideoSizeChangeListener {
    private var mGifPlayer: IGifPlayer? = null
    private var mCurrentIndex = 0
    override fun saveState(outState: Bundle) {
        super.saveState(outState)
        outState.putInt(SAVE_PAGER_INDEX, mCurrentIndex)
    }

    fun fireSurfaceCreated(adapterPosition: Int) {
        if (mCurrentIndex == adapterPosition) {
            resolvePlayerDisplay()
        }
    }

    private fun resolveToolbarTitle() {
        view?.toolbarTitle(R.string.gif_player)
    }

    private fun resolveToolbarSubtitle() {
        view?.toolbarSubtitle(
            R.string.image_number,
            mCurrentIndex + 1,
            mDocuments.size
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
        val document = mDocuments[mCurrentIndex]
        val url = document.videoPreview?.src
        mGifPlayer = url?.let { gifPlayerFactory.createGifPlayer(it, true) }
        mGifPlayer?.addStatusChangeListener(this)
        mGifPlayer?.addVideoSizeChangeListener(this)
        try {
            mGifPlayer?.play()
        } catch (e: Exception) {
            showToast(
                R.string.unable_to_play_file,
                true
            )
        }
    }

    private fun selectPage(position: Int) {
        if (mCurrentIndex == position) {
            return
        }
        mCurrentIndex = position
        initGifPlayer()
    }

    private fun resolveAddDeleteButton() {
        view?.setupAddRemoveButton(!isMy)
    }

    private val isMy: Boolean
        get() = mDocuments[mCurrentIndex].ownerId == accountId

    private fun resolveAspectRatio() {
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

    override fun onGuiCreated(viewHost: IGifPagerView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(mDocuments.size, mCurrentIndex)
        resolvePreparingProgress()
        resolveAspectRatio()
        resolveAddDeleteButton()
        resolvePlayerDisplay()
        resolveToolbarTitle()
        resolveToolbarSubtitle()
    }

    fun firePageSelected(position: Int) {
        if (mCurrentIndex == position) {
            return
        }
        selectPage(position)
        resolveToolbarSubtitle()
        resolvePreparingProgress()
    }

    fun fireAddDeleteButtonClick() {
        val document = mDocuments[mCurrentIndex]
        if (isMy) {
            delete(document.id, document.ownerId)
        } else {
            addYourself(document)
        }
    }

    fun fireHolderCreate(adapterPosition: Int) {
        val isProgress =
            adapterPosition == mCurrentIndex && mGifPlayer?.playerStatus == IGifPlayer.IStatus.PREPARING
        val size = mGifPlayer?.videoSize ?: DEF_SIZE
        view?.configHolder(
            adapterPosition,
            isProgress,
            size.width,
            size.width
        )
    }

    fun fireShareButtonClick() {
        view?.shareDocument(
            accountId,
            mDocuments[mCurrentIndex]
        )
    }

    fun fireDownloadButtonClick(context: Context, view: View?) {
        if (!hasReadWriteStoragePermission(context)) {
            resumedView?.requestWriteExternalStoragePermission()
            return
        }
        downloadImpl(context, view)
    }

    public override fun onWritePermissionResolved(context: Context, view: View?) {
        if (hasReadWriteStoragePermission(context)) {
            downloadImpl(context, view)
        }
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
        mGifPlayer?.release()
        super.onDestroyed()
    }

    private fun downloadImpl(context: Context, view: View?) {
        val document = mDocuments[mCurrentIndex]
        if (doDownloadDoc(context, document, false) == 1) {
            CustomSnackbars.createCustomSnackbars(view)
                ?.setDurationSnack(BaseTransientBottomBar.LENGTH_LONG)
                ?.themedSnack(R.string.audio_force_download)
                ?.setAction(
                    R.string.button_yes
                ) {
                    doDownloadDoc(
                        context, document, true
                    )
                }?.show()
        }
    }

    override fun onPlayerStatusChange(player: IGifPlayer, previousStatus: Int, currentStatus: Int) {
        if (mGifPlayer === player) {
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