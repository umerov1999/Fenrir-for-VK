package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.IdPair;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.interfaces.IWallStorage;
import dev.ragnarok.fenrir.db.model.PostPatch;
import dev.ragnarok.fenrir.db.model.PostUpdate;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.domain.mappers.Model2Dto;
import dev.ragnarok.fenrir.domain.mappers.Model2Entity;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class WallsRepository implements IWallsRepository {

    private final INetworker networker;

    private final IStorages storages;

    private final IOwnersRepository ownersRepository;

    private final PublishSubject<PostUpdate> minorUpdatesPublisher = PublishSubject.create();

    private final PublishSubject<Post> majorUpdatesPublisher = PublishSubject.create();

    private final PublishSubject<dev.ragnarok.fenrir.model.IdPair> postInvalidatePublisher = PublishSubject.create();

    public WallsRepository(INetworker networker, IStorages storages, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.storages = storages;
        this.ownersRepository = ownersRepository;
    }

    private static PostPatch update2patch(PostUpdate data) {
        PostPatch patch = new PostPatch();
        if (nonNull(data.getDeleteUpdate())) {
            patch.withDeletion(data.getDeleteUpdate().isDeleted());
        }

        if (nonNull(data.getLikeUpdate())) {
            patch.withLikes(data.getLikeUpdate().getCount(), data.getLikeUpdate().isLiked());
        }

        if (nonNull(data.getPinUpdate())) {
            patch.withPin(data.getPinUpdate().isPinned());
        }

        return patch;
    }

    private static String convertToApiFilter(int filter) {
        switch (filter) {
            case WallCriteria.MODE_ALL:
                return "all";
            case WallCriteria.MODE_OWNER:
                return "owner";
            case WallCriteria.MODE_SCHEDULED:
                return "postponed";
            case WallCriteria.MODE_SUGGEST:
                return "suggests";
            default:
                throw new IllegalArgumentException("Invalid wall filter");
        }
    }

    private static Collection<IdPair> singlePair(int postId, int postOwnerId) {
        return Collections.singletonList(new IdPair(postId, postOwnerId));
    }

    @Override
    public Completable editPost(int accountId, int ownerId, int postId, Boolean friendsOnly,
                                String message, List<AbsModel> attachments, String services,
                                Boolean signed, Long publishDate, Double latitude, Double longitude,
                                Integer placeId, Boolean markAsAds) {

        List<IAttachmentToken> tokens = null;

        try {
            if (nonEmpty(attachments)) {
                tokens = Model2Dto.createTokens(attachments);
            }
        } catch (Exception e) {
            return Completable.error(e);
        }

        return networker.vkDefault(accountId)
                .wall()
                .edit(ownerId, postId, friendsOnly, message, tokens, services,
                        signed, publishDate, latitude, longitude, placeId, markAsAds)
                .flatMapCompletable(aBoolean -> getAndStorePost(accountId, ownerId, postId).ignoreElement());
    }

    @Override
    public Single<Post> post(int accountId, int ownerId, Boolean friendsOnly, Boolean fromGroup, String message,
                             List<AbsModel> attachments, String services, Boolean signed,
                             Long publishDate, Double latitude, Double longitude, Integer placeId,
                             Integer postId, Integer guid, Boolean markAsAds, Boolean adsPromotedStealth) {

        List<IAttachmentToken> tokens = null;

        try {
            if (nonEmpty(attachments)) {
                tokens = Model2Dto.createTokens(attachments);
            }
        } catch (Exception e) {
            return Single.error(e);
        }

        return networker.vkDefault(accountId)
                .wall()
                .post(ownerId, friendsOnly, fromGroup, message, tokens, services, signed, publishDate,
                        latitude, longitude, placeId, postId, guid, markAsAds, adsPromotedStealth)
                .flatMap(vkid -> {
                    Completable completable;

                    if (nonNull(postId) && !postId.equals(vkid)) {
                        // если id поста изменился - удаляем его из бд
                        completable = invalidatePost(accountId, postId, ownerId);
                    } else {
                        completable = Completable.complete();
                    }

                    return completable.andThen(getAndStorePost(accountId, ownerId, vkid));
                });
    }

    private Completable invalidatePost(int accountId, int postId, int ownerId) {
        dev.ragnarok.fenrir.model.IdPair pair = new dev.ragnarok.fenrir.model.IdPair(postId, ownerId);

        return storages.wall()
                .invalidatePost(accountId, postId, ownerId)
                .doOnComplete(() -> postInvalidatePublisher.onNext(pair));
    }

    @Override
    public Single<Integer> like(int accountId, int ownerId, int postId, boolean add) {
        Single<Integer> single;
        if (add) {
            single = networker.vkDefault(accountId)
                    .likes()
                    .add("post", ownerId, postId, null);
        } else {
            single = networker.vkDefault(accountId)
                    .likes()
                    .delete("post", ownerId, postId, null);
        }

        return single.flatMap(count -> {
            // TODO: 05.09.2017 Сохранение лайков в таблице новостей надо ?
            PostUpdate update = new PostUpdate(accountId, postId, ownerId).withLikes(count, add);
            return applyPatch(update).andThen(Single.just(count));
        });
    }

    @Override
    public Single<Integer> checkAndAddLike(int accountId, int ownerId, int postId) {
        return networker.vkDefault(accountId)
                .likes().checkAndAddLike("post", ownerId, postId, null);
    }

    @Override
    public Single<Boolean> isLiked(int accountId, int ownerId, int postId) {
        return networker.vkDefault(accountId)
                .likes()
                .isLiked("post", ownerId, postId);
    }

    @Override
    public Single<List<Post>> getWallNoCache(int accountId, int ownerId, int offset, int count, int wallFilter) {
        return networker.vkDefault(accountId)
                .wall()
                .get(ownerId, null, offset, count, convertToApiFilter(wallFilter), true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    List<VKApiPost> dtos = Utils.listEmptyIfNull(response.posts);

                    VKOwnIds ids = new VKOwnIds();
                    for (VKApiPost dto : dtos) {
                        ids.append(dto);
                    }
                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .flatMap(bundle -> {
                                List<Post> posts = Dto2Model.transformPosts(dtos, bundle);
                                return Single.just(posts);
                            });
                });
    }

    @Override
    public Single<List<Post>> getWall(int accountId, int ownerId, int offset, int count, int wallFilter) {
        return networker.vkDefault(accountId)
                .wall()
                .get(ownerId, null, offset, count, convertToApiFilter(wallFilter), true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    List<VKApiPost> dtos = Utils.listEmptyIfNull(response.posts);

                    VKOwnIds ids = new VKOwnIds();
                    for (VKApiPost dto : dtos) {
                        ids.append(dto);
                    }

                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);
                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .flatMap(bundle -> {
                                List<Post> posts = Dto2Model.transformPosts(dtos, bundle);

                                List<PostEntity> dbos = new ArrayList<>(dtos.size());
                                for (VKApiPost dto : dtos) {
                                    dbos.add(Dto2Entity.mapPost(dto));
                                }

                                return storages.wall()
                                        .storeWallEntities(accountId, dbos, ownerEntities, offset == 0 ? () -> ownerId : null)
                                        .map(optional -> posts);
                            });
                });
    }

    private SingleTransformer<List<PostEntity>, List<Post>> entities2models(int accountId) {
        return single -> single
                .flatMap(dbos -> {
                    VKOwnIds ids = new VKOwnIds();
                    Entity2Model.fillOwnerIds(ids, dbos);

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<Post> posts = new ArrayList<>(dbos.size());
                                for (PostEntity dbo : dbos) {
                                    posts.add(Entity2Model.buildPostFromDbo(dbo, owners));
                                }
                                return posts;
                            });
                });
    }

    private SingleTransformer<PostEntity, Post> entity2model(int accountId) {
        return single -> single
                .flatMap(dbo -> {
                    VKOwnIds ids = new VKOwnIds();
                    Entity2Model.fillPostOwnerIds(ids, dbo);

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> Entity2Model.buildPostFromDbo(dbo, owners));
                });
    }

    @Override
    public Single<List<Post>> getCachedWall(int accountId, int ownerId, int wallFilter) {
        WallCriteria criteria = new WallCriteria(accountId, ownerId).setMode(wallFilter);
        return storages.wall()
                .findDbosByCriteria(criteria)
                .compose(entities2models(accountId));
    }

    private Completable applyPatch(PostUpdate update) {
        PostPatch patch = update2patch(update);

        return storages.wall()
                .update(update.getAccountId(), update.getOwnerId(), update.getPostId(), patch)
                .andThen(Completable.fromAction(() -> minorUpdatesPublisher.onNext(update)));
    }

    @Override
    public Completable delete(int accountId, int ownerId, int postId) {
        PostUpdate update = new PostUpdate(accountId, postId, ownerId).withDeletion(true);
        return networker.vkDefault(accountId)
                .wall()
                .delete(ownerId, postId)
                .flatMapCompletable(igrored -> applyPatch(update));
    }

    @Override
    public Completable restore(int accountId, int ownerId, int postId) {
        PostUpdate update = new PostUpdate(accountId, postId, ownerId).withDeletion(false);
        return networker.vkDefault(accountId)
                .wall()
                .restore(ownerId, postId)
                .flatMapCompletable(igrored -> applyPatch(update));
    }

    @Override
    public Single<Integer> reportPost(int accountId, int owner_id, int post_id, int reason) {
        return networker.vkDefault(accountId)
                .wall()
                .reportPost(owner_id, post_id, reason);
    }

    @Override
    public Single<Integer> subscribe(int accountId, int owner_id) {
        return networker.vkDefault(accountId)
                .wall()
                .subscribe(owner_id);
    }

    @Override
    public Single<Integer> unsubscribe(int accountId, int owner_id) {
        return networker.vkDefault(accountId)
                .wall()
                .unsubscribe(owner_id);
    }

    @Override
    public Single<Post> getById(int accountId, int ownerId, int postId) {
        IdPair id = new IdPair(postId, ownerId);

        return networker.vkDefault(accountId)
                .wall()
                .getById(Collections.singleton(id), true, 5, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (isEmpty(response.posts)) {
                        throw new NotFoundException();
                    }

                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    List<VKApiPost> dtos = response.posts;
                    VKApiPost dto = dtos.get(0);

                    VKOwnIds ids = new VKOwnIds().append(dto);
                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .map(bundle -> Dto2Model.transform(dto, bundle));
                });
    }

    @Override
    public Completable pinUnpin(int accountId, int ownerId, int postId, boolean pin) {
        Single<Boolean> single;

        if (pin) {
            single = networker.vkDefault(accountId)
                    .wall()
                    .pin(ownerId, postId);
        } else {
            single = networker.vkDefault(accountId)
                    .wall()
                    .unpin(ownerId, postId);
        }

        PostUpdate update = new PostUpdate(accountId, postId, ownerId).withPin(pin);
        return single.flatMapCompletable(ignored -> applyPatch(update));
    }

    @Override
    public Observable<PostUpdate> observeMinorChanges() {
        return minorUpdatesPublisher;
    }

    @Override
    public Observable<Post> observeChanges() {
        return majorUpdatesPublisher;
    }

    @Override
    public Observable<dev.ragnarok.fenrir.model.IdPair> observePostInvalidation() {
        return postInvalidatePublisher;
    }

    @Override
    public Single<Post> getEditingPost(int accountId, int ownerId, int type, boolean withAttachments) {
        return storages.wall()
                .getEditingPost(accountId, ownerId, type, withAttachments)
                .compose(entity2model(accountId));
    }

    @Override
    public Single<Post> post(int accountId, Post post, boolean fromGroup, boolean showSigner) {
        Long publishDate = post.isPostponed() ? post.getDate() : null;

        List<AbsModel> attachments = post.hasAttachments() ? post.getAttachments().toList() : null;

        Integer postponedPostId = post.isPostponed() ? (post.getVkid() > 0 ? post.getVkid() : null) : null;

        return post(accountId, post.getOwnerId(), post.isFriendsOnly(), fromGroup, post.getText(),
                attachments, null, showSigner, publishDate, null, null, null,
                postponedPostId, post.getDbid(), null, null);
    }

    @Override
    public Single<Post> repost(int accountId, int postId, int ownerId, Integer groupId, String message) {
        int resultOwnerId = nonNull(groupId) ? -Math.abs(groupId) : accountId;
        return networker.vkDefault(accountId)
                .wall()
                .repost(ownerId, postId, message, groupId, null)
                .flatMap(reponse -> getAndStorePost(accountId, resultOwnerId, reponse.postId));
    }

    @Override
    public Single<Integer> cachePostWithIdSaving(int accountId, Post post) {
        PostEntity entity = Model2Entity.buildPostDbo(post);

        return storages.wall()
                .replacePost(accountId, entity);
    }

    @Override
    public Completable deleteFromCache(int accountId, int postDbid) {
        return storages.wall().deletePost(accountId, postDbid);
    }

    private Single<Post> getAndStorePost(int accountId, int ownerId, int postId) {
        IWallStorage cache = storages.wall();

        return networker.vkDefault(accountId)
                .wall()
                .getById(singlePair(postId, ownerId), true, 5, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (safeCountOf(response.posts) != 1) {
                        throw new NotFoundException();
                    }

                    PostEntity dbo = Dto2Entity.mapPost(response.posts.get(0));

                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);
                    return cache.storeWallEntities(accountId, Collections.singletonList(dbo), ownerEntities, null)
                            .map(ints -> ints[0])
                            .flatMap(dbid -> cache
                                    .findPostById(accountId, dbid)
                                    .map(Optional::get)
                                    .compose(entity2model(accountId)));
                })
                .map(post -> {
                    majorUpdatesPublisher.onNext(post);
                    return post;
                });
    }

    @Override
    public Single<Pair<List<Post>, Integer>> search(int accountId, int ownerId, String query, boolean ownersPostOnly, int count, int offset) {
        return networker.vkDefault(accountId)
                .wall()
                .search(ownerId, query, ownersPostOnly, count, offset, true, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiPost> dtos = Utils.listEmptyIfNull(response.items);
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ids = new VKOwnIds();
                    for (VKApiPost dto : dtos) {
                        ids.append(dto);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .map(ownersBundle -> Pair.Companion.create(Dto2Model.transformPosts(dtos, ownersBundle), response.count));
                });
    }
}
