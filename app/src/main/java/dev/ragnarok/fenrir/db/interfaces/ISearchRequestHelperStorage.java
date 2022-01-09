package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface ISearchRequestHelperStorage {
    Single<List<String>> getQueries(int sourceId);

    Completable insertQuery(int sourceId, @NonNull String query);

    Completable delete(int sourceId);

    void clearAll();
}
