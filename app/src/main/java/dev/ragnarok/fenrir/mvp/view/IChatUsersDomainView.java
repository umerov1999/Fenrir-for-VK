package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IChatUsersDomainView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<AppChatUser> users);

    void notifyItemRemoved(int position);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void openUserWall(int accountId, Owner user);

    void addDomain(int accountId, Owner user);

    void displayRefreshing(boolean refreshing);
}
