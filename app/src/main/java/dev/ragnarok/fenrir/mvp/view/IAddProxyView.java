package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IAddProxyView extends IMvpView, IErrorView {
    void setAuthFieldsEnabled(boolean enabled);

    void setAuthChecked(boolean checked);

    void goBack();
}
