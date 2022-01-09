package dev.ragnarok.fenrir.activity;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.Auth;
import dev.ragnarok.fenrir.api.util.VKStringUtils;
import dev.ragnarok.fenrir.model.Token;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Utils;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String EXTRA_CLIENT_ID = "client_id";
    private static final String EXTRA_SCOPE = "scope";
    private static final String EXTRA_VALIDATE = "validate";
    private static final String EXTRA_LOGIN = "login";
    private static final String EXTRA_PASSWORD = "password";
    private static final String EXTRA_TWO_FA = "two_fa";
    private static final String EXTRA_SAVE = "save";
    private static final String EXTRA_GROUP_IDS = "group_ids";
    private String TLogin;
    private String TPassword;
    private String TwoFA;
    private boolean isSave;

    public static Intent createIntent(Context context, String clientId, String scope) {
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_CLIENT_ID, clientId)
                .putExtra(EXTRA_SCOPE, scope);
    }

    public static Intent createIntent(Context context, String validate_url, String Login, String Password, String TwoFa, boolean isSave) {
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_VALIDATE, validate_url).putExtra(EXTRA_LOGIN, Login)
                .putExtra(EXTRA_PASSWORD, Password).putExtra(EXTRA_TWO_FA, TwoFa)
                .putExtra(EXTRA_SAVE, isSave);
    }

    public static Intent createIntent(Context context, String clientId, String scope, Collection<Integer> groupIds) {
        String ids = Utils.join(groupIds, ",", Object::toString);
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_CLIENT_ID, clientId)
                .putExtra(EXTRA_SCOPE, scope)
                .putExtra(EXTRA_GROUP_IDS, ids);
    }

    private static String tryExtractAccessToken(String url) {
        return VKStringUtils.extractPattern(url, "access_token=(.*?)&");
    }

    private static ArrayList<Token> tryExtractAccessTokens(String url) throws Exception {
        Pattern p = Pattern.compile("access_token_(\\d*)=(.*?)(&|$)");

        ArrayList<Token> tokens = new ArrayList<>();

        Matcher matcher = p.matcher(url);
        while (matcher.find()) {
            String groupid = matcher.group(1);
            String token = matcher.group(2);

            if (nonEmpty(groupid) && nonEmpty(token)) {
                tokens.add(new Token(-Integer.parseInt(groupid), token));
            }
        }

        if (tokens.isEmpty()) {
            throw new Exception("Failed to parse redirect url " + url);
        }

        return tokens;
    }

    private static String tryExtractUserId(String url) {
        return VKStringUtils.extractPattern(url, "user_id=(\\d*)");
    }

    public static ArrayList<Token> extractGroupTokens(Intent data) {
        return data.getParcelableArrayListExtra("group_tokens");
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemesController.INSTANCE.currentStyle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        WebView webview = findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);
        webview.getSettings().setUserAgentString(Constants.USER_AGENT(Constants.DEFAULT_ACCOUNT_TYPE));

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(aBoolean -> Log.d(TAG, "Cookie removed: " + aBoolean));

        if (getIntent().getStringExtra(EXTRA_VALIDATE) == null) {
            String clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
            String scope = getIntent().getStringExtra(EXTRA_SCOPE);
            String groupIds = getIntent().getStringExtra(EXTRA_GROUP_IDS);

            try {
                if (!Utils.isEmpty(groupIds)) {
                    webview.getSettings().setUserAgentString(Constants.KATE_USER_AGENT);
                }
                String url = Auth.getUrl(clientId, scope, groupIds);
                webview.loadUrl(url);
            } catch (UnsupportedEncodingException e) {
                CustomToast.CreateCustomToast(this).showToastError(e.getLocalizedMessage());
            }
        } else {
            TLogin = getIntent().getStringExtra(EXTRA_LOGIN);
            TPassword = getIntent().getStringExtra(EXTRA_PASSWORD);
            TwoFA = getIntent().getStringExtra(EXTRA_TWO_FA);
            isSave = getIntent().getBooleanExtra(EXTRA_SAVE, false);
            webview.loadUrl(getIntent().getStringExtra(EXTRA_VALIDATE));
        }
    }

    private void parseUrl(String url) {
        try {
            if (url == null) {
                return;
            }

            Logger.d(TAG, "url=" + url);

            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    Intent intent = new Intent();

                    try {
                        ArrayList<Token> tokens = tryExtractAccessTokens(url);
                        intent.putParcelableArrayListExtra("group_tokens", tokens);
                    } catch (Exception e) {
                        String accessToken = tryExtractAccessToken(url);
                        String userId = tryExtractUserId(url);

                        intent.putExtra(Extra.TOKEN, accessToken);
                        intent.putExtra(Extra.USER_ID, Integer.parseInt(userId));
                        intent.putExtra(Extra.LOGIN, TLogin);
                        intent.putExtra(Extra.PASSWORD, TPassword);
                        intent.putExtra(Extra.TWO_FA, TwoFA);
                        intent.putExtra(Extra.SAVE, isSave);
                    }

                    setResult(RESULT_OK, intent);
                }

                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class VkontakteWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            parseUrl(url);
        }
    }
}
