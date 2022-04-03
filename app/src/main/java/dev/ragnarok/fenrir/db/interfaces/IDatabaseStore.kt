package dev.ragnarok.fenrir.db.interfaces

import dev.ragnarok.fenrir.db.model.entity.CountryEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IDatabaseStore {
    fun storeCountries(accountId: Int, dbos: List<CountryEntity>): Completable
    fun getCountries(accountId: Int): Single<List<CountryEntity>>
}