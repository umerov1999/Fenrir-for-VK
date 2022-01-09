package dev.ragnarok.fenrir.db.interfaces;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.CountryEntity;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IDatabaseStore {
    Completable storeCountries(int accountId, List<CountryEntity> dbos);

    Single<List<CountryEntity>> getCountries(int accountId);
}