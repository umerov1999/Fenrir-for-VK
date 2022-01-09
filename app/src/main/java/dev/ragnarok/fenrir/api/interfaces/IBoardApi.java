package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;

import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.TopicsResponse;
import io.reactivex.rxjava3.core.Single;


public interface IBoardApi {

    @CheckResult
    Single<DefaultCommentsResponse> getComments(int groupId, int topicId, Boolean needLikes, Integer startCommentId,
                                                Integer offset, Integer count, Boolean extended,
                                                String sort, String fields);

    @CheckResult
    Single<Boolean> restoreComment(int groupId, int topicId, int commentId);

    @CheckResult
    Single<Boolean> deleteComment(int groupId, int topicId, int commentId);

    @CheckResult
    Single<TopicsResponse> getTopics(int groupId, Collection<Integer> topicIds, Integer order,
                                     Integer offset, Integer count, Boolean extended,
                                     Integer preview, Integer previewLength, String fields);

    @CheckResult
    Single<Boolean> editComment(int groupId, int topicId, int commentId,
                                String message, Collection<IAttachmentToken> attachments);

    @CheckResult
    Single<Integer> addComment(Integer groupId, int topicId, String message,
                               Collection<IAttachmentToken> attachments, Boolean fromGroup,
                               Integer stickerId, Integer generatedUniqueId);

}
