package dev.ragnarok.fenrir.mvp.presenter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.Apis.get
import dev.ragnarok.fenrir.api.interfaces.INetworker
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IRequestExecuteView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.DownloadWorkUtils.makeLegalFilename
import dev.ragnarok.fenrir.util.Optional
import dev.ragnarok.fenrir.util.Pair
import dev.ragnarok.fenrir.util.Pair.Companion.create
import dev.ragnarok.fenrir.util.RxUtils.applySingleIOToMainSchedulers
import dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime
import dev.ragnarok.fenrir.util.Utils.join
import dev.ragnarok.fenrir.util.Utils.safelyClose
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

class RequestExecutePresenter(accountId: Int, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IRequestExecuteView>(accountId, savedInstanceState) {
    private val networker: INetworker = get()
    private var body: String? = null
    private var method: String? = null
    private var fullResponseBody: String? = null
    private var trimmedResponseBody: String? = null
    private var loadingNow = false
    private fun executeRequest() {
        val trimmedMethod = if (method.nonNullNoEmpty()) method?.trim { it <= ' ' } else null
        val trimmedBody = if (body.nonNullNoEmpty()) body?.trim { it <= ' ' } else null
        if (trimmedMethod.isNullOrEmpty()) {
            showError(Exception("Method can't be empty"))
            return
        }
        val accountId = accountId
        val params: MutableMap<String, String> = HashMap()
        if (trimmedBody.nonNullNoEmpty()) {
            try {
                val lines = trimmedBody.split("\\r?\\n".toRegex()).toTypedArray()
                for (line in lines) {
                    val parts = line.split("=>".toRegex()).toTypedArray()
                    val name = parts[0].lowercase(Locale.getDefault()).trim { it <= ' ' }
                    var value = parts[1].trim { it <= ' ' }
                    value = value.replace("\"".toRegex(), "")
                    if ((name == "user_id" || name == "peer_id" || name == "owner_id") && (value.equals(
                            "my",
                            ignoreCase = true
                        ) || value.equals("я", ignoreCase = true))
                    ) value = accountId.toString()
                    params[name] = value
                }
            } catch (e: Exception) {
                showError(e)
                return
            }
        }
        setLoadingNow(true)
        appendDisposable(executeSingle(accountId, trimmedMethod, params)
            .compose(applySingleIOToMainSchedulers())
            .subscribe({ body: Pair<String?, String?> -> onRequestResponse(body) }) { throwable: Throwable? ->
                onRequestError(
                    getCauseIfRuntime(throwable)
                )
            })
    }

    private fun hasWritePermission(): Boolean {
        return hasReadWriteStoragePermission(applicationContext)
    }

    @Suppress("DEPRECATION")
    private fun saveToFile() {
        if (!hasWritePermission()) {
            view?.requestWriteExternalStoragePermission()
            return
        }
        val rMethod = method ?: return
        var out: FileOutputStream? = null
        try {
            val filename = makeLegalFilename(rMethod, "json")
            val file = File(Environment.getExternalStorageDirectory(), filename)
            file.delete()
            val bytes = fullResponseBody?.toByteArray(StandardCharsets.UTF_8) ?: return
            out = FileOutputStream(file)
            out.write(bytes)
            out.flush()
            applicationContext.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)
                )
            )
            view?.customToast?.showToast(
                R.string.saved_to_param_file_name,
                file.absolutePath
            )
        } catch (e: Exception) {
            showError(e)
        } finally {
            safelyClose(out)
        }
    }

    override fun onGuiCreated(viewHost: IRequestExecuteView) {
        super.onGuiCreated(viewHost)
        viewHost.displayBody(trimmedResponseBody)
        resolveProgressDialog()
    }

    private fun onRequestResponse(body: Pair<String?, String?>) {
        setLoadingNow(false)
        fullResponseBody = body.first
        trimmedResponseBody = body.second
        view?.displayBody(
            trimmedResponseBody
        )
    }

    private fun onRequestError(throwable: Throwable) {
        setLoadingNow(false)
        showError(throwable)
    }

    private fun setLoadingNow(loadingNow: Boolean) {
        this.loadingNow = loadingNow
        resolveProgressDialog()
    }

    private fun resolveProgressDialog() {
        if (loadingNow) {
            view?.displayProgressDialog(
                R.string.please_wait,
                R.string.waiting_for_response_message,
                false
            )
        } else {
            view?.dismissProgressDialog()
        }
    }

    private fun executeSingle(
        accountId: Int,
        method: String,
        params: Map<String, String>
    ): Single<Pair<String?, String?>> {
        return networker.vkDefault(accountId)
            .other()
            .rawRequest(method, params)
            .map { optional: Optional<String> ->
                val responseString = optional.get()
                val fullJson = if (responseString == null) null else toPrettyFormat(responseString)
                var trimmedJson: String? = null
                if (fullJson.nonNullNoEmpty()) {
                    val lines = fullJson.split("\\r?\\n".toRegex()).toTypedArray()
                    val trimmed: MutableList<String?> = ArrayList()
                    for (line in lines) {
                        if (trimmed.size > 1500) {
                            trimmed.add("\n")
                            trimmed.add("... and more " + (lines.size - 1500) + " lines")
                            break
                        }
                        trimmed.add(line)
                    }
                    trimmedJson = join("\n", trimmed)
                }
                create(fullJson, trimmedJson)
            }
    }

    fun fireSaveClick() {
        saveToFile()
    }

    fun fireWritePermissionResolved() {
        if (hasWritePermission()) {
            saveToFile()
        }
    }

    fun fireExecuteClick() {
        view?.hideKeyboard()
        executeRequest()
    }

    fun fireMethodEdit(s: CharSequence?) {
        method = s.toString()
    }

    fun fireBodyEdit(s: CharSequence?) {
        body = s.toString()
    }

    fun fireCopyClick() {
        val clipboard =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("response", fullResponseBody)
        clipboard?.setPrimaryClip(clip)
        view?.customToast?.showToast(
            R.string.copied_to_clipboard
        )
    }

    companion object {
        /**
         * Convert a JSON string to pretty print version
         */
        private fun toPrettyFormat(jsonString: String): String {
            val json = JsonParser.parseString(jsonString).asJsonObject
            val gson = GsonBuilder().setPrettyPrinting().create()
            return gson.toJson(json)
        }
    }

}