package dev.ragnarok.fenrir.domain.interfaces

import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.model.FileRemote
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.model.Video
import kotlinx.coroutines.flow.Flow

interface ILocalServerInteractor {
    fun getVideos(offset: Int, count: Int, reverse: Boolean): Flow<List<Video>>
    fun getAudios(offset: Int, count: Int, reverse: Boolean): Flow<List<Audio>>
    fun getDiscography(offset: Int, count: Int, reverse: Boolean): Flow<List<Audio>>
    fun getPhotos(offset: Int, count: Int, reverse: Boolean): Flow<List<Photo>>
    fun searchVideos(q: String?, offset: Int, count: Int, reverse: Boolean): Flow<List<Video>>
    fun searchAudios(q: String?, offset: Int, count: Int, reverse: Boolean): Flow<List<Audio>>
    fun searchDiscography(
        q: String?,
        offset: Int,
        count: Int,
        reverse: Boolean
    ): Flow<List<Audio>>

    fun searchPhotos(q: String?, offset: Int, count: Int, reverse: Boolean): Flow<List<Photo>>
    fun updateTime(hash: String?): Flow<Int>
    fun deleteMedia(hash: String?): Flow<Int>
    fun getFileName(hash: String?): Flow<String>
    fun updateFileName(hash: String?, name: String?): Flow<Int>
    fun powerOffPC(type: String): Flow<Int>
    fun fsGet(dir: String?): Flow<List<FileRemote>>
    fun uploadAudio(hash: String?): Flow<Int>
}