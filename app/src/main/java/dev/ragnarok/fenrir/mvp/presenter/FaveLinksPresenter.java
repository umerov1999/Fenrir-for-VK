package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IFaveLinksView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FaveLinksPresenter extends AccountDependencyPresenter<IFaveLinksView> {

    private static final int getCount = 50;
    private final IFaveInteractor faveInteractor;
    private final List<FaveLink> links;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable actualDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataReceived;
    private boolean cacheLoading;
    private boolean actualLoading;
    private boolean doLoadTabs;

    public FaveLinksPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        links = new ArrayList<>();
        faveInteractor = InteractorFactory.createFaveInteractor();

        loadCachedData();
    }

    private void loadCachedData() {
        cacheLoading = true;
        int accountId = getAccountId();
        cacheDisposable.add(faveInteractor.getCachedLinks(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private void loadActual(int offset) {
        actualLoading = true;
        int accountId = getAccountId();

        resolveRefreshingView();
        actualDisposable.add(faveInteractor.getLinks(accountId, getCount, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(data, offset), this::onActualGetError));
    }

    private void onActualGetError(Throwable t) {
        actualLoading = false;
        resolveRefreshingView();
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void onActualDataReceived(List<FaveLink> data, int offset) {
        cacheDisposable.clear();
        cacheLoading = false;

        actualLoading = false;
        endOfContent = Utils.safeCountOf(data) < getCount;
        actualDataReceived = true;

        if (offset == 0) {
            links.clear();
            links.addAll(data);
            callView(IFaveLinksView::notifyDataSetChanged);
        } else {
            int sizeBefore = links.size();
            links.addAll(data);
            callView(view -> view.notifyDataAdded(sizeBefore, data.size()));
        }

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        if (doLoadTabs) {
            return;
        } else {
            doLoadTabs = true;
        }
        loadActual(0);
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(actualLoading));
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        cacheLoading = false;

        actualDisposable.clear();
        loadActual(0);
    }

    public void fireScrollToEnd() {
        if (actualDataReceived && !endOfContent && !cacheLoading && !actualLoading && nonEmpty(links)) {
            loadActual(links.size());
        }
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        actualDisposable.dispose();
        super.onDestroyed();
    }

    private void onCachedDataReceived(List<FaveLink> links) {
        cacheLoading = false;

        this.links.clear();
        this.links.addAll(links);
        callView(IFaveLinksView::notifyDataSetChanged);
    }

    @Override
    public void onGuiCreated(@NonNull IFaveLinksView view) {
        super.onGuiCreated(view);
        view.displayLinks(links);
    }

    public void fireDeleteClick(FaveLink link) {
        int accountId = getAccountId();
        String id = link.getId();
        appendDisposable(faveInteractor.removeLink(accountId, id)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onLinkRemoved(accountId, id), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    private void onLinkRemoved(int accountId, String id) {
        if (getAccountId() != accountId) {
            return;
        }

        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).getId().equals(id)) {
                links.remove(i);

                int finalI = i;
                callView(view -> view.notifyItemRemoved(finalI));
                break;
            }
        }
    }

    public void fireAdd(Context context) {
        View root = View.inflate(context, R.layout.entry_link, null);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.enter_link)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> actualDisposable.add(faveInteractor.addLink(getAccountId(), ((TextInputEditText) root.findViewById(R.id.edit_link)).getText().toString().trim())
                        .compose(RxUtils.applyCompletableIOToMainSchedulers())
                        .subscribe(this::fireRefresh, t -> callView(v -> showError(v, getCauseIfRuntime(t))))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    public void fireLinkClick(FaveLink link) {
        callView(v -> v.openLink(getAccountId(), link));
    }
}