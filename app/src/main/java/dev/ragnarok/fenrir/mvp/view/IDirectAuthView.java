package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IDirectAuthView extends IMvpView, IErrorView {
    void setLoginButtonEnabled(boolean enabled);

    void setSmsRootVisible(boolean visible);

    void setAppCodeRootVisible(boolean visible);

    void moveFocusToSmsCode();

    void moveFocusToAppCode();

    void displayLoading(boolean loading);

    void setCaptchaRootVisible(boolean visible);

    void displayCaptchaImage(String img);

    void moveFocusToCaptcha();

    void hideKeyboard();

    void returnSuccessToParent(int userId, String accessToken, String Login, String Password, String twoFA, boolean isSave);

    void returnSuccessValidation(String url, String Login, String Password, String twoFA, boolean isSave);

    void returnLoginViaWebAction();
}
