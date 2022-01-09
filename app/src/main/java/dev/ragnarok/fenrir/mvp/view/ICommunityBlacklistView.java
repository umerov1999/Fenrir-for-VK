package dev.ragnarok.fenrir.mvp.view;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.Banned;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityBlacklistView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void displayRefreshing(boolean loadingNow);

    void notifyDataSetChanged();

    void diplayData(List<Banned> data);

    void notifyItemRemoved(int index);

    void openBanEditor(int accountId, int groupId, Banned banned);

    void startSelectProfilesActivity(int accountId, int groupId);

    void addUsersToBan(int accountId, int groupId, ArrayList<User> users);

    void notifyItemsAdded(int position, int size);
}
