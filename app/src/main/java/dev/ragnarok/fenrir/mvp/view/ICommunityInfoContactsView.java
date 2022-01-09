package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityInfoContactsView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void notifyDataSetChanged();

    void displayRefreshing(boolean loadingNow);

    void displayData(List<Manager> managers);

    void showUserProfile(int accountId, User user);

    void notifyItemRemoved(int index);

    void notifyItemChanged(int index);

    void notifyItemAdded(int index);
}
