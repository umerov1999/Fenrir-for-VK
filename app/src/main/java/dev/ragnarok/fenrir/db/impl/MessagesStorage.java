package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.join;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.RecordNotFoundException;
import dev.ragnarok.fenrir.db.column.MessageColumns;
import dev.ragnarok.fenrir.db.interfaces.Cancelable;
import dev.ragnarok.fenrir.db.interfaces.IMessagesStorage;
import dev.ragnarok.fenrir.db.model.MessageEditEntity;
import dev.ragnarok.fenrir.db.model.MessagePatch;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity;
import dev.ragnarok.fenrir.db.model.entity.MessageEntity;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.ChatAction;
import dev.ragnarok.fenrir.model.DraftMessage;
import dev.ragnarok.fenrir.model.MessageStatus;
import dev.ragnarok.fenrir.model.criteria.MessagesCriteria;
import dev.ragnarok.fenrir.util.Exestime;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

class MessagesStorage extends AbsStorage implements IMessagesStorage {

    private static final String ORDER_BY = MessageColumns.FULL_STATUS + ", " + MessageColumns.FULL_ID;
    private static final Type EXTRAS_TYPE = new TypeToken<Map<Integer, String>>() {
    }.getType();

    MessagesStorage(@NonNull AppStorages base) {
        super(base);
    }

    static int appendDboOperation(int accountId, @NonNull MessageEntity dbo, @NonNull List<ContentProviderOperation> target, Integer attachToId, Integer attachToIndex) {
        ContentValues cv = new ContentValues();

        if (nonNull(attachToId)) {
            // если есть ID сообщения, к которому прикреплено dbo
            cv.put(MessageColumns.ATTACH_TO, attachToId);
        } else if (isNull(attachToIndex)) {
            // если сообщение не прикреплено к другому
            cv.put(MessageColumns._ID, dbo.getId());
            cv.put(MessageColumns.ATTACH_TO, MessageColumns.DONT_ATTACH);
        }

        cv.put(MessageColumns.PEER_ID, dbo.getPeerId());
        cv.put(MessageColumns.FROM_ID, dbo.getFromId());
        cv.put(MessageColumns.DATE, dbo.getDate());
        //cv.put(MessageColumns.READ_STATE, dbo.isRead());
        cv.put(MessageColumns.OUT, dbo.isOut());
        //cv.put(MessageColumns.TITLE, dbo.getTitle());
        cv.put(MessageColumns.BODY, dbo.getBody());
        cv.put(MessageColumns.ENCRYPTED, dbo.isEncrypted());
        cv.put(MessageColumns.IMPORTANT, dbo.isImportant());
        cv.put(MessageColumns.DELETED, dbo.isDeleted());
        cv.put(MessageColumns.FORWARD_COUNT, dbo.getForwardCount());
        cv.put(MessageColumns.HAS_ATTACHMENTS, dbo.isHasAttachmens());
        cv.put(MessageColumns.STATUS, dbo.getStatus());
        cv.put(MessageColumns.ORIGINAL_ID, dbo.getOriginalId());
        cv.put(MessageColumns.ACTION, dbo.getAction());
        cv.put(MessageColumns.ACTION_MID, dbo.getActionMemberId());
        cv.put(MessageColumns.ACTION_EMAIL, dbo.getActionEmail());
        cv.put(MessageColumns.ACTION_TEXT, dbo.getActionText());
        cv.put(MessageColumns.PHOTO_50, dbo.getPhoto50());
        cv.put(MessageColumns.PHOTO_100, dbo.getPhoto100());
        cv.put(MessageColumns.PHOTO_200, dbo.getPhoto200());
        cv.put(MessageColumns.RANDOM_ID, dbo.getRandomId());
        cv.put(MessageColumns.EXTRAS, isNull(dbo.getExtras()) ? null : GSON.toJson(dbo.getExtras()));
        cv.put(MessageColumns.UPDATE_TIME, dbo.getUpdateTime());
        cv.put(MessageColumns.PAYLOAD, dbo.getPayload());
        cv.put(MessageColumns.KEYBOARD, isNull(dbo.getKeyboard()) ? null : GSON.toJson(dbo.getKeyboard()));

        Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri)
                .withValues(cv);

