package dev.ragnarok.fenrir.mvp.view.conversations;

import java.util.List;

import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IAttachmentsPlacesView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IBaseChatAttachmentsView<T> extends IMvpView, IAccountDependencyView,
        IAttachmentsPlacesView, IErrorView {

    void displayAttachments(List<T> data);

    void notifyDataAdded(int position, int count);

    void notifyDatasetChanged();

    void showLoading(boolean loading);

    void setEmptyTextVisible(boolean visible);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);
}
