package dev.ragnarok.fenrir.domain

import android.content.Context
import dev.ragnarok.fenrir.api.model.VKApiMessage
import dev.ragnarok.fenrir.api.model.longpoll.BadgeCountChangeUpdate
import dev.ragnarok.fenrir.api.model.longpoll.InputMessagesSetReadUpdate
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsResetUpdate
import dev.ragnarok.fenrir.api.model.longpoll.MessageFlagsSetUpdate
import dev.ragnarok.fenrir.api.model.longpoll.OutputMessagesSetReadUpdate
import dev.ragnarok.fenrir.api.model.longpoll.PinConversationUpdate
import dev.ragnarok.fenrir.api.model.longpoll.ReactionMessageChangeUpdate
import dev.ragnarok.fenrir.api.model.longpoll.TypingMessageOrUploadingInDialogUpdate
import dev.ragnarok.fenrir.model.AbsModel
import dev.ragnarok.fenrir.model.AppChatUser
import dev.ragnarok.fenrir.model.Conversation
import dev.ragnarok.fenrir.model.Dialog
import dev.ragnarok.fenrir.model.Message
import dev.ragnarok.fenrir.model.MessageUpdate
import dev.ragnarok.fenrir.model.Peer
import dev.ragnarok.fenrir.model.PeerDeleting
import dev.ragnarok.fenrir.model.PeerUpdate
import dev.ragnarok.fenrir.model.ReactedMessagesPeers
import dev.ragnarok.fenrir.model.ReactionAsset
import dev.ragnarok.fenrir.model.SaveMessageBuilder
import dev.ragnarok.fenrir.model.SentMsg
import dev.ragnarok.fenrir.model.TypingMessageOrUploadingInDialog
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface IMessagesRepository {
    fun observeMessagesSendErrors(): SharedFlow<Throwable>
    fun handleFlagsUpdates(
        accountId: Long,
        setUpdates: List<MessageFlagsSetUpdate>?,
        resetUpdates: List<MessageFlagsResetUpdate>?
    ): Flow<Boolean>

    fun handleReadUpdates(
        accountId: Long,
        setUpdates: List<OutputMessagesSetReadUpdate>?,
        resetUpdates: List<InputMessagesSetReadUpdate>?
    ): Flow<Boolean>

    fun handlePinConversationUpdates(
        accountId: Long,
        updates: List<PinConversationUpdate>?,
    ): Flow<Boolean>

    fun handleUnreadBadgeUpdates(
        accountId: Long,
        updates: List<BadgeCountChangeUpdate>?
    ): Flow<Boolean>

    fun handleTypingMessageOrUploadingInDialogUpdates(
        accountId: Long,
        updates: List<TypingMessageOrUploadingInDialogUpdate>?
    ): Flow<Boolean>

    fun handleMessageReactionsChangedUpdates(
        accountId: Long,
        updates: List<ReactionMessageChangeUpdate>?
    ): Flow<Boolean>

    fun observeSentMessages(): SharedFlow<SentMsg>
    fun observePeerUpdates(): SharedFlow<List<PeerUpdate>>
    fun observeMessageUpdates(): SharedFlow<MessageUpdate>
    fun observeTypingMessageOrUploadingInDialog(): SharedFlow<List<TypingMessageOrUploadingInDialog>>
    fun observePeerDeleting(): SharedFlow<PeerDeleting>
    fun getConversationByIdSingle(accountId: Long, peerId: Long, mode: Mode): Flow<Conversation>
    fun getConversationById(accountId: Long, peerId: Long, mode: Mode): Flow<Conversation>
    fun editMessage(
        accountId: Long,
        message: Message,
        text: String?,
        attachments: List<AbsModel>,
        keepForwardMessages: Boolean
    ): Flow<Message>

    fun runSendingQueue()

    /**
     * Получить все закэшированные сообщения в локальной БД
     *
     * @param accountId идентификатор аккаунта
     * @param peerId    идентификатор диалога
     * @return полученные сообщения
     */
    fun getCachedPeerMessages(accountId: Long, peerId: Long): Flow<List<Message>>

    /**
     * Получить все закэшированные диалоги в локальной БД
     *
     * @param accountId идентификатор аккаунта
     * @return диалоги
     */
    fun getCachedConversations(accountId: Long): Flow<List<Dialog>>
    fun getMessagesFromLocalJSon(
        accountId: Long,
        context: Context
    ): Flow<Pair<Peer, List<Message>>>

    /**
     * Сохранить в локальную БД сообщения
     *
     * @param accountId идентификатор аккаунта
     * @param messages  сообщения
     * @return Completable
     */
    fun insertMessages(accountId: Long, messages: List<VKApiMessage>): Flow<Boolean>

    /**
     * Получить актуальный список сообщений для конкретного диалога
     *
     * @param accountId      идентификатор аккаунта
     * @param peerId         идентификатор диалога
     * @param count          количество сообщений
     * @param offset         сдвиг (может быть как положительным, так и отрицательным)
     * @param startCmId идентификатор сообщения, после которого необходимо получить (если null - от последнего)
     * @param cacheData      если true - сохранить полученные данные в кэш
     * @return полученные сообщения
     */
    fun getPeerMessages(
        accountId: Long,
        peerId: Long,
        count: Int,
        offset: Int?,
        startCmId: Int?,
        excludeStartMessage: Boolean,
        cacheData: Boolean,
        rev: Boolean
    ): Flow<List<Message>>

    fun getJsonHistory(
        accountId: Long,
        offset: Int?,
        count: Int?,
        peerId: Long
    ): Flow<List<String>>

    fun getImportantMessages(
        accountId: Long,
        count: Int,
        offset: Int?,
    ): Flow<List<Message>>

    fun getConversations(
        accountId: Long,
        count: Int,
        startPeerId: Long?,
        startCmId: Int?
    ): Flow<List<Dialog>>

    fun insertConversation(accountId: Long, dialog: Dialog): Flow<Boolean>
    fun findCachedMessages(accountId: Long, cmIds: List<Pair<Long, Int>>): Flow<List<Message>>
    fun put(builder: SaveMessageBuilder): Flow<Message>
    fun sendUnsentMessage(accountIds: Collection<Long>): Flow<SentMsg>
    fun enqueueAgain(accountId: Long, peerId: Long, cmId: Int): Flow<Boolean>
    fun enqueueAgainList(accountId: Long, peerId: Long, cmIds: Collection<Int>): Flow<Boolean>

    /**
     * Поиск диалогов
     *
     * @param accountId идентификатор аккаунта
     * @param count     количество результатов
     * @param q         строка поиска
     * @return список найденных диалогов
     */
    fun searchConversations(accountId: Long, count: Int, q: String?): Flow<List<Conversation>>
    fun searchMessages(
        accountId: Long,
        peerId: Long?,
        date: String?,
        count: Int,
        offset: Int,
        q: String?
    ): Flow<List<Message>>

    fun getChatUsers(accountId: Long, chatId: Long): Flow<List<AppChatUser>>
    fun removeChatMember(accountId: Long, chatId: Long, userId: Long): Flow<Boolean>
    fun addChatUsers(accountId: Long, chatId: Long, users: List<User>): Flow<List<AppChatUser>>
    fun deleteChatPhoto(accountId: Long, chatId: Long): Flow<Boolean>
    fun deleteConversation(accountId: Long, peedId: Long): Flow<Boolean>
    fun deleteMessages(
        accountId: Long,
        peerId: Long,
        cmIds: Collection<Int>,
        deleteForAll: Boolean,
        spam: Boolean?
    ): Flow<Boolean>

    fun restoreMessage(accountId: Long, peerId: Long, cmId: Int): Flow<Boolean>
    fun editChat(accountId: Long, chatId: Long, title: String?): Flow<Boolean>
    fun createGroupChat(accountId: Long, users: Collection<Long>, title: String?): Flow<Long>
    fun recogniseAudioMessage(
        accountId: Long,
        audioMessageId: String?,
        cmId: Int,
        peerId: Long
    ): Flow<Int>

    fun setMemberRole(
        accountId: Long,
        chatId: Long,
        memberId: Long,
        isAdmin: Boolean
    ): Flow<Boolean>

    fun markAsRead(accountId: Long, peerId: Long, startMessageId: Int): Flow<Boolean>
    fun markAsImportant(
        accountId: Long,
        peerId: Long,
        ids: Collection<Int>,
        important: Int?
    ): Flow<Boolean>

    fun pinMessage(accountId: Long, peerId: Long, message: Message?): Flow<Boolean>
    fun pinUnPinConversation(accountId: Long, peerId: Long, pin: Boolean): Flow<Boolean>
    fun markAsListened(accountId: Long, peerId: Long, cmId: Int): Flow<Boolean>
    fun sendOrDeleteReaction(
        accountId: Long,
        peerId: Long, cmId: Int, reactionId: Int?
    ): Flow<Boolean>

    fun getReactionsAssets(accountId: Long): Flow<List<ReactionAsset>>
    fun getReactedPeers(
        accountId: Long,
        peerId: Long,
        cmId: Int,
        reactionId: Int?
    ): Flow<List<ReactedMessagesPeers>>
}
