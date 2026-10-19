package dev.ragnarok.fenrir.db.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.db.model.MessageEditEntity
import dev.ragnarok.fenrir.db.model.MessagePatch
import dev.ragnarok.fenrir.db.model.entity.MessageDboEntity
import dev.ragnarok.fenrir.model.DraftMessage
import dev.ragnarok.fenrir.model.MessageStatus
import dev.ragnarok.fenrir.model.criteria.MessagesCriteria
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Pair
import kotlinx.coroutines.flow.Flow

interface IMessagesStorage : IStorage {
    fun insertPeerDbos(
        accountId: Long,
        peerId: Long,
        dbos: List<MessageDboEntity>,
        clearHistory: Boolean
    ): Flow<Boolean>

    fun insert(accountId: Long, dbos: List<MessageDboEntity>): Flow<IntArray>
    fun getByCriteria(
        criteria: MessagesCriteria,
        withAtatchments: Boolean,
        withForwardMessages: Boolean
    ): Flow<List<MessageDboEntity>>

    fun insert(accountId: Long, peerId: Long, patch: MessageEditEntity): Flow<Int>
    fun applyPatch(accountId: Long, messageId: Int, peerId: Long, patch: MessageEditEntity): Flow<Int>

    fun findDraftMessage(accountId: Long, peerId: Long): Flow<DraftMessage?>

    fun saveDraftMessageBody(accountId: Long, peerId: Long, text: String?): Flow<Int>

    //@CheckResult
    //Maybe<Integer> getDraftMessageId(int accountId, int peerId);
    fun getMessageStatus(accountId: Long, dbid: Int, peerId: Long): Flow<Int>
    fun applyPatches(accountId: Long, patches: Collection<MessagePatch>): Flow<Boolean>

    fun changeMessageStatus(
        accountId: Long,
        messageId: Int,
        peerId: Long,
        @MessageStatus status: Int,
        vkid: Int?,
        cmid: Int?
    ): Flow<Boolean>

    fun changeMessagesStatus(
        accountId: Long,
        peerId: Long,
        ids: Collection<Int>,
        @MessageStatus status: Int
    ): Flow<Boolean>

    //@CheckResult
    //Completable updateMessageFlag(int accountId, int messageId, Collection<Pair<Integer, Boolean>> values);
    fun deleteMessage(accountId: Long, messageId: Int, peerId: Long): Flow<Boolean>
    fun findLastSentMessageIdForPeer(accountId: Long, peerId: Long): Flow<Optional<Int>>
    fun findMessagesByIds(
        accountId: Long,
        ids: List<Pair<Long, Int>>,
        withAttachments: Boolean,
        withForwardMessages: Boolean
    ): Flow<List<MessageDboEntity>>

    fun findFirstUnsentMessage(
        accountIds: Collection<Long>,
        withAttachments: Boolean,
        withForwardMessages: Boolean
    ): Flow<Optional<Pair<Long, MessageDboEntity>>>

    //Observable<MessageUpdate> observeMessageUpdates();
    fun getMissingMessages(accountId: Long, ids: Collection<Pair<Long, Int>>): Flow<Collection<Pair<Long, Int>>>

    fun deleteMessages(accountId: Long, ids: Collection<Pair<Long, Int>>): Flow<Boolean>
}