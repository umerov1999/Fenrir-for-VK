package dev.ragnarok.fenrir.db.impl;

import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import dev.ragnarok.fenrir.crypt.AesKeyPair;
import dev.ragnarok.fenrir.db.interfaces.IKeysStorage;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;


class KeysRamStorage implements IKeysStorage {

    private final SparseArray<List<AesKeyPair>> mData = new SparseArray<>();

    private List<AesKeyPair> prepareKeysFor(int accountId) {
        List<AesKeyPair> list = mData.get(accountId);
        if (Objects.isNull(list)) {
            list = new CopyOnWriteArrayList<>();
            mData.put(accountId, list);
        }

        return list;
    }

    @Override
    public Completable saveKeyPair(@NonNull AesKeyPair pair) {
        return Completable.create(e -> {
            prepareKeysFor(pair.getAccountId()).add(pair);
            e.onComplete();
        });
    }

    @Override
    public Single<List<AesKeyPair>> getAll(int accountId) {
        return Single.create(e -> {
            List<AesKeyPair> list = mData.get(accountId);
            List<AesKeyPair> result = new ArrayList<>(Objects.isNull(list) ? 0 : 1);
            if (Objects.nonNull(list)) {
                result.addAll(list);
            }

            e.onSuccess(result);
        });
    }

    @Override
    public Single<List<AesKeyPair>> getKeys(int accountId, int peerId) {
        return Single.create(e -> {
            List<AesKeyPair> list = mData.get(accountId);
            List<AesKeyPair> result = new ArrayList<>(Objects.isNull(list) ? 0 : 1);
            if (Objects.nonNull(list)) {
                for (AesKeyPair pair : list) {
                    if (pair.getPeerId() == peerId) {
                        result.add(pair);
                    }
                }
            }

            e.onSuccess(result);
        });
    }

    @Override
    public Single<Optional<AesKeyPair>> findLastKeyPair(int accountId, int peerId) {
        return Single.create(e -> {
            List<AesKeyPair> list = mData.get(accountId);
            AesKeyPair result = null;
            if (Objects.nonNull(list)) {
                for (AesKeyPair pair : list) {
                    if (pair.getPeerId() == peerId) {
                        result = pair;
                    }
                }
            }

            e.onSuccess(Optional.wrap(result));
        });
    }

    @Override
    public Maybe<AesKeyPair> findKeyPairFor(int accountId, long sessionId) {
        return Maybe.create(e -> {
            List<AesKeyPair> pairs = mData.get(accountId);
            AesKeyPair result = null;
            if (Objects.nonNull(pairs)) {
                for (AesKeyPair pair : pairs) {
                    if (pair.getSessionId() == sessionId) {
                        result = pair;
                        break;
                    }
                }
            }

            if (Objects.nonNull(result)) {
                e.onSuccess(result);
            }

            e.onComplete();
        });
    }

    @Override
    public Completable deleteAll(int accountId) {
        return Completable.create(e -> {
            mData.remove(accountId);
            e.onComplete();
        });
    }

    @Override
    public IStorages getStores() {
        throw new UnsupportedOperationException();
    }
}
