package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumEntity;
import dev.ragnarok.fenrir.model.criteria.PhotoAlbumsCriteria;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IPhotoAlbumsStorage extends IStorage {

    @CheckResult
    Single<Optional<PhotoAlbumEntity>> findAlbumById(int accountId, int ownerId, int albumId);

    @CheckResult
    Single<List<PhotoAlbumEntity>> findAlbumsByCriteria(@NonNull PhotoAlbumsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, @NonNull List<PhotoAlbumEntity> albums, boolean clearBefore);

    @CheckResult
    Completable removeAlbumById(int accountId, int ownerId, int albumId);
}