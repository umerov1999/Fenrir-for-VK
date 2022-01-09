package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.DataWrapper;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunitiesView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(DataWrapper<Community> own, DataWrapper<Community> filtered, DataWrapper<Community> search);

    void notifyDataSetChanged();

    void notifyOwnDataAdded(int position, int count);

    void displayRefreshing(boolean refreshing);

    void showCommunityWall(int accountId, Community community);

    void notifySearchDataAdded(int position, int count);

    void showCommunityMenu(Community community);

    void showModCommunities(@Nullable List<Owner> add, @Nullable List<Owner> remove, int accountId);
}