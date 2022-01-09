package dev.ragnarok.fenrir.push;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.os.Build;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.FCMToken;
import dev.ragnarok.fenrir.api.ApiException;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.service.ApiErrorCodes;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.VkPushRegistration;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PushRegistrationResolver implements IPushRegistrationResolver {

    private static final String TAG = PushRegistrationResolver.class.getSimpleName();

    private final IDeviceIdProvider deviceIdProvider;
    private final ISettings settings;
    private final INetworker networker;

    public PushRegistrationResolver(IDeviceIdProvider deviceIdProvider, ISettings settings, INetworker networker) {
        this.deviceIdProvider = deviceIdProvider;
        this.settings = settings;
        this.networker = networker;
    }

    @Override
    public boolean canReceivePushNotification() {
        int accountId = settings.accounts().getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return false;
        }

        List<VkPushRegistration> available = settings.pushSettings().getRegistrations();
        boolean can = available.size() == 1 && available.get(0).getUserId() == accountId;

        Logger.d(TAG, "canReceivePushNotification, reason: " + String.valueOf(can).toUpperCase());
        return can;
    }

    @Override
    public Completable resolvePushRegistration() {
        return getInfo()
                .observeOn(Schedulers.io())
                .flatMapCompletable(data -> {
                    List<VkPushRegistration> available = settings.pushSettings().getRegistrations();

                    int accountId = settings.accounts().getCurrent();
                    boolean hasAuth = accountId != ISettings.IAccountsSettings.INVALID_ID;

                    if (!hasAuth && available.isEmpty()) {
                        Logger.d(TAG, "No auth, no regsitrations, OK");
                        return Completable.complete();
                    }

                    if (accountId <= 0 || settings.accounts().getType(settings.accounts().getCurrent()) != Constants.DEFAULT_ACCOUNT_TYPE)
                        return Completable.never();

                    Set<VkPushRegistration> needUnregister = new HashSet<>(0);

                    Optional<Integer> optionalAccountId = hasAuth ? Optional.wrap(accountId) : Optional.empty();

                    boolean hasOk = false;
                    boolean hasRemove = false;

                    for (VkPushRegistration registered : available) {
                        Reason reason = analizeRegistration(registered, data, optionalAccountId);

                        Logger.d(TAG, "Reason: " + reason);

                        switch (reason) {
                            case UNREGISTER_AND_REMOVE:
                                needUnregister.add(registered);
                                break;
                            case REMOVE:
                                hasRemove = true;
                                break;
                            case OK:
                                hasOk = true;
                                break;
                        }
                    }

                    if (hasAuth && hasOk && !hasRemove && needUnregister.isEmpty()) {
                        Logger.d(TAG, "Has auth, valid registration, OK");
                        return Completable.complete();
                    }

                    Completable completable = Completable.complete();

                    for (VkPushRegistration unreg : needUnregister) {
                        completable = completable.andThen(unregister(unreg));
                    }

                    List<VkPushRegistration> target = new ArrayList<>();

                    if (!hasOk && hasAuth) {
                        String vkToken = settings.accounts().getAccessToken(accountId);
                        VkPushRegistration current = new VkPushRegistration().set(accountId, data.deviceId, vkToken, data.gcmToken);
                        target.add(current);

                        completable = completable.andThen(register(current));
                    }

                    return completable
                            .doOnComplete(() -> settings.pushSettings().savePushRegistations(target))
                            .doOnComplete(() -> Logger.d(TAG, "Register success"))
                            .doOnError(throwable -> Logger.d(TAG, "Register error, t: " + throwable));
                });
    }

    private Completable register(VkPushRegistration registration) {
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
        String deviceModel = Utils.getDeviceName();
        //String osVersion = Utils.getAndroidVersion();

        if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
            return networker.vkManual(registration.getUserId(), registration.getVkToken())
                    .account()
                    .registerDevice(registration.getGmcToken(), null, null, "fcm",
                            null, null, deviceModel, registration.getDeviceId(), Build.VERSION.RELEASE, "{\"msg\":\"on\",\"chat\":\"on\",\"friend\":\"on\",\"reply\":\"on\",\"comment\":\"on\",\"mention\":\"on\",\"like\":\"off\"}")
                    .ignoreElement();
        } else {
            return networker.vkManual(registration.getUserId(), registration.getVkToken())
                    .account()
                    .registerDevice(registration.getGmcToken(), 1, Constants.VK_ANDROID_APP_VERSION_CODE, "fcm",
                            "vk_client", 4, deviceModel, registration.getDeviceId(), Build.VERSION.RELEASE, null)
                    .ignoreElement();
        }
        //} catch (JSONException e) {
        //return Completable.error(e);
        //}
    }

    private Completable unregister(VkPushRegistration registration) {
        return networker.vkManual(registration.getUserId(), registration.getVkToken())
                .account()
                .unregisterDevice(registration.getDeviceId())
                .ignoreElement()
                .onErrorResumeNext(t -> {
                    Throwable cause = getCauseIfRuntime(t);

                    if (cause instanceof ApiException && ((ApiException) cause).getError().errorCode == ApiErrorCodes.USER_AUTHORIZATION_FAILED) {
                        return Completable.complete();
                    }

                    return Completable.error(t);
                });
    }

    private Reason analizeRegistration(VkPushRegistration available, Data data, Optional<Integer> optionAccountId) {
        if (!data.deviceId.equals(available.getDeviceId())) {
            return Reason.REMOVE;
        }

        if (!data.gcmToken.equals(available.getGmcToken())) {
            return Reason.REMOVE;
        }

        if (optionAccountId.isEmpty()) {
            return Reason.UNREGISTER_AND_REMOVE;
        }

        int currentAccountId = optionAccountId.get();

        if (available.getUserId() != currentAccountId) {
            return Reason.UNREGISTER_AND_REMOVE;
        }

        String currentVkToken = settings.accounts().getAccessToken(currentAccountId);

        if (!available.getVkToken().equals(currentVkToken)) {
            return Reason.REMOVE;
        }

        return Reason.OK;
    }

    private Single<Data> getInfo() {
        return FCMToken.getFcmToken().flatMap(s -> {
            Data data = new Data(s, deviceIdProvider.getDeviceId());
            return Single.just(data);
        });
    }

    private enum Reason {
        OK, REMOVE, UNREGISTER_AND_REMOVE
    }

    private static final class Data {

        final String gcmToken;

        final String deviceId;

        Data(String gcmToken, String deviceId) {
            this.gcmToken = gcmToken;
            this.deviceId = deviceId;
        }
    }
}