package dev.ragnarok.fenrir

import android.content.res.Resources
import android.os.Build
import dev.ragnarok.fenrir.db.column.GroupColumns
import dev.ragnarok.fenrir.db.column.UserColumns
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils
import java.util.*

object Constants {
    const val RANDOM_PAGAN_SYMBOL_NUMBER = 17
    val RANDOM_EXCLUDE_PAGAN_SYMBOLS: Array<Int>? = null
    const val API_VERSION = "5.131"
    const val DATABASE_VERSION = 12

    @JvmField
    @AccountType
    val DEFAULT_ACCOUNT_TYPE: Int = BuildConfig.DEFAULT_ACCOUNT_TYPE

    @JvmField
    val AUTH_VERSION = if (DEFAULT_ACCOUNT_TYPE == AccountType.KATE) API_VERSION else "5.122"
    const val FILE_PROVIDER_AUTHORITY: String = BuildConfig.APPLICATION_ID + ".file_provider"
    const val VK_ANDROID_APP_VERSION_NAME = "7.15"
    const val VK_ANDROID_APP_VERSION_CODE = "11064"
    const val KATE_APP_VERSION_NAME = "84 lite"
    const val KATE_APP_VERSION_CODE = "510"
    const val API_ID: Int = BuildConfig.VK_API_APP_ID
    const val SECRET: String = BuildConfig.VK_CLIENT_SECRET
    const val MAIN_OWNER_FIELDS = UserColumns.API_FIELDS + "," + GroupColumns.API_FIELDS
    const val PHOTOS_PATH = "DCIM/Fenrir"
    const val AUDIO_PLAYER_SERVICE_IDLE = 300000
    const val PIN_DIGITS_COUNT = 4
    const val MAX_RECENT_CHAT_COUNT = 4
    const val FRAGMENT_CHAT_APP_BAR_VIEW_COUNT = 1
    const val FRAGMENT_CHAT_DOWN_MENU_VIEW_COUNT = 0
    const val PICASSO_TAG = "picasso_tag"

    @JvmField
    val IS_DEBUG: Boolean = BuildConfig.DEBUG

    @JvmField
    var DEVICE_COUNTRY_CODE = "ru"

    @JvmField
    val KATE_USER_AGENT = String.format(
        Locale.US,
        "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)",
        KATE_APP_VERSION_NAME,
        KATE_APP_VERSION_CODE,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        Build.SUPPORTED_ABIS[0],
        Utils.getDeviceName(),
        DEVICE_COUNTRY_CODE,
        SCREEN_RESOLUTION()
    )
    val KATE_USER_AGENT_FAKE = String.format(
        Locale.US,
        "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)",
        KATE_APP_VERSION_NAME,
        KATE_APP_VERSION_CODE,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        BuildConfig.FAKE_ABI,
        BuildConfig.FAKE_DEVICE,
        DEVICE_COUNTRY_CODE,
        SCREEN_RESOLUTION()
    )
    val VK_ANDROID_USER_AGENT = String.format(
        Locale.US,
        "VKAndroidApp/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)",
        VK_ANDROID_APP_VERSION_NAME,
        VK_ANDROID_APP_VERSION_CODE,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        Build.SUPPORTED_ABIS[0],
        Utils.getDeviceName(),
        DEVICE_COUNTRY_CODE,
        SCREEN_RESOLUTION()
    )
    private val VK_ANDROID_USER_AGENT_FAKE = String.format(
        Locale.US,
        "VKAndroidApp/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)",
        VK_ANDROID_APP_VERSION_NAME,
        VK_ANDROID_APP_VERSION_CODE,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
        BuildConfig.FAKE_ABI,
        BuildConfig.FAKE_DEVICE,
        DEVICE_COUNTRY_CODE,
        SCREEN_RESOLUTION()
    )

    private fun SCREEN_RESOLUTION(): String {
        val metrics = Resources.getSystem().displayMetrics ?: return "1920x1080"
        return metrics.heightPixels.toString() + "x" + metrics.widthPixels
    }

    private fun getTypedUserAgent(@AccountType type: Int): String {
        if (type == AccountType.VK_ANDROID_HIDDEN || type == AccountType.KATE_HIDDEN) {
            val device = Settings.get().accounts().getDevice(Settings.get().accounts().current)
            if (!Utils.isEmpty(device)) {
                return if (type == AccountType.KATE_HIDDEN) String.format(
                    Locale.US,
                    "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)",
                    KATE_APP_VERSION_NAME,
                    KATE_APP_VERSION_CODE,
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT,
                    BuildConfig.FAKE_ABI,
                    device,
                    DEVICE_COUNTRY_CODE,
                    SCREEN_RESOLUTION()
                ) else String.format(
                    Locale.US,
                    "VKAndroidApp/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)",
                    VK_ANDROID_APP_VERSION_NAME,
                    VK_ANDROID_APP_VERSION_CODE,
                    Build.VERSION.RELEASE,
                    Build.VERSION.SDK_INT,
                    BuildConfig.FAKE_ABI,
                    device,
                    DEVICE_COUNTRY_CODE,
                    SCREEN_RESOLUTION()
                )
            }
        }
        when (type) {
            AccountType.BY_TYPE, AccountType.VK_ANDROID -> return VK_ANDROID_USER_AGENT
            AccountType.VK_ANDROID_HIDDEN -> return VK_ANDROID_USER_AGENT_FAKE
            AccountType.KATE -> return KATE_USER_AGENT
            AccountType.KATE_HIDDEN -> return KATE_USER_AGENT_FAKE
        }
        return Utils.BY_DEFAULT_ACCOUNT_TYPE(VK_ANDROID_USER_AGENT, KATE_USER_AGENT)
    }

    fun USER_AGENT_ACCOUNT(): String {
        val accountId = Settings.get().accounts().current
        return if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            Utils.BY_DEFAULT_ACCOUNT_TYPE(
                VK_ANDROID_USER_AGENT,
                KATE_USER_AGENT
            )
        } else getTypedUserAgent(
            Settings.get().accounts().getType(accountId)
        )
    }

    @JvmStatic
    fun USER_AGENT(@AccountType type: Int): String {
        if (type != AccountType.BY_TYPE) {
            return getTypedUserAgent(type)
        }
        val accountId = Settings.get().accounts().current
        return if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            Utils.BY_DEFAULT_ACCOUNT_TYPE(
                VK_ANDROID_USER_AGENT,
                KATE_USER_AGENT
            )
        } else getTypedUserAgent(
            Settings.get().accounts().getType(accountId)
        )
    }
}
