package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.db.column.StikerSetColumns.ACTIVE;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns.ICON;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns.POSITION;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns.PROMOTED;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns.PURCHASED;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns.STICKERS;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns.TITLE;
import static dev.ragnarok.fenrir.db.column.StikerSetColumns._ID;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dev.ragnarok.fenrir.db.column.StickersKeywordsColumns;
import dev.ragnarok.fenrir.db.column.StikerSetColumns;
import dev.ragnarok.fenrir.db.interfaces.IStickersStorage;
import dev.ragnarok.fenrir.db.model.entity.StickerEntity;
import dev.ragnarok.fenrir.db.model.entity.StickerSetEntity;
import dev.ragnarok.fenrir.db.model.entity.StickersKeywordsEntity;
import dev.ragnarok.fenrir.util.Exestime;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


class StickersStorage extends AbsStorage implements IStickersStorage {

    private static final String[] COLUMNS = {
            _ID,
            POSITION,
            TITLE,
            ICON,
            PURCHASED,
            PROMOTED,
            ACTIVE,
            STICKERS
    };
    private static final String[] KEYWORDS_STICKER_COLUMNS = {
            _ID,
            StickersKeywordsColumns.KEYWORDS,
            StickersKeywordsColumns.STICKERS
    };
    private static final Type TYPE = new TypeToken<List<StickerEntity>>() {
    }.getType();

    private static final Type WORDS = new TypeToken<List<String>>() {
    }.getType();

    private static final Type ICONS = new TypeToken<List<StickerSetEntity.Img>>() {
    }.getType();

    private static final Comparator<StickerSetEntity> COMPARATOR = (rhs, lhs) -> Integer.compare(lhs.getPosition(), rhs.getPosition());

    StickersStorage(@NonNull AppStorages base) {
        super(base);
    }

    private static ContentValues createCv(StickerSetEntity entity, int pos) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, entity.getId());
        cv.put(POSITION, pos);
        cv.put(ICON, GSON.toJson(entity.getIcon()));
        cv.put(TITLE, entity.getTitle());
        cv.put(PURCHASED, entity.isPurchased());
        cv.put(PROMOTED, entity.isPromoted());
        cv.put(ACTIVE, entity.isActive());

        cv.put(STICKERS, GSON.toJson(entity.getStickers()));
        return cv;
    }

    private static ContentValues createCvStickersKeywords(StickersKeywordsEntity entity, int id) {
        ContentValues cv = new ContentValues();
        cv.put(_ID, id);
        cv.put(StickersKeywordsColumns.KEYWORDS, GSON.toJson(entity.getKeywords()));
        cv.put(StickersKeywordsColumns.STICKERS, GSON.toJson(entity.getStickers()));
        return cv;
    }

    private static StickerSetEntity map(Cursor cursor) {
        String stickersJson = cursor.getString(cursor.getColumnIndex(STICKERS));
        String iconJson = cursor.getString(cursor.getColumnIndex(ICON));
        return new StickerSetEntity(cursor.getInt(cursor.getColumnIndex(_ID)))
                .setStickers(GSON.fromJson(stickersJson, TYPE))
                .setActive(cursor.getInt(cursor.getColumnIndex(ACTIVE)) == 1)
                .setPurchased(cursor.getInt(cursor.getColumnIndex(PURCHASED)) == 1)
                .setPromoted(cursor.getInt(cursor.getColumnIndex(PROMOTED)) == 1)
                .setIcon(GSON.fromJson(iconJson, ICONS))
                .setPosition(cursor.getInt(cursor.getColumnIndex(POSITION)))
                .setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
    }

    private static StickersKeywordsEntity mapStickersKeywords(Cursor cursor) {
        String stickersJson = cursor.getString(cursor.getColumnIndex(StickersKeywordsColumns.STICKERS));
        String keywordsJson = cursor.getString(cursor.getColumnIndex(StickersKeywordsColumns.KEYWORDS));
        return new StickersKeywordsEntity(GSON.fromJson(keywordsJson, WORDS), GSON.fromJson(stickersJson, TYPE));
    }

    @Override
    public Completable store(int accountId, List<StickerSetEntity> sets) {
        return Completable.create(e -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper(accountId).getWritableDatabase();

            db.beginTransaction();

            try {
                db.delete(StikerSetColumns.TABLENAME, null, null);
                int i = 0;
                for (StickerSetEntity entity : sets) {
                    db.insert(StikerSetColumns.TABLENAME, null, createCv(entity, i++));
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                e.onComplete();
            } catch (Exception exception) {
                db.endTransaction();
                e.tryOnError(exception);
            }

            Exestime.log("StickersStorage.store", start, "count: " + safeCountOf(sets));
        });
    }

    @Override
    public Completable storeKeyWords(int accountId, List<StickersKeywordsEntity> sets) {
        return Completable.create(e -> {
            long start = System.currentTimeMillis();

            SQLiteDatabase db = helper(accountId).getWritableDatabase();

            db.beginTransaction();

            try {
                db.delete(StickersKeywordsColumns.TABLENAME, null, null);
                int id = 0;
                for (StickersKeywordsEntity entity : sets) {
                    db.insert(StickersKeywordsColumns.TABLENAME, null, createCvStickersKeywords(entity, id++));
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                e.onComplete();
            } catch (Exception exception) {
                db.endTransaction();
                e.tryOnError(exception);
            }

            Exestime.log("StickersStorage.storeKeyWords", start, "count: " + safeCountOf(sets));
        });
    }

    @Override
    public Single<List<StickerSetEntity>> getPurchasedAndActive(int accountId) {
        return Single.create(e -> {
            long start = System.currentTimeMillis();
            String where = PURCHASED + " = ? AND " + ACTIVE + " = ?";
            String[] args = {"1", "1"};
            Cursor cursor = helper(accountId).getReadableDatabase().query(StikerSetColumns.TABLENAME, COLUMNS, where, args, null, null, null);

            List<StickerSetEntity> stickers = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                if (e.isDisposed()) {
                    break;
                }
                stickers.add(map(cursor));
            }
            Collections.sort(stickers, COMPARATOR);

            cursor.close();
            e.onSuccess(stickers);
            Exestime.log("StickersStorage.get", start, "count: " + stickers.size());
        });
    }

    @Override
    public Single<List<StickerEntity>> getKeywordsStickers(int accountId, String s) {
        return Single.create(e -> {
            Cursor cursor = helper(accountId).getReadableDatabase().query(StickersKeywordsColumns.TABLENAME, KEYWORDS_STICKER_COLUMNS, null, null, null, null, null);

            List<StickerEntity> stickers = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                if (e.isDisposed()) {
                    break;
                }
                StickersKeywordsEntity entity = mapStickersKeywords(cursor);
                for (String v : entity.getKeywords()) {
                    if (s.equalsIgnoreCase(v)) {
                        stickers.addAll(entity.getStickers());
                        cursor.close();
                        e.onSuccess(stickers);
                        return;
                    }
                }
            }

            cursor.close();
            e.onSuccess(stickers);
        });
    }
}