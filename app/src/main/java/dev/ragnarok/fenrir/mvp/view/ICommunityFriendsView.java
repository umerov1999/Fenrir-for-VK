package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityFriendsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<Owner> users);

    void notifyItemRemoved(int position);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void openUserWall(int accountId, Owner user);

    void displayRefreshing(boolean refreshing);
}
