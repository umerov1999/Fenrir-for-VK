package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.attachments.CommentsAttachmentsColumns;
import dev.ragnarok.fenrir.db.column.attachments.MessagesAttachmentsColumns;
import dev.ragnarok.fenrir.db.column.attachments.WallAttachmentsColumns;
import dev.ragnarok.fenrir.db.interfaces.Cancelable;
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage;
import dev.ragnarok.fenrir.db.model.AttachmentsTypes;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


class AttachmentsStorage extends AbsStorage implements IAttachmentsStorage {

    AttachmentsStorage(@NonNull AppStorages base) {
        super(base);
    }

    private static Uri uriForType(@AttachToType int type, int accountId) {
        switch (type) {
            case AttachToType.COMMENT:
                return MessengerContentProvider.getCommentsAttachmentsContentUriFor(accountId);
            case AttachToType.MESSAGE:
                return MessengerContentProvider.getMessagesAttachmentsContentUriFor(accountId);
            case AttachToType.POST:
                return MessengerContentProvider.getPostsAttachmentsContentUriFor(accountId);
            default:
                throw new IllegalArgumentException();
        }
    }

    static void appendAttachOperationWithBackReference(@NonNull List<ContentProviderOperation> operations, int accountId,
                                                       @AttachToType int attachToType, int attachToBackReferenceIndex, @NonNull Entity entity) {
        ContentValues cv = new ContentValues();

        cv.put(typeColumnFor(attachToType), AttachmentsTypes.typeForInstance(entity));
        cv.put(dataColumnFor(attachToType), GSON.toJson(entity));

        operations.add(ContentProviderOperation.newInsert(uriForType(attachToType, accountId))
                .withValues(cv)
                .withValueBackReference(attachToIdColumnFor(attachToType), attachToBackReferenceIndex)
                .build());
    }

    static int appendAttachOperationWithStableAttachToId(@NonNull List<ContentProviderOperation> operations,
                                                         int accountId, @AttachToType int attachToType,
                                                         int attachToDbid, @NonNull Entity entity) {
        ContentValues cv = new ContentValues();
        cv.put(attachToIdColumnFor(attachToType), attachToDbid);
        cv.put(typeColumnFor(attachToType), AttachmentsTypes.typeForInstance(entity));
        cv.put(dataColumnFor(attachToType), serializeDbo(entity));

        return addToListAndReturnIndex(operations, ContentProviderOperation.newInsert(uriForType(attachToType, accountId))
                .withValues(cv)
                .build());
    }

    private static String idColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
            case AttachToType.MESSAGE:
            case AttachToType.POST:
                return BaseColumns._ID;
        }

        throw new IllegalArgumentException();
    }

    private static String attachToIdColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns.C_ID;
            case AttachToType.MESSAGE:
                return MessagesAttachmentsColumns.M_ID;
            case AttachToType.POST:
                return WallAttachmentsColumns.P_ID;
        }

        throw new IllegalArgumentException();
    }

    private static String typeColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns.TYPE;
            case AttachToType.MESSAGE:
                return MessagesAttachmentsColumns.TYPE;
            case AttachToType.POST:
                return WallAttachmentsColumns.TYPE;
        }

        throw new IllegalArgumentException();
    }

    private static String dataColumnFor(@AttachToType int type) {
        switch (type) {
            case AttachToType.COMMENT:
                return CommentsAttachmentsColumns.DATA;
            case AttachToType.MESSAGE:
                return MessagesAttachmentsColumns.DATA;
            case AttachToType.POST:
                return WallAttachmentsColumns.DATA;
        }

        throw new IllegalArgumentException();
    }

    private static String serializeDbo(Entity entity) {
        return GSON.toJson(entity);
    }

    private static Entity deserializeDbo(int type, String json) {
        Class<? extends Entity> dboClass = AttachmentsTypes.classForType(type);
        return GSON.fromJson(json, dboClass);
    }

    @Override
    public Single<int[]> attachDbos(int accountId, int attachToType, int attachToDbid, @NonNull List<Entity> entities) {
        return Single.create(emitter -> {
            ArrayList<ContentProviderOperation> operations = new ArrayList<>(entities.size());

            int[] indexes = new int[entities.size()];
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                indexes[i] = appendAttachOperationWithStableAttachToId(operations, accountId, attachToType, attachToDbid, entity);
            }

            ContentProviderResult[] results = getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);

            int[] ids = new int[entities.size()];

            for (int i = 0; i < indexes.length; i++) {
                ContentProviderResult result = results[indexes[i]];
                int dbid = Integer.parseInt(result.uri.getPathSegments().get(1));
                ids[i] = dbid;
            }

            emitter.onSuccess(ids);
        });
    }

    @Override
    public Single<List<Pair<Integer, Entity>>> getAttachmentsDbosWithIds(int accountId, @AttachToType int attachToType, int attachToDbid) {
        return Single.create(emitter -> {
            Cursor cursor = createCursor(accountId, attachToType, attachToDbid);

            List<Pair<Integer, Entity>> dbos = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    int id = cursor.getInt(cursor.getColumnIndex(idColumnFor(attachToType)));
                    int type = cursor.getInt(cursor.getColumnIndex(typeColumnFor(attachToType)));
                    String json = cursor.getString(cursor.getColumnIndex(dataColumnFor(attachToType)));
                    Entity entity = deserializeDbo(type, json);

                    dbos.add(Pair.Companion.create(id, entity));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }

    private Cursor createCursor(int accountId, int attachToType, int attachToDbid) {
        Uri uri = uriForType(attachToType, accountId);
        return getContentResolver().query(uri, null,
                attachToIdColumnFor(attachToType) + " = ?", new String[]{String.valueOf(attachToDbid)}, null);
    }

    @Override
    public List<Entity> getAttachmentsDbosSync(int accountId, int attachToType, int attachToDbid, @NonNull Cancelable cancelable) {
        Cursor cursor = createCursor(accountId, attachToType, attachToDbid);

        List<Entity> entities = new ArrayList<>(safeCountOf(cursor));

        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                if (cancelable.isOperationCancelled()) {
                    break;
                }

                int type = cursor.getInt(cursor.getColumnIndex(typeColumnFor(attachToType)));
                String json = cursor.getString(cursor.getColumnIndex(dataColumnFor(attachToType)));

                entities.add(deserializeDbo(type, json));
            }

            cursor.close();
        }

        return entities;
    }

    @Override
    public Completable remove(int accountId, @AttachToType int attachToType, int attachToDbid, int generatedAttachmentId) {
        return Completable.create(e -> {
            Uri uri = uriForType(attachToType, accountId);

            String selection = idColumnFor(attachToType) + " = ?";
            String[] args = {String.valueOf(generatedAttachmentId)};

            int count = getContext().getContentResolver().delete(uri, selection, args);

            if (count > 0) {
                e.onComplete();
            } else {
                e.onError(new NotFoundException());
            }
        });
    }

    @Override
    public Single<Integer> getCount(int accountId, int attachToType, int attachToDbid) {
        return Single.fromCallable(() -> {
            Uri uri = uriForType(attachToType, accountId);
            String selection = attachToIdColumnFor(attachToType) + " = ?";
            String[] args = {String.valueOf(attachToDbid)};

            Cursor cursor = getContentResolver().query(uri, null, selection, args, null);

            int count = safeCountOf(cursor);
            if (nonNull(cursor)) {
                cursor.close();
            }

            return count;
        });
    }
}
