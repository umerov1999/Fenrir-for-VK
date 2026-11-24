package dev.ragnarok.fenrir.fragment.audio.local.audioslocal

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import androidx.core.net.toUri
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.getString
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.media.music.MusicPlaybackService.Companion.startForPlayList
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.module.FileUtils
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.place.PlaceFactory.getPlayerPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.upload.IUploadManager
import dev.ragnarok.fenrir.upload.IUploadManager.IProgressUpdate
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadDestination
import dev.ragnarok.fenrir.upload.UploadDestination.Companion.forAudio
import dev.ragnarok.fenrir.upload.UploadDestination.Companion.forRemotePlay
import dev.ragnarok.fenrir.upload.UploadIntent
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.findIndexById
import dev.ragnarok.fenrir.util.Utils.safeCheck
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.delayedFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import java.io.File

class AudiosLocalPresenter(accountId: Long, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IAudiosLocalView>(accountId, savedInstanceState) {
    private val originAudios: ArrayList<Audio> = ArrayList()
    private val audios: ArrayList<Audio> = ArrayList()
    private val uploadManager: IUploadManager = Includes.uploadManager
    private val uploadsData: MutableList<Upload> = ArrayList(0)
    private val destination: UploadDestination = forAudio(accountId)
    private val remotePlay: UploadDestination = forRemotePlay()
    private var serviceStatusJob = CancelableJob()
    private var actualReceived = false
    private var loadingNow = false
    private var query: String? = null
    private var errorPermissions = false
    private var doAudioLoadTabs = false
    private var bucketId: Int
    fun fireBucketSelected(bucketId: Int) {
        this.bucketId = bucketId
        fireRefresh()
    }

    fun firePrepared() {
        appendJob(
            uploadManager[accountId, destination]
                .fromIOToMain { data -> onUploadsDataReceived(data) })
        appendJob(
            uploadManager.observeAdding()
                .sharedFlowToMain { added -> onUploadsAdded(added) })
        appendJob(
            uploadManager.observeDeleting(true)
                .sharedFlowToMain { ids -> onUploadDeleted(ids) })
        appendJob(
            uploadManager.observeResults()
                .filter {
                    destination.compareTo(
                        it.first.destination
                    ) || remotePlay.compareTo(it.first.destination)
                }
                .sharedFlowToMain { pair -> onUploadResults(pair) })
        appendJob(
            uploadManager.observeStatus()
                .sharedFlowToMain { upload -> onUploadStatusUpdate(upload) })
        appendJob(
            uploadManager.observeProgress()
                .sharedFlowToMain { updates -> onProgressUpdates(updates) })
        fireRefresh()
    }

    fun fireLocalAudioAlbums() {
        view?.goToLocalAudioAlbums(bucketId)
    }

    private fun setLoadingNow(loadingNow: Boolean) {
        this.loadingNow = loadingNow
        resolveRefreshingView()
    }

    private fun checkTitleArtists(data: Audio, q: String): Boolean {
        val r = q.split(Regex("( - )|( )|( {2})"), 2).toTypedArray()
        return r.size >= 2 && (safeCheck(
            data.artist
        ) {
            data.artist?.lowercase(Utils.appLocale)?.contains(
                r[0].lowercase(
                    Utils.appLocale
                )
            ) == true
        }
                && safeCheck(
            data.title
        ) {
            data.title?.lowercase(Utils.appLocale)?.contains(
                r[1].lowercase(
                    Utils.appLocale
                )
            ) == true
        })
    }

    private fun updateCriteria() {
        setLoadingNow(true)
        audios.clear()
        if (query.isNullOrEmpty()) {
            audios.addAll(originAudios)
            setLoadingNow(false)
            view?.notifyListChanged()
            return
        }
        query?.let {
            val locale = Utils.appLocale
            for (i in originAudios) {
                if (safeCheck(i.title) {
                        i.title?.lowercase(locale)?.contains(
                            it.lowercase(locale)
                        ) == true
                    }
                    || safeCheck(i.artist) {
                        i.artist?.lowercase(locale)?.contains(
                            it.lowercase(locale)
                        ) == true
                    } || checkTitleArtists(i, it)
                ) {
                    audios.add(i)
                }
            }
        }
        setLoadingNow(false)
        view?.notifyListChanged()
    }

    fun fireQuery(q: String?) {
        query = if (q.isNullOrEmpty()) null else {
            q
        }
        updateCriteria()
    }

    override fun onGuiResumed() {
        super.onGuiResumed()
        resolveRefreshingView()
        doAudioLoadTabs = if (doAudioLoadTabs) {
            return
        } else {
            true
        }
        view?.checkPermission()
    }

    private fun resolveRefreshingView() {
        resumedView?.displayRefreshing(
            loadingNow
        )
    }

    private fun requestList() {
        setLoadingNow(true)
        if (bucketId == 0) {
            appendJob(
                Stores.instance
                    .localMedia()
                    .getAudios(accountId)
                    .fromIOToMain({ onListReceived(it) }) {
                        onListGetError(
                            it
                        )
                    })
        } else {
            appendJob(
                Stores.instance
                    .localMedia()
                    .getAudios(accountId, bucketId.toLong())
                    .fromIOToMain({ onListReceived(it) }) {
                        onListGetError(
                            it
                        )
                    })
        }
    }

    private fun onListReceived(data: List<Audio>) {
        if (data.isEmpty()) {
            actualReceived = true
            setLoadingNow(false)
            return
        }
        originAudios.clear()
        actualReceived = true
        originAudios.addAll(data)
        updateCriteria()
        setLoadingNow(false)
    }

    fun firePlayAudio(context: Context, position: Int) {
        startForPlayList(context, audios, position)
        if (!Settings.get().main().isShow_mini_player) getPlayerPlace(accountId).tryOpenWith(
            context
        )
    }

    fun fireDelete(position: Int) {
        audios.removeAt(position)
        view?.notifyItemRemoved(position)
    }

    override fun onDestroyed() {
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    fun fireRemoveClick(upload: Upload) {
        uploadManager.cancel(upload.getObjectId())
    }

    private fun onListGetError(t: Throwable) {
        setLoadingNow(false)
        showError(
            t
        )
    }

    fun fireFileForUploadSelected(file: String?) {
        val intent = UploadIntent(accountId, destination)
            .setAutoCommit(true)
            .setFileUri(file?.toUri())
        uploadManager.enqueue(listOf(intent))
    }

    fun fireFileForRemotePlaySelected(file: String?) {
        val intent = UploadIntent(accountId, remotePlay)
            .setAutoCommit(true)
            .setFileUri(file?.toUri())
        uploadManager.enqueue(listOf(intent))
    }

    fun getAudioPos(audio: Audio?): Int {
        if (audios.isNotEmpty() && audio != null) {
            for ((pos, i) in audios.withIndex()) {
                if (i.id == audio.id && i.ownerId == audio.ownerId) {
                    i.isAnimationNow = true
                    view?.notifyItemChanged(
                        pos
                    )
                    return pos
                }
            }
        }
        return -1
    }

    fun firePermissionsCanceled() {
        errorPermissions = true
    }

    fun fireRefresh() {
        if (loadingNow) {
            return
        }
        if (errorPermissions) {
            errorPermissions = false
            view?.checkPermission()
            return
        }
        requestList()
    }

    fun fireScrollToEnd() {
        if (actualReceived) {
            requestList()
        }
    }

    override fun onGuiCreated(viewHost: IAudiosLocalView) {
        super.onGuiCreated(viewHost)
        viewHost.displayList(audios)
        viewHost.displayUploads(uploadsData)
        resolveUploadDataVisibility()
    }

    private fun onUploadsDataReceived(data: List<Upload>) {
        uploadsData.clear()
        uploadsData.addAll(data)
        resolveUploadDataVisibility()
    }

    private fun onUploadResults(pair: Pair<Upload, UploadResult<*>>) {
        val obj = pair.second.result as Audio
        if (obj.id == 0) view?.customToast?.showToastError(
            R.string.error
        )
        else {
            view?.customToast?.showToast(
                R.string.uploaded
            )
        }
    }

    private fun onProgressUpdates(updates: IProgressUpdate?) {
        updates?.let { update ->
            val index = findIndexById(uploadsData, update.id)
            if (index != -1) {
                view?.notifyUploadProgressChanged(
                    index,
                    update.progress,
                    true
                )
            }
        }
    }

    private fun onUploadStatusUpdate(upload: Upload) {
        val index = findIndexById(uploadsData, upload.getObjectId())
        if (index != -1) {
            view?.notifyUploadItemChanged(
                index
            )
        }
    }

    private fun onUploadsAdded(added: List<Upload>) {
        var count = 0
        val cur = uploadsData.size
        for (u in added) {
            if (destination.compareTo(u.destination)) {
                val index = findIndexById(uploadsData, u.getObjectId())
                if (index == -1) {
                    uploadsData.add(u)
                    count++
                }
            }
        }
        if (count > 0) {
            view?.notifyUploadItemsAdded(cur, count)
        }
        resolveUploadDataVisibility()
    }

    private fun onUploadDeleted(ids: IntArray) {
        for (id in ids) {
            val index = findIndexById(uploadsData, id)
            if (index != -1) {
                uploadsData.removeAt(index)
                view?.notifyUploadItemRemoved(
                    index
                )
            }
        }
        resolveUploadDataVisibility()
    }

    private fun resolveUploadDataVisibility() {
        view?.setUploadDataVisible(uploadsData.isNotEmpty())
    }

    fun fireOpenMenu(position: Int, audio: Audio) {
        if (position < audios.size && position >= 0) {
            view?.onOpenMenu(accountId, position, audio)
        }
    }

    private fun doLocalBitrate(context: Context, url: String): Flow<Pair<Int, Long>> {
        return flow {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA),
                BaseColumns._ID + "=? ",
                arrayOf(url.toUri().lastPathSegment),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val retriever = MediaMetadataRetriever()
                val fl =
                    cursor.getString(MediaStore.MediaColumns.DATA)
                retriever.setDataSource(fl)
                cursor.close()
                val bitrate =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                if (bitrate != null && fl != null) {
                    emit(Pair((bitrate.toLong() / 1000).toInt(), File(fl).length()))
                } else {
                    throw Throwable("Can't receipt bitrate!")
                }
            } else {
                throw Throwable("Can't receipt bitrate!")
            }
        }
    }

    private fun stripMetadata(context: Context, url: String): Flow<Boolean> {
        return flow {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA),
                BaseColumns._ID + "=? ",
                arrayOf(url.toUri().lastPathSegment),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val fl =
                    cursor.getString(MediaStore.MediaColumns.DATA)
                cursor.close()
                if (fl.nonNullNoEmpty()) {
                    if (fl.contains(".m4a")) {
                        if (FileUtils.videoTagStrip(fl)) {
                            emit(true)
                        } else {
                            throw Throwable("Can't strip metadata")
                        }
                    } else {
                        if (FileUtils.audioTagStrip(fl)) {
                            emit(true)
                        } else {
                            throw Throwable("Can't strip metadata")
                        }
                    }
                } else {
                    throw Throwable("Can't find file")
                }
            } else {
                throw Throwable("Can't find file")
            }
        }
    }

    private fun updateAudioTime(context: Context, url: String): Flow<Boolean> {
        return flow {
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns.DATA),
                BaseColumns._ID + "=? ",
                arrayOf(url.toUri().lastPathSegment),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val fl =
                    cursor.getString(MediaStore.MediaColumns.DATA)
                cursor.close()
                if (fl.nonNullNoEmpty()) {
                    val file = File(fl)
                    if (file.exists()) {
                        if (file.setLastModified(System.currentTimeMillis())) {
                            context.sendBroadcast(
                                @Suppress("deprecation")
                                Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    file.toUri()
                                )
                            )
                            emit(true)
                        } else {
                            throw Throwable("Can't update time $fl")
                        }
                    } else {
                        throw Throwable("Can't find file $fl")
                    }
                } else {
                    throw Throwable("Can't find file")
                }
            } else {
                throw Throwable("Can't find file")
            }
        }
    }

    fun fireGetLocalBitrate(context: Context, url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        setLoadingNow(true)
        appendJob(
            doLocalBitrate(context, url).fromIOToMain(
                {
                    setLoadingNow(false)
                    view?.onShowBitrate(it.first, it.second)
                }
            ) {
                setLoadingNow(false)
                view?.showThrowable(it)
            })
    }

    fun fireStripMetadata(context: Context, url: String?) {
        if (url.isNullOrEmpty()) {
            return
        }
        setLoadingNow(true)
        appendJob(
            stripMetadata(context, url).fromIOToMain(
                {
                    setLoadingNow(false)
                    view?.showSuccess()
                    fireRefresh()
                },
                {
                    setLoadingNow(false)
                    view?.showThrowable(it)
                }
            ))
    }

    fun fireUpdateAudioTime(context: Context, audio: Audio, position: Int) {
        val url = audio.url
        if (url.isNullOrEmpty()) {
            return
        }
        setLoadingNow(true)
        appendJob(
            updateAudioTime(context, url).delayedFlow(500).fromIOToMain(
                {
                    setLoadingNow(false)
                    audios.removeAt(position)
                    audios.add(0, audio)
                    view?.notifyItemMoved(position, 0)
                    view?.showSuccess()
                },
                {
                    setLoadingNow(false)
                    view?.showThrowable(it)
                }
            ))
    }

    init {
        bucketId = if (Settings.get().main().isRememberLocalAudioAlbum) {
            Settings.get().main().currentLocalAudioAlbum
        } else {
            0
        }
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }
}