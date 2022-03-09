package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.DocColumns;
import dev.ragnarok.fenrir.db.interfaces.IDocsStorage;
import dev.ragnarok.fenrir.db.model.entity.DocumentEntity;
import dev.ragnarok.fenrir.db.model.entity.PhotoSizeEntity;
import dev.ragnarok.fenrir.model.DocFilter;
import dev.ragnarok.fenrir.model.criteria.DocsCriteria;
import dev.ragnarok.fenrir.util.Exestime;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


class DocsStorage extends AbsStorage implements IDocsStorage {

    DocsStorage(@NonNull AppStorages base) {
        super(base);
    }

    private static DocumentEntity map(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DocColumns.DOC_ID));
        int ownerId = cursor.getInt(cursor.getColumnIndexOrThrow(DocColumns.OWNER_ID));

        DocumentEntity document = new DocumentEntity().set(id, ownerId)
                .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.TITLE)))
                .setSize(cursor.getLong(cursor.getColumnIndexOrThrow(DocColumns.SIZE)))
                .setExt(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.EXT)))
                .setUrl(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.URL)))
                .setType(cursor.getInt(cursor.getColumnIndexOrThrow(DocColumns.TYPE)))
                .setDate(cursor.getLong(cursor.getColumnIndexOrThrow(DocColumns.DATE)))
                .setAccessKey(cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.ACCESS_KEY)));

        String photoJson = cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.PHOTO));
        String graffitiJson = cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.GRAFFITI));
        String videoJson = cursor.getString(cursor.getColumnIndexOrThrow(DocColumns.VIDEO));

        if (nonEmpty(photoJson)) {
            document.setPhoto(GSON.fromJson(photoJson, PhotoSizeEntity.class));
        }

        if (nonEmpty(graffitiJson)) {
            document.setGraffiti(GSON.fromJson(graffitiJson, DocumentEntity.GraffitiDbo.class));
        }

        if (nonEmpty(videoJson)) {
            document.setVideo(GSON.fromJson(videoJson, DocumentEntity.VideoPreviewDbo.class));
        }

        return document;
    }

    @Override
    public Single<List<DocumentEntity>> get(@NonNull DocsCriteria criteria) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getDocsContentUriFor(criteria.getAccountId());

            String where;
            String[] args;

            Integer filter = criteria.getFilter();

            if (nonNull(filter) && filter != DocFilter.Type.ALL) {
                where = DocColumns.OWNER_ID + " = ? AND " + DocColumns.TYPE + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId()), String.valueOf(filter)};
            } else {
                where = DocColumns.OWNER_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            List<DocumentEntity> data = new ArrayList<>(safeCountOf(cursor));

            if (nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) {
                        break;
                    }

                    data.add(map(cursor));
                }

                cursor.close();
            }

            e.onSuccess(data);

            Exestime.log("DocsStorage.get", start, "count: " + data.size());
        });
    }

    @Override
    public Completable store(int accountId, int ownerId, List<DocumentEntity> entities, boolean clearBeforeInsert) {
        return Completable.create(e -> {
            long start = System.currentTimeMillis();

            Uri uri = MessengerContentProvider.getDocsContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>();
            if (clearBeforeInsert) {
                operations.add(ContentProviderOperation.newDelete(uri)
                        .withSelection(DocColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                        .build());
            }

            for (DocumentEntity entity : entities) {
                ContentValues cv = new ContentValues();
                cv.put(DocColumns.DOC_ID, entity.getId());
                cv.put(DocColumns.OWNER_ID, entity.getOwnerId());
                cv.put(DocColumns.TITLE, entity.getTitle());
                cv.put(DocColumns.SIZE, entity.getSize());
                cv.put(DocColumns.EXT, entity.getExt());
                cv.put(DocColumns.URL, entity.getUrl());
                cv.put(DocColumns.DATE, entity.getDate());
                cv.put(DocColumns.TYPE, entity.getType());
                cv.put(DocColumns.ACCESS_KEY, entity.getAccessKey());

                cv.put(DocColumns.PHOTO, nonNull(entity.getPhoto()) ? GSON.toJson(entity.getPhoto()) : null);
                cv.put(DocColumns.GRAFFITI, nonNull(entity.getGraffiti()) ? GSON.toJson(entity.getGraffiti()) : null);
                cv.put(DocColumns.VIDEO, nonNull(entity.getVideo()) ? GSON.toJson(entity.getVideo()) : null);

                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();

            Exestime.log("DocsStorage.store", start, "count: " + entities.size());
        });
    }

    @Override
    public Completable delete(int accountId, int docId, int ownerId) {
        return Completable.fromAction(() -> {
            Uri uri = MessengerContentProvider.getDocsContentUriFor(accountId);
            String where = DocColumns.DOC_ID + " = ? AND " + DocColumns.OWNER_ID + " = ?";
            String[] args = {String.valueOf(docId), String.valueOf(ownerId)};
            getContentResolver().delete(uri, where, args);
        });
    }
}