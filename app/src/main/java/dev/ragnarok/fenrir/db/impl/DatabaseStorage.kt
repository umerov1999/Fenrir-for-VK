package dev.ragnarok.fenrir.db.impl

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.provider.BaseColumns
import dev.ragnarok.fenrir.db.MessengerContentProvider
import dev.ragnarok.fenrir.db.MessengerContentProvider.Companion.getCountriesContentUriFor
import dev.ragnarok.fenrir.db.column.CountriesColumns
import dev.ragnarok.fenrir.db.interfaces.IDatabaseStore
import dev.ragnarok.fenrir.db.model.entity.CountryEntity
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter

class DatabaseStorage internal constructor(base: AppStorages) : AbsStorage(base), IDatabaseStore {
    override fun storeCountries(accountId: Int, dbos: List<CountryEntity>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val uri = getCountriesContentUriFor(accountId)
            val operations = ArrayList<ContentProviderOperation>(dbos.size + 1)
            operations.add(ContentProviderOperation.newDelete(uri).build())
            for (dbo in dbos) {
                val cv = ContentValues()
                cv.put(BaseColumns._ID, dbo.id)
                cv.put(CountriesColumns.NAME, dbo.title)
                operations.add(
                    ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build()
                )
            }
            contentResolver.applyBatch(MessengerContentProvider.AUTHORITY, operations)
            emitter.onComplete()
        }
    }

    override fun getCountries(accountId: Int): Single<List<CountryEntity>> {
        return Single.create { emitter: SingleEmitter<List<CountryEntity>> ->
            val uri = getCountriesContentUriFor(accountId)
            val cursor = contentResolver.query(uri, null, null, null, null)
            val dbos: MutableList<CountryEntity> = ArrayList(safeCountOf(cursor))
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (emitter.isDisposed) {
                        break
                    }
                    val id = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    val title =
                        cursor.getString(cursor.getColumnIndexOrThrow(CountriesColumns.NAME))
                    dbos.add(CountryEntity().set(id, title))
                }
                cursor.close()
            }
            emitter.onSuccess(dbos)
        }
    }
}