package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.interfaces.IDocsApi
import dev.ragnarok.fenrir.api.model.AccessIdPair
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VkApiDoc
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer
import dev.ragnarok.fenrir.api.model.server.VkApiVideosUploadServer
import dev.ragnarok.fenrir.api.services.IDocsService
import io.reactivex.rxjava3.core.Single

internal class DocsApi(accountId: Int, provider: IServiceProvider) : AbsApi(accountId, provider),
    IDocsApi {
    override fun delete(ownerId: Int?, docId: Int): Single<Boolean> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.delete(ownerId, docId)
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override fun add(ownerId: Int, docId: Int, accessKey: String?): Single<Int> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.add(ownerId, docId, accessKey)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getById(pairs: Collection<AccessIdPair>): Single<List<VkApiDoc>> {
        val ids =
            join(pairs, ",") { AccessIdPair.format(it) }
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.getById(ids)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun search(query: String?, count: Int?, offset: Int?): Single<Items<VkApiDoc>> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.search(query, count, offset)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun save(file: String?, title: String?, tags: String?): Single<VkApiDoc.Entry> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.save(file, title, tags)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getMessagesUploadServer(
        peerId: Int?,
        type: String?
    ): Single<VkApiDocsUploadServer> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.getMessagesUploadServer(peerId, type)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getUploadServer(groupId: Int?): Single<VkApiDocsUploadServer> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.getUploadServer(groupId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getVideoServer(
        isPrivate: Int?,
        group_id: Int?,
        name: String?
    ): Single<VkApiVideosUploadServer> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service.getVideoServer(isPrivate, group_id, name)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun get(
        ownerId: Int?,
        count: Int?,
        offset: Int?,
        type: Int?
    ): Single<Items<VkApiDoc>> {
        return provideService(IDocsService::class.java)
            .flatMap { service: IDocsService ->
                service[ownerId, count, offset, type]
                    .map(extractResponseWithErrorHandling())
            }
    }
}