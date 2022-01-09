package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;

import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VKApiVideoAlbum;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.SearchVideoResponse;
import io.reactivex.rxjava3.core.Single;


public interface IVideoApi {

    @CheckResult
    Single<DefaultCommentsResponse> getComments(Integer ownerId, int videoId, Boolean needLikes,
                                                Integer startCommentId, Integer offset, Integer count, String sort,
                                                Boolean extended, String fields);

    @CheckResult
    Single<Integer> addVideo(Integer targetId, Integer videoId, Integer ownerId);

    @CheckResult
    Single<Integer> deleteVideo(Integer videoId, Integer ownerId, Integer targetId);

    @CheckResult
    Single<Items<VKApiVideoAlbum>> getAlbums(Integer ownerId, Integer offset, Integer count, Boolean needSystem);

    @CheckResult
    Single<Items<VKApiVideoAlbum>> getAlbumsByVideo(Integer target_id, Integer owner_id, Integer video_id);

    @CheckResult
    Single<SearchVideoResponse> search(String query, Integer sort, Boolean hd, Boolean adult, String filters,
                                       Boolean searchOwn, Integer offset, Integer longer, Integer shorter,
                                       Integer count, Boolean extended);

    @CheckResult
    Single<Boolean> restoreComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Boolean> deleteComment(Integer ownerId, int commentId);

    @CheckResult
    Single<Items<VKApiVideo>> get(Integer ownerId, Collection<AccessIdPair> ids, Integer albumId,
                                  Integer count, Integer offset, Boolean extended);

    @CheckResult
    Single<Integer> createComment(Integer ownerId, int videoId, String message,
                                  Collection<IAttachmentToken> attachments, Boolean fromGroup,
                                  Integer replyToComment, Integer stickerId, Integer uniqueGeneratedId);


    @CheckResult
    Single<Boolean> editComment(Integer ownerId, int commentId, String message, Collection<IAttachmentToken> attachments);

    @CheckResult
    Single<Boolean> edit(Integer ownerId, int video_id, String name, String desc);

}
