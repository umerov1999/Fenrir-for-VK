package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.mvp.core.IMvpView;

public interface ICreatePinView extends IMvpView, IErrorView {
    void displayTitle(@StringRes int titleRes);

    void displayErrorAnimation();

    void displayPin(int[] value, int noValue);

    void sendSuccessAndClose(int[] values);
}
