package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IVideosLocalServerView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Video> videos);

    void notifyListChanged();

    void notifyItemChanged(int index);

    void notifyDataAdded(int position, int count);

    void displayLoading(boolean loading);
}
