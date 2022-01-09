package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Peer;

public interface INotReadMessagesView extends IBasicMessageListView, IErrorView {

    void focusTo(int index);

    void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState);

    void forwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void showDeleteForAllDialog(ArrayList<Integer> ids);

    void doFinish(int incoming, int outgoing, boolean notAnim);

    void displayToolbarAvatar(Peer peer);

    void displayUnreadCount(int unreadCount);
}
