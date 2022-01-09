package dev.ragnarok.fenrir.domain;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiPhotoTags;
import dev.ragnarok.fenrir.fragment.search.criteria.PhotoSearchCriteria;
import dev.ragnarok.fenrir.model.AccessIdPair;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IPhotosInteractor {
    Single<List<Photo>> get(int accountId, int ownerId, int albumId, int count, int offset, boolean rev);

    Single<List<Photo>> getUsersPhoto(int accountId, Integer ownerId, Integer extended, Integer sort, Integer offset, Integer count);

    Single<List<Photo>> getAll(int accountId, Integer ownerId, Integer extended, Integer photo_sizes, Integer offset, Integer count);

    Single<List<Photo>> search(int accountId, PhotoSearchCriteria criteria, Integer offset, Integer count);

    Single<List<Photo>> getAllCachedData(int accountId, int ownerId, int albumId, boolean sortInvert);

    Single<PhotoAlbum> getAlbumById(int accountId, int ownerId, int albumId);

    Single<List<PhotoAlbum>> getCachedAlbums(int accountId, int ownerId);

    Single<List<PhotoAlbum>> getActualAlbums(int accountId, int ownerId, int count, int offset);

    Single<Integer> like(int accountId, int ownerId, int photoId, boolean add, String accessKey);

    Single<Integer> checkAndAddLike(int accountId, int ownerId, int photoId, String accessKey);

    Single<Boolean> isLiked(int accountId, int ownerId, int photoId);

    Single<Integer> copy(int accountId, int ownerId, int photoId, String accessKey);

    Completable removedAlbum(int accountId, int ownerId, int albumId);

    Completable deletePhoto(int accountId, int ownerId, int photoId);

    Completable restorePhoto(int accountId, int ownerId, int photoId);

    Single<List<Photo>> getPhotosByIds(int accountId, Collection<AccessIdPair> ids);

    Single<List<VKApiPhotoTags>> getTags(int accountId, Integer ownerId, Integer photo_id, String access_key);

    Single<List<Comment>> getAllComments(int accountId, int ownerId, Integer album_id, int offset, int count);
}