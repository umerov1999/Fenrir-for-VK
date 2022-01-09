package dev.ragnarok.fenrir.api.impl;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.interfaces.IDocsApi;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.api.model.server.VkApiDocsUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiVideosUploadServer;
import dev.ragnarok.fenrir.api.services.IDocsService;
import io.reactivex.rxjava3.core.Single;


class DocsApi extends AbsApi implements IDocsApi {

    DocsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Boolean> delete(Integer ownerId, int docId) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.delete(ownerId, docId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> add(int ownerId, int docId, String accessKey) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.add(ownerId, docId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VkApiDoc>> getById(Collection<AccessIdPair> pairs) {
        String ids = join(pairs, ",", AccessIdPair::format);
        return provideService(IDocsService.class)
                .flatMap(service -> service.getById(ids)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiDoc>> search(String query, Integer count, Integer offset) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.search(query, count, offset)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiDoc.Entry> save(String file, String title, String tags) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.save(file, title, tags)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiDocsUploadServer> getMessagesUploadServer(Integer peerId, String type) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.getMessagesUploadServer(peerId, type)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiDocsUploadServer> getUploadServer(Integer groupId) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.getUploadServer(groupId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiVideosUploadServer> getVideoServer(Integer isPrivate, Integer group_id, String name) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.getVideoServer(isPrivate, group_id, name)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiDoc>> get(Integer ownerId, Integer count, Integer offset, Integer type) {
        return provideService(IDocsService.class)
                .flatMap(service -> service.get(ownerId, count, offset, type)
                        .map(extractResponseWithErrorHandling()));
    }
}
