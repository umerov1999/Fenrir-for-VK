package dev.ragnarok.fenrir.settings;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.push.IPushRegistrationResolver;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.PublishProcessor;

class AccountsSettings implements ISettings.IAccountsSettings {

    private static final String KEY_ACCOUNT_UIDS = "account_uids";
    private static final String KEY_CURRENT = "current_account_id";

    private final Context app;
    private final PublishProcessor<ISettings.IAccountsSettings> changesPublisher = PublishProcessor.create();
    private final SharedPreferences preferences;
    private final Map<Integer, String> tokens;
    private final Map<Integer, Integer> types;
    private final Map<Integer, String> devices;
    private final Set<String> accounts;
    private final PublishProcessor<Integer> currentPublisher = PublishProcessor.create();

    @SuppressLint("UseSparseArrays")
    AccountsSettings(Context context) {
        app = context.getApplicationContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        tokens = Collections.synchronizedMap(new HashMap<>(1));
        types = Collections.synchronizedMap(new HashMap<>(1));
        devices = Collections.synchronizedMap(new HashMap<>(1));
        accounts = Collections.synchronizedSet(new HashSet<>(preferences.getStringSet(KEY_ACCOUNT_UIDS, new HashSet<>(1))));

        Collection<Integer> aids = getRegistered();
        for (Integer aid : aids) {
            String token = preferences.getString(tokenKeyFor(aid), null);

            if (nonEmpty(token)) {
                tokens.put(aid, token);
            }
            String device = preferences.getString(deviceKeyFor(aid), null);
            if (nonEmpty(device)) {
                devices.put(aid, device);
            }
            types.put(aid, preferences.getInt(typeAccKeyFor(aid), Constants.DEFAULT_ACCOUNT_TYPE));

        }

    }

    private static String tokenKeyFor(int uid) {
        return "token" + uid;
    }

    private static String loginKeyFor(int uid) {
        return "login" + uid;
    }

    private static String deviceKeyFor(int uid) {
        return "device" + uid;
    }

    private static String typeAccKeyFor(int uid) {
        return "account_type" + uid;
    }

    private void notifyAboutRegisteredChanges() {
        changesPublisher.onNext(this);
    }

    @Override
    public Flowable<ISettings.IAccountsSettings> observeRegistered() {
        return changesPublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<Integer> observeChanges() {
        return currentPublisher.onBackpressureBuffer();
    }

    @NonNull
    @Override
    public List<Integer> getRegistered() {
        List<Integer> ids = new ArrayList<>(accounts.size());
        for (String stringuid : accounts) {
            int uid = Integer.parseInt(stringuid);
            ids.add(uid);
        }

        return ids;
    }

    private void fireAccountChange() {
        IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();
        registrationResolver.resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore());

        currentPublisher.onNext(getCurrent());
    }

    @Override
    public int getCurrent() {
        return preferences.getInt(KEY_CURRENT, INVALID_ID);
    }

    @Override
    public void setCurrent(int accountId) {
        if (getCurrent() == accountId) return;

        PreferenceManager.getDefaultSharedPreferences(app)
                .edit()
                .putInt(KEY_CURRENT, accountId)
                .apply();
        fireAccountChange();
    }

    @Override
    public void remove(int accountId) {
        int currentAccountId = getCurrent();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);

        accounts.remove(String.valueOf(accountId));
        preferences.edit()
                .putStringSet(KEY_ACCOUNT_UIDS, accounts)
                .apply();

        if (accountId == currentAccountId) {
            List<Integer> accountIds = getRegistered();

            Integer fisrtUserAccountId = null;

            // делаем активным первый аккаунт ПОЛЬЗОВАТЕЛЯ
            for (Integer existsId : accountIds) {
                if (existsId > 0) {
                    fisrtUserAccountId = existsId;
                    break;
                }
            }

            if (nonNull(fisrtUserAccountId)) {
                preferences.edit()
                        .putInt(KEY_CURRENT, fisrtUserAccountId)
                        .apply();
            } else {
                preferences.edit()
                        .remove(KEY_CURRENT)
                        .apply();
            }
        }

        notifyAboutRegisteredChanges();
        fireAccountChange();
    }

    @Override
    public void registerAccountId(int accountId, boolean setCurrent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);
        accounts.add(String.valueOf(accountId));

        SharedPreferences.Editor editor = preferences.edit();

        editor.putStringSet(KEY_ACCOUNT_UIDS, accounts);

        if (setCurrent) {
            editor.putInt(KEY_CURRENT, accountId);
        }

        editor.apply();

        notifyAboutRegisteredChanges();

        if (setCurrent) {
            fireAccountChange();
        }
    }

    @Override
    public void storeAccessToken(int accountId, String accessToken) {
        tokens.put(accountId, accessToken);
        preferences.edit()
                .putString(tokenKeyFor(accountId), accessToken)
                .apply();
    }

    @Override
    public void storeLogin(int accountId, String loginCombo) {
        preferences.edit()
                .putString(loginKeyFor(accountId), loginCombo)
                .apply();
    }

    @Override
    public void storeDevice(int accountId, String deviceName) {
        if (Utils.isEmpty(deviceName)) {
            removeDevice(accountId);
            return;
        }
        devices.put(accountId, deviceName);
        preferences.edit()
                .putString(deviceKeyFor(accountId), deviceName)
                .apply();
    }

    @Override
    public String getLogin(int accountId) {
        return preferences.getString(loginKeyFor(accountId), null);
    }

    @Override
    public void storeTokenType(int accountId, @AccountType int type) {
        types.put(accountId, type);
        preferences.edit()
                .putInt(typeAccKeyFor(accountId), type)
                .apply();
    }

    @Override
    public String getAccessToken(int accountId) {
        return tokens.get(accountId);
    }

    @Override
    public @AccountType
    int getType(int accountId) {
        if (types.containsKey(accountId)) {
            Integer ret = types.get(accountId);
            if (nonNull(ret)) {
                return ret;
            }
        }
        return Constants.DEFAULT_ACCOUNT_TYPE;
    }

    @Override
    @Nullable
    public String getDevice(int accountId) {
        if (devices.containsKey(accountId)) {
            return devices.get(accountId);
        }
        return null;
    }

    @Override
    public void removeAccessToken(int accountId) {
        tokens.remove(accountId);
        preferences.edit()
                .remove(tokenKeyFor(accountId))
                .apply();
    }

    @Override
    public void removeType(int accountId) {
        types.remove(accountId);
        preferences.edit()
                .remove(typeAccKeyFor(accountId))
                .apply();
    }

    @Override
    public void removeLogin(int accountId) {
        preferences.edit()
                .remove(loginKeyFor(accountId))
                .apply();
    }

    @Override
    public void removeDevice(int accountId) {
        devices.remove(accountId);
        preferences.edit()
                .remove(deviceKeyFor(accountId))
                .apply();
    }
}
