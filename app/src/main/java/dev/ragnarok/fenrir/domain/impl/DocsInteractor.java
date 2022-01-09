package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.db.interfaces.IDocsStorage;
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity;
import dev.ragnarok.fenrir.domain.IDocsInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.fragment.search.criteria.DocumentSearchCriteria;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.criteria.DocsCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class DocsInteractor implements IDocsInteractor {

    private final INetworker networker;
    private final IDocsStorage cache;

    public DocsInteractor(INetworker networker, IDocsStorage cache) {
        this.networker = networker;
        this.cache = cache;
    }

    @Override
    public Single<List<Document>> request(int accountId, int ownerId, int filter) {
        return networker.vkDefault(accountId)
                .docs()
                .get(ownerId, null, null, filter)
                .map(items -> listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Document> documents = new ArrayList<>(dtos.size());
                    List<DocumentEntity> entities = new ArrayList<>(dtos.size());

                    for (VkApiDoc dto : dtos) {
                        documents.add(Dto2Model.transform(dto));
                        entities.add(Dto2Entity.mapDoc(dto));
                    }

                    return cache.store(accountId, ownerId, entities, true)
                            .andThen(Single.just(documents));
                });
    }

    @Override
    public Single<List<Document>> getCacheData(int accountId, int ownerId, int filter) {
        return cache.get(new DocsCriteria(accountId, ownerId).setFilter(filter))
                .map(entities -> {
                    List<Document> documents = new ArrayList<>(entities.size());
                    for (DocumentEntity entity : entities) {
                        documents.add(Entity2Model.buildDocumentFromDbo(entity));
                    }
                    return documents;
                });
    }

    @Override
    public Single<Integer> add(int accountId, int docId, int ownerId, String accessKey) {
        return networker.vkDefault(accountId)
                .docs()
                .add(ownerId, docId, accessKey);
    }

    @Override
    public Single<Document> findById(int accountId, int ownerId, int docId, String accessKey) {
        return networker.vkDefault(accountId)
                .docs()
                .getById(Collections.singletonList(new AccessIdPair(docId, ownerId, accessKey)))
                .map(dtos -> {
                    if (dtos.isEmpty()) {
                        throw new NotFoundException();
                    }

                    return Dto2Model.transform(dtos.get(0));
                });
    }

    @Override
    public Single<List<Document>> search(int accountId, DocumentSearchCriteria criteria, int count, int offset) {
        return networker.vkDefault(accountId)
                .docs()
                .search(criteria.getQuery(), count, offset)
                .map(items -> {
                    List<VkApiDoc> dtos = listEmptyIfNull(items.getItems());
                    List<Document> documents = new ArrayList<>();

                    for (VkApiDoc dto : dtos) {
                        documents.add(Dto2Model.transform(dto));
                    }

                    return documents;
                });
    }

    @Override
    public Completable delete(int accountId, int docId, int ownerId) {
        return networker.vkDefault(accountId)
                .docs()
                .delete(ownerId, docId)
                .flatMapCompletable(ignored -> cache.delete(accountId, docId, ownerId));
    }
}