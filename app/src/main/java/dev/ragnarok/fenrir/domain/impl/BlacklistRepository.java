package dev.ragnarok.fenrir.domain.impl;

import dev.ragnarok.fenrir.domain.IBlacklistRepository;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class BlacklistRepository implements IBlacklistRepository {

    private final PublishSubject<Pair<Integer, User>> addPublisher;
    private final PublishSubject<Pair<Integer, Integer>> removePublisher;

    public BlacklistRepository() {
        addPublisher = PublishSubject.create();
        removePublisher = PublishSubject.create();
    }

    @Override
    public Completable fireAdd(int accountId, User user) {
        return Completable.fromAction(() -> addPublisher.onNext(Pair.Companion.create(accountId, user)));
    }

    @Override
    public Completable fireRemove(int accountId, int userId) {
        return Completable.fromAction(() -> removePublisher.onNext(Pair.Companion.create(accountId, userId)));
    }

    @Override
    public Observable<Pair<Integer, User>> observeAdding() {
        return addPublisher;
    }

    @Override
    public Observable<Pair<Integer, Integer>> observeRemoving() {
        return removePublisher;
    }
}