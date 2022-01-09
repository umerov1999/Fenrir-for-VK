package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.feedback.Feedback;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IFeedbackView extends IAccountDependencyView, IMvpView, IAttachmentsPlacesView, IErrorView {
    void displayData(List<Feedback> data);

    void showLoading(boolean loading);

    void notifyDataAdding(int position, int count);

    void notifyFirstListReceived();

    void notifyDataSetChanged();

    void configLoadMore(@LoadMoreState int loadmoreState);

    void showLinksDialog(int accountId, @NonNull Feedback notification);
}
