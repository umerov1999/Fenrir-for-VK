package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.IRelationshipInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class OnlineFriendsPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final int userId;
    private final IRelationshipInteractor relationshipInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataLoading;
    private boolean doLoadTabs;
    private int offset;

    public OnlineFriendsPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        relationshipInteractor = InteractorFactory.createRelationshipInteractor();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(actualDataLoading));
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
        offset = 0;
        requestActualData();
    }

    private void requestActualData() {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getOnlineFriends(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        actualDataLoading = false;
        resolveRefreshingView();

        callView(v -> showError(v, t));
    }

    private void onDataReceived(List<User> users) {
        actualDataLoading = false;

        endOfContent = users.isEmpty();

        if (offset == 0) {
            data.clear();
            data.addAll(users);
            callView(ISimpleOwnersView::notifyDataSetChanged);
        } else {
            int sizeBefore = data.size();
            data.addAll(users);
            callView(view -> view.notifyDataAdded(sizeBefore, users.size()));
        }

        resolveRefreshingView();
        offset += 200;
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !actualDataLoading && offset > 0) {
            requestActualData();
        }
    }

    @Override
    void onUserRefreshed() {
        actualDataDisposable.clear();
        offset = 0;
        requestActualData();
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}