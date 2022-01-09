package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import dev.ragnarok.fenrir.domain.IFeedbackInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAnswerVKOfficialView;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class AnswerVKOfficialPresenter extends AccountDependencyPresenter<IAnswerVKOfficialView> {

    private final AnswerVKOfficialList pages;

    private final IFeedbackInteractor fInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;

    public AnswerVKOfficialPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        pages = new AnswerVKOfficialList();
        pages.fields = new ArrayList<>();
        pages.items = new ArrayList<>();
        fInteractor = InteractorFactory.createFeedbackInteractor();

        loadActualData(0);
    }

    @Override
    public void onGuiCreated(@NonNull IAnswerVKOfficialView view) {
        super.onGuiCreated(view);
        view.displayData(pages);
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getOfficial(accountId, 100, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        callView(v -> showError(v, getCauseIfRuntime(t)));

        resolveRefreshingView();
    }

    private void onActualDataReceived(int offset, AnswerVKOfficialList data) {
        actualDataLoading = false;
        endOfContent = (data.items.size() < 100);
        actualDataReceived = true;

        if (offset == 0) {
            pages.items.clear();
            pages.fields.clear();
            pages.items.addAll(data.items);
            pages.fields.addAll(data.fields);
            callView(IAnswerVKOfficialView::notifyFirstListReceived);
        } else {
            int startSize = pages.items.size();
            pages.items.addAll(data.items);
            pages.fields.addAll(data.fields);
            callView(view -> view.notifyDataAdded(startSize, data.items.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.showRefreshing(actualDataLoading));
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && nonEmpty(pages.items) && actualDataReceived && !actualDataLoading) {
            loadActualData(pages.items.size());
            return false;
        }
        return true;
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }
}
