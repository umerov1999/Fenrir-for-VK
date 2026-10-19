package dev.ragnarok.fenrir.upload.impl

import android.content.Context
import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.server.UploadServer
import dev.ragnarok.fenrir.contentLength
import dev.ragnarok.fenrir.domain.FetchMode
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.model.Story
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.toSource
import dev.ragnarok.fenrir.upload.IUploadable
import dev.ragnarok.fenrir.upload.MessageMethod
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.upload.UploadUtils
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.Utils.safelyClose
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlowThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import okio.Source
import kotlin.coroutines.cancellation.CancellationException

class StoryUploadable(private val context: Context, private val networker: INetworker) :
    IUploadable<Story> {
    override fun doUpload(
        upload: Upload,
        initialServer: UploadServer?,
        listener: PercentagePublisher?
    ): Flow<UploadResult<Story>> {
        val accountId = upload.accountId
        val serverSingle = if (initialServer == null) {
            if (upload.destination.messageMethod == MessageMethod.VIDEO) networker.vkDefault(
                accountId
            ).stories().stories_getVideoUploadServer(null, upload.destination.ref)
            else {
                networker.vkDefault(accountId).stories()
                    .stories_getPhotoUploadServer(null, upload.destination.ref)
            }
        } else {
            toFlow(initialServer)
        }
        return serverSingle.flatMapConcat { server ->
            var inputSource: Source? = null
            try {
                val uri = upload.fileUri
                val size =
                    uri?.contentLength(context) ?: throw NotFoundException("uri.path is empty")
                inputSource = uri.toSource(context)
                if (inputSource == null) {
                    toFlowThrowable(
                        NotFoundException(
                            "Unable to open InputStream, URI: $uri"
                        )
                    )
                } else {
                    val filename = UploadUtils.findFileName(context, uri)
                    networker.uploads()
                        .uploadStoryRx(
                            server.url ?: throw NotFoundException("Upload url empty!"),
                            filename,
                            inputSource,
                            size,
                            listener,
                            upload.destination.messageMethod == MessageMethod.VIDEO
                        )
                        .onCompletion { safelyClose(inputSource) }
                        .flatMapConcat { dto ->
                            networker
                                .vkDefault(accountId)
                                .stories()
                                .stories_save(dto)
                                .map {
                                    listEmptyIfNull(it.items)
                                }
                                .flatMapConcat { tmpList ->
                                    if (tmpList.isEmpty()) {
                                        toFlowThrowable(NotFoundException("[stories_save] returned empty list"))
                                    } else {
                                        owners.findBaseOwnersDataAsBundle(
                                            accountId, listOf(
                                                Settings.get().accounts().current
                                            ), FetchMode.ANY, null
                                        ).map {
                                            val document =
                                                Dto2Model.transformStory(tmpList[0], it)
                                            UploadResult(server, document)
                                        }
                                    }
                                }
                        }
                }
            } catch (e: Exception) {
                safelyClose(inputSource)
                if (e is CancellationException) {
                    throw e
                }
                toFlowThrowable(e)
            }
        }
    }
}