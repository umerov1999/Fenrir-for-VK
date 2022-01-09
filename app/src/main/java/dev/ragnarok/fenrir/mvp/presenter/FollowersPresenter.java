package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.domain.IAccountsInteractor;
import dev.ragnarok.fenrir.domain.IRelationshipInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.view.IFollowersView;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class FollowersPresenter extends SimpleOwnersPresenter<IFollowersView> {

    private final int userId;
    private final IRelationshipInteractor relationshipInteractor;
    private final IAccountsInteractor accountsInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final boolean isNotFriendShow;
    private boolean actualDataLoading;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean cacheLoadingNow;
    private boolean doLoadTabs;
    private List<Owner> not_followers;
    private List<Owner> add_followers;

    public FollowersPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        relationshipInteractor = InteractorFactory.createRelationshipInteractor();
        accountsInteractor = InteractorFactory.createAccountInteractor();
        isNotFriendShow = Settings.get().other().isNot_friend_show();
    }

    private void requestActualData(int offset, boolean do_scan) {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getFollowers(accountId, userId, isNotFriendShow ? 1000 : 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onActualDataReceived(offset, users, do_scan), this::onActualDataGetError));
    }

    public void removeFollower(Owner owner) {
        appendDisposable(accountsInteractor.banUsers(getAccountId(), Collections.singletonList((User) owner))
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    int pos = Utils.indexOfOwner(data, owner);
                    if (pos >= 0) {
                        data.remove(pos);
                        callView(v -> v.notifyRemoved(pos));
                    }
                }, RxUtils.ignore()));
    }

    @Override
    public void onGuiCreated(@NonNull IFollowersView view) {
        super.onGuiCreated(view);
        checkAndShowModificationFriends();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doLoadTabs) {
            return;
        } else {
            doLoadTabs = true;
        }
        loadAllCacheData();
        if (!isNotFriendShow) {
            requestActualData(0, false);
        }
    }

    private void resolveRefreshingView() {
        callView(v -> v.displayRefreshing(actualDataLoading));
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void checkAndShowModificationFriends() {
        if (!Utils.isEmpty(add_followers) || !Utils.isEmpty(not_followers)) {
            callView(view -> view.showModFollowers(add_followers, not_followers, getAccountId()));
        }
    }

    public void clearModificationFollowers(boolean add, boolean not) {
        if (add && !Utils.isEmpty(add_followers)) {
            add_followers.clear();
            add_followers = null;
        }
        if (not && !Utils.isEmpty(not_followers)) {
            not_followers.clear();
            not_followers = null;
        }
    }

    private void onActualDataReceived(int offset, List<User> users, boolean do_scan) {
        if (do_scan && isNotFriendShow) {
            not_followers = new ArrayList<>();
            for (Owner i : data) {
                if (Utils.indexOf(users, i.getOwnerId()) == -1) {
                    not_followers.add(i);
                }
            }
            add_followers = new ArrayList<>();
            for (User i : users) {
                if (Utils.indexOfOwner(data, i.getId()) == -1) {
                    add_followers.add(i);
                }
            }
            checkAndShowModificationFriends();
        }
        actualDataLoading = false;
        cacheDisposable.clear();

        actualDataReceived = true;
        endOfContent = users.isEmpty();

        if (offset == 0) {
            data.clear();
            data.addAll(users);
            callView(IFollowersView::notifyDataSetChanged);
        } else {
            int startSzie = data.size();
            data.addAll(users);
            callView(view -> view.notifyDataAdded(startSzie, users.size()));
        }

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !cacheLoadingNow && !actualDataLoading && actualDataReceived) {
            requestActualData(data.size(), false);
        }
    }

    @Override
    void onUserRefreshed() {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        actualDataDisposable.clear();
        requestActualData(0, false);
    }

    private void loadAllCacheData() {
        cacheLoadingNow = true;

        int accountId = getAccountId();
        cacheDisposable.add(relationshipInteractor.getCachedFollowers(accountId, userId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, this::onCacheDataGetError));
    }

    private void onCacheDataGetError(Throwable t) {
        cacheLoadingNow = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));
        if (isNotFriendShow) {
            requestActualData(0, false);
        }
    }

    private void onCachedDataReceived(List<User> users) {
        cacheLoadingNow = false;

        data.addAll(users);
        callView(IFollowersView::notifyDataSetChanged);
        if (isNotFriendShow) {
            requestActualData(0, users.size() > 0);
        }
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}
