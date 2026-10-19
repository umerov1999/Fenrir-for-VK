package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.AttachToType
import dev.ragnarok.fenrir.db.FenrirContentProvider
import dev.ragnarok.fenrir.db.FenrirContentProvider.Companion.getMessageContentUriFor
import dev.ragnarok.fenrir.db.RecordNotFoundException
import dev.ragnarok.fenrir.db.column.MessagesColumns
import dev.ragnarok.fenrir.db.impl.AttachmentsStorage.Companion.mapAttachmentsFromBlob
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage
import dev.ragnarok.fenrir.db.interfaces.IMessagesStorage
import dev.ragnarok.fenrir.db.model.MessageEditEntity
import dev.ragnarok.fenrir.db.model.MessagePatch
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity
import dev.ragnarok.fenrir.db.model.entity.MessageDboEntity
import dev.ragnarok.fenrir.db.model.entity.ReactionEntity
import dev.ragnarok.fenrir.exception.NotFoundException
import dev.ragnarok.fenrir.getBlob
import dev.ragnarok.fenrir.getBoolean
import dev.ragnarok.fenrir.getInt
import dev.ragnarok.fenrir.getLong
import dev.ragnarok.fenrir.getString
import dev.ragnarok.fenrir.ifNonNull
import dev.ragnarok.fenrir.ifNonNullNoEmpty
import dev.ragnarok.fenrir.model.ChatAction
import dev.ragnarok.fenrir.model.DraftMessage
import dev.ragnarok.fenrir.model.MessageStatus
import dev.ragnarok.fenrir.model.criteria.MessagesCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.requireNonNull
import dev.ragnarok.fenrir.util.Exestime.log
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.empty
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.Utils.join
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.isActive
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.syncSingleSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.msgpack.MsgPack

internal class MessagesStorage(base: AppStorages) : AbsStorage(base), IMessagesStorage {
    override fun insertPeerDbos(
        accountId: Long,
        peerId: Long,
        dbos: List<MessageDboEntity>,
        clearHistory: Boolean
    ): Flow<Boolean> {
        return flow {
            val operations = ArrayList<ContentProviderOperation>()
            if (clearHistory) {
                val uri = getMessageContentUriFor(accountId)
                val where =
                    MessagesColumns.PEER_ID + " = ? AND " + MessagesColumns.STATUS + " = ?"
                val args = arrayOf(
                    peerId.toString(),
                    MessageStatus.SENT.toString()
                )
                operations.add(
                    ContentProviderOperation.newDelete(uri).withSelection(where, args).build()
                )
            }
            for (dbo in dbos) {
                appendDboOperation(
                    attachments = stores.attachments(),
                    accountId = accountId,
                    dbo = dbo,
                    target = operations
                )
            }
            contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            emit(true)
        }
    }

    override fun insert(accountId: Long, dbos: List<MessageDboEntity>): Flow<IntArray> {
        return flow {
            val operations = ArrayList<ContentProviderOperation>()
            val indexes = IntArray(dbos.size)
            for (i in dbos.indices) {
                val dbo = dbos[i]
                val index = appendDboOperation(
                    attachments = stores.attachments(),
                    accountId = accountId,
                    dbo = dbo,
                    target = operations
                )
                indexes[i] = index
            }
            val results =
                contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            val ids = IntArray(dbos.size)
            for (i in indexes.indices) {
                val index = indexes[i]
                val result = results[index]
                ids[i] = extractId(result)
            }
            emit(ids)
        }
    }

