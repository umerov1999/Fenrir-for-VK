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
    fun applyPatch(accountId: Long, cmId: Int, peerId: Long, patch: MessageEditEntity): Flow<Int>

    fun findDraftMessage(accountId: Long, peerId: Long): Flow<DraftMessage?>

    fun saveDraftMessageBody(accountId: Long, peerId: Long, text: String?): Flow<Int>

    //@CheckResult
    //Maybe<Integer> getDraftMessageId(int accountId, int peerId);
    fun getMessageStatus(accountId: Long, cmId: Int, peerId: Long): Flow<Int>
    fun applyPatches(accountId: Long, patches: Collection<MessagePatch>): Flow<Boolean>

    fun changeMessageStatus(
        accountId: Long,
        dbCmId: Int,
        peerId: Long,
        @MessageStatus status: Int,
        vkCmId: Int?,
    ): Flow<Boolean>

    fun changeMessagesStatus(
        accountId: Long,
        peerId: Long,
        cmIds: Collection<Int>,
        @MessageStatus status: Int
    ): Flow<Boolean>

    //@CheckResult
    //Completable updateMessageFlag(int accountId, int cmId, Collection<Pair<Integer, Boolean>> values);
    fun deleteMessage(accountId: Long, cmId: Int, peerId: Long): Flow<Boolean>
    fun findLastSentCmIdForPeer(accountId: Long, peerId: Long): Flow<Optional<Int>>
    fun findMessagesByIds(
        accountId: Long,
        cmIds: List<Pair<Long, Int>>,
        withAttachments: Boolean,
        withForwardMessages: Boolean
    ): Flow<List<MessageDboEntity>>

    fun findFirstUnsentMessage(
        accountIds: Collection<Long>,
        withAttachments: Boolean,
        withForwardMessages: Boolean
    ): Flow<Optional<Pair<Long, MessageDboEntity>>>

    //Observable<MessageUpdate> observeMessageUpdates();
    fun getMissingMessages(accountId: Long, cmIds: Collection<Pair<Long, Int>>): Flow<Collection<Pair<Long, Int>>>

    fun deleteMessages(accountId: Long, cmIds: Collection<Pair<Long, Int>>): Flow<Boolean>
}