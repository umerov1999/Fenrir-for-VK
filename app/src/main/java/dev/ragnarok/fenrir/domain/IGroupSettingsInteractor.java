package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.ContactInfo;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IGroupSettingsInteractor {

    Single<GroupSettings> getGroupSettings(int accountId, int groupId);

    Completable ban(int accountId, int groupId, int ownerId, Long endDateUnixtime, int reason, String comment, boolean commentVisible);

    Completable editManager(int accountId, int groupId, User user, String role, boolean asContact, String position, String email, String phone);

    Completable unban(int accountId, int groupId, int ownerId);

    Single<Pair<List<Banned>, IntNextFrom>> getBanned(int accountId, int groupId, IntNextFrom startFrom, int count);

    Single<List<Manager>> getManagers(int accountId, int groupId);

    Single<List<ContactInfo>> getContacts(int accountId, int groupId);
}