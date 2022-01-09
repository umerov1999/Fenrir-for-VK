package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IRequestExecuteView extends IMvpView, IErrorView, IProgressView, IAccountDependencyView, IToastView {
    void displayBody(String body);

    void hideKeyboard();

    void requestWriteExternalStoragePermission();
}