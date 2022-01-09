package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.IMessagesApi;
import dev.ragnarok.fenrir.api.model.IAttachmentToken;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiChat;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VkApiConversation;
import dev.ragnarok.fenrir.api.model.VkApiJsonString;
import dev.ragnarok.fenrir.api.model.VkApiLongpollServer;
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse;
import dev.ragnarok.fenrir.api.model.response.ConversationDeleteResult;
import dev.ragnarok.fenrir.api.model.response.ConversationMembersResponse;
import dev.ragnarok.fenrir.api.model.response.ConversationsResponse;
import dev.ragnarok.fenrir.api.model.response.DialogsResponse;
import dev.ragnarok.fenrir.api.model.response.ItemsProfilesGroupsResponse;
import dev.ragnarok.fenrir.api.model.response.LongpollHistoryResponse;
import dev.ragnarok.fenrir.api.model.response.MessageHistoryResponse;
import dev.ragnarok.fenrir.api.model.response.MessageImportantResponse;
import dev.ragnarok.fenrir.api.services.IMessageService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

class MessagesApi extends AbsApi implements IMessagesApi {

    MessagesApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    private Single<IMessageService> serviceRx(int... tokenTypes) {
        return provideService(IMessageService.class, tokenTypes);
    }

    @Override
    public Completable edit(int peerId, int messageId, String message, List<IAttachmentToken> attachments, boolean keepFwd, Boolean keepSnippets) {
        String atts = join(attachments, ",", AbsApi::formatAttachmentToken);
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMapCompletable(service -> service
                        .editMessage(peerId, messageId, message, atts, integerFromBoolean(keepFwd), integerFromBoolean(keepSnippets))
                        .map(extractResponseWithErrorHandling())
                        .ignoreElement());
    }

    @Override
    public Single<Boolean> removeChatMember(int chatId, int memberId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .removeChatUser(chatId, memberId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Boolean> deleteChatPhoto(int chatId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .deleteChatPhoto(chatId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response.message_id != 0));
    }

    @Override
    public Single<Boolean> addChatUser(int chatId, int userId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .addChatUser(chatId, userId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));

    }

