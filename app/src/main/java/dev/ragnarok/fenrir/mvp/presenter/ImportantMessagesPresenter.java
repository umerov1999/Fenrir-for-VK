package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.getSelected;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.view.IImportantMessagesView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ImportantMessagesPresenter extends AbsMessageListPresenter<IImportantMessagesView> {

    private final IMessagesRepository fInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;

    public ImportantMessagesPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        fInteractor = Repository.INSTANCE.getMessages();
        loadActualData(0);
    }


    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getImportantMessages(accountId, 50, offset, null)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataReceived(int offset, List<Message> data) {

        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;

        if (offset == 0) {
            getData().clear();
            getData().addAll(data);
            safeNotifyDataChanged();
        } else {
            int startSize = getData().size();
            getData().addAll(data);
            callView(view -> view.notifyDataAdded(startSize, data.size()));
        }

        resolveRefreshingView();
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    @Override
    protected void onActionModeForwardClick() {
        super.onActionModeForwardClick();
        ArrayList<Message> selected = getSelected(getData());

        if (nonEmpty(selected)) {
            callView(v -> v.forwardMessages(getAccountId(), selected));
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && nonEmpty(getData()) && actualDataReceived && !actualDataLoading) {
            loadActualData(getData().size());
            return false;
        }
        return true;
    }

    public void fireRemoveImportant(int position) {
        Message msg = getData().get(position);
        appendDisposable(fInteractor.markAsImportant(getAccountId(), msg.getPeerId(), Collections.singleton(msg.getId()), 0)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    getData().remove(position);
                    safeNotifyDataChanged();
                }, t -> {
                }));
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }

    public void fireMessagesLookup(@NonNull Message message) {
        callView(v -> v.goToMessagesLookup(getAccountId(), message.getPeerId(), message.getId()));
    }

    public void fireTranscript(String voiceMessageId, int messageId) {
        appendDisposable(fInteractor.recogniseAudioMessage(getAccountId(), messageId, voiceMessageId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(v -> {
                }, t -> {
                }));
    }
}
