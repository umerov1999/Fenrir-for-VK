package dev.ragnarok.fenrir.db.interfaces;

import java.util.List;

import dev.ragnarok.fenrir.db.serialize.ISerializeAdapter;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface ITempDataStorage {
    <T> Single<List<T>> getData(int ownerId, int sourceId, ISerializeAdapter<T> serializer);

    <T> Completable put(int ownerId, int sourceId, List<T> data, ISerializeAdapter<T> serializer);

    Completable delete(int ownerId);

    void clearAll();
}