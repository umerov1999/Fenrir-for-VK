package dev.ragnarok.fenrir.domain;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VkApiProfileInfo;
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse;
import dev.ragnarok.fenrir.model.Account;
import dev.ragnarok.fenrir.model.BannedPart;
import dev.ragnarok.fenrir.model.User;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IAccountsInteractor {
    Single<BannedPart> getBanned(int accountId, int count, int offset);

    Completable banUsers(int accountId, Collection<User> users);

    Completable unbanUser(int accountId, int userId);

    Completable changeStatus(int accountId, String status);

    Single<Boolean> setOffline(int accountId);

    Single<VkApiProfileInfo> getProfileInfo(int accountId);

    Single<List<PushSettingsResponse.ConversationsPush.ConversationPushItem>> getPushSettings(int accountId);

    Single<Integer> saveProfileInfo(int accountId, String first_name, String last_name, String maiden_name, String screen_name, String bdate, String home_town, Integer sex);

    Single<List<Account>> getAll(boolean refresh);
}