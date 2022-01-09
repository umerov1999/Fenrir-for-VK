package dev.ragnarok.fenrir.mvp.view;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IFavePhotosView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<Photo> photos);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void goToGallery(int accountId, ArrayList<Photo> photos, int position);
}