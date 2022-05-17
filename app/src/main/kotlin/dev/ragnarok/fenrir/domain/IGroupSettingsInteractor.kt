package dev.ragnarok.fenrir.domain

import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.util.Pair
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IGroupSettingsInteractor {
    fun getGroupSettings(accountId: Int, groupId: Int): Single<GroupSettings>
    fun ban(
        accountId: Int,
        groupId: Int,
        ownerId: Int,
        endDateUnixtime: Long?,
        reason: Int,
        comment: String?,
        commentVisible: Boolean
    ): Completable

    fun editManager(
        accountId: Int,
        groupId: Int,
        user: User,
        role: String?,
        asContact: Boolean,
        position: String?,
        email: String?,
        phone: String?
    ): Completable

    fun unban(accountId: Int, groupId: Int, ownerId: Int): Completable
    fun getBanned(
        accountId: Int,
        groupId: Int,
        startFrom: IntNextFrom,
        count: Int
    ): Single<Pair<List<Banned>, IntNextFrom>>

    fun getManagers(accountId: Int, groupId: Int): Single<List<Manager>>
    fun getContacts(accountId: Int, groupId: Int): Single<List<ContactInfo>>
}