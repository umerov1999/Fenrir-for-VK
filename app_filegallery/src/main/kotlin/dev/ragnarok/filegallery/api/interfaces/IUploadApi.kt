package dev.ragnarok.filegallery.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.filegallery.api.PercentagePublisher
import dev.ragnarok.filegallery.api.model.response.BaseResponse
import kotlinx.coroutines.flow.Flow
import okio.Source

interface IUploadApi {
    @CheckResult
    fun remotePlayAudioRx(
        server: String,
        filename: String?,
        source: Source,
        size: Long,
        listener: PercentagePublisher?
    ): Flow<BaseResponse<Int>>
}