package dev.ragnarok.fenrir.mvp.view;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityManagersView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void notifyDataSetChanged();

    void displayRefreshing(boolean loadingNow);

    void displayData(List<Manager> managers);

    void goToManagerEditing(int accountId, int groupId, Manager manager);

    void showUserProfile(int accountId, User user);

    void startSelectProfilesActivity(int accountId, int groupId);

    void startAddingUsersToManagers(int accountId, int groupId, ArrayList<User> users);

    void notifyItemRemoved(int index);

    void notifyItemChanged(int index);

    void notifyItemAdded(int index);
}
