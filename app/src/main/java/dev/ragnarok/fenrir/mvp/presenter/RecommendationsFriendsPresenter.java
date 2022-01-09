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


public class RecommendationsFriendsPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final IRelationshipInteractor relationshipInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean actualDataLoading;
    private boolean doLoadTabs;

    public RecommendationsFriendsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
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
        requestActualData();
    }

    private void requestActualData() {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getRecommendations(accountId, 50)
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

        data.clear();
        data.addAll(users);
        callView(ISimpleOwnersView::notifyDataSetChanged);

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {

    }

    @Override
    void onUserRefreshed() {
        actualDataDisposable.clear();
        requestActualData();
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}
