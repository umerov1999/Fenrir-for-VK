package dev.ragnarok.fenrir.api.interfaces

import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.model.VKApiJsonString
import dev.ragnarok.fenrir.api.model.response.BaseResponse
import dev.ragnarok.fenrir.api.model.upload.UploadAudioDto
import dev.ragnarok.fenrir.api.model.upload.UploadChatPhotoDto
import dev.ragnarok.fenrir.api.model.upload.UploadDocDto
import dev.ragnarok.fenrir.api.model.upload.UploadVideoDto
import kotlinx.coroutines.flow.Flow
import okio.Source

interface IUploadApi {
    fun uploadDocumentRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<UploadDocDto>

    fun uploadAudioMessageRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<VKApiJsonString>

    fun uploadAudioRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<UploadAudioDto>

    fun remotePlayAudioRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<BaseResponse<Int>>

    fun uploadStoryRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?,
        isVideo: Boolean
    ): Flow<String>

    fun uploadVideoRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<UploadVideoDto>

    fun uploadOwnerPhotoRx(
        server: String,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<VKApiJsonString>

    fun uploadChatPhotoRx(
        server: String,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<UploadChatPhotoDto>

    fun uploadPhotoToWallRx(
        server: String,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<VKApiJsonString>

    fun uploadPhotoToMessageRx(
        server: String,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<VKApiJsonString>

    fun uploadPhotoToAlbumRx(
        server: String,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<VKApiJsonString>
}
