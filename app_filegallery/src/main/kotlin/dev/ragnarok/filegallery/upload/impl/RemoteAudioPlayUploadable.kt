package dev.ragnarok.filegallery.upload.impl

import android.content.Context
import dev.ragnarok.filegallery.api.PercentagePublisher
import dev.ragnarok.filegallery.api.interfaces.INetworker
import dev.ragnarok.filegallery.contentLength
import dev.ragnarok.filegallery.exception.NotFoundException
import dev.ragnarok.filegallery.model.Audio
import dev.ragnarok.filegallery.settings.Settings
import dev.ragnarok.filegallery.toSource
import dev.ragnarok.filegallery.upload.IUploadable
import dev.ragnarok.filegallery.upload.Upload
import dev.ragnarok.filegallery.upload.UploadResult
import dev.ragnarok.filegallery.upload.UploadUtils
import dev.ragnarok.filegallery.util.Utils.firstNonEmptyString
import dev.ragnarok.filegallery.util.Utils.safelyClose
import dev.ragnarok.filegallery.util.coroutines.CoroutinesUtils.toFlowThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import okio.Source
import java.net.URLEncoder

class RemoteAudioPlayUploadable(private val context: Context, private val networker: INetworker) :
    IUploadable<Audio> {
    override fun doUpload(
        upload: Upload,
        listener: PercentagePublisher?
    ): Flow<UploadResult<Audio>> {
        var inputSource: Source? = null
        val localSettings = Settings.get().main().localServer
        return try {
            var serverUrl = firstNonEmptyString(
                localSettings.url,
                "https://debug.dev"
            ) + "/method/audio.remoteplay"
            if (localSettings.password != null) {
                serverUrl += "?password=" + URLEncoder.encode(localSettings.password, "utf-8")
            }
            val uri = upload.fileUri
            val size = uri?.contentLength(context) ?: throw NotFoundException("uri.path is empty")
            inputSource = uri.toSource(context)
            if (inputSource == null) {
                return toFlowThrowable(
                    NotFoundException(
                        "Unable to open InputStream, URI: $uri"
                    )
                )
            }
            val filename = UploadUtils.findFileName(
                context, uri
            )
            networker.uploads()
                .remotePlayAudioRx(serverUrl, filename, inputSource, size, listener)
                .onCompletion { safelyClose(inputSource) }
                .map { dto ->
                    UploadResult(
                        Audio().setId(
                            dto.response ?: throw NotFoundException()
                        )
                    )
                }
        } catch (e: Exception) {
            safelyClose(inputSource)
            toFlowThrowable(e)
        }
    }
}
