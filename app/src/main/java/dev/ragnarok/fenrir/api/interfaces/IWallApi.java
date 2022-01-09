package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;

import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.IdPair;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.PostsResponse;
import dev.ragnarok.fenrir.api.model.response.RepostReponse;
import dev.ragnarok.fenrir.api.model.response.WallResponse;
import dev.ragnarok.fenrir.api.model.response.WallSearchResponse;
import io.reactivex.rxjava3.core.Single;


public interface IWallApi {

    Single<WallSearchResponse> search(int ownerId, String query, Boolean ownersOnly,
                                      int count, int offset, Boolean extended, String fields);

    @CheckResult
    Single<Boolean> edit(Integer ownerId, Integer postId, Boolean friendsOnly, String message,
                         Collection<IAttachmentToken> attachments, String services,
                         Boolean signed, Long publishDate, Double latitude,
                         Double longitude, Integer placeId, Boolean markAsAds);

    @CheckResult
    Single<Boolean> pin(Integer ownerId, int postId);

    @CheckResult
    Single<Boolean> unpin(Integer ownerId, int postId);

    @CheckResult
    Single<RepostReponse> repost(int postOwnerId, int postId, String message, Integer groupId, Boolean markAsAds);

    @CheckResult
    Single<Integer> post(Integer ownerId, Boolean friendsOnly, Boolean fromGroup, String message,
                         Collection<IAttachmentToken> attachments, String services, Boolean signed,
                         Long publishDate, Double latitude, Double longitude, Integer placeId,
                         Integer postId, Integer guid, Boolean markAsAds, Boolean adsPromotedStealth);

    @CheckResult
    Single<Boolean> delete(Integer ownerId, int postId);

    @CheckResult
    Single<Boolean> restoreComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> deleteComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> restore(Integer ownerId, int postId);

    @CheckResult
    Single<Boolean> editComment(Integer ownerId, int commentId, String message, Collection<IAttachmentToken> attachments);

    @CheckResult
    Single<Integer> createComment(Integer ownerId, int postId, Integer fromGroup,
                                  String message, Integer replyToComment,
                                  Collection<IAttachmentToken> attachments, Integer stickerId,
                                  Integer generatedUniqueId);

    @CheckResult
    Single<WallResponse> get(Integer ownerId, String domain, Integer offset, Integer count,
                             String filter, Boolean extended, String fields);

    @CheckResult
    Single<PostsResponse> getById(Collection<IdPair> ids, Boolean extended,
                                  Integer copyHistoryDepth, String fields);

    @CheckResult
    Single<DefaultCommentsResponse> getComments(int ownerId, int postId, Boolean needLikes,
                                                Integer startCommentId, Integer offset, Integer count,
                                                String sort, Boolean extended, String fields);

    @CheckResult
    Single<Integer> reportPost(Integer owner_id, Integer post_id, Integer reason);

    @CheckResult
    Single<Integer> reportComment(Integer owner_id, Integer post_id, Integer reason);

    @CheckResult
    Single<Integer> subscribe(Integer owner_id);

    @CheckResult
    Single<Integer> unsubscribe(Integer owner_id);
}
