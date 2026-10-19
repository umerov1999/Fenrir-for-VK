package dev.ragnarok.fenrir.upload.impl

import android.content.Context
import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.server.UploadServer
import dev.ragnarok.fenrir.db.AttachmentFor
import dev.ragnarok.fenrir.domain.interfaces.IAttachmentsRepository
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.upload.IUploadable
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.upload.UploadUtils
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Utils.safelyClose
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.toFlowThrowable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import okio.Source
import kotlin.coroutines.cancellation.CancellationException

class Photo2MessageUploadable(
    private val context: Context,
    private val networker: INetworker,
    private val attachmentsRepository: IAttachmentsRepository
) : IUploadable<Photo> {
    override fun doUpload(
        upload: Upload,
        initialServer: UploadServer?,
        listener: PercentagePublisher?
    ): Flow<UploadResult<Photo>> {
        val accountId = upload.accountId
        val cmId = upload.destination.id
        val peerId = upload.destination.ownerId
        val serverSingle = if (initialServer != null) {
            toFlow(initialServer)
        } else {
            networker.vkDefault(accountId)
                .photos()
                .getMessagesUploadServer(peerId)
        }
        return serverSingle.flatMapConcat { server ->
            var input: Source? = null
            try {
                val tmpSources = UploadUtils.openSource(context, upload.fileUri, upload.size)
                input = tmpSources.second
                if (input == null) {
                    toFlowThrowable(
                        NotFoundException(
                            "Unable to open InputStream, URI: ${upload.fileUri}"
                        )
                    )
                } else {
                    networker.uploads()
                        .uploadPhotoToMessageRx(
                            server.url ?: throw NotFoundException("Upload url empty!"),
                            input,
                            tmpSources.first,
                            listener
                        )
                        .onCompletion { safelyClose(input) }
                        .flatMapConcat { dto ->
                            if (dto.photo.isNullOrEmpty()) {
                                toFlowThrowable(NotFoundException("VK doesn't upload this file"))
                            } else {
                                networker.vkDefault(accountId)
                                    .photos()
                                    .saveMessagesPhoto(dto.server, dto.photo, dto.hash)
                                    .flatMapConcat { photos ->
                                        if (photos.isEmpty()) {
                                            toFlowThrowable(NotFoundException("[saveMessagesPhoto] returned empty list"))
                                        } else {
                                            val photo = Dto2Model.transform(photos[0])
                                            val result = UploadResult(server, photo)
                                            if (upload.isAutoCommit) {
                                                attachIntoDatabaseRx(
                                                    attachmentsRepository,
                                                    accountId,
                                                    cmId,
                                                    peerId,
                                                    photo
                                                ).map {
                                                    result
                                                }
                                            } else {
                                                toFlow(result)
                                            }
                                        }
                                    }
                            }
                        }
                }
            } catch (e: Exception) {
                safelyClose(input)
                if (e is CancellationException) {
                    throw e
                }
                toFlowThrowable(e)
            }
        }
    }

    companion object {
        internal fun attachIntoDatabaseRx(
            repository: IAttachmentsRepository,
            accountId: Long, cmId: Int, peerId: Long, photo: Photo
        ): Flow<Boolean> {
            return repository
                .attach(accountId, AttachmentFor.MESSAGE, 0, Pair(peerId, cmId), listOf(photo))
        }
    }
}
