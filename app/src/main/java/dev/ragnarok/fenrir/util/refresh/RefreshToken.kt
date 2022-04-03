package dev.ragnarok.fenrir.util.refresh

import android.util.Log
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Includes.networkInterfaces
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils
import io.reactivex.rxjava3.core.Single

object RefreshToken {
    private fun upgradeTokenKate(account: Int, oldToken: String): Boolean {
        if (Utils.isHiddenAccount(account)) {
            return false
        }
        val gms = TokenModKate.requestToken() ?: return false
        val token =
            networkInterfaces.vkDefault(account).account().refreshToken(gms, null, null, null)
                .blockingGet().token
        Log.w("refresh", "$oldToken $token $gms")
        if (oldToken == token || token.isNullOrEmpty()) {
            return false
        }
        Settings.get().accounts().storeAccessToken(account, token)
        return true
    }

    private fun upgradeTokenOfficial(account: Int, oldToken: String): Boolean {
        if (Utils.isHiddenAccount(account)) {
            return false
        }
        val gms = TokenModOfficialVK.requestToken() ?: return false
        val timestamp = System.currentTimeMillis()
        val token = networkInterfaces.vkDefault(account).account()
            .refreshToken(gms[0], gms[1], TokenModOfficialVK.getNonce(timestamp), timestamp)
            .blockingGet().token
        Log.w("refresh", "$oldToken $token $gms")
        if (oldToken == token || token.isNullOrEmpty()) {
            return false
        }
        Settings.get().accounts().storeAccessToken(account, token)
        return true
    }

    fun upgradeToken(account: Int, oldToken: String): Boolean {
        if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
            return upgradeTokenKate(account, oldToken)
        } else if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID) {
            return upgradeTokenOfficial(account, oldToken)
        }
        return false
    }

    fun upgradeTokenRx(account: Int, oldToken: String): Single<Boolean> {
        return Single.create {
            try {
                upgradeToken(account, oldToken)
                it.onSuccess(true)
            } catch (ignored: Exception) {
                it.onSuccess(false)
            }
        }
    }
}