package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.SearchRequestHelper;
import dev.ragnarok.fenrir.db.column.SearchRequestColumns;
import dev.ragnarok.fenrir.db.interfaces.ISearchRequestHelperStorage;
import dev.ragnarok.fenrir.util.Exestime;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class SearchRequestHelperStorage implements ISearchRequestHelperStorage {

    private static final String[] PROJECTION = {
            BaseColumns._ID, SearchRequestColumns.SOURCE_ID, SearchRequestColumns.QUERY};
    private final Context app;

    SearchRequestHelperStorage(Context context) {
        app = context.getApplicationContext();
    }

    private SearchRequestHelper helper() {
        return SearchRequestHelper.getInstance(app);
    }

    @Override
    public Single<List<String>> getQueries(int sourceId) {
        return Single.fromCallable(() -> {
            long start = System.currentTimeMillis();

            String where = SearchRequestColumns.SOURCE_ID + " = ?";
            String[] args = {String.valueOf(sourceId)};

            Cursor cursor = helper().getReadableDatabase().query(SearchRequestColumns.TABLENAME,
                    PROJECTION, where, args, null, null, BaseColumns._ID + " DESC");

            List<String> data = new ArrayList<>(cursor.getCount());

            try {
                while (cursor.moveToNext()) {
                    data.add(cursor.getString(2));
                }
            } finally {
                cursor.close();
            }

            Exestime.log("SearchRequestHelperStorage.getQueries", start, "count: " + data.size());
            return data;
        });
    }

    @Override
    public Completable insertQuery(int sourceId, @Nullable String query) {
        if (isNull(query)) {
            return Completable.complete();
        }
        String queryClean = query.trim();
        if (Utils.isEmpty(queryClean)) {
            return Completable.complete();
        }
        return Completable.create(emitter -> {
            SQLiteDatabase db = helper().getWritableDatabase();

            db.beginTransaction();

            if (emitter.isDisposed()) {
                db.endTransaction();
                emitter.onComplete();
                return;
            }

            db.delete(SearchRequestColumns.TABLENAME,
                    SearchRequestColumns.QUERY + " = ?", new String[]{queryClean});

            try {
                ContentValues cv = new ContentValues();
                cv.put(SearchRequestColumns.SOURCE_ID, sourceId);
                cv.put(SearchRequestColumns.QUERY, queryClean);

                db.insert(SearchRequestColumns.TABLENAME, null, cv);

                if (!emitter.isDisposed()) {
                    db.setTransactionSuccessful();
                }
            } finally {
                db.endTransaction();
            }

            emitter.onComplete();
        });
    }

    @Override
    public void clearAll() {
        helper().getWritableDatabase().delete(SearchRequestColumns.TABLENAME, null, null);
    }

    @Override
    public Completable delete(int sourceId) {
        return Completable.fromAction(() -> {
            long start = System.currentTimeMillis();
            int count = helper().getWritableDatabase().delete(SearchRequestColumns.TABLENAME,
                    SearchRequestColumns.SOURCE_ID + " = ?", new String[]{String.valueOf(sourceId)});
            Exestime.log("SearchRequestHelperStorage.delete", start, "count: " + count);
        });
    }
}
