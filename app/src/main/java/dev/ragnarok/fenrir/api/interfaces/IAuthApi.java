package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.LoginResponse;
import dev.ragnarok.fenrir.api.model.VkApiValidationResponce;
import io.reactivex.rxjava3.core.Single;


public interface IAuthApi {
    Single<LoginResponse> directLogin(String grantType, int clientId, String clientSecret,
                                      String username, String pass, String v, boolean twoFaSupported,
                                      String scope, String code, String captchaSid, String captchaKey, boolean forceSms);

    Single<VkApiValidationResponce> validatePhone(int apiId, int clientId, String clientSecret, String sid, String v);
}