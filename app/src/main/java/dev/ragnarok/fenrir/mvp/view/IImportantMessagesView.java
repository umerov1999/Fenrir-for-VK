package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.model.Message;

public interface IImportantMessagesView extends IBasicMessageListView, IErrorView {
    void showRefreshing(boolean refreshing);

    void notifyDataAdded(int position, int count);

    void forwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void goToMessagesLookup(int accountId, int peerId, int messageId);
}
