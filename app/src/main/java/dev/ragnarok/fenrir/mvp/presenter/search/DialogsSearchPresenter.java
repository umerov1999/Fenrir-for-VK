package dev.ragnarok.fenrir.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.fragment.search.criteria.DialogsSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Conversation;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.mvp.view.search.IDialogsSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class DialogsSearchPresenter extends AbsSearchPresenter<IDialogsSearchView, DialogsSearchCriteria, Conversation, IntNextFrom> {

    private final IMessagesRepository messagesInteractor;

    public DialogsSearchPresenter(int accountId, @Nullable DialogsSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        messagesInteractor = Repository.INSTANCE.getMessages();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Conversation>, IntNextFrom>> doSearch(int accountId, DialogsSearchCriteria criteria, IntNextFrom startFrom) {
        return messagesInteractor.searchConversations(accountId, 255, criteria.getQuery())
                .map(models -> Pair.Companion.create(models, null));
        // null because load more not supported
    }

    @Override
    DialogsSearchCriteria instantiateEmptyCriteria() {
        return new DialogsSearchCriteria("");
    }

    @Override
    boolean canSearch(DialogsSearchCriteria criteria) {
        return Utils.trimmedNonEmpty(criteria.getQuery());
    }

    public void fireEntryClick(Conversation o) {
        int accountId = getAccountId();
        int messagesOwnerId = getAccountId(); // todo Community dialogs search !!!

        Peer peer = new Peer(Peer.fromOwnerId(o.getId())).setTitle(o.getTitle()).setAvaUrl(o.getMaxSquareAvatar());
        callView(v -> v.openChatWith(accountId, messagesOwnerId, peer));
    }
}