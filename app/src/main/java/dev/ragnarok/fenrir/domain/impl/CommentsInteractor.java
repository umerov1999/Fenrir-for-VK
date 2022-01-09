package dev.ragnarok.fenrir.domain.impl;

import static java.util.Collections.emptyList;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.interfaces.IAccountApis;
import dev.ragnarok.fenrir.api.interfaces.IBoardApi;
import dev.ragnarok.fenrir.api.interfaces.ILikesApi;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.interfaces.IPhotosApi;
import dev.ragnarok.fenrir.api.interfaces.IVideoApi;
import dev.ragnarok.fenrir.api.interfaces.IWallApi;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.db.column.GroupColumns;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.entity.CommentEntity;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.domain.ICommentsInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Dto;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.domain.mappers.Model2Dto;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.CommentIntent;
import dev.ragnarok.fenrir.model.CommentUpdate;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.CommentedType;
import dev.ragnarok.fenrir.model.CommentsBundle;
import dev.ragnarok.fenrir.model.DraftComment;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.criteria.CommentsCriteria;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.functions.BooleanSupplier;

public class CommentsInteractor implements ICommentsInteractor {

    private final INetworker networker;

    private final IStorages cache;

    private final IOwnersRepository ownersRepository;

    public CommentsInteractor(INetworker networker, IStorages cache, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.cache = cache;
        this.ownersRepository = ownersRepository;
    }

    @Override
    public Single<List<Comment>> getAllCachedData(int accountId, @NonNull Commented commented) {
        CommentsCriteria criteria = new CommentsCriteria(accountId, commented);
        return cache.comments()
                .getDbosByCriteria(criteria)
                .compose(dbos2models(accountId));
    }

    private SingleTransformer<List<CommentEntity>, List<Comment>> dbos2models(int accountId) {
        return single -> single.flatMap(dbos -> {
            VKOwnIds ownids = new VKOwnIds();
            for (CommentEntity c : dbos) {
                Entity2Model.fillCommentOwnerIds(ownids, c);
            }

            return ownersRepository
                    .findBaseOwnersDataAsBundle(accountId, ownids.getAll(), IOwnersRepository.MODE_ANY)
                    .map(owners -> {
                        List<Comment> comments = new ArrayList<>(dbos.size());
                        for (CommentEntity dbo : dbos) {
                            comments.add(Entity2Model.buildCommentFromDbo(dbo, owners));
                        }
                        return comments;
                    });
        });
    }

    private Completable cacheData(int accountId, @NonNull Commented commented, List<CommentEntity> data, OwnerEntities owners, boolean invalidateCache) {
        int sourceId = commented.getSourceId();
        int ownerId = commented.getSourceOwnerId();
        int type = commented.getSourceType();

        return Single.just(data)
                .flatMapCompletable(dbos -> cache.comments().insert(accountId, sourceId, ownerId, type, dbos, owners, invalidateCache)
                        .ignoreElement());
    }

    private Single<List<Comment>> transform(int accountId, @NonNull Commented commented, @NonNull List<VKApiComment> comments, Collection<VKApiUser> users, Collection<VKApiCommunity> groups) {
        VKOwnIds ownids = new VKOwnIds();
        for (VKApiComment dto : comments) {
            ownids.append(dto);
        }

        return ownersRepository
                .findBaseOwnersDataAsBundle(accountId, ownids.getAll(), IOwnersRepository.MODE_ANY, Dto2Model.transformOwners(users, groups))
                .map(bundle -> {
                    List<Comment> data = new ArrayList<>(comments.size());
                    for (VKApiComment dto : comments) {
                        Comment cm = Dto2Model.buildComment(commented, dto, bundle);
                        if (nonNull(cm)) {
                            data.add(cm);
                        }
                    }
                    Collections.sort(data, (o1, o2) -> Integer.compare(o2.getId(), o1.getId()));
                    return data;
                });
    }

