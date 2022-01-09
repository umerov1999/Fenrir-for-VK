package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll;

import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.api.model.VKApiPhotoTags;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.PhotoPatch;
import dev.ragnarok.fenrir.db.model.entity.PhotoAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.fragment.search.criteria.PhotoSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.options.SimpleDateOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleGPSOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.model.AccessIdPair;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.criteria.PhotoAlbumsCriteria;
import dev.ragnarok.fenrir.model.criteria.PhotoCriteria;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class PhotosInteractor implements IPhotosInteractor {

    private final INetworker networker;
    private final IStorages cache;

    public PhotosInteractor(INetworker networker, IStorages cache) {
        this.networker = networker;
        this.cache = cache;
    }

    @Override
    public Single<List<Photo>> get(int accountId, int ownerId, int albumId, int count, int offset, boolean rev) {
        return networker.vkDefault(accountId)
                .photos()
                .get(ownerId, String.valueOf(albumId), null, rev, offset, count)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());
                    List<PhotoEntity> dbos = new ArrayList<>(dtos.size());

                    for (VKApiPhoto dto : dtos) {
                        photos.add(Dto2Model.transform(dto));
                        dbos.add(Dto2Entity.mapPhoto(dto));
                    }

                    return cache.photos()
                            .insertPhotosRx(accountId, ownerId, albumId, dbos, offset == 0)
                            .andThen(Single.just(photos));
                });
    }

    @Override
    public Single<List<Photo>> getUsersPhoto(int accountId, Integer ownerId, Integer extended, Integer sort, Integer offset, Integer count) {
        return networker.vkDefault(accountId)
                .photos()
                .getUsersPhoto(ownerId, extended, sort, offset, count)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());

                    for (VKApiPhoto dto : dtos) {
                        photos.add(Dto2Model.transform(dto));
                    }

                    return Single.just(photos);
                });
    }

    @Override
    public Single<List<Photo>> getAll(int accountId, Integer ownerId, Integer extended, Integer photo_sizes, Integer offset, Integer count) {
        return networker.vkDefault(accountId)
                .photos()
                .getAll(ownerId, extended, photo_sizes, offset, count)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());

                    for (VKApiPhoto dto : dtos) {
                        photos.add(Dto2Model.transform(dto));
                    }

                    return Single.just(photos);
                });
    }

    @Override
    public Single<List<Photo>> search(int accountId, PhotoSearchCriteria criteria, Integer offset, Integer count) {
        SpinnerOption sortOption = criteria.findOptionByKey(PhotoSearchCriteria.KEY_SORT);
        Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;
        Integer radius = criteria.extractNumberValueFromOption(PhotoSearchCriteria.KEY_RADIUS);
        SimpleGPSOption gpsOption = criteria.findOptionByKey(PhotoSearchCriteria.KEY_GPS);
        SimpleDateOption startDateOption = criteria.findOptionByKey(PhotoSearchCriteria.KEY_START_TIME);
        SimpleDateOption endDateOption = criteria.findOptionByKey(PhotoSearchCriteria.KEY_END_TIME);
        return networker.vkDefault(accountId)
                .photos()
                .search(criteria.getQuery(), gpsOption.lat_gps < 0.1 ? null : gpsOption.lat_gps, gpsOption.long_gps < 0.1 ? null : gpsOption.long_gps,
                        sort, radius, startDateOption.timeUnix == 0 ? null : startDateOption.timeUnix, endDateOption.timeUnix == 0 ? null : endDateOption.timeUnix, offset, count)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());

                    for (VKApiPhoto dto : dtos) {
                        photos.add(Dto2Model.transform(dto));
                    }

                    return Single.just(photos);
                });
    }

    @Override
    public Single<List<Photo>> getAllCachedData(int accountId, int ownerId, int albumId, boolean sortInvert) {
        PhotoCriteria criteria = new PhotoCriteria(accountId).setAlbumId(albumId).setOwnerId(ownerId).setSortInvert(sortInvert);

        if (albumId == -15) {
            criteria.setOrderBy(BaseColumns._ID);
        }

        return cache.photos()
                .findPhotosByCriteriaRx(criteria)
                .map(entities -> mapAll(entities, Entity2Model::map));
    }

    @Override
    public Single<PhotoAlbum> getAlbumById(int accountId, int ownerId, int albumId) {
        return networker.vkDefault(accountId)
                .photos()
                .getAlbums(ownerId, Collections.singletonList(albumId), null, null, true, true)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .map(dtos -> {
                    if (dtos.isEmpty()) {
                        throw new NotFoundException();
                    }
                    int pos = -1;
                    for (int i = 0; i < dtos.size(); i++) {
                        if (dtos.get(i).id == albumId) {
                            pos = i;
                            break;
                        }
                    }
                    if (pos == -1) {
                        throw new NotFoundException();
                    }

                    return Dto2Model.transformPhotoAlbum(dtos.get(pos));
                });
    }

    @Override
    public Single<List<PhotoAlbum>> getCachedAlbums(int accountId, int ownerId) {
        PhotoAlbumsCriteria criteria = new PhotoAlbumsCriteria(accountId, ownerId);

        return cache.photoAlbums()
                .findAlbumsByCriteria(criteria)
                .map(entities -> mapAll(entities, Entity2Model::mapPhotoAlbum));
    }

    @Override
    public Single<List<VKApiPhotoTags>> getTags(int accountId, Integer ownerId, Integer photo_id, String access_key) {
        return networker.vkDefault(accountId)
                .photos().getTags(ownerId, photo_id, access_key)
                .map(items -> items);
    }

    @Override
    public Single<List<Comment>> getAllComments(int accountId, int ownerId, Integer album_id, int offset, int count) {
        return networker.vkDefault(accountId)
                .photos()
                .getAllComments(ownerId, album_id, 1, offset, count)
                .flatMap(items -> {
                    List<VKApiComment> dtos = Utils.listEmptyIfNull(items.getItems());
                    VKOwnIds ownids = new VKOwnIds();
                    for (VKApiComment dto : dtos) {
                        ownids.append(dto);
                    }
                    return Repository.INSTANCE.getOwners()
                            .findBaseOwnersDataAsBundle(accountId, ownids.getAll(), IOwnersRepository.MODE_ANY, Collections.emptyList())
                            .map(bundle -> {
                                List<Comment> dbos = new ArrayList<>(dtos.size());
                                for (VKApiComment i : dtos) {
                                    Commented commented = new Commented(i.pid, ownerId, CommentedType.PHOTO, null);
                                    dbos.add(Dto2Model.buildComment(commented, i, bundle));
                                }
                                return dbos;
                            });
                });
    }

    @Override
    public Single<List<PhotoAlbum>> getActualAlbums(int accountId, int ownerId, int count, int offset) {
        return networker.vkDefault(accountId)
                .photos()
                .getAlbums(ownerId, null, offset, count, true, true)
                .flatMap(items -> {
                    List<VKApiPhotoAlbum> dtos = Utils.listEmptyIfNull(items.getItems());

                    List<PhotoAlbumEntity> dbos = new ArrayList<>(dtos.size());
                    List<PhotoAlbum> albums = new ArrayList<>(dbos.size());

                    if (offset == 0) {
                        VKApiPhotoAlbum Allph = new VKApiPhotoAlbum();
                        Allph.title = "All photos";
                        Allph.id = -9001;
                        Allph.owner_id = ownerId;
                        Allph.size = -1;

                        dbos.add(Dto2Entity.buildPhotoAlbumDbo(Allph));
                        albums.add(Dto2Model.transformPhotoAlbum(Allph));

                        if (Settings.get().other().getLocalServer().enabled && accountId == ownerId) {
                            VKApiPhotoAlbum Srvph = new VKApiPhotoAlbum();
                            Srvph.title = "Local Server";
                            Srvph.id = -311;
                            Srvph.owner_id = ownerId;
                            Srvph.size = -1;

                            dbos.add(Dto2Entity.buildPhotoAlbumDbo(Srvph));
                            albums.add(Dto2Model.transformPhotoAlbum(Srvph));
                        }
                    }

                    for (VKApiPhotoAlbum dto : dtos) {
                        dbos.add(Dto2Entity.buildPhotoAlbumDbo(dto));
                        albums.add(Dto2Model.transformPhotoAlbum(dto));
                    }

                    return cache.photoAlbums()
                            .store(accountId, ownerId, dbos, offset == 0)
                            .andThen(Single.just(albums));
                });
    }

    @Override
    public Single<Boolean> isLiked(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .likes()
                .isLiked("photo", ownerId, photoId);
    }

    @Override
    public Single<Integer> checkAndAddLike(int accountId, int ownerId, int photoId, String accessKey) {
        return networker.vkDefault(accountId)
                .likes().checkAndAddLike("photo", ownerId, photoId, accessKey);
    }

    @Override
    public Single<Integer> like(int accountId, int ownerId, int photoId, boolean add, String accessKey) {
        Single<Integer> single;

        if (add) {
            single = networker.vkDefault(accountId)
                    .likes()
                    .add("photo", ownerId, photoId, accessKey);
        } else {
            single = networker.vkDefault(accountId)
                    .likes()
                    .delete("photo", ownerId, photoId, accessKey);
        }

        return single.flatMap(count -> {
            PhotoPatch patch = new PhotoPatch().setLike(new PhotoPatch.Like(count, add));
            return cache.photos()
                    .applyPatch(accountId, ownerId, photoId, patch)
                    .andThen(Single.just(count));
        });
    }

    @Override
    public Single<Integer> copy(int accountId, int ownerId, int photoId, String accessKey) {
        return networker.vkDefault(accountId)
                .photos()
                .copy(ownerId, photoId, accessKey);
    }

    @Override
    public Completable removedAlbum(int accountId, int ownerId, int albumId) {
        return networker.vkDefault(accountId)
                .photos()
                .deleteAlbum(albumId, ownerId < 0 ? Math.abs(ownerId) : null)
                .flatMapCompletable(ignored -> cache.photoAlbums()
                        .removeAlbumById(accountId, ownerId, albumId));
    }

    @Override
    public Completable deletePhoto(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .photos()
                .delete(ownerId, photoId)
                .flatMapCompletable(ignored -> {
                    PhotoPatch patch = new PhotoPatch().setDeletion(new PhotoPatch.Deletion(true));
                    return cache.photos()
                            .applyPatch(accountId, ownerId, photoId, patch);
                });
    }

    @Override
    public Completable restorePhoto(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .photos()
                .restore(ownerId, photoId)
                .flatMapCompletable(ignored -> {
                    PhotoPatch patch = new PhotoPatch().setDeletion(new PhotoPatch.Deletion(false));
                    return cache.photos()
                            .applyPatch(accountId, ownerId, photoId, patch);
                });
    }

    @Override
    public Single<List<Photo>> getPhotosByIds(int accountId, Collection<AccessIdPair> ids) {
        List<dev.ragnarok.fenrir.api.model.AccessIdPair> dtoPairs = new ArrayList<>(ids.size());

        for (AccessIdPair pair : ids) {
            dtoPairs.add(new dev.ragnarok.fenrir.api.model.AccessIdPair(pair.getId(),
                    pair.getOwnerId(), pair.getAccessKey()));
        }

        return networker.vkDefault(accountId)
                .photos()
                .getById(dtoPairs)
                .map(dtos -> mapAll(dtos, Dto2Model::transform));
    }
}