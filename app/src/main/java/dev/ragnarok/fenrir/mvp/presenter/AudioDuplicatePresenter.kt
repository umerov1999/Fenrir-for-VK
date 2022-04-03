package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.PlayerStatus
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter
import dev.ragnarok.fenrir.mvp.view.IAudioDuplicateView
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getBitrate
import dev.ragnarok.fenrir.util.Mp3InfoHelper.getLength
import dev.ragnarok.fenrir.util.RxUtils.applyObservableIOToMainSchedulers
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.hls.M3U8
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import io.reactivex.rxjava3.disposables.Disposable

class AudioDuplicatePresenter(
    private val accountId: Int,
    private val new_audio: Audio,
    private val old_audio: Audio,
    savedInstanceState: Bundle?
) : RxSupportPresenter<IAudioDuplicateView>(savedInstanceState) {
    private val mAudioInteractor = InteractorFactory.createAudioInteractor()
    private val mPlayerDisposable: Disposable
    private var oldBitrate: Int? = null
    private var newBitrate: Int? = null
    private var needShowBitrateButton = true
    private var audioListDisposable = Disposable.disposed()
    private val mp3AndBitrate: Unit
        get() {
            val mode = new_audio.needRefresh()
            if (mode.first) {
                audioListDisposable =
                    mAudioInteractor.getByIdOld(accountId, listOf(new_audio), mode.second).compose(
                        applySingleIOToMainSchedulers()
                    )
                        .subscribe({ t: List<Audio> -> getBitrate(t[0]) }) {
                            getBitrate(
                                new_audio
                            )
                        }
            } else {
                getBitrate(new_audio)
            }
        }

    private fun doBitrate(url: String): Single<Int> {
        return Single.create { v: SingleEmitter<Int> ->
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(url, HashMap())
                val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                if (bitrate != null) {
                    v.onSuccess((bitrate.toLong() / 1000).toInt())
                } else {
                    v.onError(Throwable("Can't receipt bitrate "))
                }
            } catch (e: RuntimeException) {
                v.onError(e)
            }
        }
    }

    private fun getBitrate(audio: Audio) {
        if (audio.url.isNullOrEmpty()) {
            return
        }
        if (audio.isHLS) {
            audioListDisposable = M3U8(audio.url).length.compose(applySingleIOToMainSchedulers())
                .subscribe({ r: Long ->
                    newBitrate = getBitrate(audio.duration, r)
                    view?.setNewBitrate(
                        newBitrate
                    )
                }) { t: Throwable -> onDataGetError(t) }
        } else if (!audio.isLocalServer) {
            audioListDisposable = getLength(audio.url).compose(applySingleIOToMainSchedulers())
                .subscribe({ r: Long ->
                    newBitrate = getBitrate(audio.duration, r)
                    view?.setNewBitrate(
                        newBitrate
                    )
                }) { t: Throwable -> onDataGetError(t) }
        } else {
            audioListDisposable = doBitrate(audio.url).compose(applySingleIOToMainSchedulers())
                .subscribe({ r: Int ->
                    newBitrate = r
                    view?.setNewBitrate(
                        newBitrate
                    )
                }) { t: Throwable -> onDataGetError(t) }
        }
    }

    @Suppress("DEPRECATION")
    private fun doLocalBitrate(context: Context, url: String): Single<Int> {
        return Single.create { v: SingleEmitter<Int> ->
            try {
                val cursor = context.contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.MediaColumns.DATA),
                    BaseColumns._ID + "=? ",
                    arrayOf(Uri.parse(url).lastPathSegment),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val retriever = MediaMetadataRetriever()
                    val fl =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                    retriever.setDataSource(fl)
                    cursor.close()
                    val bitrate =
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    if (bitrate != null) {
                        v.onSuccess((bitrate.toLong() / 1000).toInt())
                    } else {
                        v.onError(Throwable("Can't receipt bitrate "))
                    }
                } else {
                    v.onError(Throwable("Can't receipt bitrate "))
                }
            } catch (e: RuntimeException) {
                v.onError(e)
            }
        }
    }

    fun getBitrateAll(context: Context) {
        if (old_audio.url.isNullOrEmpty()) {
            return
        }
        needShowBitrateButton = false
        view?.updateShowBitrate(
            needShowBitrateButton
        )
        audioListDisposable = doLocalBitrate(context, old_audio.url).compose(
            applySingleIOToMainSchedulers()
        )
            .subscribe({ r: Int ->
                oldBitrate = r
                view?.setOldBitrate(
                    oldBitrate
                )
                mp3AndBitrate
            }) { t: Throwable -> onDataGetError(t) }
    }

    private fun onServiceBindEvent(@PlayerStatus status: Int) {
        when (status) {
            PlayerStatus.UPDATE_TRACK_INFO, PlayerStatus.SERVICE_KILLED, PlayerStatus.UPDATE_PLAY_PAUSE ->
                view?.displayData(
                    new_audio,
                    old_audio
                )
            PlayerStatus.REPEATMODE_CHANGED, PlayerStatus.SHUFFLEMODE_CHANGED, PlayerStatus.UPDATE_PLAY_LIST -> {}
        }
    }

    override fun onDestroyed() {
        mPlayerDisposable.dispose()
        audioListDisposable.dispose()
        super.onDestroyed()
    }

    override fun onGuiCreated(viewHost: IAudioDuplicateView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(new_audio, old_audio)
        viewHost.setNewBitrate(newBitrate)
        viewHost.setOldBitrate(oldBitrate)
        viewHost.updateShowBitrate(needShowBitrateButton)
    }

    private fun onDataGetError(t: Throwable) {
        view?.let {
            showError(
                it,
                Utils.getCauseIfRuntime(t)
            )
        }
    }

    init {
        mPlayerDisposable = observeServiceBinding()
            .compose(applyObservableIOToMainSchedulers())
            .subscribe { status: Int -> onServiceBindEvent(status) }
    }
}