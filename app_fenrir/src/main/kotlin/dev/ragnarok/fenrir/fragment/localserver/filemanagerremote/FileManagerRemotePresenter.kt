package dev.ragnarok.fenrir.fragment.localserver.filemanagerremote

import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.LinearLayoutManagerSavedState
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.interfaces.ILocalServerInteractor
import dev.ragnarok.fenrir.fragment.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.link.VKLinkParser
import dev.ragnarok.fenrir.media.music.MusicPlaybackController.observeServiceBinding
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.FileRemote
import dev.ragnarok.fenrir.model.FileType
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.PhotoSizes
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.module.parcel.ParcelFlags
import dev.ragnarok.fenrir.module.parcel.ParcelNative
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.coroutines.CancelableJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.isActive
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import kotlinx.coroutines.flow.flow

class FileManagerRemotePresenter(
    accountId: Long,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IFileManagerRemoteView>(accountId, savedInstanceState) {
    private val fileList: ArrayList<FileRemote> = ArrayList()
    private val fileListSearch: ArrayList<FileRemote> = ArrayList()
    private val fInteractor: ILocalServerInteractor =
        InteractorFactory.localServer
    private var isLoading = false
    private val directoryScrollPositions = HashMap<String?, Pair<Parcelable, Int>>()
    private var q: String? = null
    private var path: ArrayList<String> = ArrayList()
    private var serviceStatusJob = CancelableJob()

    private var tmpDownloadAudio: Audio? = null
    private var tmpDownloadPos = -1

    private inline fun <reified T> join(
        tokens: Iterable<T>?,
        delimiter: String?
    ): String? {
        if (tokens == null) {
            return null
        }
        val sb = StringBuilder()
        var firstTime = true
        for (token in tokens) {
            if (firstTime) {
                firstTime = false
            } else {
                sb.append(delimiter)
            }
            sb.append(token)
        }
        return sb.toString()
    }

    fun canRefresh(): Boolean {
        return q == null
    }

    private fun buildPath(): String? {
        return join(path, "/")
    }

    fun doSearch(str: String?) {
        if (isLoading) {
            return
        }
        var query: String? = str?.trim()
        if (query.isNullOrEmpty()) {
            query = null
        }
        if (query == this.q) {
            return
        }
        val locale = Utils.appLocale
        q = query
        if (query == null) {
            fileListSearch.clear()
            view?.resolveEmptyText(fileList.isEmpty())
            view?.displayData(fileList)
            view?.updatePathString(buildPath())
        } else {
            fileListSearch.clear()
            for (i in fileList) {
                if (i.file_name.isNullOrEmpty()) {
                    continue
                }
                if (i.file_name?.lowercase(locale)
                        ?.contains(query.lowercase(locale)) == true
                ) {
                    fileListSearch.add(i)
                }
            }
            view?.resolveEmptyText(fileListSearch.isEmpty())
            view?.displayData(fileListSearch)
            view?.updatePathString(query)
        }
    }

    override fun onGuiCreated(viewHost: IFileManagerRemoteView) {
        super.onGuiCreated(viewHost)
        (if (q == null) buildPath() else q)?.let { viewHost.updatePathString(it) }
        viewHost.displayData(if (q == null) fileList else fileListSearch)
        viewHost.updatePathString(buildPath())
        viewHost.resolveEmptyText(fileList.isEmpty())
        resolveRefreshingView()
    }

    private fun resolveRefreshingView() {
        view?.resolveLoading(
            isLoading
        )
    }

    override fun onDestroyed() {
        serviceStatusJob.cancel()
        super.onDestroyed()
    }

    fun backupDirectoryScroll(scroll: Parcelable, appVerticalOffset: Int) {
        directoryScrollPositions[buildPath()] = Pair(scroll, appVerticalOffset)
    }

    fun loadUp() {
        if (q != null) {
            q = null
            view?.updatePathString(buildPath())
            view?.displayData(fileList)
            view?.resolveEmptyText(fileList.isEmpty())
            return
        }
        path.removeLastOrNull()
        view?.updatePathString(buildPath())
        loadFiles()
    }

    fun canLoadUp(): Boolean {
        return q != null || path.nonNullNoEmpty()
    }

    fun onClickFile(item: FileRemote) {
        if (item.type == FileType.photo) {
            if (!FenrirNative.isNativeLoaded) {
                return
            }
            appendJob(
                flow {
                    val list = if (q == null) fileList else fileListSearch
                    var index = 0
                    var o = 0
                    val mem = ParcelNative.create(ParcelFlags.NULL_LIST)
                    for (i in list) {
                        if (!isActive()) {
                            mem.forceDestroy()
                            return@flow
                        }
                        if (i.type != FileType.photo) {
                            continue
                        }
                        if (i.id == item.id && i.owner_Id == item.owner_Id) {
                            index = o
                        }
                        val photo = Photo()
                        photo.setId(i.id)
                        photo.setOwnerId(i.owner_Id)
                        photo.setAlbumId(-311)
                        photo.setDate(i.modification_time)
                        photo.setSizes(
                            PhotoSizes().setW(PhotoSizes.Size(2080, 1080, i.url))
                                .setS(PhotoSizes.Size(512, 512, i.preview_url))
                        )
                        photo.setText(i.file_name)
                        mem.writeParcelable(photo)
                        o++
                    }
                    mem.writeFirstInt(o)
                    if (!isActive()) {
                        mem.forceDestroy()
                    } else {
                        emit(Pair(mem.nativePointer, index))
                    }
                }.fromIOToMain({
                    view?.displayGalleryUnSafe(
                        accountId,
                        it.first, it.second, false
                    )
                }) { obj -> obj.printStackTrace() })
        } else if (item.type == FileType.video) {
            val v = Video()
            v.setId(item.id)
            v.setOwnerId(item.owner_Id)
            v.setDate(item.modification_time)
            v.setTitle(item.file_name)
            v.setMp4link720(item.url)
            v.setDuration(item.size)
            view?.displayVideo(v)
        } else if (item.type == FileType.audio) {
            val list = if (q == null) fileList else fileListSearch
            var index = 0
            var o = 0
            val mAudios: ArrayList<Audio> = ArrayList()
            for (i in list) {
                if (i.type != FileType.audio) {
                    continue
                }
                if (i.id == item.id && i.owner_Id == item.owner_Id) {
                    index = o
                }
                val audio = Audio()
                audio.setId(i.id)
                audio.setOwnerId(i.owner_Id)
                audio.setUrl(i.url)
                audio.setThumb_image_little(i.preview_url)
                audio.setThumb_image_big(i.preview_url)
                audio.setThumb_image_very_big(i.preview_url)
                audio.setDuration(i.size.toInt())

                var trackName: String =
                    i.file_name?.replace(".mp3", "") ?: ""
                val artist: String
                val arr = trackName.split(Regex(" - ")).toTypedArray()
                if (arr.size > 1) {
                    artist = arr[0]
                    trackName = trackName.replace("$artist - ", "")
                } else {
                    artist = ""
                }
                audio.setArtist(artist)
                audio.setTitle(trackName)
                audio.setIsLocalServer()

                mAudios.add(audio)
                o++
            }
            view?.startPlayAudios(accountId, mAudios, index)
        }
    }

    fun fireOpenAudioMenu(
        position: Int,
        audio: FileRemote
    ) {
        val ent = if (q == null) fileList else fileListSearch
        if (position < ent.size && position >= 0) {
            view?.doAudioMenu(accountId, position, audio)
        }
    }

    fun fireOpenFileMenu(
        position: Int,
        file: FileRemote
    ) {
        val ent = if (q == null) fileList else fileListSearch
        if (position < ent.size && position >= 0) {
            view?.doFileMenu(accountId, position, file)
        }
    }

    fun firePrepareDownload(position: Int, audio: Audio) {
        tmpDownloadPos = position
        tmpDownloadAudio = audio
        view?.onDownloadAudioNextStage()
    }

    fun continueDownload() {
        val position = tmpDownloadPos
        val audio = tmpDownloadAudio
        val ent = if (q == null) fileList else fileListSearch
        if (position < 0 || position > ent.size - 1 || audio == null) {
            return
        }
        view?.onStartAudioDownload(accountId, position, audio)
        tmpDownloadPos = -1
        tmpDownloadAudio = null
    }

    fun fireUpdateTime(file: FileRemote) {
        val hash = VKLinkParser.parseLocalServerURL(file.url)
        if (hash.isNullOrEmpty()) {
            return
        }
        isLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.updateTime(hash).fromIOToMain(
                {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showSuccess()
                    loadFiles()
                }, {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun firePrepareEditFileName(file: FileRemote) {
        val hash = VKLinkParser.parseLocalServerURL(file.url)
        if (hash.isNullOrEmpty()) {
            return
        }
        isLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.getFileName(hash).fromIOToMain(
                {
                    isLoading = false
                    resolveRefreshingView()

                    view?.displayEditFileNameDialog(hash, it)
                }, {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun firEditFileName(hash: String, newFileName: String) {
        isLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.updateFileName(hash, newFileName).fromIOToMain(
                {
                    isLoading = false
                    resolveRefreshingView()
                    view?.showSuccess()
                    loadFiles()
                }, {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun fireDeleteFile(position: Int, file: FileRemote) {
        val ent = if (q == null) fileList else fileListSearch
        if (position >= ent.size || position < 0) {
            return
        }
        val hash = VKLinkParser.parseLocalServerURL(file.url)
        if (hash.isNullOrEmpty()) {
            return
        }
        isLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.deleteMedia(hash).fromIOToMain(
                {
                    ent.removeAt(position)
                    isLoading = false
                    resolveRefreshingView()

                    view?.showSuccess()
                    view?.notifyItemRemoved(position)
                }, {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun fireUploadAudio(audio: Audio) {
        val hash = VKLinkParser.parseLocalServerURL(audio.url)
        if (hash.isNullOrEmpty()) {
            return
        }
        isLoading = true
        resolveRefreshingView()

        appendJob(
            fInteractor.uploadAudio(hash).fromIOToMain(
                {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showSuccess()
                }, {
                    isLoading = false
                    resolveRefreshingView()

                    view?.showThrowable(it)
                })
        )
    }

    fun goFolder(item: FileRemote) {
        if (isLoading) {
            return
        }
        if (q != null) {
            q = null
            view?.updatePathString(buildPath())
            view?.displayData(fileList)
            view?.resolveEmptyText(fileList.isEmpty())
        }
        item.file_name?.let { path.add(it) }
        view?.updatePathString(buildPath())
        loadFiles()
    }

    fun scrollTo(id: Int, ownerId: Long): Boolean {
        var ret = false
        val list = if (q == null) fileList else fileListSearch
        for (i in list.indices) {
            if (!ret && list[i].id == id && list[i].owner_Id == ownerId) {
                list[i].setSelected(true)
                view?.notifyItemChanged(i)
                view?.onScrollTo(i)
                ret = true
            } else {
                if (list[i].isSelected) {
                    list[i].setSelected(false)
                    view?.notifyItemChanged(i)
                }
            }
        }
        return ret
    }

    fun loadFiles() {
        if (isLoading) {
            return
        }
        isLoading = true
        resolveRefreshingView()
        view?.resolveEmptyText(false)
        appendJob(
            fInteractor.fsGet(buildPath()).fromIOToMain({
                fileList.clear()
                fileList.addAll(it)
                isLoading = false
                resolveRefreshingView()
                view?.resolveEmptyText(fileList.isEmpty())
                view?.notifyAllChanged()
                directoryScrollPositions.remove(buildPath())?.let { scroll ->
                    view?.restoreScroll(scroll.first, scroll.second)
                } ?: view?.restoreScroll(LinearLayoutManagerSavedState(), 0)
            }, {
                view?.onError(it)
                isLoading = false
                resolveRefreshingView()
            })
        )
    }

    init {
        loadFiles()
        serviceStatusJob += observeServiceBinding()
            .sharedFlowToMain {
                view?.onServiceBindEvent(it)
            }
    }
}