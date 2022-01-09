package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.mvp.core.IMvpView;

public interface IEnterPinView extends IMvpView, IErrorView, IToastView {
    void displayPin(int[] value, int noValue);

    void sendSuccessAndClose();

    void displayErrorAnimation();

    void displayAvatarFromUrl(@NonNull String url);

    void displayDefaultAvatar();

    void showBiometricPrompt();
}
