package dev.ragnarok.fenrir.mvp.view.search;

import dev.ragnarok.fenrir.model.Message;


public interface IMessagesSearchView extends IBaseSearchView<Message> {

    void goToMessagesLookup(int accountId, int peerId, int messageId);
}
