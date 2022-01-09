package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.ICommunitiesInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityFriendsView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class CommunityFriendsPresenter extends AccountDependencyPresenter<ICommunityFriendsView> {

    private final int groupId;

    private final ICommunitiesInteractor communitiesInteractor;

    private final List<Owner> users;
    private final List<Owner> original;
    private boolean refreshing;
    private String query;

    public CommunityFriendsPresenter(int accountId, int groupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.groupId = groupId;
        users = new ArrayList<>();
        original = new ArrayList<>();
        communitiesInteractor = InteractorFactory.createCommunitiesInteractor();

        requestData();
    }

    public void setLoadingNow(boolean loadingNow) {
        refreshing = loadingNow;
        resolveRefreshing();
    }

    public void updateCriteria() {
        setLoadingNow(true);
        users.clear();
        if (Utils.isEmpty(query)) {
            users.addAll(original);
            setLoadingNow(false);
            callView(ICommunityFriendsView::notifyDataSetChanged);
            return;
        }
        for (Owner i : original) {
            if (i.getFullName().toLowerCase().contains(query.toLowerCase()) || i.getDomain().toLowerCase().contains(query.toLowerCase())) {
                users.add(i);
            }
        }
        setLoadingNow(false);
        callView(ICommunityFriendsView::notifyDataSetChanged);
    }

    public void fireQuery(String q) {
        if (Utils.isEmpty(q))
            query = null;
        else {
            query = q;
        }
        updateCriteria();
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityFriendsView view) {
        super.onGuiCreated(view);
        view.displayData(users);
    }

    private void resolveRefreshing() {
        callView(v -> v.displayRefreshing(refreshing));
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshing();
    }

    private void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        resolveRefreshing();
    }

    private void requestData() {
        int accountId = getAccountId();

        setRefreshing(true);
        appendDisposable(communitiesInteractor.getGroupFriends(accountId, groupId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        setRefreshing(false);
        callView(v -> showError(v, t));
    }

    private void onDataReceived(List<Owner> users) {
        setRefreshing(false);

        original.clear();
        original.addAll(users);
        updateCriteria();
    }

    public void fireRefresh() {
        if (!refreshing) {
            requestData();
        }
    }

    public void fireUserClick(Owner user) {
        callView(v -> v.openUserWall(getAccountId(), user));
    }
}
