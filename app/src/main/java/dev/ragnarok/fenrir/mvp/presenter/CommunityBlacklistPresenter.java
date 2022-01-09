package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.db.interfaces.IOwnersStorage;
import dev.ragnarok.fenrir.db.model.BanAction;
import dev.ragnarok.fenrir.domain.IGroupSettingsInteractor;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.impl.GroupSettingsInteractor;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityBlacklistView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class CommunityBlacklistPresenter extends AccountDependencyPresenter<ICommunityBlacklistView> {

    private static final int COUNT = 20;

    private final int groupId;
    private final List<Banned> data;

    private final IGroupSettingsInteractor groupSettingsInteractor;

    private boolean loadingNow;

    private IntNextFrom moreStartFrom;
    private boolean endOfContent;

    public CommunityBlacklistPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
        data = new ArrayList<>();
        moreStartFrom = new IntNextFrom(0);

        INetworker networker = Injection.provideNetworkInterfaces();
        IOwnersStorage repository = Injection.provideStores().owners();

        groupSettingsInteractor = new GroupSettingsInteractor(networker, repository, Repository.INSTANCE.getOwners());

        appendDisposable(repository.observeBanActions()
                .filter(action -> action.getGroupId() == groupId)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onBanActionReceived));

        requestDataAtStart();
    }

    private void onBanActionReceived(BanAction action) {
        if (action.isBan()) {
            //refresh data
            requestDataAtStart();
        } else {
            int index = Utils.findIndexByPredicate(data, banned -> banned.getBanned().getOwnerId() == action.getOwnerId());
            if (index != -1) {
                data.remove(index);
                callView(view -> view.notifyItemRemoved(index));
            }
        }
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callView(v -> v.displayRefreshing(loadingNow));
    }

    private void requestDataAtStart() {
        request(new IntNextFrom(0));
    }

    private void request(IntNextFrom startFrom) {
        if (loadingNow) return;

        int accountId = getAccountId();

        setLoadingNow(true);
        appendDisposable(groupSettingsInteractor.getBanned(accountId, groupId, startFrom, COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(pair -> onBannedUsersReceived(startFrom, pair.getSecond(), pair.getFirst()),
                        throwable -> onRequqestError(getCauseIfRuntime(throwable))));
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityBlacklistView view) {
        super.onGuiCreated(view);
        view.diplayData(data);
    }

    private void onRequqestError(Throwable throwable) {
        setLoadingNow(false);

        throwable.printStackTrace();
        callView(v -> showError(v, throwable));
    }

    private void onBannedUsersReceived(IntNextFrom startFrom, IntNextFrom nextFrom, List<Banned> users) {
        endOfContent = users.isEmpty();
        moreStartFrom = nextFrom;

        if (startFrom.getOffset() != 0) {
            int startSize = data.size();
            data.addAll(users);
            callView(view -> view.notifyItemsAdded(startSize, users.size()));
        } else {
            data.clear();
            data.addAll(users);
            callView(ICommunityBlacklistView::notifyDataSetChanged);
        }

        setLoadingNow(false);
    }

    public void fireRefresh() {
        requestDataAtStart();
    }

    public void fireBannedClick(Banned banned) {
        callView(v -> v.openBanEditor(getAccountId(), groupId, banned));
    }

    public void fireAddClick() {
        callView(v -> v.startSelectProfilesActivity(getAccountId(), groupId));
    }

    public void fireAddToBanUsersSelected(ArrayList<Owner> owners) {
        ArrayList<User> users = new ArrayList<>();
        for (Owner i : owners) {
            if (i instanceof User) {
                users.add((User) i);
            }
        }
        if (nonEmpty(users)) {
            callView(v -> v.addUsersToBan(getAccountId(), groupId, users));
        }
    }

    public void fireBannedRemoveClick(Banned banned) {
        appendDisposable(groupSettingsInteractor
                .unban(getAccountId(), groupId, banned.getBanned().getOwnerId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onUnbanComplete(banned), this::onUnbanError));
    }

    @SuppressWarnings("unused")
    private void onUnbanComplete(Banned banned) {
        callView(v -> v.showToast(R.string.deleted, false));
    }

    private void onUnbanError(Throwable throwable) {
        callView(v -> showError(v, throwable));
    }

    private boolean canLoadMore() {
        return !endOfContent && !loadingNow && nonEmpty(data) && moreStartFrom.getOffset() > 0;
    }

    public void fireScrollToBottom() {
        if (canLoadMore()) {
            request(moreStartFrom);
        }
    }
}