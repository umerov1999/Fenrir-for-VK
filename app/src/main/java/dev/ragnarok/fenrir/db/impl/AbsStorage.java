package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.DBHelper;
import dev.ragnarok.fenrir.db.MapFunction;
import dev.ragnarok.fenrir.db.interfaces.Cancelable;
import dev.ragnarok.fenrir.db.interfaces.IStorage;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.entity.AttachmentsEntity;
import dev.ragnarok.fenrir.db.model.entity.EntityWrapper;
import dev.ragnarok.fenrir.db.serialize.AttachmentsDboAdapter;
import dev.ragnarok.fenrir.db.serialize.EntityWrapperAdapter;
import dev.ragnarok.fenrir.db.serialize.UriSerializer;

public class AbsStorage implements IStorage {

    static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriSerializer())
            .registerTypeAdapter(AttachmentsEntity.class, new AttachmentsDboAdapter())
            .registerTypeAdapter(EntityWrapper.class, new EntityWrapperAdapter())
            .serializeSpecialFloatingPointValues() // for test
            .create();

    private final AppStorages mRepositoryContext;

    public AbsStorage(@NonNull AppStorages base) {
        mRepositoryContext = base;
    }

    @Nullable
    static String serializeJson(@Nullable Object o) {
        return isNull(o) ? null : GSON.toJson(o);
    }

    @Nullable
    static <T> T deserializeJson(Cursor cursor, String column, Class<T> clazz) {
        String json = cursor.getString(cursor.getColumnIndexOrThrow(column));
        if (nonEmpty(json)) {
            return GSON.fromJson(json, clazz);
        } else {
            return null;
        }
    }

    static <T> List<T> mapAll(Cursor cursor, MapFunction<T> function, boolean close) {
        List<T> data = new ArrayList<>(safeCountOf(cursor));
        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                data.add(function.map(cursor));
            }

            if (close) {
                cursor.close();
            }
        }

        return data;
    }

    static <T> List<T> mapAll(Cancelable cancelable, Cursor cursor, MapFunction<T> function, boolean close) {
        List<T> data = new ArrayList<>(safeCountOf(cursor));
        if (nonNull(cursor)) {
            while (cursor.moveToNext()) {
                if (cancelable.isOperationCancelled()) {
                    break;
                }

                data.add(function.map(cursor));
            }

            if (close) {
                cursor.close();
            }
        }

        return data;
    }

    static int extractId(ContentProviderResult result) {
        return Integer.parseInt(result.uri.getPathSegments().get(1));
    }

    static <T> int addToListAndReturnIndex(@NonNull List<T> target, @NonNull T item) {
        target.add(item);
        return target.size() - 1;
    }

    @Override
    public IStorages getStores() {
        return mRepositoryContext;
    }

    @NonNull
    public Context getContext() {
        return mRepositoryContext.getApplicationContext();
    }

    @NonNull
    DBHelper helper(int accountId) {
        return DBHelper.getInstance(getContext(), accountId);
    }

    protected ContentResolver getContentResolver() {
        return mRepositoryContext.getContentResolver();
    }
}