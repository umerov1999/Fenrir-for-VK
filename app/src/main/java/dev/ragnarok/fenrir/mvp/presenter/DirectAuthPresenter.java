package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.trimmedNonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.Auth;
import dev.ragnarok.fenrir.api.CaptchaNeedException;
import dev.ragnarok.fenrir.api.NeedValidationException;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.LoginResponse;
import dev.ragnarok.fenrir.model.Captcha;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IDirectAuthView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;


public class DirectAuthPresenter extends RxSupportPresenter<IDirectAuthView> {

    private final INetworker networker;

    private Captcha requiredCaptcha;
    private boolean requireSmsCode;
    private boolean requireAppCode;
    private boolean savePassword;

    private boolean loginNow;

    private String username;
    private String pass;
    private String smsCode;
    private String captcha;
    private String appCode;
    private String RedirectUrl;

    public DirectAuthPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        networker = Injection.provideNetworkInterfaces();
    }

    public void fireLoginClick() {
        doLogin(false);
    }

    private void doLogin(boolean forceSms) {
        callView(IDirectAuthView::hideKeyboard);

        String trimmedUsername = nonEmpty(username) ? username.trim() : "";
        String trimmedPass = nonEmpty(pass) ? pass.trim() : "";
        String captchaSid = Objects.nonNull(requiredCaptcha) ? requiredCaptcha.getSid() : null;
        String captchaCode = nonEmpty(captcha) ? captcha.trim() : null;

        String code;

        if (requireSmsCode) {
            code = (nonEmpty(smsCode) ? smsCode.trim() : null);
        } else if (requireAppCode) {
            code = (nonEmpty(appCode) ? appCode.trim() : null);
        } else {
            code = null;
        }

        setLoginNow(true);
        appendDisposable(networker.vkDirectAuth()
                .directLogin("password", Constants.API_ID, Constants.SECRET,
                        trimmedUsername, trimmedPass, Constants.AUTH_VERSION, Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID,
                        Auth.getScope(), code, captchaSid, captchaCode, forceSms)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onLoginResponse, t -> onLoginError(getCauseIfRuntime(t))));
    }

    private void onLoginError(Throwable t) {
        setLoginNow(false);

        requiredCaptcha = null;
        requireAppCode = false;
        requireSmsCode = false;

        if (t instanceof CaptchaNeedException) {
            String sid = ((CaptchaNeedException) t).getSid();
            String img = ((CaptchaNeedException) t).getImg();
            requiredCaptcha = new Captcha(sid, img);
        } else if (t instanceof NeedValidationException) {
            if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE) {
                RedirectUrl = ((NeedValidationException) t).getValidationURL();
                if (!isEmpty(RedirectUrl)) {
                    onValidate();
                }
            } else {
                String type = ((NeedValidationException) t).getValidationType();
                String sid = ((NeedValidationException) t).getSid();

                if ("2fa_sms".equalsIgnoreCase(type) || "2fa_libverify".equalsIgnoreCase(type)) {
                    requireSmsCode = true;
                    RedirectUrl = ((NeedValidationException) t).getValidationURL();
                } else if ("2fa_app".equalsIgnoreCase(type)) {
                    requireAppCode = true;
                }
                if (!isEmpty(sid)) {
                    appendDisposable(networker.vkAuth()
                            .validatePhone(Constants.API_ID, Constants.API_ID, Constants.SECRET, sid, Constants.AUTH_VERSION)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(result -> {
                            }, ex -> callView(v -> showError(v, getCauseIfRuntime(t)))));
                }
            }
        } else {
            t.printStackTrace();
            callView(v -> showError(v, t));
        }

        resolveCaptchaViews();
        resolveSmsRootVisibility();
        resolveAppCodeRootVisibility();
        resolveButtonLoginState();

        if (Objects.nonNull(requiredCaptcha)) {
            callView(IDirectAuthView::moveFocusToCaptcha);
        } else if (requireSmsCode) {
            callView(IDirectAuthView::moveFocusToSmsCode);
        } else if (requireAppCode) {
            callView(IDirectAuthView::moveFocusToAppCode);
        }
    }

    private void resolveSmsRootVisibility() {
        callView(v -> v.setSmsRootVisible(requireSmsCode));
    }

    private void resolveAppCodeRootVisibility() {
        callView(v -> v.setAppCodeRootVisible(requireAppCode));
    }

    private void resolveCaptchaViews() {
        callView(v -> v.setCaptchaRootVisible(Objects.nonNull(requiredCaptcha)));

        if (Objects.nonNull(requiredCaptcha)) {
            callView(v -> v.displayCaptchaImage(requiredCaptcha.getImg()));
        }
    }

    private void onLoginResponse(LoginResponse response) {
        setLoginNow(false);

        String TwFa = "none";
        if (nonEmpty(response.access_token) && response.user_id > 0) {
            String Pass = nonEmpty(pass) ? pass.trim() : "";
            if (requireSmsCode)
                TwFa = "2fa_sms";
            else if (requireAppCode)
                TwFa = "2fa_app";
            String TwFafin = TwFa;
            callView(view -> view.returnSuccessToParent(response.user_id, response.access_token, nonEmpty(username) ? username.trim() : "", Pass, TwFafin, savePassword));
        }
    }

    public void onValidate() {
        callView(view -> view.returnSuccessValidation(RedirectUrl, nonEmpty(username) ? username.trim() : "", nonEmpty(pass) ? pass.trim() : "", "web_validation", savePassword));
    }

    private void setLoginNow(boolean loginNow) {
        this.loginNow = loginNow;
        resolveLoadingViews();
    }

    private void resolveLoadingViews() {
        callView(v -> v.displayLoading(loginNow));
    }

    @Override
    public void onGuiCreated(@NonNull IDirectAuthView view) {
        super.onGuiCreated(view);
        resolveLoadingViews();
        resolveSmsRootVisibility();
        resolveAppCodeRootVisibility();
        resolveCaptchaViews();
    }

    public void fireLoginViaWebClick() {
        callView(IDirectAuthView::returnLoginViaWebAction);
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveButtonLoginState();
    }

    private void resolveButtonLoginState() {
        callResumedView(v -> v.setLoginButtonEnabled(trimmedNonEmpty(username)
                && nonEmpty(pass)
                && (Objects.isNull(requiredCaptcha) || trimmedNonEmpty(captcha))
                && (!requireSmsCode || trimmedNonEmpty(smsCode))
                && (!requireAppCode || trimmedNonEmpty(appCode))));
    }

    public void fireLoginEdit(CharSequence sequence) {
        username = sequence.toString();
        resolveButtonLoginState();
    }

    public void firePasswordEdit(CharSequence s) {
        pass = s.toString();
        resolveButtonLoginState();
    }

    public void fireSmsCodeEdit(CharSequence sequence) {
        smsCode = sequence.toString();
        resolveButtonLoginState();
    }

    public void fireCaptchaEdit(CharSequence s) {
        captcha = s.toString();
        resolveButtonLoginState();
    }

    public void fireSaveEdit(boolean isSave) {
        savePassword = isSave;
    }

    public void fireButtonSendCodeViaSmsClick() {
        doLogin(true);
    }

    public void fireAppCodeEdit(CharSequence s) {
        appCode = s.toString();
        resolveButtonLoginState();
    }
}
