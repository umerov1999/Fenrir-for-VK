package dev.ragnarok.filegallery.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.model.FileRemote
import dev.ragnarok.filegallery.model.Photo
import dev.ragnarok.filegallery.model.Video
import kotlinx.coroutines.flow.Flow

interface ILocalServerApi {
    @CheckResult
    fun getVideos(offset: Int?, count: Int?, reverse: Boolean): Flow<List<Video>>

    @CheckResult
    fun getAudios(offset: Int?, count: Int?, reverse: Boolean): Flow<List<Audio>>

    @CheckResult
    fun getDiscography(offset: Int?, count: Int?, reverse: Boolean): Flow<List<Audio>>

    @CheckResult
    fun getPhotos(offset: Int?, count: Int?, reverse: Boolean): Flow<MutableList<Photo>>

    @CheckResult
    fun searchVideos(
        query: String?,
        offset: Int?,
        count: Int?,
        reverse: Boolean
    ): Flow<List<Video>>

    @CheckResult
    fun searchAudios(
        query: String?,
        offset: Int?,
        count: Int?,
        reverse: Boolean
    ): Flow<List<Audio>>

    @CheckResult
    fun searchDiscography(
        query: String?,
        offset: Int?,
        count: Int?,
        reverse: Boolean
    ): Flow<List<Audio>>

    @CheckResult
    fun searchPhotos(
        query: String?,
        offset: Int?,
        count: Int?,
        reverse: Boolean
    ): Flow<List<Photo>>

    @CheckResult
    fun updateTime(hash: String?): Flow<Int>

    @CheckResult
    fun deleteMedia(hash: String?): Flow<Int>

    @CheckResult
    fun getFileName(hash: String?): Flow<String>

    @CheckResult
    fun updateFileName(hash: String?, name: String?): Flow<Int>

    @CheckResult
    fun fsGet(dir: String?): Flow<List<FileRemote>>

    @CheckResult
    fun powerOffPC(type: String): Flow<Int>
}