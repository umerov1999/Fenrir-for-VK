package dev.ragnarok.fenrir.api;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Random;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.activity.ValidateActivity;
import dev.ragnarok.fenrir.api.model.Captcha;
import dev.ragnarok.fenrir.api.model.Error;
import dev.ragnarok.fenrir.api.model.response.VkReponse;
import dev.ragnarok.fenrir.exception.UnauthorizedException;
import dev.ragnarok.fenrir.service.ApiErrorCodes;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.PersistentLogger;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.refresh.RefreshToken;
import io.reactivex.rxjava3.core.Completable;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;


abstract class AbsVkApiInterceptor implements Interceptor {

    private static final Random RANDOM = new Random();
    private final String version;
    private final Gson gson;

    AbsVkApiInterceptor(String version, Gson gson) {
        this.version = version;
        this.gson = gson;
    }

    protected abstract String getToken();

    protected abstract @AccountType
    int getType();

    protected abstract int getAccountId();

    /*
    private String RECEIPT_GMS_TOKEN() {
        try {
            GoogleApiAvailability instance = GoogleApiAvailability.getInstance();
            int isGooglePlayServicesAvailable = instance.isGooglePlayServicesAvailable(Injection.provideApplicationContext());
            if (isGooglePlayServicesAvailable != 0) {
                return null;
            }
            return FirebaseInstanceId.getInstance().getToken("54740537194", "id" + getAccountId());
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
     */

    @SuppressLint("CheckResult")
    private void startValidateActivity(Context context, String url) {
        Completable.complete()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(() -> {
                    Intent intent = ValidateActivity.createIntent(context, url);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });
    }


    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        String token = getToken();

        if (isEmpty(token)) {
            throw new UnauthorizedException("No authorization! Please, login and retry");
        }

        FormBody.Builder formBuilder = new FormBody.Builder();

        RequestBody body = original.body();

        boolean HasVersion = false;
        boolean HasDeviceId = false;
        if (body instanceof FormBody) {
            FormBody formBody = (FormBody) body;
            for (int i = 0; i < formBody.size(); i++) {
                String name = formBody.name(i);
                if (name.equals("v")) {
                    HasVersion = true;
                } else if (name.equals("device_id"))
                    HasDeviceId = true;
                String value = formBody.value(i);
                formBuilder.add(name, value);
            }
        }
        if (!HasVersion)
            formBuilder.add("v", version);

        formBuilder.add("access_token", token)
                .add("lang", Constants.DEVICE_COUNTRY_CODE)
                .add("https", "1");
        if (!HasDeviceId)
            formBuilder.add("device_id", Utils.getDeviceId(Injection.provideApplicationContext()));

        Request request = original.newBuilder()
                .method("POST", formBuilder.build())
                .build();

        Response response;
        ResponseBody responseBody;
        String responseBodyString;

        while (true) {
            response = chain.proceed(request);
            responseBody = response.body();
            assert responseBody != null;
            responseBodyString = responseBody.string();

            VkReponse vkReponse;
            try {
                vkReponse = gson.fromJson(responseBodyString, VkReponse.class);
            } catch (JsonSyntaxException ignored) {
                responseBodyString = "{ \"error\": { \"error_code\": -1, \"error_msg\": \"Internal json syntax error\" } }";
                return response.newBuilder().body(ResponseBody.create(responseBodyString, responseBody.contentType())).build();
            }

            Error error = isNull(vkReponse) ? null : vkReponse.error;

            if (nonNull(error)) {
                switch (error.errorCode) {
                    case ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND:
                        break;
                    case ApiErrorCodes.CAPTCHA_NEED:
                        if (Settings.get().other().isExtra_debug()) {
                            PersistentLogger.logThrowable("Captcha request", new Exception("URL: " + request.url() + ", dump: " + new Gson().toJson(error)));
                        }
                        break;
                    default:
                        if (Settings.get().other().isExtra_debug()) {
                            StringBuilder uu = new StringBuilder();
                            FormBody formBody = formBuilder.build();
                            boolean first = true;
                            for (int i = 0; i < formBody.size(); i++) {
                                String name = formBody.name(i);
                                String value = formBody.value(i);
                                if (first) {
                                    first = false;
                                } else {
                                    uu.append("; ");
                                }
                                uu.append(name).append("=").append(value);
                            }
                            PersistentLogger.logThrowable("ApiError", new Exception("Method: " + original.url() + ", code: " + error.errorCode + ", message: " + error.errorMsg + ", params: { " + uu + " }."));
                        }
                        break;
                }

                if (error.errorCode == ApiErrorCodes.TOO_MANY_REQUESTS_PER_SECOND) {
                    synchronized (AbsVkApiInterceptor.class) {
                        int sleepMs = 1000 + RANDOM.nextInt(500);
                        SystemClock.sleep(sleepMs);
                    }

                    continue;
                }

                if (error.errorCode == ApiErrorCodes.REFRESH_TOKEN || error.errorCode == ApiErrorCodes.CLIENT_VERSION_DEPRECATED) {
                    if (RefreshToken.upgradeToken(getAccountId(), getToken())) {
                        token = getToken();
                        formBuilder.add("access_token", token);

                        request = original.newBuilder()
                                .method("POST", formBuilder.build())
                                .build();
                        continue;
                    }
                }

                if (error.errorCode == ApiErrorCodes.VALIDATE_NEED) {
                    startValidateActivity(Injection.provideApplicationContext(), error.redirectUri);
                }

                if (error.errorCode == ApiErrorCodes.CAPTCHA_NEED) {
                    Captcha captcha = new Captcha(error.captchaSid, error.captchaImg);

                    ICaptchaProvider provider = Injection.provideCaptchaProvider();
                    provider.requestCaptha(captcha.getSid(), captcha);

                    String code = null;

                    while (true) {
                        try {
                            code = provider.lookupCode(captcha.getSid());

                            if (nonNull(code)) {
                                break;
                            } else {
                                SystemClock.sleep(1000);
                            }
                        } catch (OutOfDateException e) {
                            break;
                        }
                    }
                    if (Settings.get().other().isExtra_debug()) {
                        PersistentLogger.logThrowable("Captcha answer", new Exception("URL: " + request.url() + ", code: " + code + ", sid: " + captcha.getSid()));
                    }
                    if (nonNull(code)) {
                        formBuilder.add("captcha_sid", captcha.getSid());
                        formBuilder.add("captcha_key", code);

                        request = original.newBuilder()
                                .method("POST", formBuilder.build())
                                .build();
                        continue;
                    }
                }
            }
            break;
        }
        return response.newBuilder().body(ResponseBody.create(responseBodyString, responseBody.contentType())).build();
    }
}
