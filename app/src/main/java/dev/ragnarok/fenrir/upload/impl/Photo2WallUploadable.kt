package dev.ragnarok.fenrir.upload.impl

import android.content.Context
import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.server.UploadServer
import dev.ragnarok.fenrir.api.model.server.VkApiWallUploadServer
import dev.ragnarok.fenrir.api.model.upload.UploadPhotoToWallDto
import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.domain.IAttachmentsRepository
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.model.Photo
import dev.ragnarok.fenrir.upload.*
import dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction
import dev.ragnarok.fenrir.util.Utils.safelyClose
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.InputStream
import kotlin.math.abs

class Photo2WallUploadable(
    private val context: Context,
    private val networker: INetworker,
    private val attachmentsRepository: IAttachmentsRepository
) : IUploadable<Photo> {
    override fun doUpload(
        upload: Upload,
        initialServer: UploadServer?,
        listener: PercentagePublisher?
    ): Single<UploadResult<Photo>> {
        val subjectOwnerId = upload.destination.ownerId
        val userId = if (subjectOwnerId > 0) subjectOwnerId else null
        val groupId = if (subjectOwnerId < 0) abs(subjectOwnerId) else null
        val accountId = upload.accountId
        val serverSingle: Single<UploadServer> = if (initialServer != null) {
            Single.just(initialServer)
        } else {
            networker.vkDefault(accountId)
                .photos()
                .getWallUploadServer(groupId)
                .map { s: VkApiWallUploadServer -> s }
        }
        return serverSingle.flatMap { server: UploadServer ->
            val `is` = arrayOfNulls<InputStream>(1)
            try {
                `is`[0] = UploadUtils.openStream(context, upload.fileUri, upload.size)
                networker.uploads()
                    .uploadPhotoToWallRx(server.url, `is`[0]!!, listener)
                    .doFinally(safelyCloseAction(`is`[0]))
                    .flatMap { dto: UploadPhotoToWallDto ->
                        networker.vkDefault(accountId)
                            .photos()
                            .saveWallPhoto(
                                userId,
                                groupId,
                                dto.photo,
                                dto.server,
                                dto.hash,
                                null,
                                null,
                                null
                            )
                            .flatMap {
                                if (it.isEmpty()) {
                                    Single.error<UploadResult<Photo>>(
                                        NotFoundException()
                                    )
                                }
                                val photo = Dto2Model.transform(it[0])
                                val result = UploadResult(server, photo)
                                if (upload.isAutoCommit) {
                                    commit(
                                        attachmentsRepository,
                                        upload,
                                        photo
                                    ).andThen(Single.just(result))
                                } else {
                                    Single.just(result)
                                }
                            }
                    }
            } catch (e: Exception) {
                safelyClose(`is`[0])
                Single.error(e)
            }
        }
    }

    private fun commit(
        repository: IAttachmentsRepository,
        upload: Upload,
        photo: Photo
    ): Completable {
        val accountId = upload.accountId
        val dest = upload.destination
        when (dest.method) {
            Method.TO_COMMENT -> return repository
                .attach(accountId, AttachToType.COMMENT, dest.id, listOf(photo))
            Method.TO_WALL -> return repository
                .attach(accountId, AttachToType.POST, dest.id, listOf(photo))
        }
        return Completable.error(UnsupportedOperationException())
    }
}