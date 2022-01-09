package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IFaveUsersView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<FavePage> pages);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void openOwnerWall(int accountId, Owner owner);

    void openMention(int accountId, Owner owner);

    void notifyItemRemoved(int index);
}
