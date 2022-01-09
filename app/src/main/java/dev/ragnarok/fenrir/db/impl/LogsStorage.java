package dev.ragnarok.fenrir.db.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.LogSqliteHelper;
import dev.ragnarok.fenrir.db.column.LogColumns;
import dev.ragnarok.fenrir.db.interfaces.ILogsStorage;
import dev.ragnarok.fenrir.model.LogEvent;
import io.reactivex.rxjava3.core.Single;


public class LogsStorage implements ILogsStorage {

    private static final String[] PROJECTION = {BaseColumns._ID, LogColumns.TYPE, LogColumns.DATE, LogColumns.TAG, LogColumns.BODY};
    private final Context context;

    public LogsStorage(Context context) {
        this.context = context.getApplicationContext();
    }

    private static LogEvent map(Cursor cursor) {
        return new LogEvent(cursor.getInt(cursor.getColumnIndex(BaseColumns._ID)))
                .setType(cursor.getInt(cursor.getColumnIndex(LogColumns.TYPE)))
                .setDate(cursor.getLong(cursor.getColumnIndex(LogColumns.DATE)))
                .setTag(cursor.getString(cursor.getColumnIndex(LogColumns.TAG)))
                .setBody(cursor.getString(cursor.getColumnIndex(LogColumns.BODY)));
    }

    private LogSqliteHelper helper() {
        return LogSqliteHelper.getInstance(context);
    }

    @Override
    public void Clear() {
        helper().Clear();
    }

    @Override
    public Single<LogEvent> add(int type, String tag, String body) {
        return Single.fromCallable(() -> {
            long now = System.currentTimeMillis();

            ContentValues cv = new ContentValues();
            cv.put(LogColumns.TYPE, type);
            cv.put(LogColumns.TAG, tag);
            cv.put(LogColumns.BODY, body);
            cv.put(LogColumns.DATE, now);

            long id = helper().getWritableDatabase().insert(LogColumns.TABLENAME, null, cv);
            return new LogEvent((int) id)
                    .setBody(body)
                    .setTag(tag)
                    .setDate(now)
                    .setType(type);
        });
    }

    @Override
    public Single<List<LogEvent>> getAll(int type) {
        return Single.fromCallable(() -> {
            Cursor cursor = helper().getReadableDatabase().query(LogColumns.TABLENAME, PROJECTION, LogColumns.TYPE + " = ?",
                    new String[]{String.valueOf(type)}, null, null, BaseColumns._ID + " DESC");

            List<LogEvent> data = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                data.add(map(cursor));
            }

            cursor.close();
            return data;
        });
    }
}
