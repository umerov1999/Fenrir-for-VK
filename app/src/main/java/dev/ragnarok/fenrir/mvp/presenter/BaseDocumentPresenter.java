package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IDocsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IBasicDocumentView;
import dev.ragnarok.fenrir.util.RxUtils;


public class BaseDocumentPresenter<V extends IBasicDocumentView> extends AccountDependencyPresenter<V> {

    private final IDocsInteractor docsInteractor;

    public BaseDocumentPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        docsInteractor = InteractorFactory.createDocsInteractor();
    }

    public final void fireWritePermissionResolved(Context context, View view) {
        onWritePermissionResolved(context, view);
    }

    protected void onWritePermissionResolved(Context context, View view) {
        // hook for child classes
    }

    protected void addYourself(@NonNull Document document) {
        int accountId = getAccountId();
        int docId = document.getId();
        int ownerId = document.getOwnerId();

        appendDisposable(docsInteractor.add(accountId, docId, ownerId, document.getAccessKey())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(id -> onDocAddedSuccessfully(docId, ownerId, id), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
    }

    protected void delete(int id, int ownerId) {
        int accountId = getAccountId();
        appendDisposable(docsInteractor.delete(accountId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onDocDeleteSuccessfully(id, ownerId), this::onDocDeleteError));
    }

    private void onDocDeleteError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    @SuppressWarnings("unused")
    protected void onDocDeleteSuccessfully(int id, int ownerId) {
        callView(v -> v.showToast(R.string.deleted, true));
    }

    @SuppressWarnings("unused")
    protected void onDocAddedSuccessfully(int id, int ownerId, int resultDocId) {
        callView(v -> v.showToast(R.string.added, true));
    }
}