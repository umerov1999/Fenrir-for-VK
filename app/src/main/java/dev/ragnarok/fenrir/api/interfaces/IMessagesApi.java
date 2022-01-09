package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IMessagesApi {

    @CheckResult
    Completable edit(int peerId, int messageId, String message, List<IAttachmentToken> attachments, boolean keepFwd, Boolean keepSnippets);

    @CheckResult
    Single<Boolean> removeChatMember(int chatId, int memberId);

    @CheckResult
    Single<Boolean> deleteChatPhoto(int chatId);

    @CheckResult
    Single<Boolean> addChatUser(int chatId, int userId);

    @CheckResult
    Single<List<VKApiChat>> getChat(Integer chatId, Collection<Integer> chatIds, String fields, String name_case);

    @CheckResult
    Single<ConversationMembersResponse> getConversationMembers(Integer peer_id, String fields);

    @CheckResult
    Single<Boolean> editChat(int chatId, String title);

    @CheckResult
    Single<Integer> createChat(Collection<Integer> userIds, String title);

    @CheckResult
    Single<ConversationDeleteResult> deleteDialog(int peerId);

    @CheckResult
    Single<Boolean> restore(int messageId);

    @CheckResult
    Single<Map<String, Integer>> delete(Collection<Integer> messageIds, Boolean deleteForAll, Boolean spam);

    @CheckResult
    Single<Boolean> markAsRead(Integer peerId, Integer startMessageId);

    @CheckResult
    Single<Boolean> setActivity(int peerId, boolean typing);

    @CheckResult
    Single<Items<VKApiMessage>> search(String query, Integer peerId, Long date, Integer previewLength,
                                       Integer offset, Integer count);

    @CheckResult
    Single<List<Integer>> markAsImportant(Collection<Integer> messageIds, Integer important);

    @CheckResult
    Single<LongpollHistoryResponse> getLongPollHistory(Long ts, Long pts, Integer previewLength,
                                                       Boolean onlines, String fields,
                                                       Integer eventsLimit, Integer msgsLimit,
                                                       Integer max_msg_id);

    @CheckResult
    Single<AttachmentsHistoryResponse> getHistoryAttachments(int peerId, String mediaType, String startFrom, Integer photoSizes,
                                                             Integer count, String fields);


    @CheckResult
    Single<Integer> send(Integer randomId, Integer peerId, String domain, String message,
                         Double latitude, Double longitude, Collection<IAttachmentToken> attachments,
                         Collection<Integer> forwardMessages, Integer stickerId, String payload, Integer reply_to);

    @CheckResult
    Single<DialogsResponse> getDialogs(Integer offset, Integer count, Integer startMessageId, Boolean extended, String fields);

    @CheckResult
    Single<ItemsProfilesGroupsResponse<VkApiConversation>> getConversations(List<Integer> peers, Boolean extended, String fields);

    @CheckResult
    Single<List<VKApiMessage>> getById(Collection<Integer> ids);

    @CheckResult
    Single<MessageHistoryResponse> getHistory(Integer offset, Integer count, int peerId, Integer startMessageId, Boolean rev, Boolean extended, String fields);

    @CheckResult
    Single<Items<VkApiJsonString>> getJsonHistory(Integer offset, Integer count, int peerId);

    @CheckResult
    Single<MessageImportantResponse> getImportantMessages(Integer offset, Integer count, Integer startMessageId, Boolean extended, String fields);

    @CheckResult
    Single<VkApiLongpollServer> getLongpollServer(boolean needPts, int lpVersion);

    @CheckResult
    Single<ConversationsResponse> searchConversations(String query, Integer count, Integer extended, String fields);

    @CheckResult
    Completable pin(int peerId, int messageId);

    @CheckResult
    Completable unpin(int peerId);

    @CheckResult
    Completable pinUnPinConversation(int peerId, boolean peen);

    @CheckResult
    Single<Integer> recogniseAudioMessage(Integer message_id, String audio_message_id);

    @CheckResult
    Single<Integer> setMemberRole(Integer peer_id, Integer member_id, String role);
}