    override fun findLastSentMessageIdForPeer(
        accountId: Long,
        peerId: Long
    ): Flow<Optional<Int>> {
        return flow {
            val uri = getMessageContentUriFor(accountId)
            val projection = arrayOf(BaseColumns._ID)
            val where = MessagesColumns.PEER_ID + " = ?" +
                    " AND " + MessagesColumns.STATUS + " = ?" +
                    " AND " + MessagesColumns.DELETED + " = ?"
            val args = arrayOf(
                peerId.toString(), MessageStatus.SENT.toString(),
                "0"
            )
            val cursor = contentResolver.query(
                uri,
                projection,
                where,
                args,
                MessagesColumns.FULL_ID + " DESC LIMIT 1"
            )
            var id: Int? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(BaseColumns._ID)
                }
                cursor.close()
            }
            emit(wrap(id))
        }
    }

    private fun queryMessagesByCriteria(criteria: MessagesCriteria): Cursor? {
        val where: String
        val args: Array<String>
        val startMessageId = criteria.startMessageId
        if (startMessageId == null) {
            where = MessagesColumns.PEER_ID + " = ?" +
                    " AND " + MessagesColumns.STATUS + " != ?"
            args = arrayOf(
                criteria.peerId.toString(),
                MessageStatus.EDITING.toString()
            )
        } else {
            where = MessagesColumns.PEER_ID + " = ?" +
                    " AND " + MessagesColumns.FULL_ID + " < ? " +
                    " AND " + MessagesColumns.STATUS + " != ?"
            args = arrayOf(
                criteria.peerId.toString(),
                startMessageId.toString(), MessageStatus.EDITING.toString()
            )
        }
        val uri = getMessageContentUriFor(criteria.accountId)
        return contentResolver.query(uri, null, where, args, ORDER_BY)
    }

    override fun getByCriteria(
        criteria: MessagesCriteria,
        withAtatchments: Boolean,
        withForwardMessages: Boolean
    ): Flow<List<MessageDboEntity>> {
        return flow {
            val start = System.currentTimeMillis()
            val cursor = queryMessagesByCriteria(criteria)
            val dbos = ArrayList<MessageDboEntity>(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (!isActive()) {
                        break
                    }
                    val dbo = fullMapDbo(
                        accountId = criteria.accountId,
                        cursor = cursor,
                        withAttachments = withAtatchments,
                        withForwardMessages = withForwardMessages
                    )
                    val position = dbos.size - cursor.position
                    dbos.add(position, dbo)
                }
                cursor.close()
            }
            log("MessagesStorage.getByCriteria", start, "count: " + dbos.size)
            emit(dbos)
        }
    }

    override fun insert(accountId: Long, peerId: Long, patch: MessageEditEntity): Flow<Int> {
        return flow {
            val cv = ContentValues()
            cv.put(MessagesColumns.PEER_ID, peerId)
            cv.put(MessagesColumns.FROM_ID, patch.senderId)
            cv.put(MessagesColumns.DATE, patch.date)
            //cv.put(MessageColumns.READ_STATE, patch.isRead());
            cv.put(MessagesColumns.OUT, patch.isOut)
            //cv.put(MessageColumns.TITLE, patch.getTitle());
            cv.put(MessagesColumns.TEXT, patch.text)
            cv.put(MessagesColumns.IMPORTANT, patch.isImportant)
            cv.put(MessagesColumns.DELETED, patch.isDeleted)
            cv.put(MessagesColumns.STATUS, patch.status)
            patch.extras.ifNonNull({
                cv.put(
                    MessagesColumns.EXTRAS,
                    MsgPack.encodeToByteArrayEx(
                        MapSerializer(Int.serializer(), String.serializer()),
                        it
                    )
                )
            }, {
                cv.putNull(
                    MessagesColumns.EXTRAS
                )
            })

            cv.put(MessagesColumns.PAYLOAD, patch.payload)

            patch.forward.ifNonNullNoEmpty({
                cv.put(
                    MessagesColumns.FORWARD_BLOB,
                    MsgPack.encodeToByteArrayEx(
                        ListSerializer(MessageDboEntity.serializer()),
                        it
                    )
                )
            }, {
                cv.putNull(MessagesColumns.FORWARD_BLOB)
            })

            stores.attachments()
                .attachToBlobSync(cv, AttachToType.MESSAGE, patch.attachments).syncSingleSafe()

            val uri = getMessageContentUriFor(accountId)
            val result = contentResolver.insert(uri, cv)
            val resultMessageId = extractId(result)
            emit(resultMessageId)
        }
    }

    override fun applyPatch(
        accountId: Long,
        messageId: Int,
        peerId: Long,
        patch: MessageEditEntity
    ): Flow<Int> {
        return flow {
            val uri = getMessageContentUriFor(accountId)
            val cv = ContentValues()
            cv.put(MessagesColumns.FROM_ID, patch.senderId)
            cv.put(MessagesColumns.DATE, patch.date)
            //cv.put(MessageColumns.READ_STATE, patch.isRead());
            cv.put(MessagesColumns.OUT, patch.isOut)
            //cv.put(MessageColumns.TITLE, patch.getTitle());
            cv.put(MessagesColumns.TEXT, patch.text)
            cv.put(MessagesColumns.IMPORTANT, patch.isImportant)
            cv.put(MessagesColumns.DELETED, patch.isDeleted)
            cv.put(MessagesColumns.STATUS, patch.status)

            patch.extras.ifNonNull({
                cv.put(
                    MessagesColumns.EXTRAS,
                    MsgPack.encodeToByteArrayEx(
                        MapSerializer(
                            Int.serializer(),
                            String.serializer()
                        ), it
                    )
                )
            }, {
                cv.putNull(
                    MessagesColumns.EXTRAS
                )
            })

            cv.put(MessagesColumns.PAYLOAD, patch.payload)

            val where = BaseColumns._ID + " = ? AND " + MessagesColumns.PEER_ID + " = ?"
            val args = arrayOf(messageId.toString(), peerId.toString())

            stores.attachments()
                .attachToBlobSync(
                    cv = cv,
                    attachToType = AttachToType.MESSAGE,
                    attachmentsEntities = patch.attachments,
                    where = where,
                    args = args,
                    replace = false,
                    accountId = accountId
                )
                .syncSingleSafe()

            patch.forward.ifNonNullNoEmpty({
                cv.put(
                    MessagesColumns.FORWARD_BLOB,
                    MsgPack.encodeToByteArrayEx(
                        ListSerializer(MessageDboEntity.serializer()),
                        it
                    )
                )
            }, {
                cv.putNull(MessagesColumns.FORWARD_BLOB)
            })

            contentResolver.update(uri, cv, where, args)
            emit(messageId)
        }
    }

    private fun fullMapDbo(
        accountId: Long,
        cursor: Cursor,
        withAttachments: Boolean,
        withForwardMessages: Boolean,
    ): MessageDboEntity {
        val dbo = baseMapDbo(cursor)
        if (withAttachments) {
            val attachments = mapAttachmentsFromBlob(
                cursor = cursor,
                attachToType = AttachToType.MESSAGE
            )
            dbo.setAttachments(attachments)
        } else {
            dbo.setAttachments(null)
        }
        if (withForwardMessages) {
            val forwardsJson =
                cursor.getBlob(MessagesColumns.FORWARD_BLOB)
            val forwards = if (forwardsJson.nonNullNoEmpty()) {
                val forwardsEntity =
                    MsgPack.decodeFromByteArrayEx(
                        ListSerializer(MessageDboEntity.serializer()),
                        forwardsJson
                    )
                forwardsEntity.ifEmpty {
                    null
                }
            } else {
                null
            }
            for(i in forwards.orEmpty()) {
                i.setOut(i.fromId == accountId)
            }
            dbo.setForwardMessages(forwards)
        } else {
            dbo.setForwardMessages(null)
        }
        return dbo
    }

    override fun findDraftMessage(accountId: Long, peerId: Long): Flow<DraftMessage?> {
        return flow {
            val columns = arrayOf(BaseColumns._ID, MessagesColumns.TEXT)
            val uri = getMessageContentUriFor(accountId)
            val where = MessagesColumns.PEER_ID + " = ? AND " + MessagesColumns.STATUS + " = ?"
            val args = arrayOf(peerId.toString(), MessageStatus.EDITING.toString())
            val cursor = contentResolver.query(
                uri,
                columns,
                where,
                args,
                null
            )
            if (isActive()) {
                var message: DraftMessage? = null
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        val id = cursor.getInt(BaseColumns._ID)
                        val body = cursor.getString(MessagesColumns.TEXT)
                        message = DraftMessage(id, body)
                    }
                    cursor.close()
                }
                if (message != null) {
                    val count = stores.attachments()
                        .getAttachmentsCount(
                            accountId = accountId,
                            attachToType = AttachToType.MESSAGE,
                            where = where,
                            args = args
                        )
                        .single()
                    message.setAttachmentsCount(count)
                }
                emit(message)
            }
        }
    }

    override fun saveDraftMessageBody(accountId: Long, peerId: Long, text: String?): Flow<Int> {
        return flow {
            val start = System.currentTimeMillis()
            val uri = getMessageContentUriFor(accountId)
            val cv = ContentValues()
            cv.put(MessagesColumns.TEXT, text)
            cv.put(MessagesColumns.PEER_ID, peerId)
            cv.put(MessagesColumns.STATUS, MessageStatus.EDITING)
            val cr = contentResolver
            var existDraftMessageId = findDraftMessageId(accountId, peerId)
            if (existDraftMessageId != null) {
                cr.update(
                    uri,
                    cv,
                    BaseColumns._ID + " = ?",
                    arrayOf(existDraftMessageId.toString())
                )
            } else {
                val resultUri = cr.insert(uri, cv)
                existDraftMessageId = extractId(resultUri)
            }
            log("saveDraftMessageBody", start)
            emit(existDraftMessageId)
        }
    }

    override fun applyPatches(accountId: Long, patches: Collection<MessagePatch>): Flow<Boolean> {
        return flow {
            val uri = getMessageContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>(patches.size)
            for (patch in patches) {
                patch.reaction.requireNonNull { to ->
                    val cv = ContentValues()

                    if (!to.keepMyReaction) {
                        cv.put(MessagesColumns.REACTION_ID, to.reactionId)
                    }
                    to.reactions.ifNonNullNoEmpty({
                        cv.put(
                            MessagesColumns.REACTIONS,
                            MsgPack.encodeToByteArrayEx(
                                ListSerializer(ReactionEntity.serializer()),
                                it
                            )
                        )
                    }, {
                        cv.putNull(
                            MessagesColumns.REACTIONS
                        )
                    })

                    if (cv.size() > 0) {
                        operations.add(
                            ContentProviderOperation.newUpdate(uri)
                                .withValues(cv)
                                .withSelection(
                                    MessagesColumns.PEER_ID + " = ? AND " + MessagesColumns.CONVERSATION_MESSAGE_ID + " = ?",
                                    arrayOf(patch.peerId.toString(), patch.messageId.toString())
                                )
                                .build()
                        )
                    }
                }

                val cv = ContentValues()
                patch.deletion.requireNonNull {
                    cv.put(MessagesColumns.DELETED, it.deleted)
                    cv.put(MessagesColumns.DELETED_FOR_ALL, it.deletedForAll)
                }
                patch.important.requireNonNull {
                    cv.put(MessagesColumns.IMPORTANT, it.important)
                }
                if (cv.size() == 0) continue
                operations.add(
                    ContentProviderOperation.newUpdate(uri)
                        .withValues(cv)
                        .withSelection(
                            BaseColumns._ID + " = ? AND " + MessagesColumns.PEER_ID + " = ?",
                            arrayOf(patch.messageId.toString(), patch.peerId.toString())
                        )
                        .build()
                )
            }
            contentResolver.applyBatch(FenrirContentProvider.AUTHORITY, operations)
            emit(true)
        }
    }

    override fun getMessageStatus(accountId: Long, dbid: Int, peerId: Long): Flow<Int> {
        return flow {
            val cursor = contentResolver.query(
                getMessageContentUriFor(accountId),
                arrayOf(MessagesColumns.STATUS),
                MessagesColumns.FULL_ID + " = ? AND " + MessagesColumns.FULL_PEER_ID + " = ?",
                arrayOf(dbid.toString(), peerId.toString()),
                null
            )
            var result: Int? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    result = cursor.getInt(MessagesColumns.STATUS)
                }
                cursor.close()
            }
            if (result == null) {
                throw RecordNotFoundException("Message with id $dbid not found")
            }
            emit(result)
        }
    }

    private fun findDraftMessageId(accountId: Long, peerId: Long): Int? {
        val columns = arrayOf(BaseColumns._ID)
        val uri = getMessageContentUriFor(accountId)
        val cursor = contentResolver.query(
            uri,
            columns,
            MessagesColumns.PEER_ID + " = ? AND " + MessagesColumns.STATUS + " = ?",
            arrayOf(peerId.toString(), MessageStatus.EDITING.toString()),
            null
        )
        var id: Int? = null
        if (cursor != null) {
            if (cursor.moveToNext()) {
                id = cursor.getInt(BaseColumns._ID)
            }
            cursor.close()
        }
        return id
    }

    override fun changeMessageStatus(
        accountId: Long,
        messageId: Int,
        peerId: Long,
        @MessageStatus status: Int,
        vkid: Int?,
        cmid: Int?
    ): Flow<Boolean> {
        return flow {
            val contentValues = ContentValues()
            contentValues.put(MessagesColumns.STATUS, status)
            if (vkid != null) {
                contentValues.put(BaseColumns._ID, vkid)
            }
            if (cmid != null) {
                contentValues.put(MessagesColumns.CONVERSATION_MESSAGE_ID, cmid)
            }
            val uri = getMessageContentUriFor(accountId)
            val count = contentResolver.update(
                uri,
                contentValues,
                BaseColumns._ID + " = ? AND " + MessagesColumns.PEER_ID + " = ?",
                arrayOf(messageId.toString(), peerId.toString())
            )
            if (count > 0) {
                emit(true)
            } else {
                throw NotFoundException()
            }
        }
    }

    override fun deleteMessage(accountId: Long, messageId: Int, peerId: Long): Flow<Boolean> {
        require(messageId != 0 && peerId != 0L) { "Invalid message id: $messageId or peer id: $peerId" }
        return flow {
            val uri = getMessageContentUriFor(accountId)
            val count = contentResolver.delete(
                uri,
                BaseColumns._ID + " = ? AND " + MessagesColumns.PEER_ID + " = ?",
                arrayOf(messageId.toString(), peerId.toString())
            )
            emit(count > 0)
        }
    }

    override fun deleteMessages(accountId: Long, ids: Collection<Pair<Long, Int>>): Flow<Boolean> {
        return flow {
            val uri = getMessageContentUriFor(accountId)

            val where = ids.joinToString(" OR ", prefix = "(", postfix = ")") {
                "(${MessagesColumns.FULL_ID} = ${it.second} AND " +
                        "${MessagesColumns.FULL_PEER_ID} = ${it.first})"
            }
            val count = contentResolver.delete(uri, where, null)
            emit(count > 0)
        }
    }

    override fun changeMessagesStatus(
        accountId: Long,
        peerId: Long,
        ids: Collection<Int>,
        @MessageStatus status: Int
    ): Flow<Boolean> {
        return flow {
            val copy: Set<Int> = HashSet(ids)
            val contentValues = ContentValues()
            contentValues.put(MessagesColumns.STATUS, status)
            val uri = getMessageContentUriFor(accountId)
            val where = MessagesColumns.FULL_ID + " IN(" + join(
                ",",
                copy
            ) + ") AND " + MessagesColumns.FULL_PEER_ID + " = ?"
            val count = contentResolver.update(
                uri, contentValues,
                where, arrayOf(peerId.toString())
            )
            if (count > 0) {
                emit(true)
            } else {
                throw NotFoundException()
            }
        }
    }

    override fun getMissingMessages(
        accountId: Long,
        ids: Collection<Pair<Long, Int>>
    ): Flow<Collection<Pair<Long, Int>>> {
        return flow {
            val copy: MutableList<Pair<Long, Int>> = ArrayList(ids)
            val uri = getMessageContentUriFor(accountId)
            val projection = arrayOf(BaseColumns._ID, MessagesColumns.PEER_ID)

            val where = ids.joinToString(" OR ", prefix = "(", postfix = ")") {
                "(${MessagesColumns.FULL_ID} = ${it.second} AND " +
                        "${MessagesColumns.FULL_PEER_ID} = ${it.first})"
            }
            val cursor =
                contentResolver.query(uri, projection, where, null, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(BaseColumns._ID)
                    val peerId = cursor.getLong(MessagesColumns.PEER_ID)
                    copy.removeIf {
                        it.first == peerId && it.second == id
                    }
                }
                cursor.close()
            }
            emit(ArrayList(copy))
        }
    }

    override fun findMessagesByIds(
        accountId: Long,
        ids: List<Pair<Long, Int>>,
        withAttachments: Boolean,
        withForwardMessages: Boolean
    ): Flow<List<MessageDboEntity>> {
        return flow {
            val uri = getMessageContentUriFor(accountId)
            val where: String
            val args: Array<String>?
            if (ids.size == 1) {
                where = BaseColumns._ID + " = ? AND " + MessagesColumns.PEER_ID + " = ?"
                args = arrayOf(ids[0].second.toString(), ids[0].first.toString())
            } else {
                where = ids.joinToString(" OR ", prefix = "(", postfix = ")") {
                    "(${MessagesColumns.FULL_ID} = ${it.first} AND " +
                            "${MessagesColumns.FULL_PEER_ID} = ${it.second})"
                }
                args = null
            }
            val cursor = contentResolver.query(uri, null, where, args, null)
            val dbos = ArrayList<MessageDboEntity>(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (!isActive()) {
                        break
                    }
                    val dbo = fullMapDbo(
                        accountId = accountId,
                        cursor = cursor,
                        withAttachments = withAttachments,
                        withForwardMessages = withForwardMessages
                    )
                    val position = dbos.size - cursor.position
                    dbos.add(position, dbo)
                }
                cursor.close()
            }
            emit(dbos)
        }
    }

    override fun findFirstUnsentMessage(
        accountIds: Collection<Long>,
        withAttachments: Boolean,
        withForwardMessages: Boolean
    ): Flow<Optional<Pair<Long, MessageDboEntity>>> {
        return flow {
            val where = MessagesColumns.STATUS + " = ?"
            val args = arrayOf(MessageStatus.QUEUE.toString())
            val orderBy = BaseColumns._ID + " ASC LIMIT 1"
            for (accountId in accountIds) {
                if (!isActive()) {
                    break
                }
                val uri = getMessageContentUriFor(accountId)
                val cursor = contentResolver.query(uri, null, where, args, orderBy)
                var entity: MessageDboEntity? = null
                if (cursor != null) {
                    if (cursor.moveToNext()) {
                        entity = fullMapDbo(
                            accountId = accountId,
                            cursor = cursor,
                            withAttachments = withAttachments,
                            withForwardMessages = withForwardMessages
                        )
                    }
                    cursor.close()
                }
                if (entity != null) {
                    emit(wrap(create(accountId, entity)))
                    return@flow
                }
            }
            emit(empty())
        }
    }

    companion object {
        private const val ORDER_BY = MessagesColumns.FULL_STATUS + ", " + MessagesColumns.FULL_ID
        fun appendDboOperation(
            attachments: IAttachmentsStorage,
            accountId: Long,
            dbo: MessageDboEntity,
            target: MutableList<ContentProviderOperation>
        ): Int {
            val cv = ContentValues()
            cv.put(BaseColumns._ID, dbo.id)
            cv.put(MessagesColumns.PEER_ID, dbo.peerId)
            cv.put(MessagesColumns.FROM_ID, dbo.fromId)
            cv.put(MessagesColumns.DATE, dbo.date)
            //cv.put(MessageColumns.READ_STATE, dbo.isRead());
            cv.put(MessagesColumns.OUT, dbo.isOut)
            //cv.put(MessageColumns.TITLE, dbo.getTitle());
            cv.put(MessagesColumns.TEXT, dbo.text)
            cv.put(MessagesColumns.IMPORTANT, dbo.isImportant)
            cv.put(MessagesColumns.DELETED, dbo.isDeleted)
            cv.put(MessagesColumns.STATUS, dbo.status)
            cv.put(MessagesColumns.ACTION, dbo.action)
            cv.put(MessagesColumns.ACTION_MID, dbo.actionMemberId)
            cv.put(MessagesColumns.ACTION_EMAIL, dbo.actionEmail)
            cv.put(MessagesColumns.ACTION_TEXT, dbo.actionText)
            cv.put(MessagesColumns.PHOTO_50, dbo.photo50)
            cv.put(MessagesColumns.PHOTO_100, dbo.photo100)
            cv.put(MessagesColumns.PHOTO_200, dbo.photo200)
            cv.put(MessagesColumns.RANDOM_ID, dbo.randomId)
            dbo.extras.ifNonNull({
                cv.put(
                    MessagesColumns.EXTRAS,
                    MsgPack.encodeToByteArrayEx(
                        MapSerializer(Int.serializer(), String.serializer()),
                        it
                    )
                )
            }, {
                cv.putNull(
                    MessagesColumns.EXTRAS
                )
            })

            dbo.keyboard.ifNonNull({
                cv.put(
                    MessagesColumns.KEYBOARD,
                    MsgPack.encodeToByteArrayEx(KeyboardEntity.serializer(), it)
                )
            }, {
                cv.putNull(
                    MessagesColumns.KEYBOARD
                )
            })
            dbo.reactions.ifNonNull({
                cv.put(
                    MessagesColumns.REACTIONS,
                    MsgPack.encodeToByteArrayEx(ListSerializer(ReactionEntity.serializer()), it)
                )
            }, {
                cv.putNull(
                    MessagesColumns.REACTIONS
                )
            })
            cv.put(MessagesColumns.UPDATE_TIME, dbo.updateTime)
            cv.put(MessagesColumns.CONVERSATION_MESSAGE_ID, dbo.conversation_message_id)
            cv.put(MessagesColumns.REACTION_ID, dbo.reaction_id)
            cv.put(MessagesColumns.PAYLOAD, dbo.payload)
            attachments.attachToBlobSync(cv, AttachToType.MESSAGE, dbo.getAttachments())
                .syncSingleSafe()

            dbo.forwardMessages.ifNonNullNoEmpty({
                cv.put(
                    MessagesColumns.FORWARD_BLOB,
                    MsgPack.encodeToByteArrayEx(
                        ListSerializer(MessageDboEntity.serializer()),
                        it
                    )
                )
            }, {
                cv.putNull(MessagesColumns.FORWARD_BLOB)
            })

            val uri = getMessageContentUriFor(accountId)
            val builder = ContentProviderOperation.newInsert(uri)
                .withValues(cv)

            return addToListAndReturnIndex(target, builder.build())
        }

        internal fun baseMapDbo(cursor: Cursor): MessageDboEntity {
            @MessageStatus val status =
                cursor.getInt(MessagesColumns.STATUS)
            @ChatAction val action =
                cursor.getInt(MessagesColumns.ACTION)
            val id = cursor.getInt(BaseColumns._ID)
            val peerId = cursor.getLong(MessagesColumns.PEER_ID)
            val fromId = cursor.getLong(MessagesColumns.FROM_ID)
            var extras: Map<Int, String>? = null
            var keyboard: KeyboardEntity? = null
            var reactions: List<ReactionEntity>? = null
            val extrasText = cursor.getBlob(MessagesColumns.EXTRAS)
            if (extrasText.nonNullNoEmpty()) {
                extras = MsgPack.decodeFromByteArrayEx(
                    MapSerializer(
                        Int.serializer(),
                        String.serializer()
                    ), extrasText
                )
            }
            val keyboardText =
                cursor.getBlob(MessagesColumns.KEYBOARD)
            if (keyboardText.nonNullNoEmpty()) {
                keyboard = MsgPack.decodeFromByteArrayEx(KeyboardEntity.serializer(), keyboardText)
            }

            val reactionText =
                cursor.getBlob(MessagesColumns.REACTIONS)
            if (reactionText.nonNullNoEmpty()) {
                reactions = MsgPack.decodeFromByteArrayEx(
                    ListSerializer(ReactionEntity.serializer()),
                    reactionText
                )
            }

            return MessageDboEntity().set(id, peerId, fromId)
                .setStatus(status)
                .setAction(action)
                .setExtras(extras)
                .setText(cursor.getString(MessagesColumns.TEXT)) //.setRead(cursor.getBoolean(MessageColumns.READ_STATE))
                .setOut(cursor.getBoolean(MessagesColumns.OUT))
                .setStatus(status)
                .setDate(cursor.getLong(MessagesColumns.DATE))
                .setDeleted(cursor.getBoolean(MessagesColumns.DELETED))
                .setDeletedForAll(cursor.getBoolean(MessagesColumns.DELETED_FOR_ALL)) //.setTitle(cursor.getString(MessageColumns.TITLE))
                .setImportant(cursor.getBoolean(MessagesColumns.IMPORTANT))
                .setAction(action)
                .setActionMemberId(cursor.getLong(MessagesColumns.ACTION_MID))
                .setActionEmail(cursor.getString(MessagesColumns.ACTION_EMAIL))
                .setActionText(cursor.getString(MessagesColumns.ACTION_TEXT))
                .setPhoto50(cursor.getString(MessagesColumns.PHOTO_50))
                .setPhoto100(cursor.getString(MessagesColumns.PHOTO_100))
                .setPhoto200(cursor.getString(MessagesColumns.PHOTO_200))
                .setRandomId(cursor.getLong(MessagesColumns.RANDOM_ID))
                .setUpdateTime(cursor.getLong(MessagesColumns.UPDATE_TIME))
                .setConversationMessageId(cursor.getInt(MessagesColumns.CONVERSATION_MESSAGE_ID))
                .setReactionId(cursor.getInt(MessagesColumns.REACTION_ID))
                .setPayload(cursor.getString(MessagesColumns.PAYLOAD))
                .setKeyboard(keyboard)
                .setReactions(reactions)
        }
    }
}