    @Override
    public Single<List<Comment>> getCommentsNoCache(int accountId, int ownerId, int postId, int offset) {
        return networker.vkDefault(accountId)
                .comments()
                .get("post", ownerId, postId, offset, 100, "desc", null, null, null, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiComment> commentDtos = nonNull(response.main) ? listEmptyIfNull(response.main.comments) : emptyList();
                    List<VKApiUser> users = nonNull(response.main) ? listEmptyIfNull(response.main.profiles) : emptyList();
                    List<VKApiCommunity> groups = nonNull(response.main) ? listEmptyIfNull(response.main.groups) : emptyList();

                    return transform(accountId, new Commented(postId, ownerId, CommentedType.POST, null), commentDtos, users, groups);
                });
    }

    @Override
    public Single<CommentsBundle> getCommentsPortion(int accountId, @NonNull Commented commented, int offset, int count, Integer startCommentId, Integer threadComment, boolean invalidateCache, String sort) {
        String type = commented.getTypeForStoredProcedure();

        return networker.vkDefault(accountId)
                .comments()
                .get(type, commented.getSourceOwnerId(), commented.getSourceId(), offset, count, sort, startCommentId, threadComment, commented.getAccessKey(), Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<VKApiComment> commentDtos = nonNull(response.main) ? listEmptyIfNull(response.main.comments) : emptyList();
                    List<VKApiUser> users = nonNull(response.main) ? listEmptyIfNull(response.main.profiles) : emptyList();
                    List<VKApiCommunity> groups = nonNull(response.main) ? listEmptyIfNull(response.main.groups) : emptyList();

                    Single<List<Comment>> modelsSingle = transform(accountId, commented, commentDtos, users, groups);

                    List<CommentEntity> dbos = new ArrayList<>(commentDtos.size());
                    for (VKApiComment dto : commentDtos)
                        dbos.add(Dto2Entity.mapComment(commented.getSourceId(), commented.getSourceOwnerId(), commented.getSourceType(), commented.getAccessKey(), dto));

                    if (threadComment != null) {
                        return modelsSingle.map(data -> {
                            CommentsBundle bundle = new CommentsBundle(data)
                                    .setAdminLevel(response.admin_level)
                                    .setFirstCommentId(response.firstId)
                                    .setLastCommentId(response.lastId);

                            if (nonNull(response.main) && nonNull(response.main.poll)) {
                                Poll poll = Dto2Model.transform(response.main.poll);
                                poll.setBoard(true); // так как это может быть только из топика
                                bundle.setTopicPoll(poll);
                            }

                            return bundle;
                        });
                    }

                    return cacheData(accountId, commented, dbos, Dto2Entity.mapOwners(users, groups), invalidateCache)
                            .andThen(modelsSingle.map(data -> {
                                CommentsBundle bundle = new CommentsBundle(data)
                                        .setAdminLevel(response.admin_level)
                                        .setFirstCommentId(response.firstId)
                                        .setLastCommentId(response.lastId);

                                if (nonNull(response.main) && nonNull(response.main.poll)) {
                                    Poll poll = Dto2Model.transform(response.main.poll);
                                    poll.setBoard(true); // так как это может быть только из топика
                                    bundle.setTopicPoll(poll);
                                }

                                return bundle;
                            }));
                });
    }

    @Override
    public Maybe<DraftComment> restoreDraftComment(int accountId, @NonNull Commented commented) {
        return cache.comments()
                .findEditingComment(accountId, commented);
    }

    @Override
    public Single<Integer> safeDraftComment(int accountId, @NonNull Commented commented, String body, int replyToCommentId, int replyToUserId) {
        return cache.comments()
                .saveDraftComment(accountId, commented, body, replyToUserId, replyToCommentId);
    }

    @Override
    public Single<Boolean> isLiked(int accountId, Commented commented, int commentId) {
        String type;

        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                type = "photo_comment";
                break;
            case CommentedType.POST:
                type = "comment";
                break;
            case CommentedType.VIDEO:
                type = "video_comment";
                break;
            case CommentedType.TOPIC:
                type = "topic_comment";
                break;

            default:
                throw new IllegalArgumentException();
        }
        return networker.vkDefault(accountId)
                .likes()
                .isLiked(type, commented.getSourceOwnerId(), commentId);
    }

    @Override
    public Single<Integer> checkAndAddLike(int accountId, Commented commented, int commentId) {
        String type;

        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                type = "photo_comment";
                break;
            case CommentedType.POST:
                type = "comment";
                break;
            case CommentedType.VIDEO:
                type = "video_comment";
                break;
            case CommentedType.TOPIC:
                type = "topic_comment";
                break;

            default:
                throw new IllegalArgumentException();
        }
        return networker.vkDefault(accountId)
                .likes().checkAndAddLike(type, commented.getSourceOwnerId(), commentId, commented.getAccessKey());
    }

    @Override
    public Completable like(int accountId, Commented commented, int commentId, boolean add) {
        String type;

        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                type = "photo_comment";
                break;
            case CommentedType.POST:
                type = "comment";
                break;
            case CommentedType.VIDEO:
                type = "video_comment";
                break;
            case CommentedType.TOPIC:
                type = "topic_comment";
                break;

            default:
                throw new IllegalArgumentException();
        }

        ILikesApi api = networker.vkDefault(accountId).likes();

        CommentUpdate update = CommentUpdate.create(accountId, commented, commentId);

        if (add) {
            return api.add(type, commented.getSourceOwnerId(), commentId, commented.getAccessKey())
                    .flatMapCompletable(count -> {
                        update.withLikes(true, count);
                        return cache.comments().commitMinorUpdate(update);
                    });
        } else {
            return api.delete(type, commented.getSourceOwnerId(), commentId, commented.getAccessKey())
                    .flatMapCompletable(count -> {
                        update.withLikes(false, count);
                        return cache.comments().commitMinorUpdate(update);
                    });
        }
    }

    @Override
    public Completable deleteRestore(int accountId, Commented commented, int commentId, boolean delete) {
        IAccountApis apis = networker.vkDefault(accountId);
        int ownerId = commented.getSourceOwnerId();
        CommentUpdate update = CommentUpdate.create(accountId, commented, commentId)
                .withDeletion(delete);

        Single<Boolean> single;

        switch (commented.getSourceType()) {
            case CommentedType.PHOTO:
                IPhotosApi photosApi = apis.photos();
                if (delete) {
                    single = photosApi.deleteComment(ownerId, commentId);
                } else {
                    single = photosApi.restoreComment(ownerId, commentId);
                }
                break;

            case CommentedType.POST:
                IWallApi wallApi = apis.wall();
                if (delete) {
                    single = wallApi.deleteComment(ownerId, commentId);
                } else {
                    single = wallApi.restoreComment(ownerId, commentId);
                }
                break;

            case CommentedType.VIDEO:
                IVideoApi videoApi = apis.video();
                if (delete) {
                    single = videoApi.deleteComment(ownerId, commentId);
                } else {
                    single = videoApi.restoreComment(ownerId, commentId);
                }
                break;

            case CommentedType.TOPIC:
                int groupId = Math.abs(ownerId);
                int topicId = commented.getSourceId();

                IBoardApi boardApi = apis.board();
                if (delete) {
                    single = boardApi.deleteComment(groupId, topicId, commentId);
                } else {
                    single = boardApi.restoreComment(groupId, topicId, commentId);
                }
                break;

            default:
                throw new UnsupportedOperationException();
        }

        return single.flatMapCompletable(ignore -> cache
                .comments()
                .commitMinorUpdate(update));
    }

    @Override
    public Single<Comment> send(int accountId, Commented commented, Integer commentThread, CommentIntent intent) {
        Single<List<IAttachmentToken>> cachedAttachments;

        if (nonNull(intent.getDraftMessageId())) {
            cachedAttachments = getCachedAttachmentsToken(accountId, intent.getDraftMessageId());
        } else {
            cachedAttachments = Single.just(emptyList());
        }

        return cachedAttachments
                .flatMap(cachedTokens -> {
                    List<IAttachmentToken> tokens = new ArrayList<>();

                    if (nonNull(cachedTokens)) {
                        tokens.addAll(cachedTokens);
                    }

                    if (nonEmpty(intent.getModels())) {
                        tokens.addAll(Model2Dto.createTokens(intent.getModels()));
                    }

                    return sendComment(accountId, commented, intent, tokens)
                            .flatMap(id -> getCommentByIdAndStore(accountId, commented, id, commentThread, true))
                            .flatMap(comment -> {
                                if (isNull(intent.getDraftMessageId())) {
                                    return Single.just(comment);
                                }

                                return cache.comments()
                                        .deleteByDbid(accountId, intent.getDraftMessageId())
                                        .andThen(Single.just(comment));
                            });
                });
    }

    private Single<List<IAttachmentToken>> getCachedAttachmentsToken(int accountId, int commentDbid) {
        return cache.attachments()
                .getAttachmentsDbosWithIds(accountId, AttachToType.COMMENT, commentDbid)
                .map(pairs -> {
                    List<IAttachmentToken> tokens = new ArrayList<>(pairs.size());
                    for (Pair<Integer, Entity> pair : pairs) {
                        tokens.add(Entity2Dto.createToken(pair.getSecond()));
                    }

                    return tokens;
                });
    }

    @Override
    public Single<List<Comment>> getAllCommentsRange(int accountId, Commented commented, int startFromCommentId, int continueToCommentId) {
        TempData tempData = new TempData();

        BooleanSupplier booleanSupplier = () -> {
            for (VKApiComment c : tempData.comments) {
                if (continueToCommentId == c.id) {
                    return true;
                }
            }

            return false;
        };

        Completable completable = startLooking(accountId, commented, tempData, startFromCommentId, continueToCommentId)
                .repeatUntil(booleanSupplier);

        return completable.toSingleDefault(tempData)
                .flatMap(data -> transform(accountId, commented, data.comments, data.profiles, data.groups));
    }

    @Override
    public Single<List<Owner>> getAvailableAuthors(int accountId) {
        return ownersRepository.getBaseOwnerInfo(accountId, accountId, IOwnersRepository.MODE_ANY)
                .flatMap(owner -> networker.vkDefault(accountId)
                        .groups()
                        .get(accountId, true, "admin,editor", GroupColumns.API_FIELDS, null, 1000)
                        .map(Items::getItems)
                        .map(groups -> {
                            List<Owner> owners = new ArrayList<>(groups.size() + 1);
                            owners.add(owner);
                            owners.addAll(Dto2Model.transformCommunities(groups));
                            return owners;
                        }));
    }

    @Override
    public Single<Comment> edit(int accountId, Commented commented, int commentId, String body, Integer commentThread, List<AbsModel> attachments) {
        List<IAttachmentToken> tokens = new ArrayList<>();

        try {
            if (nonNull(attachments)) {
                tokens.addAll(Model2Dto.createTokens(attachments));
            }
        } catch (Exception e) {
            return Single.error(e);
        }

        Single<Boolean> editSingle;

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                editSingle = networker
                        .vkDefault(accountId)
                        .wall()
                        .editComment(commented.getSourceOwnerId(), commentId, body, tokens);
                break;

            case CommentedType.PHOTO:
                editSingle = networker
                        .vkDefault(accountId)
                        .photos()
                        .editComment(commented.getSourceOwnerId(), commentId, body, tokens);
                break;

            case CommentedType.TOPIC:
                int groupId = Math.abs(commented.getSourceOwnerId());
                int topicId = commented.getSourceId();

                editSingle = networker
                        .vkDefault(accountId)
                        .board()
                        .editComment(groupId, topicId, commentId, body, tokens);
                break;

            case CommentedType.VIDEO:
                editSingle = networker
                        .vkDefault(accountId)
                        .video()
                        .editComment(commented.getSourceOwnerId(), commentId, body, tokens);
                break;

            default:
                return Single.error(new IllegalArgumentException("Unknown commented source type"));
        }

        return editSingle.flatMap(ignored -> getCommentByIdAndStore(accountId, commented, commentId, commentThread, true));
    }

    private Completable startLooking(int accountId, Commented commented, TempData tempData, int startFromCommentId, int continueToCommentId) {
        int[] tryNumber = {0};

        return Single
                .fromCallable(() -> {
                    tryNumber[0]++;

                    if (tryNumber[0] == 1) {
                        return startFromCommentId;
                    }

                    if (tempData.comments.isEmpty()) {
                        throw new NotFoundException();
                    }

                    VKApiComment older = tempData.comments.get(tempData.comments.size() - 1);

                    if (older.id < continueToCommentId) {
                        throw new NotFoundException();
                    }

                    return older.id;
                }).flatMapCompletable(id -> getDefaultCommentsService(accountId, commented, id, 1, 100, "desc", true, Constants.MAIN_OWNER_FIELDS)
                        .map(response -> {
                                    tempData.append(response, continueToCommentId);
                                    return response;
                                }
                        ).ignoreElement());
    }

    private Single<DefaultCommentsResponse> getDefaultCommentsService(int accountId, Commented commented, Integer startCommentId,
                                                                      Integer offset, Integer count, String sort, Boolean extended, String fields) {
        int ownerId = commented.getSourceOwnerId();
        int sourceId = commented.getSourceId();

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                return networker.vkDefault(accountId)
                        .wall()
                        .getComments(ownerId, sourceId, true, startCommentId, offset, count, sort, extended, fields);
            case CommentedType.PHOTO:
                return networker.vkDefault(accountId)
                        .photos()
                        .getComments(ownerId, sourceId, true, startCommentId, offset, count, sort, commented.getAccessKey(), extended, fields);
            case CommentedType.VIDEO:
                return networker.vkDefault(accountId)
                        .video()
                        .getComments(ownerId, sourceId, true, startCommentId, offset, count, sort, extended, fields);
            case CommentedType.TOPIC:
                return networker.vkDefault(accountId)
                        .board()
                        .getComments(Math.abs(ownerId), sourceId, true, startCommentId, offset, count, extended, sort, fields);
        }

        throw new UnsupportedOperationException();
    }

    private Single<Integer> sendComment(int accountId, @NonNull Commented commented, @NonNull CommentIntent intent, @Nullable List<IAttachmentToken> attachments) {
        IAccountApis apies = networker.vkDefault(accountId);

        switch (commented.getSourceType()) {
            case CommentedType.POST:
                Integer fromGroup = intent.getAuthorId() < 0 ? Math.abs(intent.getAuthorId()) : null;

                return apies.wall()
                        .createComment(commented.getSourceOwnerId(), commented.getSourceId(),
                                fromGroup, intent.getMessage(), intent.getReplyToComment(),
                                attachments, intent.getStickerId(), intent.getDraftMessageId());

            case CommentedType.PHOTO:
                return apies.photos()
                        .createComment(commented.getSourceOwnerId(), commented.getSourceId(),
                                intent.getAuthorId() < 0, intent.getMessage(), intent.getReplyToComment(),
                                attachments, intent.getStickerId(), commented.getAccessKey(), intent.getDraftMessageId());

            case CommentedType.VIDEO:
                return apies.video()
                        .createComment(commented.getSourceOwnerId(), commented.getSourceId(),
                                intent.getMessage(), attachments, intent.getAuthorId() < 0,
                                intent.getReplyToComment(), intent.getStickerId(), intent.getDraftMessageId());

            case CommentedType.TOPIC:
                int topicGroupId = Math.abs(commented.getSourceOwnerId());

                return apies.board()
                        .addComment(topicGroupId, commented.getSourceId(), intent.getMessage(),
                                attachments, intent.getAuthorId() < 0, intent.getStickerId(), intent.getDraftMessageId());
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Single<Comment> getCommentByIdAndStore(int accountId, Commented commented, int commentId, Integer commentThread, boolean storeToCache) {
        String type = commented.getTypeForStoredProcedure();
        int sourceId = commented.getSourceId();
        int ownerId = commented.getSourceOwnerId();
        int sourceType = commented.getSourceType();

        return networker.vkDefault(accountId)
                .comments()
                .get(type, commented.getSourceOwnerId(), commented.getSourceId(), 0, 1,
                        null, commentId, commentThread, commented.getAccessKey(), Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    if (isNull(response.main) || safeCountOf(response.main.comments) != 1) {
                        throw new NotFoundException();
                    }

                    List<VKApiComment> comments = response.main.comments;
                    List<VKApiUser> users = response.main.profiles;
                    List<VKApiCommunity> communities = response.main.groups;

                    Completable storeCompletable;
                    if (storeToCache) {
                        List<CommentEntity> dbos = new ArrayList<>(comments.size());
                        for (VKApiComment dto : comments) {
                            dbos.add(Dto2Entity.mapComment(commented.getSourceId(), commented.getSourceOwnerId(), commented.getSourceType(), commented.getAccessKey(), dto));
                        }

                        storeCompletable = cache.comments()
                                .insert(accountId, sourceId, ownerId, sourceType, dbos, Dto2Entity.mapOwners(users, communities), false)
                                .ignoreElement();
                    } else {
                        storeCompletable = Completable.complete();
                    }

                    return storeCompletable.andThen(transform(accountId, commented, comments, users, communities)
                            .map(data -> data.get(0)));
                });
    }

    @Override
    public Single<Integer> reportComment(int accountId, int owner_id, int post_id, int reason) {
        return networker.vkDefault(accountId)
                .wall()
                .reportComment(owner_id, post_id, reason);
    }

    private static final class TempData {

        final Set<VKApiUser> profiles = new HashSet<>();
        final Set<VKApiCommunity> groups = new HashSet<>();
        final List<VKApiComment> comments = new ArrayList<>();

        void append(DefaultCommentsResponse response, int continueToCommentId) {
            if (nonNull(response.groups)) {
                groups.addAll(response.groups);
            }

            if (nonNull(response.profiles)) {
                profiles.addAll(response.profiles);
            }

            boolean hasTargetComment = false;
            int additionalCount = 0;

            for (VKApiComment comment : response.items) {
                if (comment.id == continueToCommentId) {
                    hasTargetComment = true;
                } else if (hasTargetComment) {
                    additionalCount++;
                }

                comments.add(comment);

                if (additionalCount > 5) {
                    break;
                }
            }
        }
    }
}
