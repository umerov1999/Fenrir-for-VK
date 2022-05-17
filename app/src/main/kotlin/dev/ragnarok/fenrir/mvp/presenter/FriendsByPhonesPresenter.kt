package dev.ragnarok.fenrir.mvp.presenter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.ragnarok.fenrir.Includes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IAccountsInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.ContactConversation
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IFriendsByPhonesView
import dev.ragnarok.fenrir.trimmedNonNullNoEmpty
import dev.ragnarok.fenrir.util.CustomToast
import dev.ragnarok.fenrir.util.Utils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.*

class FriendsByPhonesPresenter(accountId: Int, context: Context, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IFriendsByPhonesView>(accountId, savedInstanceState) {
    private val data: MutableList<ContactConversation>
    private val dataSearch: MutableList<ContactConversation>
    private val accountsInteractor: IAccountsInteractor =
        InteractorFactory.createAccountInteractor()
    private val context: Context
    private var netLoadingNow = false
    private var query: String? = null
    private fun resolveRefreshingView() {
        view?.displayLoading(
            netLoadingNow
        )
    }

    fun fireImport(reader: JsonArray) {
        data.clear()
        for (i in reader) {
            val elem = i.asJsonObject
            data.add(
                ContactConversation(elem.get("peer_id").asInt).setIsContact(true)
                    .setPhone(if (elem.has("number")) elem.get("number").asString else null)
                    .setPhoto(if (elem.has("photo")) elem.get("photo").asString else null)
                    .setTitle(if (elem.has("name")) elem.get("name").asString else null)
            )
        }
        view?.notifyDataSetChanged()
    }

    private fun requestActualData() {
        netLoadingNow = true
        resolveRefreshingView()
        val accountId = accountId
        appendDisposable(accountsInteractor.getContactList(accountId, 0, 1000)
            .fromIOToMain()
            .subscribe({ owners -> onActualDataReceived(owners) }) { t ->
                onActualDataGetError(
                    t
                )
            })
    }

    @Suppress("DEPRECATION")
    fun fireExport(context: Context, file: File) {
        var out: FileOutputStream? = null
        try {
            val root = JsonObject()
            val arr = JsonArray()

            for (i in data) {
                if (i.phone.isNullOrEmpty()) {
                    continue
                }
                val temp = JsonObject()
                temp.addProperty("name", i.title)
                temp.addProperty("number", i.phone)
                temp.addProperty("peer_id", i.id)
                temp.addProperty("photo", i.photo)
                arr.add(temp)
            }

            root.add("contacts", arr)
            val bytes = GsonBuilder().setPrettyPrinting().create().toJson(root).toByteArray(
                StandardCharsets.UTF_8
            )
            out = FileOutputStream(file)
            out.write(bytes)
            out.flush()
            Includes.provideApplicationContext().sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)
                )
            )
            CustomToast.CreateCustomToast(context).showToast(
                R.string.saved_to_param_file_name,
                file.absolutePath
            )
        } catch (e: Exception) {
            CustomToast.CreateCustomToast(context).showToastError(e.localizedMessage)
        } finally {
            Utils.safelyClose(out)
        }
    }

    private fun updateCriteria() {
        dataSearch.clear()
        if (query.isNullOrEmpty()) {
            view?.displayData(data)
            view?.notifyDataSetChanged()
            return
        }
        for (i in data) {
            if (query?.lowercase(Locale.getDefault())?.let {
                    i.title?.lowercase(Locale.getDefault())?.contains(it)
                } == true
            ) {
                dataSearch.add(i)
            }
        }
        view?.displayData(dataSearch)
        view?.notifyDataSetChanged()
    }

    fun fireQuery(q: String?) {
        query = if (q.isNullOrEmpty()) null else {
            q
        }
        updateCriteria()
    }

    fun fireRefresh(context: Context) {
        if (query.trimmedNonNullNoEmpty()) {
            return
        }
        netLoadingNow = true
        resolveRefreshingView()
        appendDisposable(accountsInteractor.importMessagesContacts(accountId, context, 0, 1000)
            .fromIOToMain()
            .subscribe({ owners -> onActualDataReceived(owners) }) { t ->
                onActualDataGetError(
                    t
                )
            })
    }

    fun fireReset() {
        if (query.trimmedNonNullNoEmpty()) {
            return
        }
        netLoadingNow = true
        resolveRefreshingView()
        appendDisposable(accountsInteractor.resetMessagesContacts(accountId, 0, 1000)
            .fromIOToMain()
            .subscribe({ owners -> onActualDataReceived(owners) }) { t ->
                onActualDataGetError(
                    t
                )
            })
    }

    private fun onActualDataGetError(t: Throwable) {
        netLoadingNow = false
        resolveRefreshingView()
        showError(t)
    }

    private fun onActualDataReceived(owners: List<ContactConversation>) {
        netLoadingNow = false
        resolveRefreshingView()
        data.clear()
        data.addAll(owners)
        view?.notifyDataSetChanged()
    }

    override fun onGuiCreated(viewHost: IFriendsByPhonesView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(data)
        resolveRefreshingView()
    }

    fun fireRefresh() {
        if (query.trimmedNonNullNoEmpty()) {
            netLoadingNow = false
            resolveRefreshingView()
            return
        }
        requestActualData()
    }

    fun onUserOwnerClicked(owner: ContactConversation) {
        view?.showChat(
            accountId,
            owner
        )
    }

    init {
        data = ArrayList()
        dataSearch = ArrayList()
        this.context = context
        requestActualData()
    }
}