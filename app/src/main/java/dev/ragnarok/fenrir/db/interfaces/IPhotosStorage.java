package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.PhotoPatch;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.model.criteria.PhotoCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IPhotosStorage extends IStorage {

    Completable insertPhotosRx(int accountId, int ownerId, int albumId, @NonNull List<PhotoEntity> photos, boolean clearBefore);

    Single<List<PhotoEntity>> findPhotosByCriteriaRx(@NonNull PhotoCriteria criteria);

    Completable applyPatch(int accountId, int ownerId, int photoId, PhotoPatch patch);
}