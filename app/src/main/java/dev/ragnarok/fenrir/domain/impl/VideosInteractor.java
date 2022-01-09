package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Utils.join;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VKApiVideoAlbum;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.entity.VideoAlbumEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.domain.IVideosInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.fragment.search.criteria.VideoSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VideoAlbum;
import dev.ragnarok.fenrir.model.VideoAlbumCriteria;
import dev.ragnarok.fenrir.model.VideoCriteria;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class VideosInteractor implements IVideosInteractor {

    private final INetworker networker;

    private final IStorages cache;

    public VideosInteractor(INetworker networker, IStorages cache) {
        this.networker = networker;
        this.cache = cache;
    }

    private static String buildFiltersByCriteria(VideoSearchCriteria criteria) {
        Boolean youtube = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_YOUTUBE);
        Boolean vimeo = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_VIMEO);
        Boolean shortVideos = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_SHORT);
        Boolean longVideos = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_LONG);

        ArrayList<String> list = new ArrayList<>();
        if (youtube != null && youtube) {
            list.add("youtube");
        }

        if (vimeo != null && vimeo) {
            list.add("vimeo");
        }

        if (shortVideos != null && shortVideos) {
            list.add("short");
        }

        if (longVideos != null && longVideos) {
            list.add("long");
        }

        return list.isEmpty() ? null : join(",", list);
    }

    @Override
    public Single<List<Video>> get(int accountId, int ownerId, int albumId, int count, int offset) {
        return networker.vkDefault(accountId)
                .video()
                .get(ownerId, null, albumId, count, offset, true)
                .flatMap(items -> {
                    List<VKApiVideo> dtos = listEmptyIfNull(items.getItems());
                    List<VideoEntity> dbos = new ArrayList<>(dtos.size());
                    List<Video> videos = new ArrayList<>(dbos.size());

                    for (VKApiVideo dto : dtos) {
                        dbos.add(Dto2Entity.mapVideo(dto));
                        videos.add(Dto2Model.transform(dto));
                    }

                    return cache.videos()
                            .insertData(accountId, ownerId, albumId, dbos, offset == 0)
                            .andThen(Single.just(videos));
                });
    }

    @Override
    public Single<List<Video>> getCachedVideos(int accountId, int ownerId, int albumId) {
        VideoCriteria criteria = new VideoCriteria(accountId, ownerId, albumId);
        return cache.videos()
                .findByCriteria(criteria)
                .map(dbos -> {
                    List<Video> videos = new ArrayList<>(dbos.size());
                    for (VideoEntity dbo : dbos) {
                        videos.add(Entity2Model.buildVideoFromDbo(dbo));
                    }
                    return videos;
                });
    }

    @Override
    public Single<Video> getById(int accountId, int ownerId, int videoId, String accessKey, boolean cacheData) {
        Collection<AccessIdPair> ids = Collections.singletonList(new AccessIdPair(videoId, ownerId, accessKey));
        return networker.vkDefault(accountId)
                .video()
                .get(null, ids, null, null, null, true)
                .map(items -> {
                    if (Utils.nonEmpty(items.getItems())) {
                        return items.getItems().get(0);
                    }

                    throw new NotFoundException();
                })
                .flatMap(dto -> {
                    if (cacheData) {
                        VideoEntity dbo = Dto2Entity.mapVideo(dto);

                        return cache.videos()
                                .insertData(accountId, ownerId, dto.album_id, Collections.singletonList(dbo), false)
                                .andThen(Single.just(dto));
                    }

                    return Single.just(dto);
                })
                .map(Dto2Model::transform);
    }

    @Override
    public Completable addToMy(int accountId, int targetOwnerId, int videoOwnerId, int videoId) {
        return networker.vkDefault(accountId)
                .video()
                .addVideo(targetOwnerId, videoId, videoOwnerId)
                .ignoreElement();
    }

    @Override
    public Completable edit(int accountId, Integer ownerId, int video_id, String name, String desc) {
        return networker.vkDefault(accountId)
                .video()
                .edit(ownerId, video_id, name, desc)
                .ignoreElement();
    }

    @Override
    public Completable delete(int accountId, Integer videoId, Integer ownerId, Integer targetId) {
        return networker.vkDefault(accountId)
                .video()
                .deleteVideo(videoId, ownerId, targetId)
                .ignoreElement();
    }

    @Override
    public Single<Integer> checkAndAddLike(int accountId, int ownerId, int videoId, String accessKey) {
        return networker.vkDefault(accountId)
                .likes().checkAndAddLike("video", ownerId, videoId, accessKey);
    }

    @Override
    public Single<Boolean> isLiked(int accountId, int ownerId, int videoId) {
        return networker.vkDefault(accountId)
                .likes()
                .isLiked("video", ownerId, videoId);
    }

    @Override
    public Single<Pair<Integer, Boolean>> likeOrDislike(int accountId, int ownerId, int videoId, String accessKey, boolean like) {
        if (like) {
            return networker.vkDefault(accountId)
                    .likes()
                    .add("video", ownerId, videoId, accessKey)
                    .map(integer -> Pair.Companion.create(integer, true));
        } else {
            return networker.vkDefault(accountId)
                    .likes()
                    .delete("video", ownerId, videoId, accessKey)
                    .map(integer -> Pair.Companion.create(integer, false));
        }
    }

    @Override
    public Single<List<VideoAlbum>> getCachedAlbums(int accountId, int ownerId) {
        VideoAlbumCriteria criteria = new VideoAlbumCriteria(accountId, ownerId);
        return cache.videoAlbums()
                .findByCriteria(criteria)
                .map(dbos -> {
                    List<VideoAlbum> albums = new ArrayList<>(dbos.size());
                    for (VideoAlbumEntity dbo : dbos) {
                        albums.add(Entity2Model.buildVideoAlbumFromDbo(dbo));
                    }
                    return albums;
                });
    }

    @Override
    public Single<List<VideoAlbum>> getAlbumsByVideo(int accountId, int target_id, int owner_id, int video_id) {
        return networker.vkDefault(accountId)
                .video()
                .getAlbumsByVideo(target_id, owner_id, video_id)
                .flatMap(items -> {
                    List<VKApiVideoAlbum> dtos = listEmptyIfNull(items.getItems());
                    List<VideoAlbum> albums = new ArrayList<>(dtos.size());

                    for (VKApiVideoAlbum dto : dtos) {
                        VideoAlbumEntity dbo = Dto2Entity.buildVideoAlbumDbo(dto);
                        albums.add(Entity2Model.buildVideoAlbumFromDbo(dbo));
                    }

                    return Single.just(albums);
                });
    }

    @Override
    public Single<List<VideoAlbum>> getActualAlbums(int accountId, int ownerId, int count, int offset) {
        return networker.vkDefault(accountId)
                .video()
                .getAlbums(ownerId, offset, count, true)
                .flatMap(items -> {
                    List<VKApiVideoAlbum> dtos = listEmptyIfNull(items.getItems());
                    List<VideoAlbumEntity> dbos = new ArrayList<>(dtos.size());
                    List<VideoAlbum> albums = new ArrayList<>(dbos.size());

                    for (VKApiVideoAlbum dto : dtos) {
                        VideoAlbumEntity dbo = Dto2Entity.buildVideoAlbumDbo(dto);
                        dbos.add(dbo);
                        albums.add(Entity2Model.buildVideoAlbumFromDbo(dbo));
                    }

                    return cache.videoAlbums()
                            .insertData(accountId, ownerId, dbos, offset == 0)
                            .andThen(Single.just(albums));
                });
    }


    @Override
    public Single<List<Video>> search(int accountId, VideoSearchCriteria criteria, int count, int offset) {
        SpinnerOption sortOption = criteria.findOptionByKey(VideoSearchCriteria.KEY_SORT);
        Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;

        Boolean hd = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_HD);
        Boolean adult = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_ADULT);
        String filters = buildFiltersByCriteria(criteria);
        Boolean searchOwn = criteria.extractBoleanValueFromOption(VideoSearchCriteria.KEY_SEARCH_OWN);
        Integer longer = criteria.extractNumberValueFromOption(VideoSearchCriteria.KEY_DURATION_FROM);
        Integer shoter = criteria.extractNumberValueFromOption(VideoSearchCriteria.KEY_DURATION_TO);

        return networker.vkDefault(accountId)
                .video()
                .search(criteria.getQuery(), sort, hd, adult, filters, searchOwn, offset, longer, shoter, count, false)
                .map(response -> {
                    List<VKApiVideo> dtos = listEmptyIfNull(response.items);

                    List<Video> videos = new ArrayList<>(dtos.size());
                    for (VKApiVideo dto : dtos) {
                        videos.add(Dto2Model.transform(dto));
                    }

                    return videos;
                });
    }
}