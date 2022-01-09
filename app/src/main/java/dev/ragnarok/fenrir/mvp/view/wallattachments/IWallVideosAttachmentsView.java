package dev.ragnarok.fenrir.mvp.view.wallattachments;

import java.util.List;

import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IWallVideosAttachmentsView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Video> videos);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void onSetLoadingStatus(int isLoad);
}
