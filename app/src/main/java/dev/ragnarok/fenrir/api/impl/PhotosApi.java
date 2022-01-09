package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IPhotosApi;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.api.model.VKApiPhotoTags;
import dev.ragnarok.fenrir.api.model.VkApiPrivacy;
import dev.ragnarok.fenrir.api.model.response.DefaultCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.UploadChatPhotoResponse;
import dev.ragnarok.fenrir.api.model.response.UploadOwnerPhotoResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiChatPhotoUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiOwnerPhotoUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiPhotoMessageServer;
import dev.ragnarok.fenrir.api.model.server.VkApiUploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiWallUploadServer;
import dev.ragnarok.fenrir.api.services.IPhotosService;
import io.reactivex.rxjava3.core.Single;

class PhotosApi extends AbsApi implements IPhotosApi {

    PhotosApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    private static String findAccessKey(Collection<AccessIdPair> data, int id, int ownerId) {
        for (AccessIdPair pair : data) {
            if (pair.id == id && pair.ownerId == ownerId) {
                return pair.accessKey;
            }
        }

        return null;
    }

    @Override
    public Single<Boolean> deleteAlbum(int albumId, Integer groupId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.deleteAlbum(albumId, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> restore(Integer ownerId, int photoId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.restore(ownerId, photoId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> delete(Integer ownerId, int photoId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.delete(ownerId, photoId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> deleteComment(Integer ownerId, int commentId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.deleteComment(ownerId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> restoreComment(Integer ownerId, int commentId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.restoreComment(ownerId, commentId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> editComment(Integer ownerId, int commentId, String message,
                                       Collection<IAttachmentToken> attachments) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.editComment(ownerId, commentId, message, join(attachments, ",", AbsApi::formatAttachmentToken))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<VKApiPhotoAlbum> createAlbum(String title, Integer groupId, String description, VkApiPrivacy privacyView, VkApiPrivacy privacyComment, Boolean uploadByAdminsOnly, Boolean commentsDisabled) {
        String privacyViewTxt = isNull(privacyView) ? null : privacyView.buildJsonArray();
        String privacyCommentTxt = isNull(privacyComment) ? null : privacyComment.buildJsonArray();
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .createAlbum(title, groupId, description, privacyViewTxt, privacyCommentTxt,
                                integerFromBoolean(uploadByAdminsOnly), integerFromBoolean(commentsDisabled))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> editAlbum(int albumId, String title, String description, Integer ownerId, VkApiPrivacy privacyView, VkApiPrivacy privacyComment, Boolean uploadByAdminsOnly, Boolean commentsDisabled) {
        String privacyViewTxt = isNull(privacyView) ? null : privacyView.buildJsonArray();
        String privacyCommentTxt = isNull(privacyComment) ? null : privacyComment.buildJsonArray();
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .editAlbum(albumId, title, description, ownerId, privacyViewTxt, privacyCommentTxt,
                                integerFromBoolean(uploadByAdminsOnly), integerFromBoolean(commentsDisabled))
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> copy(int ownerId, int photoId, String accessKey) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.copy(ownerId, photoId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> createComment(Integer ownerId, int photoId, Boolean fromGroup, String message,
                                         Integer replyToComment, Collection<IAttachmentToken> attachments,
                                         Integer stickerId, String accessKey, Integer generatedUniqueId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .createComment(ownerId, photoId, integerFromBoolean(fromGroup), message, replyToComment,
                                join(attachments, ",", AbsApi::formatAttachmentToken), stickerId, accessKey, generatedUniqueId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<DefaultCommentsResponse> getComments(Integer ownerId, int photoId, Boolean needLikes, Integer startCommentId, Integer offset, Integer count, String sort, String accessKey, Boolean extended, String fields) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .getComments(ownerId, photoId, integerFromBoolean(needLikes), startCommentId,
                                offset, count, sort, accessKey, integerFromBoolean(extended), fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiPhoto>> getById(@NonNull Collection<AccessIdPair> ids) {
        String line = join(ids, ",", pair -> pair.ownerId + "_" + pair.id + (pair.accessKey == null ? "" : "_" + pair.accessKey));

        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.getById(line, 1, 1)
                        .map(extractResponseWithErrorHandling())
                        .map(photos -> {
                            if (isNull(photos)) {
                                photos = Collections.emptyList();
                            }

                            // пересохраняем access_key, потому что не получим в ответе
                            for (VKApiPhoto photo : photos) {
                                if (isNull(photo.access_key)) {
                                    photo.access_key = findAccessKey(ids, photo.id, photo.owner_id);
                                }
                            }

                            return photos;
                        }));
    }

    @Override
    public Single<VkApiUploadServer> getUploadServer(int albumId, Integer groupId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .getUploadServer(albumId, groupId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<UploadOwnerPhotoResponse> saveOwnerPhoto(String server, String hash, String photo) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .saveOwnerPhoto(server, hash, photo)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiOwnerPhotoUploadServer> getOwnerPhotoUploadServer(Integer ownerId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .getOwnerPhotoUploadServer(ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiChatPhotoUploadServer> getChatUploadServer(Integer chat_id) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .getChatUploadServer(chat_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<UploadChatPhotoResponse> setChatPhoto(String file) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .setChatPhoto(file)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiPhoto>> saveWallPhoto(Integer userId, Integer groupId, String photo,
                                                  int server, String hash, Double latitude,
                                                  Double longitude, String caption) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .saveWallPhoto(userId, groupId, photo, server, hash, latitude, longitude, caption)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiWallUploadServer> getWallUploadServer(Integer groupId) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .getWallUploadServer(groupId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiPhoto>> save(int albumId, Integer groupId, int server, String photosList,
                                         String hash, Double latitude, Double longitude, String caption) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service
                        .save(albumId, groupId, server, photosList, hash, latitude, longitude, caption)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> get(Integer ownerId, String albumId, Collection<Integer> photoIds,
                                         Boolean rev, Integer offset, Integer count) {
        String photos = join(photoIds, ",");
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.get(ownerId, albumId, photos, integerFromBoolean(rev), 1, 1, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> getUsersPhoto(Integer ownerId, Integer extended, Integer sort, Integer offset, Integer count) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.getUserPhotos(ownerId, extended, sort, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> getAll(Integer ownerId, Integer extended, Integer photo_sizes, Integer offset, Integer count) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.getAll(ownerId, extended, photo_sizes, offset, count, 0, 1, 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiPhotoMessageServer> getMessagesUploadServer() {
        return provideService(IPhotosService.class, TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service.getMessagesUploadServer()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiPhoto>> saveMessagesPhoto(Integer server, String photo, String hash) {
        return provideService(IPhotosService.class, TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service.saveMessagesPhoto(server, photo, hash)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhotoAlbum>> getAlbums(Integer ownerId, Collection<Integer> albumIds,
                                                    Integer offset, Integer count, Boolean needSystem,
                                                    Boolean needCovers) {
        String ids = join(albumIds, ",");

        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.getAlbums(ownerId, ids, offset, count, integerFromBoolean(needSystem), integerFromBoolean(needCovers), 1)
                        .map(extractResponseWithErrorHandling()));

    }

    @Override
    public Single<List<VKApiPhotoTags>> getTags(Integer ownerId, Integer photo_id, String access_key) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.getTags(ownerId, photo_id, access_key).map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiComment>> getAllComments(Integer ownerId, Integer album_id, Integer need_likes, Integer offset, Integer count) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.getAllComments(ownerId, album_id, need_likes, offset, count).map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> search(String q, Double lat_gps, Double long_gps, Integer sort, Integer radius, Long startTime, Long endTime, Integer offset, Integer count) {
        return provideService(IPhotosService.class, TokenType.USER)
                .flatMap(service -> service.search(q, lat_gps, long_gps, sort, radius, startTime, endTime, offset, count).map(extractResponseWithErrorHandling()));
    }
}
