package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.model.FriendsCounters;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IFriendsTabsView extends IMvpView, IAccountDependencyView, IErrorView {
    void displayConters(FriendsCounters counters);

    void configTabs(int accountId, int userId, boolean isNotMyPage);

    void displayUserNameAtToolbar(String userName);

    void setDrawerFriendsSectionSelected(boolean selected);
}