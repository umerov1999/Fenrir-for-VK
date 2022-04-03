package dev.ragnarok.fenrir.domain.impl

import dev.ragnarok.fenrir.domain.IBlacklistRepository
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class BlacklistRepository : IBlacklistRepository {
    private val addPublisher: PublishSubject<Pair<Int, User>> = PublishSubject.create()
    private val removePublisher: PublishSubject<Pair<Int, Int>> = PublishSubject.create()
    override fun fireAdd(accountId: Int, user: User): Completable {
        return Completable.fromAction { addPublisher.onNext(create(accountId, user)) }
    }

    override fun fireRemove(accountId: Int, userId: Int): Completable {
        return Completable.fromAction { removePublisher.onNext(create(accountId, userId)) }
    }

    override fun observeAdding(): Observable<Pair<Int, User>> {
        return addPublisher
    }

    override fun observeRemoving(): Observable<Pair<Int, Int>> {
        return removePublisher
    }

}