    @Override
    public Single<List<VKApiChat>> getChat(Integer chatId, Collection<Integer> chatIds, String fields, String nameCase) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getChat(chatId, join(chatIds, ","), fields, nameCase)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> listEmptyIfNull(response.chats)));
    }

    @Override
    public Single<ConversationMembersResponse> getConversationMembers(Integer peer_id, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getConversationMembers(peer_id, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> editChat(int chatId, String title) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .editChat(chatId, title)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> createChat(Collection<Integer> userIds, String title) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .createChat(join(userIds, ","), title)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> recogniseAudioMessage(Integer message_id, String audio_message_id) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .recogniseAudioMessage(message_id, audio_message_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> setMemberRole(Integer peer_id, Integer member_id, String role) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .setMemberRole(peer_id, member_id, role)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<ConversationDeleteResult> deleteDialog(int peerId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .deleteDialog(peerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> restore(int messageId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .restore(messageId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Map<String, Integer>> delete(Collection<Integer> messageIds, Boolean deleteForAll, Boolean spam) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .delete(join(messageIds, ","), integerFromBoolean(deleteForAll), integerFromBoolean(spam))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> markAsRead(Integer peerId, Integer startMessageId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .markAsRead(peerId, startMessageId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<List<Integer>> markAsImportant(Collection<Integer> messageIds, Integer important) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .markAsImportant(join(messageIds, ","), important)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> setActivity(int peerId, boolean typing) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .setActivity(peerId, typing ? "typing" : null)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Items<VKApiMessage>> search(String query, Integer peerId, Long date, Integer previewLength, Integer offset, Integer count) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                                .search(query, peerId, date, previewLength, offset, count)
                                .map(extractResponseWithErrorHandling())
                        /*.map(response -> {
                            fixMessageList(response.getItems());
                            return response;
                        })*/);
    }

    @Override
    public Single<LongpollHistoryResponse> getLongPollHistory(Long ts, Long pts, Integer previewLength, Boolean onlines, String fields, Integer eventsLimit, Integer msgsLimit, Integer max_msg_id) {
        return serviceRx(TokenType.USER)
                .flatMap(service -> service
                        .getLongPollHistory(ts, pts, previewLength, integerFromBoolean(onlines), fields,
                                eventsLimit, msgsLimit, max_msg_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<AttachmentsHistoryResponse> getHistoryAttachments(int peerId, String mediaType, String startFrom, Integer photoSizes, Integer count, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getHistoryAttachments(peerId, mediaType, startFrom, count, photoSizes, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> send(Integer randomId, Integer peerId, String domain, String message,
                                Double latitude, Double longitude, Collection<IAttachmentToken> attachments,
                                Collection<Integer> forwardMessages, Integer stickerId, String payload, Integer reply_to) {

        String atts = join(attachments, ",", AbsApi::formatAttachmentToken);
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .send(randomId, peerId, domain, message, latitude, longitude, atts,
                                join(forwardMessages, ","), stickerId, payload, reply_to)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<ItemsProfilesGroupsResponse<VkApiConversation>> getConversations(List<Integer> peers, Boolean extended, String fields) {
        String ids = join(peers, ",", Object::toString);
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getConversationsById(ids, integerFromBoolean(extended), fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<DialogsResponse> getDialogs(Integer offset, Integer count, Integer startMessageId, Boolean extended, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                                .getDialogs(offset, count, startMessageId, integerFromBoolean(extended), fields)
                                .map(extractResponseWithErrorHandling())
                        /*.map(response -> {
                            if (nonNull(response.dialogs)) {
                                for (VkApiDialog dialog : response.dialogs) {
                                    fixMessage(dialog.message);
                                }
                            }
                            return response;
                        })*/);
    }

    @Override
    public Completable unpin(int peerId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMapCompletable(service -> service.unpin(peerId)
                        .map(extractResponseWithErrorHandling())
                        .ignoreElement());
    }

    @Override
    public Completable pin(int peerId, int messageId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMapCompletable(service -> service.pin(peerId, messageId)
                        .map(extractResponseWithErrorHandling())
                        .ignoreElement());
    }

    @Override
    public Completable pinUnPinConversation(int peerId, boolean peen) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMapCompletable(service -> (peen ? service.pinConversation(peerId) : service.unpinConversation(peerId))
                        .map(extractResponseWithErrorHandling())
                        .ignoreElement());
    }

    @Override
    public Single<List<VKApiMessage>> getById(Collection<Integer> identifiers) {
        String ids = join(identifiers, ",");

        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getById(ids, null)
                        .map(extractResponseWithErrorHandling())
                        .map(Items::getItems));
    }

    @Override
    public Single<MessageHistoryResponse> getHistory(Integer offset, Integer count, int peerId, Integer startMessageId, Boolean rev, Boolean extended, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getHistory(offset, count, peerId, startMessageId, integerFromBoolean(rev), integerFromBoolean(extended), fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiJsonString>> getJsonHistory(Integer offset, Integer count, int peerId) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getJsonHistory(offset, count, peerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<MessageImportantResponse> getImportantMessages(Integer offset, Integer count, Integer startMessageId, Boolean extended, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getImportantMessages(offset, count, startMessageId, integerFromBoolean(extended), fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiLongpollServer> getLongpollServer(boolean needPts, int lpVersion) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .getLongpollServer(needPts ? 1 : 0, lpVersion)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<ConversationsResponse> searchConversations(String query, Integer count, Integer extended, String fields) {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
                .flatMap(service -> service
                        .searchConversations(query, count, extended, fields)
                        .map(extractResponseWithErrorHandling()));
    }
}