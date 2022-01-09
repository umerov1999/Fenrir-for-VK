package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import java.util.Collection;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IWallApi;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.IdPair;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.PostsResponse;
import dev.ragnarok.fenrir.api.model.response.RepostReponse;
import dev.ragnarok.fenrir.api.model.response.WallResponse;
import dev.ragnarok.fenrir.api.model.response.WallSearchResponse;
import dev.ragnarok.fenrir.api.services.IWallService;
import io.reactivex.rxjava3.core.Single;


class WallApi extends AbsApi implements IWallApi {

    WallApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<WallSearchResponse> search(int ownerId, String query, Boolean ownersOnly, int count, int offset, Boolean extended, String fields) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .search(ownerId, null, query, integerFromBoolean(ownersOnly),
                                count, offset, integerFromBoolean(extended), fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> edit(Integer ownerId, Integer postId, Boolean friendsOnly, String message, Collection<IAttachmentToken> attachments, String services, Boolean signed, Long publishDate, Double latitude, Double longitude, Integer placeId, Boolean markAsAds) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .edit(ownerId, postId, integerFromBoolean(friendsOnly), message,
                                join(attachments, ",", AbsApi::formatAttachmentToken), services,
                                integerFromBoolean(signed), publishDate, latitude, longitude, placeId,
                                integerFromBoolean(markAsAds))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response.postId != 0));
    }

    @Override
    public Single<Boolean> pin(Integer ownerId, int postId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.pin(ownerId, postId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> unpin(Integer ownerId, int postId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.unpin(ownerId, postId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<RepostReponse> repost(int ownerId, int postId, String message, Integer groupId, Boolean markAsAds) {
        String object = "wall" + ownerId + "_" + postId;
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.repost(object, message, groupId, integerFromBoolean(markAsAds))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> post(Integer ownerId, Boolean friendsOnly, Boolean fromGroup, String message,
                                Collection<IAttachmentToken> attachments, String services, Boolean signed, Long publishDate, Double latitude, Double longitude, Integer placeId, Integer postId, Integer guid, Boolean markAsAds, Boolean adsPromotedStealth) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .post(ownerId, integerFromBoolean(friendsOnly), integerFromBoolean(fromGroup), message,
                                join(attachments, ",", AbsApi::formatAttachmentToken), services, integerFromBoolean(signed),
                                publishDate, latitude, longitude, placeId, postId, guid, integerFromBoolean(markAsAds),
                                integerFromBoolean(adsPromotedStealth))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response.postId));
    }

    @Override
    public Single<Boolean> delete(Integer ownerId, int postId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.delete(ownerId, postId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> restoreComment(Integer ownerId, int commentId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.restoreComment(ownerId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> deleteComment(Integer ownerId, int commentId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.deleteComment(ownerId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> restore(Integer ownerId, int postId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.restore(ownerId, postId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> editComment(Integer ownerId, int commentId, String message,
                                       Collection<IAttachmentToken> attachments) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service.editComment(ownerId, commentId, message, join(attachments, ",", AbsApi::formatAttachmentToken))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> createComment(Integer ownerId, int postId, Integer fromGroup, String message,
                                         Integer replyToComment, Collection<IAttachmentToken> attachments,
                                         Integer stickerId, Integer generatedUniqueId) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .createComment(ownerId, postId, fromGroup, message, replyToComment,
                                join(attachments, ",", AbsApi::formatAttachmentToken), stickerId, generatedUniqueId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response.commentId));
    }

    @Override
    public Single<WallResponse> get(Integer ownerId, String domain, Integer offset, Integer count, String filter, Boolean extended, String fields) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .get(ownerId, domain, offset, count, filter, nonNull(extended) ? (extended ? 1 : 0) : null, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<PostsResponse> getById(Collection<IdPair> ids, Boolean extended, Integer copyHistoryDepth, String fields) {
        String line = join(ids, ",", orig -> orig.ownerId + "_" + orig.id);
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .getById(line, nonNull(extended) ? (extended ? 1 : 0) : null, copyHistoryDepth, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> reportPost(Integer owner_id, Integer post_id, Integer reason) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .reportPost(owner_id, post_id, reason)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> subscribe(Integer owner_id) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .subscribe(owner_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> unsubscribe(Integer owner_id) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .unsubscribe(owner_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> reportComment(Integer owner_id, Integer post_id, Integer reason) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .reportComment(owner_id, post_id, reason)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<DefaultCommentsResponse> getComments(int ownerId, int postId, Boolean needLikes,
                                                       Integer startCommentId, Integer offset, Integer count,
                                                       String sort, Boolean extended, String fields) {
        return provideService(IWallService.class, TokenType.USER)
                .flatMap(service -> service
                        .getComments(ownerId, postId, integerFromBoolean(needLikes), startCommentId, offset, count, sort, integerFromBoolean(extended), 10, fields)
                        .map(extractResponseWithErrorHandling()));
    }
}