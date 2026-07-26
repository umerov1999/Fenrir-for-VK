package dev.ragnarok.fenrir.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import androidx.webkit.WebViewClientCompat
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.UserAgentTool
import dev.ragnarok.fenrir.api.Auth
import dev.ragnarok.fenrir.api.util.VKStringUtils
import dev.ragnarok.fenrir.api.validation.IValidateProvider
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.theme.ThemesController.currentStyle
import dev.ragnarok.fenrir.util.Logger
import dev.ragnarok.fenrir.util.coroutines.CompositeJob
import dev.ragnarok.fenrir.util.coroutines.CoroutinesUtils.sharedFlowToMain
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast
import kotlinx.coroutines.flow.filter
import kotlin.math.max

class ValidateActivity : AppCompatActivity() {
    private var validateProvider: IValidateProvider? = null
    private val mCompositeJob = CompositeJob()
    private var urlVal: String? = null
    private var accountId: Long = ISettings.IAccountsSettings.INVALID_ID

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentStyle())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        urlVal = (intent.getStringExtra(EXTRA_VALIDATE) ?: return)
        accountId = intent.getLongExtra(Extra.ACCOUNT_ID, ISettings.IAccountsSettings.INVALID_ID)

        validateProvider = Includes.validationProvider

        validateProvider?.let {
            mCompositeJob.add(
                it.observeWaiting()
                    .filter { ob -> ob == urlVal }
                    .sharedFlowToMain { onWaitingRequestReceived() }
            )
            mCompositeJob.add(
                it.observeCanceling()
                    .filter { ob -> ob == urlVal }
                    .sharedFlowToMain { onRequestCancelled() }
            )
        }

        val webview = findViewById<WebView>(R.id.item_web_auth)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.blockNetworkLoads = false
        webview.settings.blockNetworkImage = false
        webview.clearCache(true)
        webview.settings.userAgentString = UserAgentTool.getAccountUserAgent(accountId)

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.webViewClient = object : WebViewClientCompat() {
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                webview.destroy()
                createCustomToast(
                    this@ValidateActivity,
                    null,
                    null
                )?.showToastError(R.string.crash_error_activity_out_of_memory)
                finish()
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                parseUrl(url)
            }
        }
        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies {
            Log.d(
                TAG,
                "Cookie removed: $it"
            )
        }
        webview.loadUrl(urlVal ?: "")
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                cancel()
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item_root)) { v, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val imeFixedBottom =
                if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.ime()
                    ).bottom, insets.bottom
                ) else insets.bottom
            v.setPadding(
                insets.left, insets.top,
                insets.right,
                imeFixedBottom
            )
            WindowInsetsCompat.CONSUMED
        }

        val statusBarColor = Color.TRANSPARENT
        val navigationBarColor = Color.TRANSPARENT
        val invertIcons = !Settings.get().ui().isDarkModeEnabled(
            this
        )
        val statusBarStyle = if (invertIcons) SystemBarStyle.light(
            statusBarColor,
            statusBarColor
        ) else SystemBarStyle.dark(statusBarColor)
        val navigationBarStyle = if (invertIcons) SystemBarStyle.light(
            navigationBarColor,
            navigationBarColor
        ) else SystemBarStyle.dark(navigationBarColor)
        for (i in (window.decorView as ViewGroup)) {
            if (i is ProtectionLayout) {
                (window.decorView as ViewGroup).removeView(i)
            }
        }
        enableEdgeToEdge(statusBarStyle, navigationBarStyle)
    }

    internal fun cancel() {
        urlVal?.let { validateProvider?.cancel(it) }
        supportFinishAfterTransition()
    }

    private fun onRequestCancelled() {
        supportFinishAfterTransition()
    }

    private fun onWaitingRequestReceived() {
        urlVal?.let { validateProvider?.notifyThatValidateEntryActive(it) }
    }

    override fun onDestroy() {
        mCompositeJob.cancel()
        super.onDestroy()
    }

    internal fun parseUrl(url: String?) {
        try {
            if (url == null) {
                return
            }
            Logger.d(TAG, "url=$url")
            if (url.startsWith(Auth.redirect_url)) {
                if (!url.contains("error=")) {
                    val intent = Intent()
                    try {
                        val accessToken = tryExtractAccessToken(url)
                        val userId = tryExtractUserId(url)
                        if (accessToken.nonNullNoEmpty() || userId.nonNullNoEmpty()) {
                            userId?.toLong()
                                ?.let {
                                    Settings.get().accounts().storeAccessToken(it, accessToken)
                                }
                        }
                    } catch (_: Exception) {
                    }
                    setResult(RESULT_OK, intent)
                }
                urlVal?.let { validateProvider?.enterState(it, true) }
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = ValidateActivity::class.simpleName.orEmpty()
        private const val EXTRA_VALIDATE = "validate"


        fun createIntent(context: Context, validate_url: String?, accountId: Long): Intent {
            return Intent(context, ValidateActivity::class.java)
                .putExtra(EXTRA_VALIDATE, validate_url)
                .putExtra(Extra.ACCOUNT_ID, accountId)
        }

        internal fun tryExtractAccessToken(url: String): String? {
            return VKStringUtils.extractPattern(url, "access_token=(.*?)&")
        }

        internal fun tryExtractUserId(url: String): String? {
            return VKStringUtils.extractPattern(url, "user_id=(\\d*)")
        }
    }
}