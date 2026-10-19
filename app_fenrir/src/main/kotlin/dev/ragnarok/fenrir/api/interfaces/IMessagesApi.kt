package dev.ragnarok.fenrir.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.api.model.Assets
import dev.ragnarok.fenrir.api.model.Items
import dev.ragnarok.fenrir.api.model.VKApiChat
import dev.ragnarok.fenrir.api.model.VKApiConversation
import dev.ragnarok.fenrir.api.model.VKApiJsonString
import dev.ragnarok.fenrir.api.model.VKApiLongpollServer
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.VKApiReactionAsset
import dev.ragnarok.fenrir.api.model.interfaces.IAttachmentToken
import dev.ragnarok.fenrir.api.model.response.AttachmentsHistoryResponse
import dev.ragnarok.fenrir.api.model.response.ConversationDeleteResult
import dev.ragnarok.fenrir.api.model.response.ConversationMembersResponse
import dev.ragnarok.fenrir.api.model.response.ConversationsResponse
import dev.ragnarok.fenrir.api.model.response.DialogsResponse
import dev.ragnarok.fenrir.api.model.response.ItemsProfilesGroupsResponse
import dev.ragnarok.fenrir.api.model.response.MessageDeleteResponse
import dev.ragnarok.fenrir.api.model.response.MessageHistoryResponse
import dev.ragnarok.fenrir.api.model.response.MessageImportantResponse
import dev.ragnarok.fenrir.api.model.response.ReactedMessagesPeersResponse
import dev.ragnarok.fenrir.api.model.response.SendMessageResponse
import kotlinx.coroutines.flow.Flow

interface IMessagesApi {
    @CheckResult
    fun editMessage(
        peerId: Long,
        cmId: Int,
        message: String?,
        attachment: List<IAttachmentToken>?,
        keepForwardMessages: Boolean,
        keepSnippets: Boolean?
    ): Flow<Boolean>

    @CheckResult
    fun removeChatMember(chatId: Long, memberId: Long): Flow<Boolean>

    @CheckResult
    fun deleteChatPhoto(chatId: Long): Flow<Boolean>

    @CheckResult
    fun addChatUser(chatId: Long, userId: Long): Flow<Boolean>

    @CheckResult
    fun getChat(
        chatId: Long?,
        chatIds: Collection<Long>?,
        fields: String?,
        nameCase: String?
    ): Flow<List<VKApiChat>>

    @CheckResult
    fun getConversationMembers(
        peerId: Long?,
        fields: String?
    ): Flow<ConversationMembersResponse>

    @CheckResult
    fun editChat(chatId: Long, title: String?): Flow<Boolean>

    @CheckResult
    fun createChat(userIds: Collection<Long>, title: String?): Flow<Long>

    @CheckResult
    fun deleteConversation(peerId: Long): Flow<ConversationDeleteResult>

    @CheckResult
    fun restoreMessage(peerId: Long, cmId: Int): Flow<Boolean>

    @CheckResult
    fun deleteMessages(
        peerId: Long,
        cmIds: Collection<Int>,
        deleteForAll: Boolean,
        spam: Boolean?
    ): Flow<List<MessageDeleteResponse>>

    @CheckResult
    fun markAsRead(peerId: Long, startMessageId: Int): Flow<Boolean>

    @CheckResult
    fun setActivity(peerId: Long, typing: Boolean): Flow<Boolean>

    @CheckResult
    fun searchMessages(
        query: String?, peerId: Long?, date: String?,
        offset: Int?, count: Int?
    ): Flow<Items<VKApiMessage>>

    @CheckResult
    fun markAsImportant(messageIds: Collection<Int>, important: Int?): Flow<List<Int>>

    @CheckResult
    fun getHistoryAttachments(
        peerId: Long,
        mediaType: String?,
        startFrom: String?,
        count: Int?,
        photoSizes: Int?,
        preserveOrder: Int?,
        maxForwardsLevel: Int?,
        fields: String?
    ): Flow<AttachmentsHistoryResponse>

    @CheckResult
    fun send(
        randomId: Long?,
        peerId: Long?,
        domain: String?,
        message: String?,
        latitude: Double?,
        longitude: Double?,
        attachment: Collection<IAttachmentToken>?,
        forward: String?,
        stickerId: Int?,
        payload: String?
    ): Flow<SendMessageResponse>

    @CheckResult
    fun getConversations(
        offset: Int?,
        count: Int?,
        startMessageId: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<DialogsResponse>

    @CheckResult
    fun getConversationsById(
        peers: List<Long>,
        extended: Boolean?,
        fields: String?
    ): Flow<ItemsProfilesGroupsResponse<VKApiConversation>>

    @CheckResult
    fun getByConversationMessageId(peerId: Long, cmIds: Collection<Int>?): Flow<List<VKApiMessage>>

    @CheckResult
    fun getPeerMessages(
        offset: Int?,
        count: Int?,
        peerId: Long,
        startCmId: Int?,
        rev: Boolean?,
        extended: Boolean?,
        fields: String?
    ): Flow<MessageHistoryResponse>

    @CheckResult
    fun getJsonHistory(offset: Int?, count: Int?, peerId: Long): Flow<Items<VKApiJsonString>>

    @CheckResult
    fun getImportantMessages(
        offset: Int?,
        count: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<MessageImportantResponse>

    @CheckResult
    fun getLongpollServer(needPts: Boolean, lpVersion: Int): Flow<VKApiLongpollServer>

    @CheckResult
    fun searchConversations(
        query: String?,
        count: Int?,
        extended: Int?,
        fields: String?
    ): Flow<ConversationsResponse>

    @CheckResult
    fun pinMessage(
        peerId: Long,
        cmId: Int
    ): Flow<Boolean>

    @CheckResult
    fun unpinMessage(peerId: Long): Flow<Boolean>

    @CheckResult
    fun pinUnPinConversation(peerId: Long, peen: Boolean): Flow<Boolean>

    @CheckResult
    fun markAsListened(peerId: Long, cmId: Int): Flow<Boolean>

    @CheckResult
    fun recogniseAudioMessage(
        audioMessageId: String?,
        cmId: Int,
        peerId: Long
    ): Flow<Int>

    @CheckResult
    fun setMemberRole(
        peerId: Long?,
        memberId: Long?,
        role: String?
    ): Flow<Int>

    @CheckResult
    fun sendOrDeleteReaction(peerId: Long, cmId: Int, reactionId: Int?): Flow<Int>

    @CheckResult
    fun getReactionsAssets(): Flow<Assets<VKApiReactionAsset>>

    @CheckResult
    fun getReactedPeers(
        peerId: Long,
        cmId: Int,
        reactionId: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<ReactedMessagesPeersResponse>
}
