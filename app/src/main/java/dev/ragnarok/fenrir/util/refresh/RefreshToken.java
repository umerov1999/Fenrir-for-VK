package dev.ragnarok.fenrir.util.refresh;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.util.Log;

import java.util.List;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;


public class RefreshToken {
    private static boolean upgradeTokenKate(int account, String oldToken) {
        if (Utils.isHiddenAccount(account)) {
            return false;
        }
        String gms = TokenModKate.requestToken();
        if (gms == null) {
            return false;
        }
        String token = Injection.provideNetworkInterfaces().vkDefault(account).account().refreshToken(gms, null, null, null).blockingGet().token;
        Log.w("refresh", oldToken + " " + token + " " + gms);
        if (oldToken.equals(token) || isEmpty(token)) {
            return false;
        }
        Settings.get().accounts().storeAccessToken(account, token);
        return true;
    }

    private static boolean upgradeTokenOfficial(int account, String oldToken) {
        if (Utils.isHiddenAccount(account)) {
            return false;
        }
        List<String> gms = TokenModOfficialVK.requestToken();
        if (gms == null) {
            return false;
        }
        long timestamp = System.currentTimeMillis();
        String token = Injection.provideNetworkInterfaces().vkDefault(account).account().refreshToken(gms.get(0), gms.get(1), TokenModOfficialVK.getNonce(timestamp), timestamp).blockingGet().token;
        Log.w("refresh", oldToken + " " + token + " " + gms);
        if (oldToken.equals(token) || isEmpty(token)) {
            return false;
        }
        Settings.get().accounts().storeAccessToken(account, token);
        return true;
    }

    public static boolean upgradeToken(int account, String oldToken) {
        if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
            return upgradeTokenKate(account, oldToken);
        } else if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID) {
            return upgradeTokenOfficial(account, oldToken);
        }
        return false;
    }

    public static Single<Boolean> upgradeTokenRx(int account, String oldToken) {
        return Single.create(v -> {
            try {
                upgradeToken(account, oldToken);
                v.onSuccess(true);
            } catch (Exception ignored) {
                v.onSuccess(false);
            }
        });
    }
}
