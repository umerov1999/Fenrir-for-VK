package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.CommentCreateResponse;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.PostCreateResponse;
import dev.ragnarok.fenrir.api.model.response.PostsResponse;
import dev.ragnarok.fenrir.api.model.response.RepostReponse;
import dev.ragnarok.fenrir.api.model.response.WallEditReponce;
import dev.ragnarok.fenrir.api.model.response.WallResponse;
import dev.ragnarok.fenrir.api.model.response.WallSearchResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IWallService {

    //https://vk.com/dev/wall.search
    @FormUrlEncoded
    @POST("wall.search")
    Single<BaseResponse<WallSearchResponse>> search(@Field("owner_id") Integer ownerId,
                                                    @Field("domain") String domain,
                                                    @Field("query") String query,
                                                    @Field("owners_only") Integer ownersOnly,
                                                    @Field("count") Integer count,
                                                    @Field("offset") Integer offset,
                                                    @Field("extended") Integer extended,
                                                    @Field("fields") String fields);

    //https://vk.com/dev/wall.edit
    @FormUrlEncoded
    @POST("wall.edit")
    Single<BaseResponse<WallEditReponce>> edit(@Field("owner_id") Integer ownerId,
                                               @Field("post_id") Integer postId,
                                               @Field("friends_only") Integer friendsOnly,
                                               @Field("message") String message,
                                               @Field("attachments") String attachments,
                                               @Field("services") String services,
                                               @Field("signed") Integer signed,
                                               @Field("publish_date") Long publishDate,
                                               @Field("lat") Double latitude,
                                               @Field("long") Double longitude,
                                               @Field("place_id") Integer placeId,
                                               @Field("mark_as_ads") Integer markAsAds);

    //https://vk.com/dev/wall.pin
    @FormUrlEncoded
    @POST("wall.pin")
    Single<BaseResponse<Integer>> pin(@Field("owner_id") Integer ownerId,
                                      @Field("post_id") int postId);

    //https://vk.com/dev/wall.unpin
    @FormUrlEncoded
    @POST("wall.unpin")
    Single<BaseResponse<Integer>> unpin(@Field("owner_id") Integer ownerId,
                                        @Field("post_id") int postId);

    //https://vk.com/dev/wall.repost
    @FormUrlEncoded
    @POST("wall.repost")
    Single<BaseResponse<RepostReponse>> repost(@Field("object") String object,
                                               @Field("message") String message,
                                               @Field("group_id") Integer groupId,
                                               @Field("mark_as_ads") Integer markAsAds);

    //https://vk.com/dev/wall.post
    @FormUrlEncoded
    @POST("wall.post")
    Single<BaseResponse<PostCreateResponse>> post(@Field("owner_id") Integer ownerId,
                                                  @Field("friends_only") Integer friendsOnly,
                                                  @Field("from_group") Integer fromGroup,
                                                  @Field("message") String message,
                                                  @Field("attachments") String attachments,
                                                  @Field("services") String services,
                                                  @Field("signed") Integer signed,
                                                  @Field("publish_date") Long publishDate,
                                                  @Field("lat") Double latitude,
                                                  @Field("long") Double longitude,
                                                  @Field("place_id") Integer placeId,
                                                  @Field("post_id") Integer postId,
                                                  @Field("guid") Integer guid,
                                                  @Field("mark_as_ads") Integer markAsAds,
                                                  @Field("ads_promoted_stealth") Integer adsPromotedStealth);

    /**
     * Deletes a post from a user wall or community wall.
     *
     * @param ownerId User ID or community ID. Use a negative value to designate a community ID.
     *                Current user id is used by default
     * @param postId  ID of the post to be deleted
     * @return 1
     */
    @FormUrlEncoded
    @POST("wall.delete")
    Single<BaseResponse<Integer>> delete(@Field("owner_id") Integer ownerId,
                                         @Field("post_id") int postId);

    /**
     * Restores a comment deleted from a user wall or community wall.
     *
     * @param ownerId   User ID or community ID. Use a negative value to designate a community ID.
     *                  Current user id is used by default
     * @param commentId Comment ID.
     * @return 1
     */
    @FormUrlEncoded
    @POST("wall.restoreComment")
    Single<BaseResponse<Integer>> restoreComment(@Field("owner_id") Integer ownerId,
                                                 @Field("comment_id") int commentId);

    /**
     * Deletes a comment on a post on a user wall or community wall.
     *
     * @param ownerId   User ID or community ID. Use a negative value to designate a community ID.
     *                  Current user id is used by default
     * @param commentId Comment ID.
     * @return 1
     */
    @FormUrlEncoded
    @POST("wall.deleteComment")
    Single<BaseResponse<Integer>> deleteComment(@Field("owner_id") Integer ownerId,
                                                @Field("comment_id") int commentId);

    /**
     * Restores a post deleted from a user wall or community wall.
     *
     * @param ownerId User ID or community ID from whose wall the post was deleted.
     *                Use a negative value to designate a community ID.
     *                Current user id is used by default
     * @param postId  ID of the post to be restored.
     * @return 1
     */
    @FormUrlEncoded
    @POST("wall.restore")
    Single<BaseResponse<Integer>> restore(@Field("owner_id") Integer ownerId,
                                          @Field("post_id") int postId);

    /**
     * Edits a comment on a user wall or community wall.
     *
     * @param ownerId     User ID or community ID. Use a negative value to designate a community ID.
     *                    Current user id is used by default
     * @param commentId   Comment ID.
     * @param message     New comment text.
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
    @POST("wall.editComment")
    Single<BaseResponse<Integer>> editComment(@Field("owner_id") Integer ownerId,
                                              @Field("comment_id") int commentId,
                                              @Field("message") String message,
                                              @Field("attachments") String attachments);

    @FormUrlEncoded
    @POST("wall.createComment")
    Single<BaseResponse<CommentCreateResponse>> createComment(@Field("owner_id") Integer ownerId,
                                                              @Field("post_id") int postId,
                                                              @Field("from_group") Integer fromGroup,
                                                              @Field("message") String message,
                                                              @Field("reply_to_comment") Integer replyToComment,
                                                              @Field("attachments") String attachments,
                                                              @Field("sticker_id") Integer stickerId,
                                                              @Field("guid") Integer generatedUniqueId);

    //https://vk.com/dev/wall.getComments
    @FormUrlEncoded
    @POST("wall.getComments")
    Single<BaseResponse<DefaultCommentsResponse>> getComments(@Field("owner_id") Integer ownerId,
                                                              @Field("post_id") int postId,
                                                              @Field("need_likes") Integer needLikes,
                                                              @Field("start_comment_id") Integer startCommentId,
                                                              @Field("offset") Integer offset,
                                                              @Field("count") Integer count,
                                                              @Field("sort") String sort,
                                                              @Field("extended") Integer extended,
                                                              @Field("thread_items_count") Integer thread_items_count,
                                                              @Field("fields") String fields);

    @FormUrlEncoded
    @POST("wall.get")
    Single<BaseResponse<WallResponse>> get(@Field("owner_id") Integer ownerId,
                                           @Field("domain") String domain,
                                           @Field("offset") Integer offset,
                                           @Field("count") Integer count,
                                           @Field("filter") String filter,
                                           @Field("extended") Integer extended,
                                           @Field("fields") String fields);

    @FormUrlEncoded
    @POST("wall.getById")
    Single<BaseResponse<PostsResponse>> getById(@Field("posts") String ids,
                                                @Field("extended") Integer extended,
                                                @Field("copy_history_depth") Integer copyHistoryDepth,
                                                @Field("fields") String fields);

    @POST("wall.reportComment")
    @FormUrlEncoded
    Single<BaseResponse<Integer>> reportComment(@Field("owner_id") Integer owner_id,
                                                @Field("comment_id") Integer comment_id,
                                                @Field("reason") Integer reason);

    @POST("wall.reportPost")
    @FormUrlEncoded
    Single<BaseResponse<Integer>> reportPost(@Field("owner_id") Integer owner_id,
                                             @Field("post_id") Integer post_id,
                                             @Field("reason") Integer reason);

    @POST("wall.subscribe")
    @FormUrlEncoded
    Single<BaseResponse<Integer>> subscribe(@Field("owner_id") Integer owner_id);

    @POST("wall.unsubscribe")
    @FormUrlEncoded
    Single<BaseResponse<Integer>> unsubscribe(@Field("owner_id") Integer owner_id);
}
