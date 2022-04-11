package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.provider.BaseColumns
import android.text.TextUtils
import dev.ragnarok.fenrir.api.model.VKApiChat
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getDialogsContentUriFor
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getPeersContentUriFor
import dev.ragnarok.fenrir.db.PeerStateEntity
import dev.ragnarok.fenrir.db.column.DialogsColumns
import dev.ragnarok.fenrir.db.column.DialogsColumns.getCV
import dev.ragnarok.fenrir.db.column.PeersColumns
import dev.ragnarok.fenrir.db.interfaces.IDialogsStorage
import dev.ragnarok.fenrir.db.model.PeerPatch
import dev.ragnarok.fenrir.db.model.entity.DialogEntity
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity
import dev.ragnarok.fenrir.db.model.entity.MessageEntity
import dev.ragnarok.fenrir.db.model.entity.SimpleDialogEntity
import dev.ragnarok.fenrir.model.Chat
import dev.ragnarok.fenrir.model.ChatAction
import dev.ragnarok.fenrir.model.criteria.DialogsCriteria
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.Exestime.log
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Optional.Companion.wrap
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Utils.join
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.subjects.PublishSubject

internal class DialogsStorage(base: AppStorages) : AbsStorage(base), IDialogsStorage {
    private val unreadDialogsCounter: PublishSubject<Pair<Int, Int>> = PublishSubject.create()
    private val preferences: SharedPreferences =
        base.getSharedPreferences("dialogs_prefs", Context.MODE_PRIVATE)

    override fun getUnreadDialogsCount(accountId: Int): Int {
        synchronized(this) { return preferences.getInt(unreadKeyFor(accountId), 0) }
    }

    override fun observeUnreadDialogsCount(): Observable<Pair<Int, Int>> {
        return unreadDialogsCounter
    }

