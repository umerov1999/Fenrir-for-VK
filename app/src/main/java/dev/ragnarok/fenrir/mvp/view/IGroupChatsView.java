package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.GroupChats;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IGroupChatsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(@NonNull List<GroupChats> chats);

    void notifyDataSetChanged();

    void notifyDataAdd(int position, int count);

    void showRefreshing(boolean refreshing);

    void setupLoadMore(@LoadMoreState int state);

    void goToChat(int accountId, int chat_id);
}
