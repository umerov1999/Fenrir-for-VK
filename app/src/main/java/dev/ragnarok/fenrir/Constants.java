package dev.ragnarok.fenrir;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import java.util.Locale;

import dev.ragnarok.fenrir.db.column.GroupColumns;
import dev.ragnarok.fenrir.db.column.UserColumns;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class Constants {
    public static final int RANDOM_PAGAN_SYMBOL_NUMBER = 17;
    public static final Integer[] RANDOM_EXCLUDE_PAGAN_SYMBOLS = null;

    public static final String API_VERSION = "5.131";
    public static final int DATABASE_VERSION = 12;

    public static final @AccountType
    int DEFAULT_ACCOUNT_TYPE = BuildConfig.DEFAULT_ACCOUNT_TYPE;

    public static final boolean IS_HAS_LOGIN_WEB = DEFAULT_ACCOUNT_TYPE == AccountType.KATE;
    public static final String AUTH_VERSION = DEFAULT_ACCOUNT_TYPE == AccountType.KATE ? API_VERSION : "5.122";
    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".file_provider";

    public static final String VK_ANDROID_APP_VERSION_NAME = "7.14";
    public static final String VK_ANDROID_APP_VERSION_CODE = "10948";
    public static final String KATE_APP_VERSION_NAME = "84 lite";
    public static final String KATE_APP_VERSION_CODE = "510";
    public static final int API_ID = BuildConfig.VK_API_APP_ID;
    public static final String SECRET = BuildConfig.VK_CLIENT_SECRET;
    public static final String MAIN_OWNER_FIELDS = UserColumns.API_FIELDS + "," + GroupColumns.API_FIELDS;
    public static final String PHOTOS_PATH = "DCIM/Fenrir";
    public static final int AUDIO_PLAYER_SERVICE_IDLE = 300000;
    public static final int PIN_DIGITS_COUNT = 4;
    public static final int MAX_RECENT_CHAT_COUNT = 4;
    public static final int FRAGMENT_CHAT_APP_BAR_VIEW_COUNT = 1;
    public static final int FRAGMENT_CHAT_DOWN_MENU_VIEW_COUNT = 0;
    public static final String PICASSO_TAG = "picasso_tag";
    public static final boolean IS_DEBUG = BuildConfig.DEBUG;
    public static String DEVICE_COUNTRY_CODE = "ru";
    public static final String KATE_USER_AGENT = String.format(Locale.US, "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", KATE_APP_VERSION_NAME, KATE_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS[0], Utils.getDeviceName(), DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());
    public static final String KATE_USER_AGENT_FAKE = String.format(Locale.US, "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", KATE_APP_VERSION_NAME, KATE_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, BuildConfig.FAKE_ABI, BuildConfig.FAKE_DEVICE, DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());
    public static final String VK_ANDROID_USER_AGENT = String.format(Locale.US, "VKAndroidApp/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", VK_ANDROID_APP_VERSION_NAME, VK_ANDROID_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS[0], Utils.getDeviceName(), DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());
    public static final String VK_ANDROID_USER_AGENT_FAKE = String.format(Locale.US, "VKAndroidApp/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", VK_ANDROID_APP_VERSION_NAME, VK_ANDROID_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, BuildConfig.FAKE_ABI, BuildConfig.FAKE_DEVICE, DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());

    private static String SCREEN_RESOLUTION() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        if (metrics == null) {
            return "1920x1080";
        }
        return metrics.heightPixels + "x" + metrics.widthPixels;
    }

    @NonNull
    private static String getTypedUserAgent(@AccountType int type) {
        if (type == AccountType.VK_ANDROID_HIDDEN || type == AccountType.KATE_HIDDEN) {
            String device = Settings.get().accounts().getDevice(Settings.get().accounts().getCurrent());
            if (!Utils.isEmpty(device)) {
                return type == AccountType.KATE_HIDDEN ? String.format(Locale.US, "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", KATE_APP_VERSION_NAME, KATE_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, BuildConfig.FAKE_ABI, device, DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION()) : String.format(Locale.US, "VKAndroidApp/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", VK_ANDROID_APP_VERSION_NAME, VK_ANDROID_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, BuildConfig.FAKE_ABI, device, DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());
            }
        }
        switch (type) {
            case AccountType.BY_TYPE:
            case AccountType.VK_ANDROID:
                return VK_ANDROID_USER_AGENT;
            case AccountType.VK_ANDROID_HIDDEN:
                return VK_ANDROID_USER_AGENT_FAKE;
            case AccountType.KATE:
                return KATE_USER_AGENT;
            case AccountType.KATE_HIDDEN:
                return KATE_USER_AGENT_FAKE;
        }
        return Utils.BY_DEFAULT_ACCOUNT_TYPE(VK_ANDROID_USER_AGENT, KATE_USER_AGENT);
    }

    @NonNull
    public static String USER_AGENT_ACCOUNT() {
        int account_id = Settings.get().accounts().getCurrent();
        if (account_id == ISettings.IAccountsSettings.INVALID_ID) {
            return Utils.BY_DEFAULT_ACCOUNT_TYPE(VK_ANDROID_USER_AGENT, KATE_USER_AGENT);
        }
        return getTypedUserAgent(Settings.get().accounts().getType(account_id));
    }

    @NonNull
    public static String USER_AGENT(@AccountType int type) {
        if (type != AccountType.BY_TYPE) {
            return getTypedUserAgent(type);
        }
        int account_id = Settings.get().accounts().getCurrent();
        if (account_id == ISettings.IAccountsSettings.INVALID_ID) {
            return Utils.BY_DEFAULT_ACCOUNT_TYPE(VK_ANDROID_USER_AGENT, KATE_USER_AGENT);
        }
        return getTypedUserAgent(Settings.get().accounts().getType(account_id));
    }
}
