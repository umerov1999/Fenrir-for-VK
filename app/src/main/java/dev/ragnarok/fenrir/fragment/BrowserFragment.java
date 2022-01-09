package dev.ragnarok.fenrir.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.link.VkLinkParser;
import dev.ragnarok.fenrir.link.types.AbsLink;
import dev.ragnarok.fenrir.link.types.AwayLink;
import dev.ragnarok.fenrir.link.types.PageLink;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;

public class BrowserFragment extends BaseFragment implements BackPressCallback {

    public static final String TAG = BrowserFragment.class.getSimpleName();
    private static final String SAVE_TITLE = "save_title";
    protected WebView mWebView;
    private int mAccountId;
    private String title;
    private Bundle webState;

    public static Bundle buildArgs(int accountId, @NonNull String url) {
        Bundle args = new Bundle();
        args.putString(Extra.URL, url);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static BrowserFragment newInstance(Bundle args) {
        BrowserFragment fragment = new BrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "RequiresFeature"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_browser, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mWebView = root.findViewById(R.id.webview);

        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

        mWebView.setWebViewClient(new VkLinkSupportWebClient());

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                BrowserFragment.this.title = title;
                refreshActionBar();
            }
        });


        if (Settings.get().main().isWebview_night_mode() && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) && Settings.get().ui().isDarkModeEnabled(requireActivity())) {
            WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
        }
        mWebView.getSettings().setUserAgentString(Constants.USER_AGENT(AccountType.BY_TYPE));

        mWebView.getSettings().setJavaScriptEnabled(true); // из-за этого не срабатывал метод
        // shouldOverrideUrlLoading в WebClient

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        } else if (webState != null) {
            mWebView.restoreState(webState);
            webState = null;
        } else {
            loadAtFirst();
        }

        return root;
    }

    protected void loadAtFirst() {
        String url = requireArguments().getString(Extra.URL);
        Logger.d(TAG, "url: " + url);
        mWebView.loadUrl(url);
    }

    private void refreshActionBar() {
        if (!isAdded()) {
            return;
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.browser);
            actionBar.setSubtitle(title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshActionBar();

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        webState = new Bundle();
        mWebView.saveState(webState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_TITLE, title);
        mWebView.saveState(outState);
    }

    private void restoreFromInstanceState(@NonNull Bundle bundle) {
        if (mWebView != null) {
            mWebView.restoreState(bundle);
        }

        title = bundle.getString(SAVE_TITLE);
        Logger.d(TAG, "restoreFromInstanceState, bundle: " + bundle);
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return false;
        }

        return true;
    }

    private class VkLinkSupportWebClient extends WebViewClient {

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Logger.d(TAG, "onLoadResource, url: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.contains("github.com") || url.contains("vk.com/@") || url.contains("vk.com/activation")) {
                view.loadUrl(url);
                return true;
            }
            AbsLink link = VkLinkParser.parse(url);
            Logger.d(TAG, "shouldOverrideUrlLoading, link: " + link + ", url: " + url);

            //link: null, url: https://vk.com/doc124456557_415878705

            if (link == null) {
                LinkHelper.openLinkInBrowser(requireActivity(), url);
                return true;
            }

            if (link instanceof PageLink) {
                view.loadUrl(url + "?api_view=0df43cdc43a25550c6beb7357c9d41");
                return true;
            }

            if (link instanceof AwayLink) {
                LinkHelper.openLinkInBrowser(requireActivity(), ((AwayLink) link).link);
                return true;
            }

            if (LinkHelper.openVKLink(requireActivity(), mAccountId, link, false)) {
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            title = view.getTitle();
            refreshActionBar();
        }


    }
}
