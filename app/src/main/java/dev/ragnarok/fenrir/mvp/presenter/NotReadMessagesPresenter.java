package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.getSelected;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Mode;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.mvp.view.IBasicMessageListView;
import dev.ragnarok.fenrir.mvp.view.INotReadMessagesView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Observable;

public class NotReadMessagesPresenter extends AbsMessageListPresenter<INotReadMessagesView> {

    private static final int COUNT = 30;
    private final IMessagesRepository messagesInteractor;
    private final LOADING_STATE loadingState;
    private final Peer peer;
    private int mFocusMessageId;
    private int unreadCount;

    public NotReadMessagesPresenter(int accountId, int focusTo, int incoming, int outgoing, int unreadCount, @NonNull Peer peer, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        messagesInteractor = Repository.INSTANCE.getMessages();
        lastReadId.setIncoming(incoming);
        lastReadId.setOutgoing(outgoing);
        this.peer = peer;
        this.unreadCount = unreadCount;
        loadingState = new LOADING_STATE((Header, Footer) -> {

            @LoadMoreState int header;
            @LoadMoreState int footer;
            switch (Header) {
                case Side.LOADING:
                    header = LoadMoreState.LOADING;
                    break;
                case Side.NO_LOADING:
                    header = LoadMoreState.CAN_LOAD_MORE;
                    break;
                default:
                    header = LoadMoreState.INVISIBLE;
                    break;
            }
            switch (Footer) {
                case Side.DISABLED:
                    footer = LoadMoreState.END_OF_LIST;
                    break;
                case Side.LOADING:
                    footer = LoadMoreState.LOADING;
                    break;
                case Side.NO_LOADING:
                    footer = LoadMoreState.CAN_LOAD_MORE;
                    break;
                default:
                    footer = LoadMoreState.INVISIBLE;
                    break;
            }
            callView(v -> v.setupHeaders(footer, header));
        });

        if (savedInstanceState == null) {
            mFocusMessageId = focusTo;
            initRequest();
        }
    }

    @Override
    public void onGuiCreated(@NonNull INotReadMessagesView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayMessages(getData(), lastReadId);
        loadingState.updateState();

        resolveToolbar();
        resolveUnreadCount();
    }

