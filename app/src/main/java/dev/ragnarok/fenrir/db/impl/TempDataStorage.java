package dev.ragnarok.fenrir.db.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.TempDataHelper;
import dev.ragnarok.fenrir.db.column.TempDataColumns;
import dev.ragnarok.fenrir.db.interfaces.ITempDataStorage;
import dev.ragnarok.fenrir.db.serialize.ISerializeAdapter;
import dev.ragnarok.fenrir.util.Exestime;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class TempDataStorage implements ITempDataStorage {

    private static final String[] PROJECTION = {
            BaseColumns._ID, TempDataColumns.OWNER_ID, TempDataColumns.SOURCE_ID, TempDataColumns.DATA};
    private final Context app;

    TempDataStorage(Context context) {
        app = context.getApplicationContext();
    }

    private TempDataHelper helper() {
        return TempDataHelper.getInstance(app);
    }

    @Override
    public <T> Single<List<T>> getData(int ownerId, int sourceId, ISerializeAdapter<T> serializer) {
        return Single.fromCallable(() -> {
            long start = System.currentTimeMillis();

            String where = TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?";
            String[] args = {String.valueOf(ownerId), String.valueOf(sourceId)};

            Cursor cursor = helper().getReadableDatabase().query(TempDataColumns.TABLENAME,
                    PROJECTION, where, args, null, null, null);

            List<T> data = new ArrayList<>(cursor.getCount());

            try {
                while (cursor.moveToNext()) {
                    String raw = cursor.getString(3);
                    data.add(serializer.deserialize(raw));
                }
            } finally {
                cursor.close();
            }

            Exestime.log("TempDataStorage.getData", start, "count: " + data.size());
            return data;
        });
    }

    @Override
    public <T> Completable put(int ownerId, int sourceId, List<T> data, ISerializeAdapter<T> serializer) {
        return Completable.create(emitter -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper().getWritableDatabase();

            db.beginTransaction();

            try {
                // clear
                db.delete(TempDataColumns.TABLENAME,
                        TempDataColumns.OWNER_ID + " = ? AND " + TempDataColumns.SOURCE_ID + " = ?",
                        new String[]{String.valueOf(ownerId), String.valueOf(sourceId)});

                for (T t : data) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    ContentValues cv = new ContentValues();
                    cv.put(TempDataColumns.OWNER_ID, ownerId);
                    cv.put(TempDataColumns.SOURCE_ID, sourceId);
                    cv.put(TempDataColumns.DATA, serializer.serialize(t));

                    db.insert(TempDataColumns.TABLENAME, null, cv);
                }

                if (!emitter.isDisposed()) {
                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }

            Exestime.log("TempDataStorage.put", start, "count: " + data.size());

            emitter.onComplete();
        });
    }

    @Override
    public void clearAll() {
        helper().getWritableDatabase().delete(TempDataColumns.TABLENAME, null, null);
    }

    @Override
    public Completable delete(int ownerId) {
        return Completable.fromAction(() -> {
            long start = System.currentTimeMillis();
            int count = helper().getWritableDatabase().delete(TempDataColumns.TABLENAME,
                    TempDataColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)});
            Exestime.log("TempDataStorage.delete", start, "count: " + count);
        });
    }
}