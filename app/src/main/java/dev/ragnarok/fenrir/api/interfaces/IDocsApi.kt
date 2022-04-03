package dev.ragnarok.fenrir.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.api.model.AccessIdPair
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VkApiDoc
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer
import dev.ragnarok.fenrir.api.model.server.VkApiVideosUploadServer
import io.reactivex.rxjava3.core.Single

interface IDocsApi {
    @CheckResult
    fun delete(ownerId: Int?, docId: Int): Single<Boolean>

    @CheckResult
    fun add(ownerId: Int, docId: Int, accessKey: String?): Single<Int>

    @CheckResult
    fun getById(pairs: Collection<AccessIdPair>): Single<List<VkApiDoc>>

    @CheckResult
    fun search(query: String?, count: Int?, offset: Int?): Single<Items<VkApiDoc>>

    @CheckResult
    fun save(file: String?, title: String?, tags: String?): Single<VkApiDoc.Entry>

    @CheckResult
    fun getUploadServer(groupId: Int?): Single<VkApiDocsUploadServer>

    @CheckResult
    fun getMessagesUploadServer(peerId: Int?, type: String?): Single<VkApiDocsUploadServer>

    @CheckResult
    fun getVideoServer(
        isPrivate: Int?,
        group_id: Int?,
        name: String?
    ): Single<VkApiVideosUploadServer>

    @CheckResult
    operator fun get(
        ownerId: Int?,
        count: Int?,
        offset: Int?,
        type: Int?
    ): Single<Items<VkApiDoc>>
}