    override fun getDialogs(criteria: DialogsCriteria): Single<List<DialogEntity>> {
        return Single.create { e: SingleEmitter<List<DialogEntity>> ->
            val start = System.currentTimeMillis()
            val uri = getDialogsContentUriFor(criteria.accountId)
            val cursor = context.contentResolver.query(
                uri, null, null,
                null, DialogsColumns.MAJOR_ID + " DESC, " + DialogsColumns.MINOR_ID + " DESC"
            )
            val dbos: MutableList<DialogEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed) {
                        break
                    }
                    dbos.add(mapEntity(cursor))
                }
                cursor.close()
            }
            e.onSuccess(dbos)
            log("getDialogs", start)
        }
    }

    override fun removePeerWithId(accountId: Int, peerId: Int): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getDialogsContentUriFor(accountId)
            contentResolver.delete(uri, BaseColumns._ID + " = ?", arrayOf(peerId.toString()))
            emitter.onComplete()
        }
    }

    override fun insertDialogs(
        accountId: Int,
        dbos: List<DialogEntity>,
        clearBefore: Boolean
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val start = System.currentTimeMillis()
            val uri = getDialogsContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            if (clearBefore) {
                operations.add(ContentProviderOperation.newDelete(uri).build())
            }
            for (entity in dbos) {
                val simple = entity.simplify()
                operations.add(
                    ContentProviderOperation.newInsert(uri).withValues(createCv(entity)).build()
                )
                operations.add(
                    ContentProviderOperation
                        .newInsert(getPeersContentUriFor(accountId))
                        .withValues(createPeerCv(simple))
                        .build()
                )
                MessagesStorage.appendDboOperation(
                    accountId,
                    entity.message,
                    operations,
                    null,
                    null
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
            log(
                "DialogsStorage.insertDialogs",
                start,
                "count: " + dbos.size + ", clearBefore: " + clearBefore
            )
        }
    }

    private fun createCv(entity: DialogEntity): ContentValues {
        val cv = ContentValues()
        val messageDbo = entity.message
        cv.put(BaseColumns._ID, messageDbo.peerId)
        cv.put(DialogsColumns.UNREAD, entity.unreadCount)
        cv.put(DialogsColumns.TITLE, entity.title)
        cv.put(DialogsColumns.IN_READ, entity.inRead)
        cv.put(DialogsColumns.OUT_READ, entity.outRead)
        cv.put(DialogsColumns.PHOTO_50, entity.photo50)
        cv.put(DialogsColumns.PHOTO_100, entity.photo100)
        cv.put(DialogsColumns.PHOTO_200, entity.photo200)
        cv.put(DialogsColumns.LAST_MESSAGE_ID, messageDbo.id)
        cv.put(DialogsColumns.ACL, entity.acl)
        cv.put(DialogsColumns.IS_GROUP_CHANNEL, entity.isGroupChannel)
        cv.put(DialogsColumns.MAJOR_ID, entity.major_id)
        cv.put(DialogsColumns.MINOR_ID, entity.minor_id)
        return cv
    }

    private fun createPeerCv(entity: SimpleDialogEntity): ContentValues {
        val cv = ContentValues()
        cv.put(BaseColumns._ID, entity.peerId)
        cv.put(PeersColumns.UNREAD, entity.unreadCount)
        cv.put(PeersColumns.TITLE, entity.title)
        cv.put(PeersColumns.IN_READ, entity.inRead)
        cv.put(PeersColumns.OUT_READ, entity.outRead)
        cv.put(PeersColumns.PHOTO_50, entity.photo50)
        cv.put(PeersColumns.PHOTO_100, entity.photo100)
        cv.put(PeersColumns.PHOTO_200, entity.photo200)
        cv.put(PeersColumns.KEYBOARD, serializeJson(entity.currentKeyboard))
        cv.put(PeersColumns.PINNED, serializeJson(entity.pinned))
        cv.put(PeersColumns.ACL, entity.acl)
        cv.put(PeersColumns.IS_GROUP_CHANNEL, entity.isGroupChannel)
        cv.put(PeersColumns.MAJOR_ID, entity.major_id)
        cv.put(PeersColumns.MINOR_ID, entity.minor_id)
        return cv
    }

    override fun saveSimple(accountId: Int, entity: SimpleDialogEntity): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getPeersContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>()
            operations.add(
                ContentProviderOperation.newInsert(uri).withValues(createPeerCv(entity)).build()
            )
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun updateDialogKeyboard(
        accountId: Int,
        peerId: Int,
        keyboardEntity: KeyboardEntity?
    ): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getPeersContentUriFor(accountId)
            val args = arrayOf(peerId.toString())
            val operations = ArrayList<ContentProviderOperation>(1)
            val cv = ContentValues()
            cv.put(PeersColumns.KEYBOARD, serializeJson(keyboardEntity))
            operations.add(
                ContentProviderOperation.newUpdate(uri)
                    .withSelection(BaseColumns._ID + " = ?", args)
                    .withValues(cv)
                    .build()
            )
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun findPeerStates(
        accountId: Int,
        ids: Collection<Int>
    ): Single<List<PeerStateEntity>> {
        return if (ids.isEmpty()) {
            Single.just(emptyList())
        } else Single.create { emitter: SingleEmitter<List<PeerStateEntity>> ->
            val projection = arrayOf(
                BaseColumns._ID,
                PeersColumns.UNREAD,
                PeersColumns.IN_READ,
                PeersColumns.OUT_READ,
                PeersColumns.LAST_MESSAGE_ID
            )
            val uri = getPeersContentUriFor(accountId)
            val where = BaseColumns._ID + " IN (" + join(",", ids) + ")"
            val cursor = contentResolver.query(uri, projection, where, null, null)
            val entities: MutableList<PeerStateEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val entity =
                        PeerStateEntity(cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                            .setInRead(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.IN_READ)))
                            .setOutRead(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.OUT_READ)))
                            .setLastMessageId(
                                cursor.getInt(
                                    cursor.getColumnIndexOrThrow(
                                        PeersColumns.LAST_MESSAGE_ID
                                    )
                                )
                            )
                            .setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.UNREAD)))
                    entities.add(entity)
                }
                cursor.close()
            }
            emitter.onSuccess(entities)
        }
    }

    override fun findSimple(accountId: Int, peerId: Int): Single<Optional<SimpleDialogEntity>> {
        return Single.create { emitter: SingleEmitter<Optional<SimpleDialogEntity>> ->
            val projection = arrayOf(
                PeersColumns.UNREAD,
                PeersColumns.TITLE,
                PeersColumns.IN_READ,
                PeersColumns.OUT_READ,
                PeersColumns.PHOTO_50,
                PeersColumns.PHOTO_100,
                PeersColumns.PHOTO_200,
                PeersColumns.KEYBOARD,
                PeersColumns.PINNED,
                PeersColumns.LAST_MESSAGE_ID,
                PeersColumns.ACL,
                PeersColumns.IS_GROUP_CHANNEL,
                PeersColumns.MAJOR_ID,
                PeersColumns.MINOR_ID
            )
            val uri = getPeersContentUriFor(accountId)
            val cursor = contentResolver.query(
                uri, projection,
                PeersColumns.FULL_ID + " = ?", arrayOf(peerId.toString()), null
            )
            var entity: SimpleDialogEntity? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    entity = SimpleDialogEntity(peerId)
                        .setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.UNREAD)))
                        .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(PeersColumns.TITLE)))
                        .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(PeersColumns.PHOTO_200)))
                        .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(PeersColumns.PHOTO_100)))
                        .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(PeersColumns.PHOTO_50)))
                        .setInRead(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.IN_READ)))
                        .setOutRead(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.OUT_READ)))
                        .setPinned(
                            deserializeJson(
                                cursor,
                                PeersColumns.PINNED,
                                MessageEntity::class.java
                            )
                        )
                        .setCurrentKeyboard(
                            deserializeJson(
                                cursor,
                                PeersColumns.KEYBOARD,
                                KeyboardEntity::class.java
                            )
                        )
                        .setLastMessageId(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.LAST_MESSAGE_ID)))
                        .setAcl(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.ACL)))
                        .setMajor_id(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.MAJOR_ID)))
                        .setMinor_id(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.MINOR_ID)))
                        .setGroupChannel(cursor.getInt(cursor.getColumnIndexOrThrow(PeersColumns.IS_GROUP_CHANNEL)) == 1)
                }
                cursor.close()
            }
            emitter.onSuccess(wrap(entity))
        }
    }

    override fun setUnreadDialogsCount(accountId: Int, unreadCount: Int) {
        synchronized(this) {
            preferences.edit()
                .putInt(unreadKeyFor(accountId), unreadCount)
                .apply()
        }
        unreadDialogsCounter.onNext(Pair(accountId, unreadCount))
    }

    override fun getMissingGroupChats(
        accountId: Int,
        ids: Collection<Int>
    ): Single<Collection<Int>> {
        return Single.create { e: SingleEmitter<Collection<Int>> ->
            if (ids.isEmpty()) {
                e.onSuccess(emptyList())
                return@create
            }
            val peerIds: MutableSet<Int> = HashSet(ids)
            val projection = arrayOf(BaseColumns._ID)
            val uri = getDialogsContentUriFor(accountId)
            val cursor = contentResolver.query(
                uri, projection,
                DialogsColumns.FULL_ID + " IN (" + TextUtils.join(",", peerIds) + ")", null, null
            )
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val peerId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    peerIds.remove(peerId)
                }
                cursor.close()
            }
            e.onSuccess(peerIds)
        }
    }

    override fun insertChats(accountId: Int, chats: List<VKApiChat>): Completable {
        return Completable.fromAction {
            val operations = ArrayList<ContentProviderOperation>(chats.size)
            for (chat in chats) {
                operations.add(
                    ContentProviderOperation
                        .newInsert(getDialogsContentUriFor(accountId))
                        .withValues(getCV(chat))
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
        }
    }

    override fun applyPatches(accountId: Int, patches: List<PeerPatch>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val dialogsUri = getDialogsContentUriFor(accountId)
            val peersUri = getPeersContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation?>(patches.size * 2)
            for (patch in patches) {
                val dialogscv = ContentValues()
                val peerscv = ContentValues()
                if (patch.inRead != null) {
                    dialogscv.put(DialogsColumns.IN_READ, patch.inRead.id)
                    peerscv.put(PeersColumns.IN_READ, patch.inRead.id)
                }
                if (patch.unread != null) {
                    dialogscv.put(DialogsColumns.UNREAD, patch.unread.count)
                    peerscv.put(PeersColumns.UNREAD, patch.unread.count)
                }
                if (patch.outRead != null) {
                    dialogscv.put(DialogsColumns.OUT_READ, patch.outRead.id)
                    peerscv.put(PeersColumns.OUT_READ, patch.outRead.id)
                }
                if (patch.lastMessage != null) {
                    dialogscv.put(DialogsColumns.LAST_MESSAGE_ID, patch.lastMessage.id)
                    peerscv.put(PeersColumns.LAST_MESSAGE_ID, patch.lastMessage.id)
                    dialogscv.put(DialogsColumns.MINOR_ID, patch.lastMessage.id)
                    peerscv.put(PeersColumns.MINOR_ID, patch.lastMessage.id)
                }
                if (patch.pin != null) {
                    peerscv.put(PeersColumns.PINNED, serializeJson(patch.pin.pinned))
                }
                if (patch.title != null) {
                    peerscv.put(PeersColumns.TITLE, patch.title.title)
                    dialogscv.put(DialogsColumns.TITLE, patch.title.title)
                }
                val args = arrayOf(patch.id.toString())
                if (dialogscv.size() > 0) {
                    operations.add(
                        ContentProviderOperation.newUpdate(dialogsUri)
                            .withSelection(BaseColumns._ID + " = ?", args)
                            .withValues(dialogscv)
                            .build()
                    )
                }
                if (peerscv.size() > 0) {
                    operations.add(
                        ContentProviderOperation.newUpdate(peersUri)
                            .withSelection(BaseColumns._ID + " = ?", args)
                            .withValues(peerscv)
                            .build()
                    )
                }
            }
            if (operations.nonNullNoEmpty()) {
                contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            }
            emitter.onComplete()
        }
    }

    override fun findChatById(accountId: Int, peerId: Int): Single<Optional<Chat>> {
        return Single.fromCallable {
            val projection = arrayOf(
                DialogsColumns.TITLE,
                DialogsColumns.PHOTO_200,
                DialogsColumns.PHOTO_100,
                DialogsColumns.PHOTO_50
            )
            val uri = getDialogsContentUriFor(accountId)
            val cursor = contentResolver.query(
                uri, projection,
                DialogsColumns.FULL_ID + " = ?", arrayOf(peerId.toString()), null
            )
            var chat: Chat? = null
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    chat = Chat(peerId)
                    chat.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.TITLE)))
                        .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.PHOTO_200)))
                        .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.PHOTO_100))).photo50 =
                        cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.PHOTO_50))
                }
                cursor.close()
            }
            wrap(chat)
        }
    }

    private fun mapEntity(cursor: Cursor): DialogEntity {
        @ChatAction val action =
            cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_ACTION))
        val encrypted =
            cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_ENCRYPTED)) == 1
        val messageId = cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.LAST_MESSAGE_ID))
        val peerId = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
        val fromId =
            cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_FROM_ID))
        val message = MessageEntity().set(messageId, peerId, fromId)
            .setBody(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_BODY)))
            .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_DATE)))
            .setOut(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_OUT)) == 1)
            .setHasAttachmens(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_HAS_ATTACHMENTS)) == 1)
            .setForwardCount(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.FOREIGN_MESSAGE_FWD_COUNT)))
            .setAction(action)
            .setEncrypted(encrypted)
        return DialogEntity(peerId)
            .setMessage(message)
            .setInRead(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.IN_READ)))
            .setOutRead(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.OUT_READ)))
            .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.TITLE)))
            .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.PHOTO_50)))
            .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.PHOTO_100)))
            .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(DialogsColumns.PHOTO_200)))
            .setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.UNREAD)))
            .setLastMessageId(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.LAST_MESSAGE_ID)))
            .setAcl(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.ACL)))
            .setMajor_id(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.MAJOR_ID)))
            .setMinor_id(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.MINOR_ID)))
            .setGroupChannel(cursor.getInt(cursor.getColumnIndexOrThrow(DialogsColumns.IS_GROUP_CHANNEL)) == 1)
    }

    companion object {
        private fun unreadKeyFor(accountId: Int): String {
            return "unread$accountId"
        }
    }

}