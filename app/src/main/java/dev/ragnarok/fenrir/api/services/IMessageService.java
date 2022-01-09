package dev.ragnarok.fenrir.api.services;

import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.model.VkApiJsonString;
import dev.ragnarok.fenrir.api.model.VkApiLongpollServer;
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.api.model.response.ChatsInfoResponse;
import dev.ragnarok.fenrir.api.model.response.ConversationDeleteResult;
import dev.ragnarok.fenrir.api.model.response.ConversationMembersResponse;
import dev.ragnarok.fenrir.api.model.response.ConversationsResponse;
import dev.ragnarok.fenrir.api.model.response.DialogsResponse;
import dev.ragnarok.fenrir.api.model.response.ItemsProfilesGroupsResponse;
import dev.ragnarok.fenrir.api.model.response.LongpollHistoryResponse;
import dev.ragnarok.fenrir.api.model.response.MessageHistoryResponse;
import dev.ragnarok.fenrir.api.model.response.MessageImportantResponse;
import dev.ragnarok.fenrir.api.model.response.UploadChatPhotoResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IMessageService {

    @FormUrlEncoded
    @POST("messages.edit")
    Single<BaseResponse<Integer>> editMessage(@Field("peer_id") int peedId,
                                              @Field("message_id") int messageId,
                                              @Field("message") String message,
                                              @Field("attachment") String attachment,
                                              @Field("keep_forward_messages") Integer keepForwardMessages,
                                              @Field("keep_snippets") Integer keepSnippets);

    @FormUrlEncoded
    @POST("messages.pin")
    Single<BaseResponse<VKApiMessage>> pin(@Field("peer_id") int peerId,
                                           @Field("message_id") int messageId);

    @FormUrlEncoded
    @POST("messages.pinConversation")
    Single<BaseResponse<Integer>> pinConversation(@Field("peer_id") int peerId);

    @FormUrlEncoded
    @POST("messages.unpinConversation")
    Single<BaseResponse<Integer>> unpinConversation(@Field("peer_id") int peerId);

    @FormUrlEncoded
    @POST("messages.unpin")
    Single<BaseResponse<Integer>> unpin(@Field("peer_id") int peerId);

    /**
     * Allows the current user to leave a chat or, if the current user started the chat,
     * allows the user to remove another user from the chat.
     *
     * @param chatId   Chat ID
     * @param memberId ID of the member to be removed from the chat
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.removeChatUser")
    Single<BaseResponse<Integer>> removeChatUser(@Field("chat_id") int chatId,
                                                 @Field("member_id") int memberId);

    @FormUrlEncoded
    @POST("messages.deleteChatPhoto")
    Single<BaseResponse<UploadChatPhotoResponse>> deleteChatPhoto(@Field("chat_id") int chatId);

    /**
     * Adds a new user to a chat.
     *
     * @param chatId Chat ID
     * @param userId ID of the user to be added to the chat.
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.addChatUser")
    Single<BaseResponse<Integer>> addChatUser(@Field("chat_id") int chatId,
                                              @Field("user_id") int userId);

    /**
     * Returns information about a chat.
     *
     * @param chatId   Chat ID.
     * @param chatIds  Chat IDs. List of comma-separated numbers
     * @param fields   Profile fields to return. List of comma-separated words
     * @param nameCase Case for declension of user name and surname:
     *                 nom — nominative (default)
     *                 gen — genitive
     *                 dat — dative
     *                 acc — accusative
     *                 ins — instrumental
     *                 abl — prepositional
     * @return Returns a list of chat objects.
     */
    @FormUrlEncoded
    @POST("messages.getChat")
    Single<BaseResponse<ChatsInfoResponse>> getChat(@Field("chat_id") Integer chatId,
                                                    @Field("chat_ids") String chatIds,
                                                    @Field("fields") String fields,
                                                    @Field("name_case") String nameCase);


    @FormUrlEncoded
    @POST("messages.getConversationMembers")
    Single<BaseResponse<ConversationMembersResponse>> getConversationMembers(@Field("peer_id") Integer peer_id,
                                                                             @Field("fields") String fields);

    /**
     * Edits the title of a chat.
     *
     * @param chatId Chat ID.
     * @param title  New title of the chat.
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.editChat")
    Single<BaseResponse<Integer>> editChat(@Field("chat_id") int chatId,
                                           @Field("title") String title);

    /**
     * Creates a chat with several participants.
     *
     * @param userIds IDs of the users to be added to the chat. List of comma-separated positive numbers
     * @param title   Chat title
     * @return the ID of the created chat (chat_id).
     */
    @FormUrlEncoded
    @POST("messages.createChat")
    Single<BaseResponse<Integer>> createChat(@Field("user_ids") String userIds,
                                             @Field("title") String title);

    /**
     * Deletes all private messages in a conversation.
     *
     * @param peerId Destination ID.
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.deleteConversation")
    Single<BaseResponse<ConversationDeleteResult>> deleteDialog(@Field("peer_id") int peerId);

    /**
     * Restores a deleted message.
     *
     * @param messageId ID of a previously-deleted message to restore
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.restore")
    Single<BaseResponse<Integer>> restore(@Field("message_id") int messageId);

    /**
     * Deletes one or more messages.
     *
     * @param messageIds Message IDs. List of comma-separated positive numbers
     * @param spam       1 — to mark message as spam.
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.delete")
    Single<BaseResponse<Map<String, Integer>>> delete(@Field("message_ids") String messageIds,
                                                      @Field("delete_for_all") Integer deleteForAll,
                                                      @Field("spam") Integer spam);

    /**
     * Marks messages as read.
     *
     * @param peerId         Destination ID.
     * @param startMessageId Message ID to start from
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.markAsRead")
    Single<BaseResponse<Integer>> markAsRead(@Field("peer_id") Integer peerId,
                                             @Field("start_message_id") Integer startMessageId);

    @FormUrlEncoded
    @POST("messages.markAsImportant")
    Single<BaseResponse<List<Integer>>> markAsImportant(@Field("message_ids") String messageIds,
                                                        @Field("important") Integer important);

    /**
     * Changes the status of a user as typing in a conversation.
     *
     * @param peerId Destination ID
     * @param type   typing — user has started to type.
     * @return 1
     */
    @FormUrlEncoded
    @POST("messages.setActivity")
    Single<BaseResponse<Integer>> setActivity(@Field("peer_id") int peerId,
                                              @Field("type") String type);

    /**
     * Returns a list of the current user's private messages that match search criteria.
     *
     * @param query         Search query string
     * @param peerId        Destination ID
     * @param date          Date to search message before in Unixtime
     * @param previewLength Number of characters after which to truncate a previewed message.
     *                      To preview the full message, specify 0
     *                      NOTE: Messages are not truncated by default. Messages are truncated by words
     * @param offset        Offset needed to return a specific subset of messages.
     * @param count         Number of messages to return
     * @return list of the current user's private messages
     */
    @FormUrlEncoded
    @POST("messages.search")
    Single<BaseResponse<Items<VKApiMessage>>> search(@Field("q") String query,
                                                     @Field("peer_id") Integer peerId,
                                                     @Field("date") Long date,
                                                     @Field("preview_length") Integer previewLength,
                                                     @Field("offset") Integer offset,
                                                     @Field("count") Integer count);

    /**
     * Returns updates in user's private messages.
     * To speed up handling of private messages, it can be useful to cache previously loaded messages
     * on a user's mobile device/desktop, to prevent re-receipt at each call.
     * With this method, you can synchronize a local copy of the message list with the actual version.
     *
     * @param ts            Last value of the ts parameter returned from the Long Poll server or by using
     * @param pts           Last value of pts parameter returned from the Long Poll server or by using
     * @param previewLength Number of characters after which to truncate a previewed message.
     *                      To preview the full message, specify 0.
     *                      NOTE: Messages are not truncated by default. Messages are truncated by words.
     * @param onlines       1 — to return history with online users only
     * @param fields        Additional profile fileds to return. List of comma-separated words, default
     * @param eventsLimit   Maximum number of events to return.
     * @param msgsLimit     Maximum number of messages to return.
     * @param maxMsgId      Maximum ID of the message among existing ones in the local copy.
     *                      Both messages received with API methods (for example, messages.getDialogs, messages.getHistory),
     *                      and data received from a Long Poll server (events with code 4) are taken into account.
     * @return an object that contains the following fields:
     * history — An array similar to updates field returned from the Long Poll server, with these exceptions:
     * For events with code 4 (addition of a new message), there are no fields except the first three.
     * There are no events with codes 8, 9 (friend goes online/offline) or with codes 61, 62 (typing during conversation/chat).
     * messages — An array of private message objects that were found among events with code 4
     * (addition of a new message) from the history field. Each object of message contains a set
     * of fields described here. The first array element is the total number of messages.
     */
    @FormUrlEncoded
    @POST("messages.getLongPollHistory")
    Single<BaseResponse<LongpollHistoryResponse>> getLongPollHistory(@Field("ts") Long ts,
                                                                     @Field("pts") Long pts,
                                                                     @Field("preview_length") Integer previewLength,
                                                                     @Field("onlines") Integer onlines,
                                                                     @Field("fields") String fields,
                                                                     @Field("events_limit") Integer eventsLimit,
                                                                     @Field("msgs_limit") Integer msgsLimit,
                                                                     @Field("max_msg_id") Integer maxMsgId);

    /**
     * Returns media files from the dialog or group chat.
     *
     * @param peerId     Peer ID.
     * @param mediaType  Type of media files to return: photo, video, audio, doc, link.
     * @param startFrom  Message ID to start return results from.
     * @param count      Number of objects to return. Maximum value 200, default 30
     * @param photoSizes 1 — to return photo sizes in a special format
     * @param fields     Additional profile fields to return
     * @return a list of photo, video, audio or doc objects depending on media_type parameter value
     * and additional next_from field containing new offset value.
     */
    @FormUrlEncoded
    @POST("messages.getHistoryAttachments")
    Single<BaseResponse<AttachmentsHistoryResponse>> getHistoryAttachments(@Field("peer_id") int peerId,
                                                                           @Field("media_type") String mediaType,
                                                                           @Field("start_from") String startFrom,
                                                                           @Field("count") Integer count,
                                                                           @Field("photo_sizes") Integer photoSizes,
                                                                           @Field("fields") String fields);

    /**
     * Sends a message.
     *
     * @param randomId        Unique identifier to avoid resending the message
     * @param peerId          Destination ID
     * @param domain          User's short address (for example, illarionov).
     * @param message         (Required if attachments is not set.) Text of the message
     * @param latitude        Geographical latitude of a check-in, in degrees (from -90 to 90).
     * @param longitude       Geographical longitude of a check-in, in degrees (from -180 to 180).
     * @param attachment      (Required if message is not set.) List of objects attached to the message,
     *                        separated by commas, in the following format: {type}{owner_id}_{media_id}_{access_key}
     * @param forwardMessages ID of forwarded messages, separated with a comma.
     *                        Listed messages of the sender will be shown in the message body at the recipient's.
     * @param stickerId       Sticker id
     * @return sent message ID.
     */
    @FormUrlEncoded
    @POST("messages.send")
    Single<BaseResponse<Integer>> send(@Field("random_id") Integer randomId,
                                       @Field("peer_id") Integer peerId,
                                       @Field("domain") String domain,
                                       @Field("message") String message,
                                       @Field("lat") Double latitude,
                                       @Field("long") Double longitude,
                                       @Field("attachment") String attachment,
                                       @Field("forward_messages") String forwardMessages,
                                       @Field("sticker_id") Integer stickerId,
                                       @Field("payload") String payload,
                                       @Field("reply_to") Integer reply_to);

    /**
     * Returns messages by their IDs.
     *
     * @param messageIds    Message IDs. List of comma-separated positive numbers
     * @param previewLength Number of characters after which to truncate a previewed message.
     *                      To preview the full message, specify 0.
     *                      NOTE: Messages are not truncated by default. Messages are truncated by words.
     * @return a list of message objects.
     */
    @FormUrlEncoded
    @POST("messages.getById")
    Single<BaseResponse<Items<VKApiMessage>>> getById(@Field("message_ids") String messageIds,
                                                      @Field("preview_length") Integer previewLength);

    //https://vk.com/dev/messages.getConversations
    @FormUrlEncoded
    @POST("messages.getConversations")
    Single<BaseResponse<DialogsResponse>> getDialogs(@Field("offset") Integer offset,
                                                     @Field("count") Integer count,
                                                     @Field("start_message_id") Integer startMessageId,
                                                     @Field("extended") Integer extended,
                                                     @Field("fields") String fields);

    //https://vk.com/dev/messages.getConversationsById
    @FormUrlEncoded
    @POST("messages.getConversationsById")
    Single<BaseResponse<ItemsProfilesGroupsResponse<VkApiConversation>>> getConversationsById(@Field("peer_ids") String peerIds,
                                                                                              @Field("extended") Integer extended,
                                                                                              @Field("fields") String fields);

    @FormUrlEncoded
    @POST("messages.getLongPollServer")
    Single<BaseResponse<VkApiLongpollServer>> getLongpollServer(@Field("need_pts") int needPts,
                                                                @Field("lp_version") int lpVersion);

    /**
     * Returns message history for the specified user or group chat.
     *
     * @param offset         Offset needed to return a specific subset of messages.
     * @param count          Number of messages to return. Default 20, maximum value 200
     * @param peerId         Destination ID
     * @param startMessageId Starting message ID from which to return history.
     * @param rev            Sort order:
     *                       1 — return messages in chronological order.
     *                       0 — return messages in reverse chronological order.
     * @return Returns a list of message objects.
     */
    @FormUrlEncoded
    @POST("messages.getHistory")
    Single<BaseResponse<MessageHistoryResponse>> getHistory(@Field("offset") Integer offset,
                                                            @Field("count") Integer count,
                                                            @Field("peer_id") int peerId,
                                                            @Field("start_message_id") Integer startMessageId,
                                                            @Field("rev") Integer rev,
                                                            @Field("extended") Integer extended,
                                                            @Field("fields") String fields);

    @FormUrlEncoded
    @POST("messages.getHistory")
    Single<BaseResponse<Items<VkApiJsonString>>> getJsonHistory(@Field("offset") Integer offset,
                                                                @Field("count") Integer count,
                                                                @Field("peer_id") int peerId);

    @FormUrlEncoded
    @POST("messages.getImportantMessages")
    Single<BaseResponse<MessageImportantResponse>> getImportantMessages(@Field("offset") Integer offset,
                                                                        @Field("count") Integer count,
                                                                        @Field("start_message_id") Integer startMessageId,
                                                                        @Field("extended") Integer extended,
                                                                        @Field("fields") String fields);

    //https://vk.com/dev/messages.searchDialogs
    @FormUrlEncoded
    @POST("messages.searchConversations")
    Single<BaseResponse<ConversationsResponse>> searchConversations(@Field("q") String q,
                                                                    @Field("count") Integer count,
                                                                    @Field("extended") Integer extended,
                                                                    @Field("fields") String fields);

    @FormUrlEncoded
    @POST("messages.recogniseAudioMessage")
    Single<BaseResponse<Integer>> recogniseAudioMessage(@Field("message_id") Integer message_id,
                                                        @Field("audio_message_id") String audio_message_id);

    @FormUrlEncoded
    @POST("messages.setMemberRole")
    Single<BaseResponse<Integer>> setMemberRole(@Field("peer_id") Integer peer_id,
                                                @Field("member_id") Integer member_id,
                                                @Field("role") String role);
}
