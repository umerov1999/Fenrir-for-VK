package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfo;
import dev.ragnarok.fenrir.api.model.response.PushSettingsResponse;
import dev.ragnarok.fenrir.db.column.UserColumns;
import dev.ragnarok.fenrir.domain.IAccountsInteractor;
import dev.ragnarok.fenrir.domain.IBlacklistRepository;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.model.Account;
import dev.ragnarok.fenrir.model.BannedPart;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.ISettings;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class AccountsInteractor implements IAccountsInteractor {

    private final INetworker networker;
    private final ISettings.IAccountsSettings settings;
    private final IOwnersRepository ownersRepository;
    private final IBlacklistRepository blacklistRepository;

    public AccountsInteractor(INetworker networker, ISettings.IAccountsSettings settings, IBlacklistRepository blacklistRepository, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.settings = settings;
        this.blacklistRepository = blacklistRepository;
        this.ownersRepository = ownersRepository;
    }

    @Override
    public Single<BannedPart> getBanned(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .account()
                .getBanned(count, offset, UserColumns.API_FIELDS)
                .map(items -> {
                    List<VKApiUser> dtos = listEmptyIfNull(items.profiles);
                    List<User> users = Dto2Model.transformUsers(dtos);
                    return new BannedPart(users);
                });
    }

    @Override
    public Completable banUsers(int accountId, Collection<User> users) {
        Completable completable = Completable.complete();

        for (User user : users) {
            completable = completable.andThen(networker.vkDefault(accountId)
                    .account()
                    .banUser(user.getId()))
                    .delay(1, TimeUnit.SECONDS) // чтобы не дергало UI
                    .ignoreElement()
                    .andThen(blacklistRepository.fireAdd(accountId, user));
        }

        return completable;
    }

    @Override
    public Completable unbanUser(int accountId, int userId) {
        return networker.vkDefault(accountId)
                .account()
                .unbanUser(userId)
                .delay(1, TimeUnit.SECONDS) // чтобы не дергало UI
                .ignoreElement()
                .andThen(blacklistRepository.fireRemove(accountId, userId));
    }

    @Override
    public Completable changeStatus(int accountId, String status) {
        return networker.vkDefault(accountId)
                .status()
                .set(status, null)
                .flatMapCompletable(ignored -> ownersRepository.handleStatusChange(accountId, accountId, status));
    }

    @Override
    public Single<Boolean> setOffline(int accountId) {
        return networker.vkDefault(accountId)
                .account()
                .setOffline();
    }

    @Override
    public Single<VkApiProfileInfo> getProfileInfo(int accountId) {
        return networker.vkDefault(accountId)
                .account()
                .getProfileInfo();
    }

    @Override
    public Single<List<PushSettingsResponse.ConversationsPush.ConversationPushItem>> getPushSettings(int accountId) {
        return networker.vkDefault(accountId)
                .account()
                .getPushSettings().map(PushSettingsResponse::getPushSettings);
    }

    @Override
    public Single<Integer> saveProfileInfo(int accountId, String first_name, String last_name, String maiden_name, String screen_name, String bdate, String home_town, Integer sex) {
        return networker.vkDefault(accountId)
                .account()
                .saveProfileInfo(first_name, last_name, maiden_name, screen_name, bdate, home_town, sex)
                .map(t -> t.status);
    }

    @Override
    public Single<List<Account>> getAll(boolean refresh) {
        return Single.create(emitter -> {
            Collection<Integer> ids = settings.getRegistered();

            List<Account> accounts = new ArrayList<>(ids.size());

            for (int id : ids) {
                if (emitter.isDisposed()) {
                    break;
                }

                Owner owner = ownersRepository.getBaseOwnerInfo(id, id, refresh ? IOwnersRepository.MODE_NET : IOwnersRepository.MODE_ANY)
                        .onErrorReturn(ignored -> id > 0 ? new User(id) : new Community(-id))
                        .blockingGet();

                Account account = new Account(id, owner);
                accounts.add(account);
            }

            emitter.onSuccess(accounts);
        });
    }
}