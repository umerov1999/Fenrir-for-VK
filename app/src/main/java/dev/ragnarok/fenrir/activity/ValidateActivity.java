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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.slidr.Slidr;
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig;
import dev.ragnarok.fenrir.api.Auth;
import dev.ragnarok.fenrir.api.util.VKStringUtils;
import dev.ragnarok.fenrir.model.Token;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Logger;

public class ValidateActivity extends AppCompatActivity {

    private static final String TAG = ValidateActivity.class.getSimpleName();
    private static final String EXTRA_VALIDATE = "validate";

    public static Intent createIntent(Context context, String validate_url) {
        return new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_VALIDATE, validate_url);
    }

    private static String tryExtractAccessToken(String url) {
        return VKStringUtils.extractPattern(url, "access_token=(.*?)&");
    }

    private static ArrayList<Token> tryExtractAccessTokens(String url) throws Exception {
        Pattern p = Pattern.compile("access_token_(\\d*)=(.*?)&");

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

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemesController.INSTANCE.currentStyle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Slidr.attach(this, new SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this)).build());

        WebView webview = findViewById(R.id.vkontakteview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.clearCache(true);
        webview.getSettings().setUserAgentString(Constants.USER_AGENT(AccountType.KATE));

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.setWebViewClient(new VkontakteWebViewClient());

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(aBoolean -> Log.d(TAG, "Cookie removed: " + aBoolean));

        webview.loadUrl(getIntent().getStringExtra(EXTRA_VALIDATE));
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
                        String accessToken = tryExtractAccessToken(url);
                        String userId = tryExtractUserId(url);
                        Settings.get().accounts().storeAccessToken(Integer.parseInt(userId), accessToken);
                    } catch (Exception ignored) {
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
