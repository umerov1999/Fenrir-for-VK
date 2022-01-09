package dev.ragnarok.fenrir.mvp.presenter.search;

import static dev.ragnarok.fenrir.util.Utils.trimmedNonEmpty;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.fragment.search.criteria.MessageSeachCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.view.search.IMessagesSearchView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;

public class MessagesSearchPresenter extends AbsSearchPresenter<IMessagesSearchView, MessageSeachCriteria, Message, IntNextFrom> {

    private static final int COUNT = 50;
    private final IMessagesRepository messagesInteractor;

    public MessagesSearchPresenter(int accountId, @Nullable MessageSeachCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        messagesInteractor = Repository.INSTANCE.getMessages();

        if (canSearch(getCriteria())) {
            doSearch();
        }
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
    Single<Pair<List<Message>, IntNextFrom>> doSearch(int accountId, MessageSeachCriteria criteria, IntNextFrom nextFrom) {
        int offset = Objects.isNull(nextFrom) ? 0 : nextFrom.getOffset();
        return messagesInteractor
                .searchMessages(accountId, criteria.getPeerId(), COUNT, offset, criteria.getQuery())
                .map(messages -> Pair.Companion.create(messages, new IntNextFrom(offset + COUNT)));
    }

    @Override
    MessageSeachCriteria instantiateEmptyCriteria() {
        return new MessageSeachCriteria("");
    }

    @Override
    boolean canSearch(MessageSeachCriteria criteria) {
        return trimmedNonEmpty(criteria.getQuery());
    }

    public void fireMessageClick(Message message) {
        callView(v -> v.goToMessagesLookup(getAccountId(), message.getPeerId(), message.getId()));
    }
}
