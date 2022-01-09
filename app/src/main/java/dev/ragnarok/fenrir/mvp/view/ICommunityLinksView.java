package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityLinksView extends IAccountDependencyView, IErrorView, IMvpView {

    void displayRefreshing(boolean loadingNow);

    void notifyDataSetChanged();

    void displayData(List<VKApiCommunity.Link> links);

    void openLink(String link);
}
