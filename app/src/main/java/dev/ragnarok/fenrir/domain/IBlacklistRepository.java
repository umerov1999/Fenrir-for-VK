package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface IBlacklistRepository {
    Completable fireAdd(int accountId, User user);

    Completable fireRemove(int accountId, int userId);

    Observable<Pair<Integer, User>> observeAdding();

    Observable<Pair<Integer, Integer>> observeRemoving();
}