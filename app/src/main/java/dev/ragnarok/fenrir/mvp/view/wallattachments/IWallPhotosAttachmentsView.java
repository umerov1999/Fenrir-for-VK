package dev.ragnarok.fenrir.mvp.view.wallattachments;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IWallPhotosAttachmentsView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Photo> photos);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index);

    void goToTempPhotosGallery(int accountId, long ptr, int index);

    void onSetLoadingStatus(int isLoad);
}
