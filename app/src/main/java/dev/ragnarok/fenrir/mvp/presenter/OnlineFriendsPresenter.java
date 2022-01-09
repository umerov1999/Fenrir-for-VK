package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

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
        requestActualData(0);
    }

    private void requestActualData(int offset) {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getOnlineFriends(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onDataReceived(offset, users), this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        actualDataLoading = false;
        resolveRefreshingView();

        callView(v -> showError(v, t));
    }

    private void onDataReceived(int offset, List<User> users) {
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
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !actualDataLoading && nonEmpty(data)) {
            requestActualData(data.size());
        }
    }

    @Override
    void onUserRefreshed() {
        actualDataDisposable.clear();
        requestActualData(0);
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}