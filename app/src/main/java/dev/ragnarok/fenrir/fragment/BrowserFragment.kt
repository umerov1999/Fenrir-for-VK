package dev.ragnarok.fenrir.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import dev.ragnarok.fenrir.AccountType
import dev.ragnarok.fenrir.Constants.USER_AGENT
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.domain.IUtilsInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.base.BaseFragment
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.link.LinkHelper.openLinkInBrowser
import dev.ragnarok.fenrir.link.LinkHelper.openVKLink
import dev.ragnarok.fenrir.link.VkLinkParser.parse
import dev.ragnarok.fenrir.link.types.AwayLink
import dev.ragnarok.fenrir.link.types.DomainLink
import dev.ragnarok.fenrir.link.types.PageLink
import dev.ragnarok.fenrir.listener.BackPressCallback
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Logger.d

class BrowserFragment : BaseFragment(), BackPressCallback {
    private var mWebView: WebView? = null
    private var mAccountId = 0
    private var title: String? = null
    private var webState: Bundle? = null
    private var mUtilsInteractor: IUtilsInteractor = InteractorFactory.createUtilsInteractor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID)
        savedInstanceState?.let { restoreFromInstanceState(it) }
    }

    @SuppressLint("SetJavaScriptEnabled", "RequiresFeature")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_browser, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        mWebView = root.findViewById(R.id.webview)
        mWebView?.settings?.builtInZoomControls = true
        mWebView?.settings?.displayZoomControls = false
        mWebView?.webViewClient = VkLinkSupportWebClient()
        mWebView?.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                this@BrowserFragment.title = title
                refreshActionBar()
            }
        }
        if (Settings.get().main().isWebview_night_mode && WebViewFeature.isFeatureSupported(
                WebViewFeature.FORCE_DARK
            ) && Settings.get().ui().isDarkModeEnabled(requireActivity())
        ) {
            mWebView?.settings
                ?.let { WebSettingsCompat.setForceDark(it, WebSettingsCompat.FORCE_DARK_ON) }
        }
        mWebView?.settings?.userAgentString = USER_AGENT(AccountType.BY_TYPE)
        mWebView?.settings?.javaScriptEnabled = true // из-за этого не срабатывал метод
        // shouldOverrideUrlLoading в WebClient
        when {
            savedInstanceState != null -> {
                restoreFromInstanceState(savedInstanceState)
            }
            webState != null -> {
                mWebView?.restoreState(webState ?: return null)
                webState = null
            }
            else -> {
                loadAtFirst()
            }
        }
        return root
    }

    private fun loadAtFirst() {
        val url = requireArguments().getString(Extra.URL)
        d(TAG, "url: $url")
        if (url != null) {
            mWebView?.loadUrl(url)
        }
    }

    private fun refreshActionBar() {
        if (!isAdded) {
            return
        }
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.browser)
            actionBar.subtitle = title
        }
    }

    override fun onResume() {
        super.onResume()
        refreshActionBar()
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onPause() {
        super.onPause()
        webState = Bundle()
        mWebView?.saveState(webState ?: return)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVE_TITLE, title)
        mWebView?.saveState(outState)
    }

    private fun restoreFromInstanceState(bundle: Bundle) {
        mWebView?.restoreState(bundle)
        title = bundle.getString(SAVE_TITLE)
        d(TAG, "restoreFromInstanceState, bundle: $bundle")
    }

    override fun onBackPressed(): Boolean {
        if (mWebView?.canGoBack() == true) {
            mWebView?.goBack()
            return false
        }
        return true
    }

    private inner class VkLinkSupportWebClient : WebViewClient() {
        override fun onLoadResource(view: WebView, url: String) {
            super.onLoadResource(view, url)
            d(TAG, "onLoadResource, url: $url")
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            if (url.contains("github.com") || url.contains("vk.com/@") || url.contains("vk.com/activation") || url.contains(
                    "vk.com/app"
                )
            ) {
                view.loadUrl(url)
                return true
            }
            val link = parse(url)
            d(TAG, "shouldOverrideUrlLoading, link: $link, url: $url")

            //link: null, url: https://vk.com/doc124456557_415878705
            if (link == null) {
                activity?.let { openLinkInBrowser(it, url) }
                return true
            }
            if (link is PageLink) {
                view.loadUrl("$url?api_view=0df43cdc43a25550c6beb7357c9d41")
                return true
            }
            if (link is DomainLink) {
                appendDisposable(
                    mUtilsInteractor.resolveDomain(mAccountId, link.domain)
                        .fromIOToMain()
                        .subscribe({ optionalOwner ->
                            if (optionalOwner.isEmpty) {
                                view.loadUrl(url)
                            } else {
                                optionalOwner.get()?.let {
                                    PlaceFactory.getOwnerWallPlace(
                                        mAccountId,
                                        it
                                    ).tryOpenWith(requireActivity())
                                }
                            }
                        }) {
                            view.loadUrl(url)
                        })
                return true
            }
            if (link is AwayLink) {
                activity?.let { openLinkInBrowser(it, link.link) }
                return true
            }
            activity?.let {
                if (openVKLink(it, mAccountId, link, false)) {
                    return true
                }
            }
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            title = view.title
            refreshActionBar()
        }
    }

    companion object {
        val TAG: String = BrowserFragment::class.java.simpleName
        private const val SAVE_TITLE = "save_title"
        fun buildArgs(accountId: Int, url: String): Bundle {
            val args = Bundle()
            args.putString(Extra.URL, url)
            args.putInt(Extra.ACCOUNT_ID, accountId)
            return args
        }

        fun newInstance(args: Bundle?): BrowserFragment {
            val fragment = BrowserFragment()
            fragment.arguments = args
            return fragment
        }
    }
}