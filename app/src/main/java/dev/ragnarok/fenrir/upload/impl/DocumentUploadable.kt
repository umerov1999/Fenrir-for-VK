package dev.ragnarok.fenrir.upload.impl

import android.content.Context
import dev.ragnarok.fenrir.api.PercentagePublisher
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.api.model.VkApiDoc
import dev.ragnarok.fenrir.api.model.server.UploadServer
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer
import dev.ragnarok.fenrir.api.model.upload.UploadDocDto
import dev.ragnarok.fenrir.db.interfaces.IDocsStorage
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity
import dev.ragnarok.fenrir.domain.mappers.Dto2Model
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.model.Document
import dev.ragnarok.fenrir.upload.IUploadable
import dev.ragnarok.fenrir.upload.Upload
import dev.ragnarok.fenrir.upload.UploadResult
import dev.ragnarok.fenrir.upload.UploadUtils
import dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction
import dev.ragnarok.fenrir.util.Utils.safelyClose
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class DocumentUploadable(
    private val context: Context,
    private val networker: INetworker,
    private val storage: IDocsStorage
) : IUploadable<Document> {
    override fun doUpload(
        upload: Upload,
        initialServer: UploadServer?,
        listener: PercentagePublisher?
    ): Single<UploadResult<Document>> {
        val ownerId = upload.destination.ownerId
        val groupId = if (ownerId >= 0) null else ownerId
        val accountId = upload.accountId
        val serverSingle: Single<UploadServer> = if (initialServer == null) {
            networker.vkDefault(accountId)
                .docs()
                .getUploadServer(groupId)
                .map { s: VkApiDocsUploadServer -> s }
        } else {
            Single.just(initialServer)
        }
        return serverSingle.flatMap { server: UploadServer ->
            val `is` = arrayOfNulls<InputStream>(1)
            try {
                val uri = upload.fileUri
                val file = File(uri!!.path!!)
                if (file.isFile) {
                    `is`[0] = FileInputStream(file)
                } else {
                    `is`[0] = context.contentResolver.openInputStream(uri)
                }
                if (`is`[0] == null) {
                    return@flatMap Single.error<UploadResult<Document>>(
                        NotFoundException(
                            "Unable to open InputStream, URI: $uri"
                        )
                    )
                }
                val filename = UploadUtils.findFileName(context, uri)
                networker.uploads()
                    .uploadDocumentRx(server.url, filename, `is`[0]!!, listener)
                    .doFinally(safelyCloseAction(`is`[0]))
                    .flatMap { dto: UploadDocDto ->
                        networker
                            .vkDefault(accountId)
                            .docs()
                            .save(dto.file, filename, null)
                            .flatMap { tmpList: VkApiDoc.Entry ->
                                if (tmpList.type.isEmpty()) {
                                    Single.error<UploadResult<Document>>(
                                        NotFoundException()
                                    )
                                }
                                val document = Dto2Model.transform(tmpList.doc)
                                val result = UploadResult(server, document)
                                if (upload.isAutoCommit) {
                                    val entity = Dto2Entity.mapDoc(tmpList.doc)
                                    commit(
                                        storage,
                                        upload,
                                        entity
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

    private fun commit(storage: IDocsStorage, upload: Upload, entity: DocumentEntity): Completable {
        return storage.store(upload.accountId, entity.ownerId, listOf(entity), false)
    }
}