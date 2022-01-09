package dev.ragnarok.fenrir.mvp.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IRelationshipInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFriendsByPhonesView;
import dev.ragnarok.fenrir.util.RxUtils;


public class FriendsByPhonesPresenter extends AccountDependencyPresenter<IFriendsByPhonesView> {
    private final List<Owner> data;
    private final IRelationshipInteractor friendsInteractor;
    private final Context context;
    private boolean netLoadingNow;

    public FriendsByPhonesPresenter(int accountId, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        friendsInteractor = InteractorFactory.createRelationshipInteractor();
        data = new ArrayList<>();
        this.context = context;

        requestActualData();
    }

    private void resolveRefreshingView() {
        callView(v -> v.displayLoading(netLoadingNow));
    }

    private void requestActualData() {
        netLoadingNow = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        appendDisposable(friendsInteractor.getByPhones(accountId, context)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualDataReceived, this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();

        callView(v -> showError(v, t));
    }

    private void onActualDataReceived(List<User> owners) {
        netLoadingNow = false;
        resolveRefreshingView();

        data.clear();
        data.addAll(owners);
        callView(IFriendsByPhonesView::notifyDataSetChanged);
    }

    @Override
    public void onGuiCreated(@NonNull IFriendsByPhonesView view) {
        super.onGuiCreated(view);
        view.displayData(data);

        resolveRefreshingView();
    }

    public void fireRefresh() {
        requestActualData();
    }

    public void onUserOwnerClicked(Owner owner) {
        callView(v -> v.showOwnerWall(getAccountId(), owner));
    }
}