    private void initRequest() {
        int accountId = getAccountId();

        appendDisposable(messagesInteractor.getPeerMessages(accountId, peer.getId(), COUNT, -COUNT / 2, mFocusMessageId, false, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitDataLoaded, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        loadingState.FooterDisable();
        loadingState.HeaderDisable();

        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onUpDataGetError(Throwable t) {
        loadingState.FooterEnable();
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onDownDataGetError(Throwable t) {
        loadingState.HeaderEnable();
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    public void fireDeleteForAllClick(ArrayList<Integer> ids) {
        deleteSentImpl(ids, true);
    }

    public void fireDeleteForMeClick(ArrayList<Integer> ids) {
        deleteSentImpl(ids, false);
    }

    private void deleteSentImpl(Collection<Integer> ids, boolean forAll) {
        appendDisposable(messagesInteractor.deleteMessages(getAccountId(), peer.getId(), ids, forAll, false)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), t -> callView(v -> showError(v, t))));
    }

    public void fireFooterLoadMoreClick() {
        loadMoreUp();
    }

    public void fireHeaderLoadMoreClick() {
        loadMoreDown();
    }

    private void loadMoreDown() {
        if (!loadingState.canLoadingHeader()) return;

        Integer firstMessageId = getFirstMessageId();
        if (firstMessageId == null) {
            return;
        }

        loadingState.headerLoading();

        int accountId = getAccountId();

        int targetMessageId = firstMessageId;

        appendDisposable(messagesInteractor.getPeerMessages(accountId, peer.getId(), COUNT, -COUNT, targetMessageId, false, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDownDataLoaded, this::onDownDataGetError));
    }

    @Override
    protected void onActionModeDeleteClick() {
        super.onActionModeDeleteClick();
        int accountId = getAccountId();

        List<Integer> ids = Observable.fromIterable(getData())
                .filter(Message::isSelected)
                .map(Message::getId)
                .toList()
                .blockingGet();

        if (nonEmpty(ids)) {
            appendDisposable(messagesInteractor.deleteMessages(accountId, peer.getId(), ids, false, false)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> onMessagesDeleteSuccessfully(ids), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
        }
    }

    @Override
    protected void onActionModeSpamClick() {
        super.onActionModeDeleteClick();
        int accountId = getAccountId();

        List<Integer> ids = Observable.fromIterable(getData())
                .filter(Message::isSelected)
                .map(Message::getId)
                .toList()
                .blockingGet();

        if (nonEmpty(ids)) {
            appendDisposable(messagesInteractor.deleteMessages(accountId, peer.getId(), ids, false, true)
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> onMessagesDeleteSuccessfully(ids), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
        }
    }

    private void loadMoreUp() {
        if (!loadingState.canLoadingFooter()) return;

        Integer lastMessageId = getLastMessageId();
        if (lastMessageId == null) {
            return;
        }

        loadingState.footerLoading();

        int targetLastMessageId = lastMessageId;
        int accountId = getAccountId();

        appendDisposable(messagesInteractor.getPeerMessages(accountId, peer.getId(), COUNT, 0, targetLastMessageId, false, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUpDataLoaded, this::onUpDataGetError));
    }

    private Integer getLastMessageId() {
        return isEmpty(getData()) ? null : getData().get(getData().size() - 1).getId();
    }

    private Integer getFirstMessageId() {
        return isEmpty(getData()) ? null : getData().get(0).getId();
    }

    @Override
    protected void onActionModeForwardClick() {
        super.onActionModeForwardClick();
        ArrayList<Message> selected = getSelected(getData());

        if (nonEmpty(selected)) {
            callView(v -> v.forwardMessages(getAccountId(), selected));
        }
    }

    @SuppressWarnings("unused")
    public void fireMessageRestoreClick(@NonNull Message message, int position) {
        int accountId = getAccountId();
        int id = message.getId();

        appendDisposable(messagesInteractor.restoreMessage(accountId, peer.getId(), id)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onMessageRestoredSuccessfully(id), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onMessageRestoredSuccessfully(int id) {
        Message message = findById(id);

        if (Objects.nonNull(message)) {
            message.setDeleted(false);
            safeNotifyDataChanged();
        }
    }

    private void onMessagesDeleteSuccessfully(Collection<Integer> ids) {
        for (Integer id : ids) {
            Message message = findById(id);

            if (Objects.nonNull(message)) {
                message.setDeleted(true);
            }
        }

        safeNotifyDataChanged();
    }

    private void onInitDataLoaded(List<Message> messages) {
        getData().clear();
        getData().addAll(messages);
        callView(IBasicMessageListView::notifyDataChanged);
        loadingState.reset();

        int index = Utils.indexOf(messages, mFocusMessageId);
        if (index != -1) {
            callView(v -> v.focusTo(index));
        } else if (mFocusMessageId == 0) {
            callView(v -> v.focusTo(messages.size() - 1));
        }
    }

    private void resolveToolbar() {
        callView(v -> v.displayToolbarAvatar(peer));
    }

    private void resolveUnreadCount() {
        callView(v -> v.displayUnreadCount(unreadCount));
    }

    @Override
    public void onMessageClick(@NonNull Message message) {
        readUnreadMessagesUpIfExists(message);
    }

    private void readUnreadMessagesUpIfExists(Message message) {
        if (Utils.isHiddenAccount(getAccountId())) return;

        if (!message.isOut() && message.getOriginalId() > lastReadId.getIncoming()) {
            lastReadId.setIncoming(message.getOriginalId());
            callView(IBasicMessageListView::notifyDataChanged);

            appendDisposable(messagesInteractor.markAsRead(getAccountId(), peer.getId(), message.getOriginalId())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(() -> appendDisposable(messagesInteractor.getConversationSingle(
                            getAccountId(),
                            peer.getId(), Mode.NET)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(e -> {
                                unreadCount = e.getUnreadCount();
                                resolveUnreadCount();
                            }, s -> callView(v -> showError(v, s)))), t -> callView(v -> showError(v, t))));
        }
    }

    private void onUpDataLoaded(List<Message> messages) {
        if (isEmpty(messages)) {
            loadingState.FooterDisable();
        } else {
            loadingState.FooterEnable();
        }
        int size = getData().size();

        getData().addAll(messages);
        callView(v -> v.notifyMessagesUpAdded(size, messages.size()));
    }

    public void fireFinish() {
        callView(v -> v.doFinish(lastReadId.getIncoming(), lastReadId.getOutgoing(), true));
    }

    private void onDownDataLoaded(List<Message> messages) {
        if (isEmpty(messages)) {
            callView(v -> v.doFinish(lastReadId.getIncoming(), lastReadId.getOutgoing(), false));
            loadingState.HeaderDisable();
        } else {
            loadingState.HeaderEnable();
        }
        getData().addAll(0, messages);
        callView(v -> v.notifyMessagesDownAdded(messages.size()));
    }

    @IntDef({Side.DISABLED, Side.NO_LOADING,
            Side.LOADING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Side {
        int DISABLED = 0;
        int NO_LOADING = 1;
        int LOADING = 2;
    }

    private static class LOADING_STATE {
        private final NotifyChanges changes;
        private @Side
        int Header = Side.DISABLED;
        private @Side
        int Footer = Side.DISABLED;

        public LOADING_STATE(@NonNull NotifyChanges changes) {
            this.changes = changes;
        }

        public void updateState() {
            changes.updateState(Header, Footer);
        }

        public void reset() {
            Header = Side.NO_LOADING;
            Footer = Side.NO_LOADING;
            updateState();
        }

        public void footerLoading() {
            Footer = Side.LOADING;
            updateState();
        }

        public void headerLoading() {
            Header = Side.LOADING;
            updateState();
        }

        public void FooterDisable() {
            Footer = Side.DISABLED;
            updateState();
        }

        public void HeaderEnable() {
            Header = Side.NO_LOADING;
            updateState();
        }

        public void FooterEnable() {
            Footer = Side.NO_LOADING;
            updateState();
        }

        public boolean canLoadingHeader() {
            return Header == Side.NO_LOADING && Footer != Side.LOADING;
        }

        public boolean canLoadingFooter() {
            return Footer == Side.NO_LOADING && Header != Side.LOADING;
        }

        public void HeaderDisable() {
            Header = Side.DISABLED;
            updateState();
        }

        public interface NotifyChanges {
            void updateState(@Side int Header, @Side int Footer);
        }
    }
}
