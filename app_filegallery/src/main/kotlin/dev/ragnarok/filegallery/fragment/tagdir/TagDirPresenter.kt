package dev.ragnarok.filegallery.fragment.tagdir

import dev.ragnarok.fenrir.module.parcel.ParcelFlags
import dev.ragnarok.fenrir.module.parcel.ParcelNative
import dev.ragnarok.filegallery.Includes
import dev.ragnarok.filegallery.db.interfaces.ISearchRequestHelperStorage
import dev.ragnarok.filegallery.filePathToUrl
import dev.ragnarok.filegallery.fragment.base.RxSupportPresenter
import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.model.FileType
import dev.ragnarok.filegallery.model.Photo
import dev.ragnarok.filegallery.model.Video
import dev.ragnarok.filegallery.model.tags.TagDir
import dev.ragnarok.filegallery.util.Objects
import dev.ragnarok.filegallery.util.Utils
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.fromIOToMain
import java.util.Locale

class TagDirPresenter(private val owner_id: Long) :
    RxSupportPresenter<ITagDirView>() {
    private val tagDirData: ArrayList<TagDir> = ArrayList()
    private val tagDirDataSearch: ArrayList<TagDir> = ArrayList()
    private var q: String? = null
    private val storage: ISearchRequestHelperStorage =
        Includes.stores.searchQueriesStore()

    override fun onGuiCreated(viewHost: ITagDirView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(if (q == null) tagDirData else tagDirDataSearch)
    }

    fun doSearch(str: String?) {
        var query: String? = str?.trim()
        if (query.isNullOrEmpty()) {
            query = null
        }
        if (Objects.safeEquals(query, this.q)) {
            return
        }
        q = query
        val locale = Utils.appLocale
        if (query == null) {
            tagDirDataSearch.clear()
            view?.displayData(tagDirData)
        } else {
            tagDirDataSearch.clear()
            for (i in tagDirData) {
                if (i.name.isNullOrEmpty()) {
                    continue
                }
                if (i.name?.lowercase(locale)
                        ?.contains(query.lowercase(locale)) == true
                ) {
                    tagDirDataSearch.add(i)
                }
            }
            view?.displayData(tagDirDataSearch)
        }
    }

    private fun loadActualData() {
        appendJob(
            storage.getTagDirs(owner_id)
                .fromIOToMain(
                    { onActualDataReceived(it) },
                    { t -> onActualDataGetError(t) })
        )
    }

    fun deleteTagDir(pos: Int, owner: TagDir) {
        appendJob(
            storage.deleteTagDir(owner.id)
                .fromIOToMain(
                    {
                        tagDirData.removeAt(pos)
                        view?.notifyRemove(pos)
                    }, { t -> onActualDataGetError(t) })
        )
    }

    fun scrollTo(item: String): Boolean {
        var ret = false
        val list = tagDirData
        for (i in list.indices) {
            if (!ret && list[i].path == item) {
                list[i].isSelected = true
                view?.notifyItemChanged(i)
                view?.onScrollTo(i)
                ret = true
            } else {
                if (list[i].isSelected) {
                    list[i].isSelected = false
                    view?.notifyItemChanged(i)
                }
            }
        }
        return ret
    }

    fun onClickFile(item: TagDir) {
        if (item.type == FileType.photo) {
            val list = tagDirData
            var index = 0
            var o = 0
            val mem = ParcelNative.create(ParcelFlags.NULL_LIST)
            for (i in list) {
                if (i.type != FileType.photo && i.type != FileType.video) {
                    continue
                }
                if (i.path == item.path) {
                    index = o
                }
                val photo = Photo()
                photo.setId(i.fileNameHash)
                photo.setOwnerId(i.filePathHash)
                photo.setPhoto_url(i.path?.filePathToUrl())
                photo.setPreview_url(i.path?.filePathToUrl("thumb_file"))
                photo.setLocal(true)
                photo.setIsAnimation(i.type == FileType.video)
                photo.setText(i.name)
                mem.writeParcelable(photo)
                o++
            }
            mem.writeFirstInt(o)
            view?.displayGalleryUnSafe(mem.nativePointer, index, false)
        } else if (item.type == FileType.video) {
            val v = Video()
            v.setId(item.fileNameHash)
            v.setOwnerId(item.filePathHash)
            v.setTitle(item.name)
            v.setLink(item.path?.filePathToUrl())
            view?.displayVideo(v)
        } else if (item.type == FileType.audio) {
            val list = tagDirData
            var index = 0
            var o = 0
            val mAudios: ArrayList<Audio> = ArrayList()
            for (i in list) {
                if (i.type != FileType.audio) {
                    continue
                }
                if (i.path == item.path) {
                    index = o
                }
                val audio = Audio()
                audio.setId(i.fileNameHash)
                audio.setOwnerId(i.filePathHash)
                audio.setUrl(i.path?.filePathToUrl())
                audio.setThumb_image(i.path?.filePathToUrl("thumb_file"))

                var TrackName: String =
                    i.name?.replace(".mp3", "") ?: ""
                val Artist: String
                val arr = TrackName.split(Regex(" - ")).toTypedArray()
                if (arr.size > 1) {
                    Artist = arr[0]
                    TrackName = TrackName.replace("$Artist - ", "")
                } else {
                    Artist = i.name ?: ""
                }
                audio.setIsLocal()
                audio.setArtist(Artist)
                audio.setTitle(TrackName)

                mAudios.add(audio)
                o++
            }
            view?.startPlayAudios(mAudios, index)
        }
    }

    private fun onActualDataGetError(t: Throwable) {
        view?.showThrowable(t)
    }

    private fun onActualDataReceived(data: List<TagDir>) {
        tagDirData.clear()
        tagDirData.addAll(data)
        view?.notifyChanges()
    }

    init {
        loadActualData()
    }
}
