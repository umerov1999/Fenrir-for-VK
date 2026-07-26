package dev.ragnarok.fenrir.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.HitTestResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewFeature
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.UserAgentTool
import dev.ragnarok.fenrir.activity.ActivityFeatures
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.domain.IUtilsInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fragment.base.BaseFragment
import dev.ragnarok.fenrir.link.LinkHelper.openLinkInBrowser
import dev.ragnarok.fenrir.link.LinkHelper.openVKLink
import dev.ragnarok.fenrir.link.VKLinkParser.parse
import dev.ragnarok.fenrir.link.types.AwayLink
import dev.ragnarok.fenrir.link.types.DomainLink
import dev.ragnarok.fenrir.link.types.PageLink
import dev.ragnarok.fenrir.listener.BackPressCallback
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.DownloadWorkUtils
import dev.ragnarok.fenrir.util.Logger.d
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.fromIOToMain
import dev.ragnarok.fenrir.util.toast.CustomToast
import java.io.File
import java.net.URL

class BrowserFragment : BaseFragment(), MenuProvider, BackPressCallback,
    View.OnCreateContextMenuListener {
    private var mWebView: WebView? = null
    private var mAccountId = 0L
    private var title: String? = null
    private var webState: Bundle? = null
    private var mUtilsInteractor: IUtilsInteractor = InteractorFactory.createUtilsInteractor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccountId = requireArguments().getLong(Extra.ACCOUNT_ID)
        savedInstanceState?.let { restoreFromInstanceState(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.copy_url) {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText(getString(R.string.link), mWebView?.url)
            clipboard?.setPrimaryClip(clip)
            CustomToast.createCustomToast(requireActivity(), view)?.showToast(R.string.copied)
            return true
        }
        return false
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.browser_menu, menu)
    }

    @Suppress("SetJavaScriptEnabled", "RequiresFeature", "deprecation")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_browser, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            root.findViewById<View>(R.id.toolbar)?.setPadding(0, insets.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }

        mWebView = root.findViewById(R.id.webview)
        mWebView?.let {
            it.settings.builtInZoomControls = true
            it.settings.displayZoomControls = false
            it.webViewClient = VkLinkSupportWebClient()
            it.webChromeClient = object : WebChromeClient() {
                override fun onReceivedTitle(view: WebView, title: String) {
                    this@BrowserFragment.title = title
                    refreshActionBar()
                }
            }
            if (Settings.get().main().isWebview_night_mode && WebViewFeature.isFeatureSupported(
                    WebViewFeature.FORCE_DARK
                ) && Settings.get().ui().isDarkModeEnabled(requireActivity())
            ) {
                WebSettingsCompat.setForceDark(it.settings, WebSettingsCompat.FORCE_DARK_ON)
            }
            registerForContextMenu(it)

            it.settings.userAgentString = UserAgentTool.USER_AGENT_CURRENT_ACCOUNT
            it.settings.javaScriptEnabled = true // из-за этого не срабатывал метод
            it.settings.domStorageEnabled = true
            it.settings.blockNetworkLoads = false
            it.settings.blockNetworkImage = false
            it.settings.databaseEnabled = true

            // shouldOverrideUrlLoading в WebClient
            val tmpWebState = webState
            when {
                savedInstanceState != null -> {
                    restoreFromInstanceState(savedInstanceState)
                }

                tmpWebState != null -> {
                    it.restoreState(tmpWebState)
                    webState = null
                }

                else -> {
                    loadAtFirst()
                }
            }
        }
        return root
    }

    private fun downloadResult(Prefix: String?, dirL: File, url: String, type: String) {
        var dir = dirL
        if (Prefix != null && Settings.get().main().isPhoto_to_user_dir) {
            val dir_final = File(dir.absolutePath + "/" + Prefix)
            if (!dir_final.isDirectory) {
                val created = dir_final.mkdirs()
                if (!created) {
                    CustomToast.createCustomToast(requireActivity(), view)
                        ?.showToastError("Can't create directory $dir_final")
                    return
                }
            } else dir_final.setLastModified(System.currentTimeMillis())
            dir = dir_final
        }
        DownloadWorkUtils.doDownloadPhoto(
            requireActivity(),
            url,
            dir.absolutePath,
            (if (Prefix != null) Prefix + "_" else "") + type
        )
    }

    private fun showBrowserImageMenuOptions(imageUrl: String) {
        val menus = ModalBottomSheetDialogFragment.Builder()
        val owner = arguments?.getString(Extra.OWNER)
        var typeRes = arguments?.getString(Extra.TYPE).orEmpty()
        if (owner != null && typeRes.isNotEmpty()) {
            menus.add(
                OptionRequest(
                    R.id.button_ok,
                    getString(R.string.download),
                    R.drawable.save,
                    true
                )
            )
        }
        menus.add(
            OptionRequest(
                R.id.button_cancel,
                getString(R.string.copy_simple),
                R.drawable.content_copy,
                true
            )
        )
        menus.show(
            childFragmentManager,
            "left_options"
        ) { _, option ->
            if (option.id == R.id.button_ok) {
                val urlObj = URL(imageUrl)
                val urlPath: String = urlObj.path
                var fileName = urlPath.substring(urlPath.lastIndexOf('/') + 1)
                if (fileName.lastIndexOf('.') != -1) {
                    fileName = fileName.take(fileName.lastIndexOf('.'))
                }

                if (owner == null || typeRes.isEmpty()) {
                    return@show
                }
                typeRes += ("_$fileName")
                val dir = File(Settings.get().main().photoDir)
                if (!dir.isDirectory) {
                    val created = dir.mkdirs()
                    if (!created) {
                        CustomToast.createCustomToast(requireActivity(), view)
                            ?.showToastError("Can't create directory $dir")
                        return@show
                    }
                } else dir.setLastModified(System.currentTimeMillis())
                downloadResult(
                    DownloadWorkUtils.makeLegalFilename(
                        (DownloadWorkUtils.fixStart(owner) ?: typeRes),
                        null
                    ), dir, imageUrl, typeRes
                )
            } else if (option.id == R.id.button_cancel) {
                val clipboard =
                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("response", imageUrl)
                clipboard?.setPrimaryClip(clip)
                CustomToast.createCustomToast(requireActivity(), view)
                    ?.showToast(R.string.copied_to_clipboard)
            }
        }
    }

    override
    fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (v is WebView) {
            val result = v.hitTestResult
            val type = result.type

            if (type == HitTestResult.IMAGE_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                showBrowserImageMenuOptions(result.extra ?: return)
            } /* else if (type == HitTestResult.UNKNOWN_TYPE){
                val message = Message()
                message.target = ImgHandler()
                v.requestFocusNodeHref(message)
            } */
        }
    }

    private fun loadAtFirst() {
        val url = requireArguments().getString(Extra.URL)
        d(TAG, "url: $url")
        if (url != null) {
            mWebView?.loadUrl(url)
        }
    }

    internal fun refreshActionBar() {
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

    private inner class VkLinkSupportWebClient : WebViewClientCompat() {
        override fun onRenderProcessGone(
            view: WebView,
            detail: RenderProcessGoneDetail?
        ): Boolean {
            view.destroy()
            CustomToast.createCustomToast(
                requireActivity(),
                null,
                null
            )?.showToastError(R.string.crash_error_activity_out_of_memory)
            return true
        }

        override fun onLoadResource(view: WebView, url: String) {
            super.onLoadResource(view, url)
            d(TAG, "onLoadResource, url: $url")
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            if (Regex("((?:vk\\.(?:ru|com|me)/?$)|(?:github.com)|(?:vk\\.(?:ru|com|me)/(?:@|activation|login|app))|(?:login\\.vk\\.(?:ru|com|me)))").containsMatchIn(
                    url
                )
            ) {
                view.loadUrl(url)
                return true
            }
            val link = parse(url)
            d(TAG, "shouldOverrideUrlLoading, link: $link, url: $url")

            //link: null, url: https://vk.ru/doc124456557_415878705
            if (link == null) {
                activity?.let { openLinkInBrowser(it, url) }
                return true
            }
            if (link is PageLink) {
                view.loadUrl("$url?api_view=0df43cdc43a25550c6beb7357c9d41")
                return true
            }
            if (link is DomainLink) {
                appendJob(
                    mUtilsInteractor.resolveDomain(mAccountId, link.domain)
                        .fromIOToMain({ optionalOwner ->
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
        val TAG: String = BrowserFragment::class.simpleName.orEmpty()
        private const val SAVE_TITLE = "save_title"
        fun buildArgs(accountId: Long, url: String, owner: String?, type: String?): Bundle {
            val args = Bundle()
            args.putString(Extra.URL, url)
            args.putLong(Extra.ACCOUNT_ID, accountId)
            args.putString(Extra.OWNER, owner)
            args.putString(Extra.TYPE, type)
            return args
        }

        fun newInstance(args: Bundle?): BrowserFragment {
            val fragment = BrowserFragment()
            fragment.arguments = args
            return fragment
        }
    }
}