package dev.ragnarok.fenrir.domain

import dev.ragnarok.fenrir.api.model.VkApiProfileInfo
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse.ConversationsPush.ConversationPushItem
import dev.ragnarok.fenrir.model.Account
import dev.ragnarok.fenrir.model.BannedPart
import dev.ragnarok.fenrir.model.User
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface IAccountsInteractor {
    fun getBanned(accountId: Int, count: Int, offset: Int): Single<BannedPart>
    fun banUsers(accountId: Int, users: Collection<User>): Completable
    fun unbanUser(accountId: Int, userId: Int): Completable
    fun changeStatus(accountId: Int, status: String?): Completable
    fun setOffline(accountId: Int): Single<Boolean>
    fun getProfileInfo(accountId: Int): Single<VkApiProfileInfo>
    fun getPushSettings(accountId: Int): Single<List<ConversationPushItem>>
    fun saveProfileInfo(
        accountId: Int,
        first_name: String?,
        last_name: String?,
        maiden_name: String?,
        screen_name: String?,
        bdate: String?,
        home_town: String?,
        sex: Int?
    ): Single<Int>

    fun getAll(refresh: Boolean): Single<List<Account>>
}