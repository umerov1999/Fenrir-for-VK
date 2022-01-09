package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IBasicDocumentView extends IMvpView, IAccountDependencyView, IToastView, IErrorView {

    void shareDocument(int accountId, @NonNull Document document);

    void requestWriteExternalStoragePermission();

}
