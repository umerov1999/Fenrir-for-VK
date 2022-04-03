package dev.ragnarok.fenrir.upload.impl

import android.content.Context
import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.response.UploadOwnerPhotoResponse
import dev.ragnarok.fenrir.api.model.server.UploadServer
import dev.ragnarok.fenrir.api.model.server.VkApiOwnerPhotoUploadServer
import dev.ragnarok.fenrir.api.model.upload.UploadOwnerPhotoDto
import dev.ragnarok.fenrir.domain.IWallsRepository
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.model.Post
import dev.ragnarok.fenrir.upload.IUploadable
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.upload.UploadUtils
import dev.ragnarok.fenrir.util.Utils.safelyClose
import io.reactivex.rxjava3.core.Single
import java.io.InputStream

class OwnerPhotoUploadable(
    private val context: Context,
    private val networker: INetworker,
    private val walls: IWallsRepository
) : IUploadable<Post> {
    override fun doUpload(
        upload: Upload,
        initialServer: UploadServer?,
        listener: PercentagePublisher?
    ): Single<UploadResult<Post>> {
        val accountId = upload.accountId
        val ownerId = upload.destination.ownerId
        val serverSingle: Single<UploadServer> = if (initialServer == null) {
            networker.vkDefault(accountId)
                .photos()
                .getOwnerPhotoUploadServer(ownerId)
                .map { s: VkApiOwnerPhotoUploadServer -> s }
        } else {
            Single.just(initialServer)
        }
        return serverSingle.flatMap { server: UploadServer ->
            val `is` = arrayOfNulls<InputStream>(1)
            try {
                `is`[0] = UploadUtils.openStream(context, upload.fileUri, upload.size)
                networker.uploads()
                    .uploadOwnerPhotoRx(server.url, `is`[0]!!, listener)
                    .doFinally { safelyClose(`is`[0]) }
                    .flatMap { dto: UploadOwnerPhotoDto ->
                        networker.vkDefault(accountId)
                            .photos()
                            .saveOwnerPhoto(dto.server, dto.hash, dto.photo)
                            .flatMap { response: UploadOwnerPhotoResponse ->
                                if (response.postId == 0) {
                                    Single.error<UploadResult<Post>>(
                                        NotFoundException("Post id=0")
                                    )
                                }
                                walls.getById(accountId, ownerId, response.postId)
                                    .map { post: Post -> UploadResult(server, post) }
                            }
                    }
            } catch (e: Exception) {
                safelyClose(`is`[0])
                Single.error(e)
            }
        }
    }
}