        // если сообщение прикреплено к другому, но его ID на данный момент неизвестен
        if (isNull(attachToId) && nonNull(attachToIndex)) {
            builder.withValueBackReference(MessageColumns.ATTACH_TO, attachToIndex);
        }

        int index = addToListAndReturnIndex(target, builder.build());

        if (dbo.isHasAttachmens() && nonNull(dbo.getAttachments())) {
            List<Entity> entities = dbo.getAttachments();

            for (Entity attachmentEntity : entities) {
                AttachmentsStorage.appendAttachOperationWithBackReference(target, accountId, AttachToType.MESSAGE, index, attachmentEntity);
            }
        }

        if (dbo.getForwardCount() > 0 && !Utils.isEmpty(dbo.getForwardMessages())) {
            for (MessageEntity fwdDbo : dbo.getForwardMessages()) {
                appendDboOperation(accountId, fwdDbo, target, null, index);
            }
        }

        return index;
    }

    private static MessageEntity baseMapDbo(Cursor cursor) {
        @MessageStatus
        int status = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.STATUS));

        @ChatAction
        int action = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.ACTION));

        int id = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns._ID));
        int peerId = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.PEER_ID));
        int fromId = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.FROM_ID));

        HashMap<Integer, String> extras = null;
        KeyboardEntity keyboard = null;

        String extrasText = cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.EXTRAS));
        if (nonEmpty(extrasText)) {
            extras = GSON.fromJson(extrasText, EXTRAS_TYPE);
        }
        String keyboardText = cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.KEYBOARD));
        if (nonEmpty(keyboardText)) {
            keyboard = GSON.fromJson(keyboardText, KeyboardEntity.class);
        }

        return new MessageEntity().set(id, peerId, fromId)
                .setEncrypted(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.ENCRYPTED)) == 1)
                .setStatus(status)
                .setAction(action)
                .setExtras(extras)
                .setBody(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.BODY)))
                //.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.READ_STATE)) == 1)
                .setOut(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.OUT)) == 1)
                .setStatus(status)
                .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(MessageColumns.DATE)))
                .setHasAttachmens(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.HAS_ATTACHMENTS)) == 1)
                .setForwardCount(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.FORWARD_COUNT)))
                .setDeleted(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.DELETED)) == 1)
                .setDeletedForAll(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.DELETED_FOR_ALL)) == 1)
                //.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.TITLE)))
                .setOriginalId(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.ORIGINAL_ID)))
                .setImportant(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.IMPORTANT)) == 1)
                .setAction(action)
                .setActionMemberId(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.ACTION_MID)))
                .setActionEmail(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.ACTION_EMAIL)))
                .setActionText(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.ACTION_TEXT)))
                .setPhoto50(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.PHOTO_50)))
                .setPhoto100(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.PHOTO_100)))
                .setPhoto200(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.PHOTO_200)))
                .setRandomId(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.RANDOM_ID)))
                .setUpdateTime(cursor.getLong(cursor.getColumnIndexOrThrow(MessageColumns.UPDATE_TIME)))
                .setPayload(cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.PAYLOAD)))
                .setKeyboard(keyboard);

    }

    @Override
    public Completable insertPeerDbos(int accountId, int peerId, @NonNull List<MessageEntity> dbos, boolean clearHistory) {
        return Completable.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (clearHistory) {
                Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
                String where = MessageColumns.PEER_ID + " = ? AND " + MessageColumns.ATTACH_TO + " = ? AND " + MessageColumns.STATUS + " = ?";
                String[] args = {String.valueOf(peerId), String.valueOf(MessageColumns.DONT_ATTACH), String.valueOf(MessageStatus.SENT)};

                operations.add(ContentProviderOperation.newDelete(uri).withSelection(where, args).build());
            }

            for (MessageEntity dbo : dbos) {
                appendDboOperation(accountId, dbo, operations, null, null);
            }

            getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<int[]> insert(int accountId, @NonNull List<MessageEntity> dbos) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            int[] indexes = new int[dbos.size()];

            for (int i = 0; i < dbos.size(); i++) {
                MessageEntity dbo = dbos.get(i);
                int index = appendDboOperation(accountId, dbo, operations, null, null);

                indexes[i] = index;
            }

            ContentProviderResult[] results = getContext().getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int[] ids = new int[dbos.size()];

            for (int i = 0; i < indexes.length; i++) {
                int index = indexes[i];

                ContentProviderResult result = results[index];
                ids[i] = extractId(result);
            }

            emitter.onSuccess(ids);
        });
    }

    @Override
    public Single<Optional<Integer>> findLastSentMessageIdForPeer(int accountId, int peerId) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            String[] projection = {MessageColumns._ID};

            String where = MessageColumns.PEER_ID + " = ?" +
                    " AND " + MessageColumns.STATUS + " = ?" +
                    " AND " + MessageColumns.ATTACH_TO + " = ?" +
                    " AND " + MessageColumns.DELETED + " = ?";

            String[] args = {String.valueOf(peerId),
                    String.valueOf(MessageStatus.SENT),
                    String.valueOf(MessageColumns.DONT_ATTACH),
                    "0"
            };

            Cursor cursor = getContentResolver().query(uri, projection, where, args, MessageColumns.FULL_ID + " DESC LIMIT 1");

            Integer id = null;
            if (nonNull(cursor)) {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns._ID));
                }

                cursor.close();
            }

            emitter.onSuccess(Optional.wrap(id));
        });
    }

    private Cursor queryMessagesByCriteria(MessagesCriteria criteria) {
        String where;
        String[] args;

        if (criteria.getStartMessageId() == null) {
            where = MessageColumns.PEER_ID + " = ?" +
                    " AND " + MessageColumns.ATTACH_TO + " = ?" +
                    " AND " + MessageColumns.STATUS + " != ?";

            args = new String[]{
                    String.valueOf(criteria.getPeerId()),
                    "0",
                    String.valueOf(MessageStatus.EDITING)
            };
        } else {
            where = MessageColumns.PEER_ID + " = ?" +
                    " AND " + MessageColumns.ATTACH_TO + " = ? " +
                    " AND " + MessageColumns.FULL_ID + " < ? " +
                    " AND " + MessageColumns.STATUS + " != ?";

            args = new String[]{
                    String.valueOf(criteria.getPeerId()),
                    "0",
                    String.valueOf(criteria.getStartMessageId()),
                    String.valueOf(MessageStatus.EDITING)
            };
        }

        Uri uri = MessengerContentProvider.getMessageContentUriFor(criteria.getAccountId());
        return getContext().getContentResolver().query(uri, null, where, args, ORDER_BY);
    }

    @Override
    public Single<List<MessageEntity>> getByCriteria(@NonNull MessagesCriteria criteria, boolean withAtatchments, boolean withForwardMessages) {
        return Single.create(emitter -> {
            long start = System.currentTimeMillis();

            Cancelable cancelable = emitter::isDisposed;

            Cursor cursor = queryMessagesByCriteria(criteria);

            ArrayList<MessageEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    MessageEntity dbo = fullMapDbo(criteria.getAccountId(), cursor, withAtatchments, withForwardMessages, cancelable);

                    int position = dbos.size() - cursor.getPosition();
                    dbos.add(position, dbo);
                }

                cursor.close();
            }

            Exestime.log("MessagesStorage.getByCriteria", start, "count: " + dbos.size());
            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Single<Integer> insert(int accountId, int peerId, @NonNull MessageEditEntity patch) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.PEER_ID, peerId);
            cv.put(MessageColumns.FROM_ID, patch.getSenderId());
            cv.put(MessageColumns.DATE, patch.getDate());
            //cv.put(MessageColumns.READ_STATE, patch.isRead());
            cv.put(MessageColumns.OUT, patch.isOut());
            //cv.put(MessageColumns.TITLE, patch.getTitle());
            cv.put(MessageColumns.BODY, patch.getBody());
            cv.put(MessageColumns.ENCRYPTED, patch.isEncrypted());
            cv.put(MessageColumns.IMPORTANT, patch.isImportant());
            cv.put(MessageColumns.DELETED, patch.isDeleted());
            cv.put(MessageColumns.FORWARD_COUNT, safeCountOf(patch.getForward()));
            cv.put(MessageColumns.HAS_ATTACHMENTS, nonEmpty(patch.getAttachments()));
            cv.put(MessageColumns.STATUS, patch.getStatus());
            cv.put(MessageColumns.ATTACH_TO, MessageColumns.DONT_ATTACH);
            cv.put(MessageColumns.EXTRAS, isNull(patch.getExtras()) ? null : GSON.toJson(patch.getExtras()));
            cv.put(MessageColumns.PAYLOAD, patch.getPayload());
            cv.put(MessageColumns.KEYBOARD, isNull(patch.getKeyboard()) ? null : GSON.toJson(patch.getKeyboard()));

            // Other fileds is NULL

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri).withValues(cv);

            int index = addToListAndReturnIndex(operations, builder.build());

            if (nonEmpty(patch.getAttachments())) {
                List<Entity> entities = patch.getAttachments();

                for (Entity attachmentEntity : entities) {
                    AttachmentsStorage.appendAttachOperationWithBackReference(operations, accountId, AttachToType.MESSAGE, index, attachmentEntity);
                }
            }

            if (nonEmpty(patch.getForward())) {
                for (MessageEntity fwdDbo : patch.getForward()) {
                    appendDboOperation(accountId, fwdDbo, operations, null, index);
                }
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            int resultMessageId = extractId(results[index]);
            emitter.onSuccess(resultMessageId);
        });
    }

    @Override
    public Single<Integer> applyPatch(int accountId, int messageId, @NonNull MessageEditEntity patch) {
        return getStores().attachments()
                .getCount(accountId, AttachToType.MESSAGE, messageId)
                .flatMap(count -> Single
                        .create(emitter -> {
                            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
                            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

                            ContentValues cv = new ContentValues();
                            cv.put(MessageColumns.FROM_ID, patch.getSenderId());
                            cv.put(MessageColumns.DATE, patch.getDate());
                            //cv.put(MessageColumns.READ_STATE, patch.isRead());
                            cv.put(MessageColumns.OUT, patch.isOut());
                            //cv.put(MessageColumns.TITLE, patch.getTitle());
                            cv.put(MessageColumns.BODY, patch.getBody());
                            cv.put(MessageColumns.ENCRYPTED, patch.isEncrypted());
                            cv.put(MessageColumns.IMPORTANT, patch.isImportant());
                            cv.put(MessageColumns.DELETED, patch.isDeleted());
                            cv.put(MessageColumns.FORWARD_COUNT, safeCountOf(patch.getForward()));
                            cv.put(MessageColumns.HAS_ATTACHMENTS, count + safeCountOf(patch.getAttachments()) > 0);
                            cv.put(MessageColumns.STATUS, patch.getStatus());
                            cv.put(MessageColumns.ATTACH_TO, MessageColumns.DONT_ATTACH);
                            cv.put(MessageColumns.EXTRAS, isNull(patch.getExtras()) ? null : GSON.toJson(patch.getExtras()));
                            cv.put(MessageColumns.PAYLOAD, patch.getPayload());
                            cv.put(MessageColumns.KEYBOARD, isNull(patch.getKeyboard()) ? null : GSON.toJson(patch.getKeyboard()));

                            String where = MessageColumns._ID + " = ?";
                            String[] args = {String.valueOf(messageId)};

                            operations.add(ContentProviderOperation.newUpdate(uri).withValues(cv).withSelection(where, args).build());

                            if (nonEmpty(patch.getAttachments())) {
                                for (Entity entity : patch.getAttachments()) {
                                    AttachmentsStorage.appendAttachOperationWithStableAttachToId(operations, accountId, AttachToType.MESSAGE, messageId, entity);
                                }
                            }

                            if (nonEmpty(patch.getForward())) {
                                for (MessageEntity dbo : patch.getForward()) {
                                    appendDboOperation(accountId, dbo, operations, messageId, null);
                                }
                            }

                            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
                            emitter.onSuccess(messageId);
                        }));
    }

    private MessageEntity fullMapDbo(int accountId, Cursor cursor, boolean withAttachments, boolean withForwardMessages, @NonNull Cancelable cancelable) {
        MessageEntity dbo = baseMapDbo(cursor);

        if (withAttachments && dbo.isHasAttachmens()) {
            List<Entity> attachments = getStores()
                    .attachments()
                    .getAttachmentsDbosSync(accountId, AttachToType.MESSAGE, dbo.getId(), cancelable);

            dbo.setAttachments(attachments);
        } else {
            dbo.setAttachments(null);
        }

        if (withForwardMessages && dbo.getForwardCount() > 0) {
            List<MessageEntity> fwds = getForwardMessages(accountId, dbo.getId(), withAttachments, cancelable);
            dbo.setForwardMessages(fwds);
        } else {
            dbo.setForwardMessages(null);
        }

        return dbo;
    }

    @Override
    public Maybe<DraftMessage> findDraftMessage(int accountId, int peerId) {
        return Maybe.create(e -> {
            String[] columns = {MessageColumns._ID, MessageColumns.BODY};
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            Cursor cursor = getContext().getContentResolver().query(uri, columns,
                    MessageColumns.PEER_ID + " = ? AND " + MessageColumns.STATUS + " = ?",
                    new String[]{String.valueOf(peerId), String.valueOf(MessageStatus.EDITING)}, null);

            if (e.isDisposed()) return;

            DraftMessage message = null;
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns._ID));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(MessageColumns.BODY));
                    message = new DraftMessage(id, body);
                }

                cursor.close();
            }

            if (nonNull(message)) {
                Integer count = getStores().attachments()
                        .getCount(accountId, AttachToType.MESSAGE, message.getId())
                        .blockingGet();

                message.setAttachmentsCount(nonNull(count) ? count : 0);
                e.onSuccess(message);
            }

            e.onComplete();
        });
    }

    @Override
    public Single<Integer> saveDraftMessageBody(int accountId, int peerId, String body) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.BODY, body);
            cv.put(MessageColumns.PEER_ID, peerId);
            cv.put(MessageColumns.STATUS, MessageStatus.EDITING);

            ContentResolver cr = getContentResolver();

            Integer existDraftMessageId = findDraftMessageId(accountId, peerId);
            //.blockingGet();

            if (existDraftMessageId != null) {
                cr.update(uri, cv, MessageColumns._ID + " = ?", new String[]{String.valueOf(existDraftMessageId)});
            } else {
                Uri resultUri = cr.insert(uri, cv);
                existDraftMessageId = Integer.parseInt(resultUri.getLastPathSegment());
            }

            e.onSuccess(existDraftMessageId);

            Exestime.log("saveDraftMessageBody", start);
        });
    }

    @Override
    public Completable applyPatches(int accountId, @NonNull Collection<MessagePatch> patches) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>(patches.size());
            for (MessagePatch patch : patches) {
                ContentValues cv = new ContentValues();

                if (patch.getDeletion() != null) {
                    cv.put(MessageColumns.DELETED, patch.getDeletion().getDeleted());
                    cv.put(MessageColumns.DELETED_FOR_ALL, patch.getDeletion().getDeletedForAll());
                }

                if (patch.getImportant() != null) {
                    cv.put(MessageColumns.IMPORTANT, patch.getImportant().getImportant());
                }

                if (cv.size() == 0) continue;

                operations.add(ContentProviderOperation.newUpdate(uri)
                        .withValues(cv)
                        .withSelection(MessageColumns._ID + " = ?", new String[]{String.valueOf(patch.getMessageId())})
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<Integer> getMessageStatus(int accountId, int dbid) {
        return Single.fromCallable(() -> {
            Cursor cursor = getContentResolver().query(MessengerContentProvider.getMessageContentUriFor(accountId),
                    new String[]{MessageColumns.STATUS}, MessageColumns.FULL_ID + " = ?", new String[]{String.valueOf(dbid)}, null);

            Integer result = null;

            if (cursor != null) {
                if (cursor.moveToNext()) {
                    result = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.STATUS));
                }

                cursor.close();
            }

            if (isNull(result)) {
                throw new RecordNotFoundException("Message with id " + dbid + " not found");
            }

            return result;
        });
    }

    private Integer findDraftMessageId(int accountId, int peerId) {
        String[] columns = {MessageColumns._ID};
        Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

        Cursor cursor = getContext().getContentResolver().query(uri, columns,
                MessageColumns.PEER_ID + " = ? AND " + MessageColumns.STATUS + " = ?",
                new String[]{String.valueOf(peerId), String.valueOf(MessageStatus.EDITING)}, null);

        Integer id = null;
        if (cursor != null) {
            if (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns._ID));
            }

            cursor.close();
        }

        return id;
    }

    @Override
    public Completable changeMessageStatus(int accountId, int messageId, @MessageStatus int status, @Nullable Integer vkid) {
        return Completable.create(e -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MessageColumns.STATUS, status);
            if (vkid != null) {
                contentValues.put(MessageColumns._ID, vkid);
            }

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            int count = getContext().getContentResolver().update(uri, contentValues,
                    MessageColumns._ID + " = ?", new String[]{String.valueOf(messageId)});

            if (count > 0) {
                e.onComplete();
            } else {
                e.onError(new NotFoundException());
            }
        });
    }

    @Override
    public Single<Boolean> deleteMessage(int accountId, int messageId) {
        if (messageId == 0) {
            throw new IllegalArgumentException("Invalid message id: " + messageId);
        }

        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            int count = getContext().getContentResolver().delete(uri, MessageColumns._ID + " = ?",
                    new String[]{String.valueOf(messageId)});

            e.onSuccess(count > 0);
        });
    }

    @Override
    public Single<Boolean> deleteMessages(int accountId, Collection<Integer> ids) {
        return Single.create(e -> {
            Set<Integer> copy = new HashSet<>(ids);
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            String where = MessageColumns.FULL_ID + " IN(" + TextUtils.join(",", copy) + ")";
            int count = getContext().getContentResolver().delete(uri, where, null);

            e.onSuccess(count > 0);
        });
    }

    @Override
    public Completable changeMessagesStatus(int accountId, Collection<Integer> ids, @MessageStatus int status) {
        return Completable.create(e -> {
            Set<Integer> copy = new HashSet<>(ids);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MessageColumns.STATUS, status);

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            String where = MessageColumns.FULL_ID + " IN(" + TextUtils.join(",", copy) + ")";
            int count = getContext().getContentResolver().update(uri, contentValues,
                    where, null);

            if (count > 0) {
                e.onComplete();
            } else {
                e.onError(new NotFoundException());
            }
        });
    }

    @Override
    public Single<List<Integer>> getMissingMessages(int accountId, Collection<Integer> ids) {
        return Single.create(e -> {
            Set<Integer> copy = new HashSet<>(ids);

            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            String[] projection = {MessageColumns._ID};
            String where = MessageColumns.FULL_ID + " IN(" + TextUtils.join(",", copy) + ")";
            Cursor cursor = getContentResolver().query(uri, projection, where, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns._ID));
                    copy.remove(id);
                }

                cursor.close();
            }

            e.onSuccess(new ArrayList<>(copy));
        });
    }

    private List<MessageEntity> getForwardMessages(int accountId, int attachTo, boolean withAttachments, @NonNull Cancelable cancelable) {
        Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
        String where = MessageColumns.ATTACH_TO + " = ?";
        String[] args = {String.valueOf(attachTo)};

        Cursor cursor = getContentResolver().query(uri, null, where, args, MessageColumns.FULL_ID + " DESC");

        List<MessageEntity> dbos = new ArrayList<>(safeCountOf(cursor));

        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                if (cancelable.isOperationCancelled()) {
                    break;
                }

                MessageEntity dbo = fullMapDbo(accountId, cursor, withAttachments, true, cancelable);

                // Хз куда это еще влепить
                //dbo.setRead(true);
                dbo.setOut(dbo.getFromId() == accountId);
                dbos.add(dbos.size() - cursor.getPosition(), dbo);
            }

            cursor.close();
        }

        return dbos;
    }

    @Override
    public Single<List<MessageEntity>> findMessagesByIds(int accountId, List<Integer> ids, boolean withAtatchments, boolean withForwardMessages) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

            String where;
            String[] args;

            if (ids.size() == 1) {
                where = MessageColumns._ID + " = ?";
                args = new String[]{String.valueOf(ids.get(0))};
            } else {
                where = MessageColumns.FULL_ID + " IN (" + join(",", ids) + ")";
                args = null;
            }

            Cursor cursor = getContext().getContentResolver().query(uri, null, where, args, null);

            Cancelable cancelable = emitter::isDisposed;

            ArrayList<MessageEntity> dbos = new ArrayList<>(safeCountOf(cursor));
            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    MessageEntity dbo = fullMapDbo(accountId, cursor, withAtatchments, withForwardMessages, cancelable);

                    int position = dbos.size() - cursor.getPosition();
                    dbos.add(position, dbo);
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    @Override
    public Single<Optional<Pair<Integer, MessageEntity>>> findFirstUnsentMessage(Collection<Integer> accountIds, boolean withAtatchments, boolean withForwardMessages) {
        return Single.create(emitter -> {
            String where = MessageColumns.STATUS + " = ?";
            String[] args = {String.valueOf(MessageStatus.QUEUE)};
            String orderBy = MessageColumns._ID + " ASC LIMIT 1";

            for (int accountId : accountIds) {
                if (emitter.isDisposed()) {
                    break;
                }

                Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);

                Cursor cursor = getContentResolver().query(uri, null, where, args, orderBy);

                MessageEntity entity = null;

                if (nonNull(cursor)) {
                    if (cursor.moveToNext()) {
                        entity = fullMapDbo(accountId, cursor, withAtatchments, withForwardMessages, emitter::isDisposed);
                    }

                    cursor.close();
                }

                if (nonNull(entity)) {
                    emitter.onSuccess(Optional.wrap(Pair.Companion.create(accountId, entity)));
                    return;
                }
            }

            emitter.onSuccess(Optional.empty());
        });
    }

    @Override
    public Completable notifyMessageHasAttachments(int accountId, int messageId) {
        return Completable.fromAction(() -> {
            ContentValues cv = new ContentValues();
            cv.put(MessageColumns.HAS_ATTACHMENTS, true);
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            String where = MessageColumns._ID + " = ?";
            String[] args = {String.valueOf(messageId)};
            getContentResolver().update(uri, cv, where, args);
        });
    }

    @Override
    public Single<Pair<Boolean, List<Integer>>> getForwardMessageIds(int accountId, int attachTo, int pair) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getMessageContentUriFor(accountId);
            Cursor cursor = getContext().getContentResolver().query(uri,
                    new String[]{MessageColumns.ORIGINAL_ID, MessageColumns.PEER_ID}, MessageColumns.ATTACH_TO + " = ?",
                    new String[]{String.valueOf(attachTo)}, MessageColumns.FULL_ID + " DESC");

            ArrayList<Integer> ids = new ArrayList<>(safeCountOf(cursor));
            Integer from_peer = null;
            boolean isFirst = true;
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }
                    if (isFirst) {
                        isFirst = false;
                        from_peer = cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.PEER_ID));
                    }
                    ids.add(cursor.getInt(cursor.getColumnIndexOrThrow(MessageColumns.ORIGINAL_ID)));
                }

                cursor.close();
            }

            e.onSuccess(new Pair<>(ids.size() == 1 && pair == from_peer, ids));
        });
    }
}
