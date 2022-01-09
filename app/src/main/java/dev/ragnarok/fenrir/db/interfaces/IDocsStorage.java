package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.DocumentEntity;
import dev.ragnarok.fenrir.model.criteria.DocsCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IDocsStorage extends IStorage {

    @CheckResult
    Single<List<DocumentEntity>> get(@NonNull DocsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, List<DocumentEntity> entities, boolean clearBeforeInsert);

    @CheckResult
    Completable delete(int accountId, int docId, int ownerId);
}