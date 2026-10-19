package dev.ragnarok.fenrir.upload.impl

import android.content.Context
import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.server.UploadServer
import dev.ragnarok.fenrir.api.model.server.VKApiAudioUploadServer
import dev.ragnarok.fenrir.contentLength
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.model.Audio
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toSource
import dev.ragnarok.fenrir.upload.IUploadable
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.upload.UploadUtils
import dev.ragnarok.fenrir.util.Utils.firstNonEmptyString
import dev.ragnarok.fenrir.util.Utils.safelyClose
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlowThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import okio.Source
import java.net.URLEncoder

class RemoteAudioPlayUploadable(private val context: Context, private val networker: INetworker) :
    IUploadable<Audio> {
    override fun doUpload(
        upload: Upload,
        initialServer: UploadServer?,
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
                        VKApiAudioUploadServer(serverUrl), Audio().setId(
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
