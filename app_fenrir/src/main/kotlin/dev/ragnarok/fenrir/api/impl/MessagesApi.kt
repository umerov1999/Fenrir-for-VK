package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.api.TokenType
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.api.interfaces.IMessagesApi
import dev.ragnarok.fenrir.api.interfaces.providers.IServiceProvider
import dev.ragnarok.fenrir.api.model.Assets
import dev.ragnarok.fenrir.api.model.Error
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
import dev.ragnarok.fenrir.api.services.IMessageService
import dev.ragnarok.fenrir.util.Utils.listEmptyIfNull
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.checkInt
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.ignoreElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

internal class MessagesApi(accountId: Long, provider: IServiceProvider) :
    AbsApi(accountId, provider), IMessagesApi {
    private fun serviceRx(@TokenType vararg tokenTypes: Int): Flow<IMessageService> {
        return provideService(IMessageService(), *tokenTypes)
    }

    override fun editMessage(
        peerId: Long,
        cmId: Int,
        message: String?,
        attachment: List<IAttachmentToken>?,
        keepForwardMessages: Boolean,
        keepSnippets: Boolean?
    ): Flow<Boolean> {
        val attrs = join(attachment, ",") {
            formatAttachmentToken(it)
        }
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.editMessage(
                    peerId,
                    cmId,
                    message,
                    attrs,
                    integerFromBoolean(keepForwardMessages),
                    integerFromBoolean(keepSnippets)
                )
                    .map(extractResponseWithErrorHandling())
                    .ignoreElement()
            }
    }

    override fun removeChatMember(chatId: Long, memberId: Long): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.removeChatUser(chatId, memberId)
                    .map(extractResponseWithErrorHandling())
                    .checkInt()
            }
    }

    override fun deleteChatPhoto(chatId: Long): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat { s ->
                s.deleteChatPhoto(chatId)
                    .map(extractResponseWithErrorHandling())
                    .map { it.message_id != 0 }
            }
    }

    override fun addChatUser(chatId: Long, userId: Long): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.addChatUser(chatId, userId)
                    .map(extractResponseWithErrorHandling())
                    .checkInt()
            }
    }

    override fun getChat(
        chatId: Long?,
        chatIds: Collection<Long>?,
        fields: String?,
        nameCase: String?
    ): Flow<List<VKApiChat>> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat { s ->
                s.getChat(chatId, join(chatIds, ","), fields, nameCase)
                    .map(extractResponseWithErrorHandling())
                    .map { listEmptyIfNull(it.chats) }
            }
    }

    override fun getConversationMembers(
        peerId: Long?,
        fields: String?
    ): Flow<ConversationMembersResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getConversationMembers(peerId, fields)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun editChat(chatId: Long, title: String?): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.editChat(chatId, title)
                    .map(extractResponseWithErrorHandling())
                    .checkInt()
            }
    }

    override fun createChat(userIds: Collection<Long>, title: String?): Flow<Long> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.createChat(join(userIds, ","), title)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun recogniseAudioMessage(
        audioMessageId: String?,
        cmId: Int,
        peerId: Long
    ): Flow<Int> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.recogniseAudioMessage(audioMessageId, cmId, peerId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun setMemberRole(peerId: Long?, memberId: Long?, role: String?): Flow<Int> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.setMemberRole(peerId, memberId, role)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun deleteConversation(peerId: Long): Flow<ConversationDeleteResult> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.deleteConversation(peerId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun restoreMessage(peerId: Long, cmId: Int): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.restoreMessage(peerId, cmId)
                    .map(extractResponseWithErrorHandling())
                    .checkInt()
            }
    }

    override fun deleteMessages(
        peerId: Long,
        cmIds: Collection<Int>,
        deleteForAll: Boolean,
        spam: Boolean?
    ): Flow<List<MessageDeleteResponse>> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.deleteMessages(
                    peerId,
                    join(cmIds, ","),
                    integerFromBoolean(deleteForAll),
                    integerFromBoolean(spam)
                )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun markAsRead(peerId: Long, startMessageId: Int): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.markAsRead(peerId, startMessageId)
                    .map(extractResponseWithErrorHandling())
                    .checkInt()
            }
    }

    override fun markAsImportant(messageIds: Collection<Int>, important: Int?): Flow<List<Int>> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.markAsImportant(join(messageIds, ","), important)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun setActivity(peerId: Long, typing: Boolean): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.setActivity(peerId, if (typing) "typing" else null)
                    .map(extractResponseWithErrorHandling())
                    .checkInt()
            }
    }

    override fun searchMessages(
        query: String?,
        peerId: Long?,
        date: String?,
        offset: Int?,
        count: Int?
    ): Flow<Items<VKApiMessage>> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.searchMessages(query, peerId, date, offset, count)
                    .map(extractResponseWithErrorHandling())
            }

    }

    override fun getHistoryAttachments(
        peerId: Long,
        mediaType: String?,
        startFrom: String?,
        count: Int?,
        photoSizes: Boolean?,
        preserveOrder: Boolean?,
        maxForwardsLevel: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<AttachmentsHistoryResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getHistoryAttachments(
                    peerId,
                    mediaType,
                    startFrom,
                    count,
                    integerFromBoolean(photoSizes),
                    integerFromBoolean(preserveOrder),
                    maxForwardsLevel,
                    integerFromBoolean(extended),
                    fields
                )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun send(
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
    ): Flow<SendMessageResponse> {
        val attrs = join(attachment, ",") {
            formatAttachmentToken(it)
        }
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat { s ->
                s.send(
                    randomId,
                    join(listOf(peerId), ","),
                    domain,
                    message,
                    latitude,
                    longitude,
                    attrs,
                    forward,
                    stickerId,
                    payload
                )
                    .map(extractResponseWithErrorHandling())
                    .map {
                        if (it.isEmpty()) {
                            throw NullPointerException("VK return null response")
                        }
                        it[0].error?.let { err ->
                            val error = Error()
                            error.errorCode = err.errorCode
                            error.errorMsg = err.description
                            throw ApiException(error)
                        }
                        it[0]
                    }
            }
    }

    override fun getConversationsById(
        peers: List<Long>,
        extended: Boolean?,
        fields: String?
    ): Flow<ItemsProfilesGroupsResponse<VKApiConversation>> {
        val ids = join(peers, ",") { it.toString() }
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getConversationsById(ids, integerFromBoolean(extended), fields)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getConversations(
        offset: Int?,
        count: Int?,
        startMessageId: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<DialogsResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getConversations(
                    offset,
                    count,
                    startMessageId,
                    integerFromBoolean(extended),
                    fields
                )
                    .map(extractResponseWithErrorHandling())
            }

    }

    override fun unpinMessage(peerId: Long): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.unpinMessage(peerId)
                    .map(extractResponseWithErrorHandling())
                    .ignoreElement()
            }
    }

    override fun pinMessage(peerId: Long, cmId: Int): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.pinMessage(peerId, cmId)
                    .map(extractResponseWithErrorHandling())
                    .ignoreElement()
            }
    }

    override fun pinUnPinConversation(peerId: Long, peen: Boolean): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                (if (peen) it.pinConversation(peerId) else it.unpinConversation(peerId))
                    .map(extractResponseWithErrorHandling())
                    .ignoreElement()
            }
    }

    override fun markAsListened(peerId: Long, cmId: Int): Flow<Boolean> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.markAsListened(peerId, cmId)
                    .map(extractResponseWithErrorHandling())
                    .ignoreElement()
            }
    }

    override fun getByConversationMessageId(
        peerId: Long,
        cmIds: Collection<Int>?
    ): Flow<List<VKApiMessage>> {
        val ids = join(cmIds, ",")
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat { s ->
                s.getByConversationMessageId(peerId, ids)
                    .map(extractResponseWithErrorHandling())
                    .map { listEmptyIfNull(it.items) }
            }
    }

    override fun getPeerMessages(
        offset: Int?,
        count: Int?,
        peerId: Long,
        startCmId: Int?,
        rev: Boolean?,
        extended: Boolean?,
        fields: String?
    ): Flow<MessageHistoryResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getPeerMessages(
                    offset,
                    count,
                    peerId,
                    startCmId,
                    integerFromBoolean(rev),
                    integerFromBoolean(extended),
                    fields
                )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getJsonHistory(
        offset: Int?,
        count: Int?,
        peerId: Long
    ): Flow<Items<VKApiJsonString>> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getJsonHistory(offset, count, peerId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getImportantMessages(
        offset: Int?,
        count: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<MessageImportantResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getImportantMessages(
                    offset,
                    count,
                    integerFromBoolean(extended),
                    fields
                )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getLongpollServer(needPts: Boolean, lpVersion: Int): Flow<VKApiLongpollServer> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getLongpollServer(if (needPts) 1 else 0, lpVersion)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun searchConversations(
        query: String?,
        count: Int?,
        extended: Int?,
        fields: String?
    ): Flow<ConversationsResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.searchConversations(query, count, extended, fields)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getReactionsAssets(): Flow<Assets<VKApiReactionAsset>> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getReactionsAssets(null)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun sendOrDeleteReaction(peerId: Long, cmId: Int, reactionId: Int?): Flow<Int> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                if (reactionId != null) it.sendReaction(peerId, cmId, reactionId)
                    .map(extractResponseWithErrorHandling()) else it.deleteReaction(
                    peerId,
                    cmId
                ).map(extractResponseWithErrorHandling())
            }
    }

    override fun getReactedPeers(
        peerId: Long,
        cmId: Int,
        reactionId: Int?,
        extended: Boolean?,
        fields: String?
    ): Flow<ReactedMessagesPeersResponse> {
        return serviceRx(TokenType.USER, TokenType.COMMUNITY)
            .flatMapConcat {
                it.getReactedPeers(peerId, cmId, reactionId, integerFromBoolean(extended), fields)
                    .map(extractResponseWithErrorHandling())
            }
    }
}
