package dev.ragnarok.fenrir.api.model.response;

public class DeleteFriendResponse {
    /**
     * удалось успешно удалить друга
     */
    public boolean success;

    /**
     * был удален друг
     */
    public boolean friend_deleted;

    /**
     * отменена исходящая заявка
     */
    public boolean out_request_deleted;

    /**
     * отклонена входящая заявка
     */
    public boolean in_request_deleted;

    /**
     * отклонена рекомендация друга
     */
    public boolean suggestion_deleted;
}