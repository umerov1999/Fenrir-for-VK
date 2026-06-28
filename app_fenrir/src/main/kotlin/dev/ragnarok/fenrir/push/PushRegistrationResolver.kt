package dev.ragnarok.fenrir.push

import android.content.Context
import android.os.Build
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.api.exceptions.ApiException
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.service.ApiErrorCodes
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.VKPushRegistration
import dev.ragnarok.fenrir.util.Logger.d
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.deviceName
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.andThen
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.emptyTaskFlow
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.ignoreElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.Locale

class PushRegistrationResolver(
    private val settings: ISettings,
    private val networker: INetworker
) : IPushRegistrationResolver {
    override fun canReceivePushNotification(accountId: Long): Boolean {
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return false
        }
        val available = settings.pushSettings().registered
        val can = available != null && available.userId == accountId
        d(
            TAG, "canReceivePushNotification, reason: " + can.toString()
                .uppercase(Locale.getDefault())
        )
        return can
    }

    override fun resolvePushRegistration(
        fcmToken: String,
        context: Context
    ): Flow<Boolean> {
        val accountId = Settings.get().accounts().current
        val available = settings.pushSettings().registered

        var completable = emptyTaskFlow()

        if (accountId != ISettings.IAccountsSettings.INVALID_ID && accountId > 0 && settings.accounts()
                .getType(accountId) == Constants.DEFAULT_ACCOUNT_TYPE
        ) {
            val deviceId = Utils.getDeviceId(Settings.get().accounts().getType(accountId), context)
            if (available != null && (available.userId != accountId || available.deviceId != deviceId || available.fcmToken != fcmToken)) {
                completable = completable.andThen(unregister(available))
            }
            val reg =
                VKPushRegistration().set(
                    accountId,
                    deviceId,
                    fcmToken
                )
            completable = completable.andThen(register(reg))
                .map {
                    settings.pushSettings().registered = reg
                    d(TAG, "Register success")
                    true
                }
                .catch { d(TAG, "Register error, t: $it") }
        }
        return completable
    }

    private fun register(registration: VKPushRegistration): Flow<Boolean> {
        //try {
        /*
            JSONArray fr_of_fr = new JSONArray();
            fr_of_fr.put("fr_of_fr");

            JSONObject json = new JSONObject();
            json.put("msg", "on"); // личные сообщения +
            json.put("sdk_open", "on");
            json.put("mention", "on");
            json.put("event_soon", "on");
            json.put("app_request", "on");
            json.put("chat", "on"); // групповые чаты +
            json.put("wall_post", "on"); // новая запись на стене пользователя +
            json.put("comment", "on"); // комментарии +
            json.put("reply", "on"); // ответы +
            json.put("wall_publish", "on"); // размещение предложенной новости +
            json.put("friend", "on");  // запрос на добавления в друзья +
            json.put("friend_accepted", "on"); // подтверждение заявки в друзья +
            json.put("group_invite", "on"); // приглашение в сообщество +
            json.put("birthday", "on"); // уведомления о днях рождениях на текущую дату

            //(хер приходят)
            json.put("like", fr_of_fr); // отметки "Мне нравится"
            json.put("group_accepted", fr_of_fr); // подтверждение заявки на вступление в группу - (хер приходят) 09.01.2016
            json.put("mention", fr_of_fr); // упоминания - (хер приходят) 09.01.2016
            json.put("repost", fr_of_fr); // действия "Рассказать друзьям" - (хер приходят) 09.01.2016

            json.put("new_post", "on"); //записи выбранных людей и сообществ;

            String targetSettingsStr = json.toString();

             */
        val deviceModel = deviceName
        //String osVersion = Utils.getAndroidVersion();
        return if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
            networker.vkDefault(registration.userId)
                .account()
                .registerDevice(
                    Constants.API_ID,
                    Constants.API_ID,
                    registration.fcmToken,
                    null,
                    null,
                    "fcm",
                    null,
                    null,
                    deviceModel,
                    registration.deviceId,
                    Build.VERSION.RELEASE,
                    "{\"msg\":\"on\",\"chat\":\"on\",\"friend\":\"on\",\"reply\":\"on\",\"comment\":\"on\",\"mention\":\"on\",\"like\":\"off\"}"
                )
                .ignoreElement()
        } else {
            networker.vkDefault(registration.userId)
                .account()
                .registerDevice(
                    Constants.API_ID,
                    Constants.API_ID,
                    registration.fcmToken,
                    1,
                    Constants.VK_ANDROID_APP_VERSION_CODE,
                    "fcm",
                    "vk_client",
                    4,
                    deviceModel,
                    registration.deviceId,
                    Build.VERSION.RELEASE,
                    null
                )
                .ignoreElement()
        }
    }

    override fun unregister(registration: VKPushRegistration): Flow<Boolean> {
        return networker.vkDefault(registration.userId)
            .account()
            .unregisterDevice(registration.deviceId)
            .ignoreElement()
            .catch {
                val cause = getCauseIfRuntime(it)
                if (cause is ApiException && cause.error.errorCode == ApiErrorCodes.USER_AUTHORIZATION_FAILED) {
                    return@catch emit(false)
                }
                throw it
            }
    }

    companion object {
        private val TAG = PushRegistrationResolver::class.simpleName.orEmpty()
    }
}