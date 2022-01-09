package dev.ragnarok.fenrir.api.services;

import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.api.model.VKApiPhotoTags;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.UploadChatPhotoResponse;
import dev.ragnarok.fenrir.api.model.response.UploadOwnerPhotoResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiChatPhotoUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiOwnerPhotoUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiPhotoMessageServer;
import dev.ragnarok.fenrir.api.model.server.VkApiUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiWallUploadServer;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface IPhotosService {

    //https://vk.com/dev/photos.deleteAlbum
    @FormUrlEncoded
    @POST("photos.deleteAlbum")
    Single<BaseResponse<Integer>> deleteAlbum(@Field("album_id") int albumId,
                                              @Field("group_id") Integer groupId);

    //https://vk.com/dev/photos.restore
    @FormUrlEncoded
    @POST("photos.restore")
    Single<BaseResponse<Integer>> restore(@Field("owner_id") Integer ownerId,
                                          @Field("photo_id") int photoId);

    //https://vk.com/dev/photos.delete
    @FormUrlEncoded
    @POST("photos.delete")
    Single<BaseResponse<Integer>> delete(@Field("owner_id") Integer ownerId,
                                         @Field("photo_id") int photoId);

    //https://vk.com/dev/photos.deleteComment
    @FormUrlEncoded
    @POST("photos.deleteComment")
    Single<BaseResponse<Integer>> deleteComment(@Field("owner_id") Integer ownerId,
                                                @Field("comment_id") int commentId);

    //https://vk.com/dev/photos.restoreComment
    @FormUrlEncoded
    @POST("photos.restoreComment")
    Single<BaseResponse<Integer>> restoreComment(@Field("owner_id") Integer ownerId,
                                                 @Field("comment_id") int commentId);

    //https://vk.com/dev/photos.getComments
    @FormUrlEncoded
    @POST("photos.getComments")
    Single<BaseResponse<DefaultCommentsResponse>> getComments(@Field("owner_id") Integer ownerId,
                                                              @Field("photo_id") int photoId,
                                                              @Field("need_likes") Integer needLikes,
                                                              @Field("start_comment_id") Integer startCommentId,
                                                              @Field("offset") Integer offset,
                                                              @Field("count") Integer count,
                                                              @Field("sort") String sort,
                                                              @Field("access_key") String accessKey,
                                                              @Field("extended") Integer extended,
                                                              @Field("fields") String fields);

    /**
     * Edits a comment on a photo.
     *
     * @param ownerId     ID of the user or community that owns the photo. Current user id is used by default
     * @param commentId   Comment ID.
     * @param message     New text of the comment.
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
    @POST("photos.editComment")
    Single<BaseResponse<Integer>> editComment(@Field("owner_id") Integer ownerId,
                                              @Field("comment_id") int commentId,
                                              @Field("message") String message,
                                              @Field("attachments") String attachments);

    /**
     * Creates an empty photo album.
     *
     * @param title              Album title.
     * @param groupId            ID of the community in which the album will be created.
     * @param description        Album description.
     * @param privacyView        privacy settings view album, list of comma-separated words
     * @param privacyComment     privacy settings album comments, list of comma-separated words
     * @param uploadByAdminsOnly who can upload pictures to an album (only for the community).
     *                           0 - photos can add all users;
     *                           1 - Photos can be added only editors and administrators.
     * @param commentsDisabled   the album commenting is disabled (only for the community).
     *                           0 - commenting on;
     *                           1 - commenting is disabled.
     * @return Returns an instance of photo album
     */
    @FormUrlEncoded
    @POST("photos.createAlbum")
    Single<BaseResponse<VKApiPhotoAlbum>> createAlbum(@Field("title") String title,
                                                      @Field("group_id") Integer groupId,
                                                      @Field("description") String description,
                                                      @Field("privacy_view") String privacyView,
                                                      @Field("privacy_comment") String privacyComment,
                                                      @Field("upload_by_admins_only") Integer uploadByAdminsOnly,
                                                      @Field("comments_disabled") Integer commentsDisabled);

    /**
     * Edits information about a photo album.
     *
     * @param albumId            ID of the photo album to be edited.
     * @param title              New album title.
     * @param description        New album description.
     * @param ownerId            ID of the user or community that owns the album. Current user id is used by default
     * @param privacyView        privacy settings view album, list of comma-separated words
     * @param privacyComment     privacy settings album comments, list of comma-separated words
     * @param uploadByAdminsOnly who can upload pictures to an album (only for the community).
     *                           0 - photos can add all users;
     *                           1 - Photos can be added only editors and administrators.
     * @param commentsDisabled   the album commenting is disabled (only for the community).
     *                           0 - commenting on;
     *                           1 - commenting is disabled.
     * @return 1
     */
    @FormUrlEncoded
    @POST("photos.editAlbum")
    Single<BaseResponse<Integer>> editAlbum(@Field("album_id") int albumId,
                                            @Field("title") String title,
                                            @Field("description") String description,
                                            @Field("owner_id") Integer ownerId,
                                            @Field("privacy_view") String privacyView,
                                            @Field("privacy_comment") String privacyComment,
                                            @Field("upload_by_admins_only") Integer uploadByAdminsOnly,
                                            @Field("comments_disabled") Integer commentsDisabled);

    /**
     * Allows to copy a photo to the "Saved photos" album
     *
     * @param ownerId   photo's owner ID
     * @param photoId   photo ID
     * @param accessKey special access key for private photos
     * @return Returns the created photo ID.
     */
    @FormUrlEncoded
    @POST("photos.copy")
    Single<BaseResponse<Integer>> copy(@Field("owner_id") int ownerId,
                                       @Field("photo_id") int photoId,
                                       @Field("access_key") String accessKey);

    @FormUrlEncoded
    @POST("photos.createComment")
    Single<BaseResponse<Integer>> createComment(@Field("owner_id") Integer ownerId,
                                                @Field("photo_id") int photoId,
                                                @Field("from_group") Integer fromGroup,
                                                @Field("message") String message,
                                                @Field("reply_to_comment") Integer replyToComment,
                                                @Field("attachments") String attachments,
                                                @Field("sticker_id") Integer stickerId,
                                                @Field("access_key") String accessKey,
                                                @Field("guid") Integer generatedUniqueId);

    @FormUrlEncoded
    @POST("photos.getById")
    Single<BaseResponse<List<VKApiPhoto>>> getById(@Field("photos") String photos,
                                                   @Field("extended") Integer extended,
                                                   @Field("photo_sizes") Integer photo_sizes);

    @FormUrlEncoded
    @POST("photos.getUploadServer")
    Single<BaseResponse<VkApiUploadServer>> getUploadServer(@Field("album_id") int albumId,
                                                            @Field("group_id") Integer groupId);


    @FormUrlEncoded
    @POST("photos.saveOwnerPhoto")
    Single<BaseResponse<UploadOwnerPhotoResponse>> saveOwnerPhoto(@Field("server") String server,
                                                                  @Field("hash") String hash,
                                                                  @Field("photo") String photo);

    @FormUrlEncoded
    @POST("messages.setChatPhoto")
    Single<BaseResponse<UploadChatPhotoResponse>> setChatPhoto(@Field("file") String file);

    @FormUrlEncoded
    @POST("photos.getOwnerPhotoUploadServer")
    Single<BaseResponse<VkApiOwnerPhotoUploadServer>> getOwnerPhotoUploadServer(@Field("owner_id") Integer ownerId);

    @FormUrlEncoded
    @POST("photos.getChatUploadServer")
    Single<BaseResponse<VkApiChatPhotoUploadServer>> getChatUploadServer(@Field("chat_id") Integer chat_id);


    @FormUrlEncoded
    @POST("photos.saveWallPhoto")
    Single<BaseResponse<List<VKApiPhoto>>> saveWallPhoto(@Field("user_id") Integer userId,
                                                         @Field("group_id") Integer groupId,
                                                         @Field("photo") String photo,
                                                         @Field("server") int server,
                                                         @Field("hash") String hash,
                                                         @Field("latitude") Double latitude,
                                                         @Field("longitude") Double longitude,
                                                         @Field("caption") String caption);

    @FormUrlEncoded
    @POST("photos.getWallUploadServer")
    Single<BaseResponse<VkApiWallUploadServer>> getWallUploadServer(@Field("group_id") Integer groupId);

    @FormUrlEncoded
    @POST("photos.save")
    Single<BaseResponse<List<VKApiPhoto>>> save(@Field("album_id") int albumId,
                                                @Field("group_id") Integer groupId,
                                                @Field("server") int server,
                                                @Field("photos_list") String photosList,
                                                @Field("hash") String hash,
                                                @Field("latitude") Double latitude,
                                                @Field("longitude") Double longitude,
                                                @Field("caption") String caption);

    @FormUrlEncoded
    @POST("photos.get")
    Single<BaseResponse<Items<VKApiPhoto>>> get(@Field("owner_id") Integer ownerId,
                                                @Field("album_id") String albumId,
                                                @Field("photo_ids") String photoIds,
                                                @Field("rev") Integer rev,
                                                @Field("extended") Integer extended,
                                                @Field("photo_sizes") Integer photoSizes,
                                                @Field("offset") Integer offset,
                                                @Field("count") Integer count);

    @FormUrlEncoded
    @POST("photos.getUserPhotos")
    Single<BaseResponse<Items<VKApiPhoto>>> getUserPhotos(@Field("user_id") Integer ownerId,
                                                          @Field("extended") Integer extended,
                                                          @Field("sort") Integer sort,
                                                          @Field("offset") Integer offset,
                                                          @Field("count") Integer count);

    @FormUrlEncoded
    @POST("photos.getAll")
    Single<BaseResponse<Items<VKApiPhoto>>> getAll(@Field("owner_id") Integer ownerId,
                                                   @Field("extended") Integer extended,
                                                   @Field("photo_sizes") Integer photo_sizes,
                                                   @Field("offset") Integer offset,
                                                   @Field("count") Integer count,
                                                   @Field("no_service_albums") Integer no_service_albums,
                                                   @Field("need_hidden") Integer need_hidden,
                                                   @Field("skip_hidden") Integer skip_hidden);

    @GET("photos.getMessagesUploadServer")
    Single<BaseResponse<VkApiPhotoMessageServer>> getMessagesUploadServer();

    @FormUrlEncoded
    @POST("photos.saveMessagesPhoto")
    Single<BaseResponse<List<VKApiPhoto>>> saveMessagesPhoto(@Field("server") Integer server,
                                                             @Field("photo") String photo,
                                                             @Field("hash") String hash);

    @FormUrlEncoded
    @POST("photos.getAlbums")
    Single<BaseResponse<Items<VKApiPhotoAlbum>>> getAlbums(@Field("owner_id") Integer ownerId,
                                                           @Field("album_ids") String albumIds,
                                                           @Field("offset") Integer offset,
                                                           @Field("count") Integer count,
                                                           @Field("need_system") Integer needSystem,
                                                           @Field("need_covers") Integer needCovers,
                                                           @Field("photo_sizes") Integer photoSizes);

    @FormUrlEncoded
    @POST("photos.getTags")
    Single<BaseResponse<List<VKApiPhotoTags>>> getTags(@Field("owner_id") Integer ownerId,
                                                       @Field("photo_id") Integer photo_id,
                                                       @Field("access_key") String access_key);

    @FormUrlEncoded
    @POST("photos.getAllComments")
    Single<BaseResponse<Items<VKApiComment>>> getAllComments(@Field("owner_id") Integer ownerId,
                                                             @Field("album_id") Integer album_id,
                                                             @Field("need_likes") Integer need_likes,
                                                             @Field("offset") Integer offset,
                                                             @Field("count") Integer count);

    @FormUrlEncoded
    @POST("photos.search")
    Single<BaseResponse<Items<VKApiPhoto>>> search(@Field("q") String q,
                                                   @Field("lat") Double lat_gps,
                                                   @Field("long") Double long_gps,
                                                   @Field("sort") Integer sort,
                                                   @Field("radius") Integer radius,
                                                   @Field("start_time") Long start_time,
                                                   @Field("end_time") Long end_time,
                                                   @Field("offset") Integer offset,
                                                   @Field("count") Integer count);

}
