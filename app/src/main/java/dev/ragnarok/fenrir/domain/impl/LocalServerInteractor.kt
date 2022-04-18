package dev.ragnarok.fenrir.domain.impl

import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.VKApiAudio
import dev.ragnarok.fenrir.api.model.VKApiPhoto
import dev.ragnarok.fenrir.api.model.VKApiVideo
import dev.ragnarok.fenrir.db.model.entity.VideoEntity
import dev.ragnarok.fenrir.domain.ILocalServerInteractor
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity.mapVideo
import dev.ragnarok.fenrir.domain.mappers.Dto2Model.transform
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.Video
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import io.reactivex.rxjava3.core.Single

class LocalServerInteractor(private val networker: INetworker) : ILocalServerInteractor {
    override fun getVideos(offset: Int, count: Int, reverse: Boolean): Single<List<Video>> {
        return networker.localServerApi()
            .getVideos(offset, count, reverse)
            .flatMap { items ->
                val dtos = listEmptyIfNull<VKApiVideo>(
                    items.items
                )
                val dbos: MutableList<VideoEntity> = ArrayList(dtos.size)
                val videos: MutableList<Video> = ArrayList(dbos.size)
                for (dto in dtos) {
                    dbos.add(mapVideo(dto))
                    videos.add(transform(dto))
                }
                Single.just<List<Video>>(videos)
            }
    }

    override fun getAudios(offset: Int, count: Int, reverse: Boolean): Single<List<Audio>> {
        return networker.localServerApi()
            .getAudios(offset, count, reverse)
            .map { items ->
                listEmptyIfNull<VKApiAudio>(
                    items.items
                )
            }
            .map { out ->
                val ret: MutableList<Audio> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]).setIsLocalServer())
                ret
            }
    }

    override fun getDiscography(offset: Int, count: Int, reverse: Boolean): Single<List<Audio>> {
        return networker.localServerApi()
            .getDiscography(offset, count, reverse)
            .map { items ->
                listEmptyIfNull<VKApiAudio>(
                    items.items
                )
            }
            .map { out ->
                val ret: MutableList<Audio> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]).setIsLocalServer())
                ret
            }
    }

    override fun getPhotos(offset: Int, count: Int, reverse: Boolean): Single<List<Photo>> {
        return networker.localServerApi()
            .getPhotos(offset, count, reverse)
            .map { items ->
                listEmptyIfNull<VKApiPhoto>(
                    items.items
                )
            }
            .map { out ->
                val ret: MutableList<Photo> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]))
                ret
            }
    }

    override fun searchVideos(
        q: String?,
        offset: Int,
        count: Int,
        reverse: Boolean
    ): Single<List<Video>> {
        return networker.localServerApi()
            .searchVideos(q, offset, count, reverse)
            .flatMap { items ->
                val dtos = listEmptyIfNull<VKApiVideo>(
                    items.items
                )
                val dbos: MutableList<VideoEntity> = ArrayList(dtos.size)
                val videos: MutableList<Video> = ArrayList(dbos.size)
                for (dto in dtos) {
                    dbos.add(mapVideo(dto))
                    videos.add(transform(dto))
                }
                Single.just<List<Video>>(videos)
            }
    }

    override fun searchAudios(
        q: String?,
        offset: Int,
        count: Int,
        reverse: Boolean
    ): Single<List<Audio>> {
        return networker.localServerApi()
            .searchAudios(q, offset, count, reverse)
            .map { items ->
                listEmptyIfNull<VKApiAudio>(
                    items.items
                )
            }
            .map { out ->
                val ret: MutableList<Audio> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]).setIsLocalServer())
                ret
            }
    }

    override fun searchDiscography(
        q: String?,
        offset: Int,
        count: Int,
        reverse: Boolean
    ): Single<List<Audio>> {
        return networker.localServerApi()
            .searchDiscography(q, offset, count, reverse)
            .map { items ->
                listEmptyIfNull<VKApiAudio>(
                    items.items
                )
            }
            .map { out ->
                val ret: MutableList<Audio> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]).setIsLocalServer())
                ret
            }
    }

    override fun searchPhotos(
        q: String?,
        offset: Int,
        count: Int,
        reverse: Boolean
    ): Single<List<Photo>> {
        return networker.localServerApi()
            .searchPhotos(q, offset, count, reverse)
            .map { items ->
                listEmptyIfNull<VKApiPhoto>(
                    items.items
                )
            }
            .map { out ->
                val ret: MutableList<Photo> = ArrayList()
                for (i in out.indices) ret.add(transform(out[i]))
                ret
            }
    }

    override fun update_time(hash: String?): Single<Int> {
        return networker.localServerApi()
            .update_time(hash)
            .map { resultId -> resultId }
    }

    override fun delete_media(hash: String?): Single<Int> {
        return networker.localServerApi()
            .delete_media(hash)
            .map { resultId -> resultId }
    }

    override fun get_file_name(hash: String?): Single<String> {
        return networker.localServerApi()
            .get_file_name(hash)
            .map { resultId -> resultId }
    }

    override fun update_file_name(hash: String?, name: String?): Single<Int> {
        return networker.localServerApi()
            .update_file_name(hash, name)
            .map { resultId -> resultId }
    }
}