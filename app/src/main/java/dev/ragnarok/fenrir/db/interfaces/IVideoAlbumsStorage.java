package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.VideoAlbumEntity;
import dev.ragnarok.fenrir.model.VideoAlbumCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IVideoAlbumsStorage extends IStorage {
    Single<List<VideoAlbumEntity>> findByCriteria(@NonNull VideoAlbumCriteria criteria);

    Completable insertData(int accountId, int ownerId, @NonNull List<VideoAlbumEntity> data, boolean invalidateBefore);
}