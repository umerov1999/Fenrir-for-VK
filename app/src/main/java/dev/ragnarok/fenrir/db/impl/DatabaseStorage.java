package dev.ragnarok.fenrir.db.impl;

import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.MessengerContentProvider;
import dev.ragnarok.fenrir.db.column.CountriesColumns;
import dev.ragnarok.fenrir.db.interfaces.IDatabaseStore;
import dev.ragnarok.fenrir.db.model.entity.CountryEntity;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public class DatabaseStorage extends AbsStorage implements IDatabaseStore {

    DatabaseStorage(@NonNull AppStorages base) {
        super(base);
    }

    @Override
    public Completable storeCountries(int accountId, List<CountryEntity> dbos) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getCountriesContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>(dbos.size() + 1);
            operations.add(ContentProviderOperation.newDelete(uri).build());

            for (CountryEntity dbo : dbos) {
                ContentValues cv = new ContentValues();
                cv.put(BaseColumns._ID, dbo.getId());
                cv.put(CountriesColumns.NAME, dbo.getTitle());

                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<List<CountryEntity>> getCountries(int accountId) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getCountriesContentUriFor(accountId);

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            List<CountryEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if (Objects.nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed()) {
                        break;
                    }

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(CountriesColumns.NAME));
                    dbos.add(new CountryEntity().set(id, title));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }
}