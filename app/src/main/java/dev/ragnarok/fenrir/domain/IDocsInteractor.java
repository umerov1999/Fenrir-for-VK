package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.fragment.search.criteria.DocumentSearchCriteria;
import dev.ragnarok.fenrir.model.Document;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IDocsInteractor {
    Single<List<Document>> request(int accountId, int ownerId, int filter);

    Single<List<Document>> getCacheData(int accountId, int ownerId, int filter);

    Single<Integer> add(int accountId, int docId, int ownerId, String accessKey);

    Single<Document> findById(int accountId, int ownerId, int docId, String accessKey);

    Single<List<Document>> search(int accountId, DocumentSearchCriteria criteria, int count, int offset);

    Completable delete(int accountId, int docId, int ownerId);
}