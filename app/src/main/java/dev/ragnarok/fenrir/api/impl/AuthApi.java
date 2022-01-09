package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import com.google.gson.Gson;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.ApiException;
import dev.ragnarok.fenrir.api.AuthException;
import dev.ragnarok.fenrir.api.CaptchaNeedException;
import dev.ragnarok.fenrir.api.IDirectLoginSeviceProvider;
import dev.ragnarok.fenrir.api.NeedValidationException;
import dev.ragnarok.fenrir.api.interfaces.IAuthApi;
import dev.ragnarok.fenrir.api.model.LoginResponse;
import dev.ragnarok.fenrir.api.model.VkApiValidationResponce;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.HttpException;


public class AuthApi implements IAuthApi {

    private static final Gson BASE_RESPONSE_GSON = new Gson();
    private final IDirectLoginSeviceProvider service;

    public AuthApi(IDirectLoginSeviceProvider service) {
        this.service = service;
    }

    static <T> Function<BaseResponse<T>, T> extractResponseWithErrorHandling() {
        return response -> {
            if (nonNull(response.error)) {
                throw Exceptions.propagate(new ApiException(response.error));
            }

            return response.response;
        };
    }

    private static <T> SingleTransformer<T, T> withHttpErrorHandling() {
        return single -> single.onErrorResumeNext(throwable -> {

            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;

                try {
                    ResponseBody body = httpException.response().errorBody();
                    LoginResponse response = BASE_RESPONSE_GSON.fromJson(body.string(), LoginResponse.class);

                    //{"error":"need_captcha","captcha_sid":"846773809328","captcha_img":"https:\/\/api.vk.com\/captcha.php?sid=846773809328"}

                    if ("need_captcha".equalsIgnoreCase(response.error)) {
                        return Single.error(new CaptchaNeedException(response.captchaSid, response.captchaImg));
                    }

                    if ("need_validation".equalsIgnoreCase(response.error)) {
                        return Single.error(new NeedValidationException(response.validationType, response.redirect_uri, response.validation_sid, response.phoneMask));
                    }

                    if (nonEmpty(response.error)) {
                        return Single.error(new AuthException(response.error, response.errorDescription));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return Single.error(throwable);
        });
    }

    @Override
    public Single<LoginResponse> directLogin(String grantType, int clientId, String clientSecret,
                                             String username, String pass, String v, boolean twoFaSupported,
                                             String scope, String code, String captchaSid, String captchaKey, boolean forceSms) {
        return service.provideAuthService()
                .flatMap(service -> service
                        .directLogin(grantType, clientId, clientSecret, username, pass, v, twoFaSupported ? 1 : null, scope, code, captchaSid, captchaKey, forceSms ? 1 : 0, Utils.getDeviceId(Injection.provideApplicationContext()), 1)
                        .compose(withHttpErrorHandling()));
    }

    @Override
    public Single<VkApiValidationResponce> validatePhone(int apiId, int clientId, String clientSecret, String sid, String v) {
        return service.provideAuthService()
                .flatMap(service -> service
                        .validatePhone(apiId, clientId, clientSecret, sid, v)
                        .map(extractResponseWithErrorHandling()));
    }
}
