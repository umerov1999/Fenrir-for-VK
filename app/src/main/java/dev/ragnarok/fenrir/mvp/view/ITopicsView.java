package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ITopicsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(@NonNull List<Topic> topics);

    void notifyDataSetChanged();

    void notifyDataAdd(int position, int count);

    void showRefreshing(boolean refreshing);

    void setupLoadMore(@LoadMoreState int state);

    void goToComments(int accountId, @NonNull Topic topic);

    void setButtonCreateVisible(boolean visible);
}