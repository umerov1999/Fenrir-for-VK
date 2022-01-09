package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.TopicsResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface IBoardService {

    //https://vk.com/dev/board.getComments
    @FormUrlEncoded
    @POST("board.getComments")
    Single<BaseResponse<DefaultCommentsResponse>> getComments(@Field("group_id") int groupId,
                                                              @Field("topic_id") int topicId,
                                                              @Field("need_likes") Integer needLikes,
                                                              @Field("start_comment_id") Integer startCommentId,
                                                              @Field("offset") Integer offset,
                                                              @Field("count") Integer count,
                                                              @Field("extended") Integer extended,
                                                              @Field("sort") String sort,
                                                              @Field("fields") String fields);

    //https://vk.com/dev/board.restoreComment
    @FormUrlEncoded
    @POST("board.restoreComment")
    Single<BaseResponse<Integer>> restoreComment(@Field("group_id") int groupId,
                                                 @Field("topic_id") int topicId,
                                                 @Field("comment_id") int commentId);

    //https://vk.com/dev/board.deleteComment
    @FormUrlEncoded
    @POST("board.deleteComment")
    Single<BaseResponse<Integer>> deleteComment(@Field("group_id") int groupId,
                                                @Field("topic_id") int topicId,
                                                @Field("comment_id") int commentId);

    /**
     * Returns a list of topics on a community's discussion board.
     *
     * @param groupId       ID of the community that owns the discussion board.
     * @param topicIds      IDs of topics to be returned (100 maximum). By default, all topics are returned.
     *                      If this parameter is set, the order, offset, and count parameters are ignored.
     *                      List of comma-separated numbers
     * @param order         Sort order:
     *                      1 — by date updated in reverse chronological order.
     *                      2 — by date created in reverse chronological order.
     *                      -1 — by date updated in chronological order.
     *                      -2 — by date created in chronological order.
     *                      If no sort order is specified, topics are returned in the order specified by the group administrator.
     *                      Pinned topics are returned first, regardless of the sorting.
     * @param offset        Offset needed to return a specific subset of topics.
     * @param count         Number of topics to return.
     * @param extended      1 — to return information about users who created topics or who posted there last
     *                      0 — to return no additional fields (default)
     * @param preview       1 — to return the first comment in each topic;
     *                      2 — to return the last comment in each topic;
     *                      0 — to return no comments.
     *                      By default: 0.
     * @param previewLength Number of characters after which to truncate the previewed comment.
     *                      To preview the full comment, specify 0. Default 90
     * @return array of objects describing topics.
     */
    @FormUrlEncoded
    @POST("board.getTopics")
    Single<BaseResponse<TopicsResponse>> getTopics(@Field("group_id") int groupId,
                                                   @Field("topic_ids") String topicIds,
                                                   @Field("order") Integer order,
                                                   @Field("offset") Integer offset,
                                                   @Field("count") Integer count,
                                                   @Field("extended") Integer extended,
                                                   @Field("preview") Integer preview,
                                                   @Field("preview_length") Integer previewLength,
                                                   @Field("fields") String fields); // not doccumented

    /**
     * Edits a comment on a topic on a community's discussion board.
     *
     * @param groupId     ID of the community that owns the discussion board.
     * @param topicId     Topic ID.
     * @param commentId   ID of the comment on the topic.
     * @param message     (Required if attachments is not set). New comment text.
     * @param attachments (Required if message is not set.) List of objects attached to the post, in the following format:
     *                    {type}{owner_id}_{media_id},{type}{owner_id}_{media_id}
     *                    {type} — Type of media attachment:
     *                    photo — photo
     *                    video — video
     *                    audio — audio
     *                    doc — document
     *                    {owner_id} — Media attachment owner ID.
     *                    {media_id} — Media attachment ID.
     *                    Example:
     *                    photo100172_166443618,photo66748_265827614
     *                    List of comma-separated words
     * @return 1
     */
    @FormUrlEncoded
    @POST("board.editComment")
    Single<BaseResponse<Integer>> editComment(@Field("group_id") int groupId,
                                              @Field("topic_id") int topicId,
                                              @Field("comment_id") int commentId,
                                              @Field("message") String message,
                                              @Field("attachments") String attachments);

    @FormUrlEncoded
    @POST("board.addComment")
    Single<BaseResponse<Integer>> addComment(@Field("group_id") Integer groupId,
                                             @Field("topic_id") int topicId,
                                             @Field("message") String message,
                                             @Field("attachments") String attachments,
                                             @Field("from_group") Integer fromGroup,
                                             @Field("sticker_id") Integer stickerId,
                                             @Field("guid") Integer generatedUniqueId);

}
