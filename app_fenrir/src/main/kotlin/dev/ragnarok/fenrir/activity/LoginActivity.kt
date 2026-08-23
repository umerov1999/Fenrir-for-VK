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
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.insets.ProtectionLayout
import androidx.core.view.iterator
import androidx.webkit.WebViewClientCompat
import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.UserAgentTool.getUserAgentByType
import dev.ragnarok.fenrir.api.Auth
import dev.ragnarok.fenrir.api.util.VKStringUtils
import dev.ragnarok.fenrir.getParcelableArrayListExtraCompat
import dev.ragnarok.fenrir.model.Token
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.theme.ThemesController.currentStyle
import dev.ragnarok.fenrir.util.Logger
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.toast.CustomToast.Companion.createCustomToast
import java.io.UnsupportedEncodingException
import kotlin.math.max

class LoginActivity : AppCompatActivity() {
    private var TLogin: String? = null
    private var TPassword: String? = null
    private var TwoFA: String? = null
    private var isSave = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(currentStyle())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val webview = findViewById<WebView>(R.id.item_web_auth)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.settings.blockNetworkLoads = false
        webview.settings.blockNetworkImage = false
        webview.clearCache(true)
        webview.settings.userAgentString = getUserAgentByType(Constants.DEFAULT_ACCOUNT_TYPE, true)

        //Чтобы получать уведомления об окончании загрузки страницы
        webview.webViewClient = object : WebViewClientCompat() {
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                webview.destroy()
                createCustomToast(
                    this@LoginActivity,
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
        if (intent.getStringExtra(EXTRA_VALIDATE).isNullOrEmpty()) {
            val clientId = intent.getStringExtra(EXTRA_CLIENT_ID) ?: return
            val scope = intent.getStringExtra(EXTRA_SCOPE) ?: return
            val groupIds = intent.getStringExtra(EXTRA_GROUP_IDS)
            try {
                val url = Auth.getUrl(clientId, scope, groupIds)
                webview.loadUrl(url)
            } catch (e: UnsupportedEncodingException) {
                createCustomToast(this, null)?.showToastError(e.localizedMessage)
            }
        } else {
            TLogin = intent.getStringExtra(EXTRA_LOGIN)
            TPassword = intent.getStringExtra(EXTRA_PASSWORD)
            TwoFA = intent.getStringExtra(EXTRA_TWO_FA)
            isSave = intent.getBooleanExtra(EXTRA_SAVE, false)
            webview.loadUrl(intent.getStringExtra(EXTRA_VALIDATE) ?: return)
        }
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

    internal fun parseUrl(url: String?) {
        try {
            if (url == null) {
                return
            }
            Logger.d(TAG, "url=$url")
            if (url.startsWith(Auth.getRedirectUrl())) {
                if (!url.contains("error=")) {
                    val intent = Intent()
                    try {
                        val tokens = tryExtractAccessTokens(url)
                        intent.putParcelableArrayListExtra("group_tokens", tokens)
                    } catch (_: Exception) {
                        val accessToken = tryExtractAccessToken(url)
                        val userId = tryExtractUserId(url)
                        intent.putExtra(Extra.TOKEN, accessToken)
                        intent.putExtra(Extra.USER_ID, userId?.toLong())
                        intent.putExtra(Extra.LOGIN, TLogin)
                        intent.putExtra(Extra.PASSWORD, TPassword)
                        intent.putExtra(Extra.TWO_FA, TwoFA)
                        intent.putExtra(Extra.SAVE, isSave)
                    }
                    setResult(RESULT_OK, intent)
                }
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = LoginActivity::class.simpleName.orEmpty()
        private const val EXTRA_CLIENT_ID = "client_id"
        private const val EXTRA_SCOPE = "scope"
        private const val EXTRA_VALIDATE = "validate"
        private const val EXTRA_LOGIN = "login"
        private const val EXTRA_PASSWORD = "password"
        private const val EXTRA_TWO_FA = "two_fa"
        private const val EXTRA_SAVE = "save"
        private const val EXTRA_GROUP_IDS = "group_ids"

        fun createIntent(context: Context?, clientId: String?, scope: String?): Intent {
            return Intent(context, LoginActivity::class.java)
                .putExtra(EXTRA_CLIENT_ID, clientId)
                .putExtra(EXTRA_SCOPE, scope)
        }

        fun createIntent(
            context: Context?,
            validate_url: String?,
            Login: String?,
            Password: String?,
            TwoFa: String?,
            isSave: Boolean
        ): Intent {
            return Intent(context, LoginActivity::class.java)
                .putExtra(EXTRA_VALIDATE, validate_url).putExtra(EXTRA_LOGIN, Login)
                .putExtra(EXTRA_PASSWORD, Password).putExtra(EXTRA_TWO_FA, TwoFa)
                .putExtra(EXTRA_SAVE, isSave)
        }

        fun createIntent(
            context: Context?,
            clientId: String?,
            scope: String?,
            groupIds: Collection<Long>?
        ): Intent {
            val ids = Utils.join(groupIds, ",") {
                it.toString()
            }
            return Intent(context, LoginActivity::class.java)
                .putExtra(EXTRA_CLIENT_ID, clientId)
                .putExtra(EXTRA_SCOPE, scope)
                .putExtra(EXTRA_GROUP_IDS, ids)
        }

        internal fun tryExtractAccessToken(url: String): String? {
            return VKStringUtils.extractPattern(url, "access_token=(.*?)&")
        }

        @Throws(Exception::class)
        internal fun tryExtractAccessTokens(url: String): ArrayList<Token> {
            val tokens = ArrayList<Token>()
            val p = Regex("access_token_(\\d*)=(.*?)(&|$)")
            val res = p.findAll(url)
            for (i in res) {
                val groupId = i.groupValues.getOrNull(1)
                val token = i.groupValues.getOrNull(2)
                if (groupId.nonNullNoEmpty() && token.nonNullNoEmpty()) {
                    tokens.add(Token(-groupId.toLong(), token))
                }
            }
            if (tokens.isEmpty()) {
                throw Exception("Failed to parse redirect url $url")
            }
            return tokens
        }

        internal fun tryExtractUserId(url: String): String? {
            return VKStringUtils.extractPattern(url, "user_id=(\\d*)")
        }

        fun extractGroupTokens(data: Intent): ArrayList<Token>? {
            return data.getParcelableArrayListExtraCompat("group_tokens")
        }
    }
}
