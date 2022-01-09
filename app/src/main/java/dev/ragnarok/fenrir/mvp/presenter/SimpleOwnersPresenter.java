package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ISimpleOwnersView;


public abstract class SimpleOwnersPresenter<V extends ISimpleOwnersView> extends AccountDependencyPresenter<V> {

    final List<Owner> data;

    public SimpleOwnersPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        data = new ArrayList<>();
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);
        view.displayOwnerList(data);
    }

    public final void fireRefresh() {
        onUserRefreshed();
    }

    void onUserRefreshed() {

    }

    public final void fireScrollToEnd() {
        onUserScrolledToEnd();
    }

    void onUserScrolledToEnd() {

    }

    void onUserOwnerClicked(Owner owner) {
        callView(v -> v.showOwnerWall(getAccountId(), owner));
    }

    public final void fireOwnerClick(Owner owner) {
        onUserOwnerClicked(owner);
    }
}
