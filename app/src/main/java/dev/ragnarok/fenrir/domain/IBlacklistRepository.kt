package dev.ragnarok.fenrir.domain

import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.util.Pair
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface IBlacklistRepository {
    fun fireAdd(accountId: Int, user: User): Completable
    fun fireRemove(accountId: Int, userId: Int): Completable
    fun observeAdding(): Observable<Pair<Int, User>>
    fun observeRemoving(): Observable<Pair<Int, Int>>
}