package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.NewsfeedComment;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface INewsfeedCommentsView extends IAccountDependencyView, IAttachmentsPlacesView, IMvpView, IErrorView {
    void displayData(List<NewsfeedComment> data);

    void notifyDataAdded(int position, int count);

    void notifyDataSetChanged();

    void showLoading(boolean loading);
}
