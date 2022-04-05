package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.*
import dev.ragnarok.fenrir.*
import dev.ragnarok.fenrir.Constants.DEFAULT_ACCOUNT_TYPE
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityUtils.supportToolbarFor
import dev.ragnarok.fenrir.activity.EnterPinActivity
import dev.ragnarok.fenrir.activity.FileManagerSelectActivity
import dev.ragnarok.fenrir.activity.LoginActivity.Companion.createIntent
import dev.ragnarok.fenrir.activity.ProxyManagerActivity
import dev.ragnarok.fenrir.adapter.AccountAdapter
import dev.ragnarok.fenrir.api.Auth.scope
import dev.ragnarok.fenrir.db.DBHelper
import dev.ragnarok.fenrir.dialog.DirectAuthDialog
import dev.ragnarok.fenrir.dialog.DirectAuthDialog.Companion.newInstance
import dev.ragnarok.fenrir.domain.IAccountsInteractor
import dev.ragnarok.fenrir.domain.IOwnersRepository
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.domain.Repository.owners
import dev.ragnarok.fenrir.fragment.base.BaseFragment
import dev.ragnarok.fenrir.longpoll.LongpollInstance.longpollManager
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.Option
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest
import dev.ragnarok.fenrir.model.Account
import dev.ragnarok.fenrir.model.IOwnersBundle
import dev.ragnarok.fenrir.model.SaveAccount
import dev.ragnarok.fenrir.model.User
import dev.ragnarok.fenrir.place.PlaceFactory.getPreferencesPlace
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.backup.SettingsBackup
import dev.ragnarok.fenrir.util.AppPerms.hasReadStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.hasReadWriteStoragePermission
import dev.ragnarok.fenrir.util.AppPerms.requestPermissionsAbs
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.MessagesReplyItemCallback
import dev.ragnarok.fenrir.util.ShortcutUtils.createAccountShortcutRx
import dev.ragnarok.fenrir.util.Utils.getAppVersionName
import dev.ragnarok.fenrir.util.Utils.isHiddenAccount
import dev.ragnarok.fenrir.util.Utils.safelyClose
import dev.ragnarok.fenrir.util.ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.*

class AccountsFragment : BaseFragment(), View.OnClickListener, AccountAdapter.Callback {
    private val mCompositeDisposable = CompositeDisposable()
    private val requestPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            startExportAccounts()
        }
    }
    private val requestWritePermission = requestPermissionsAbs(
        arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    ) {
        if (Settings.get().security().isUsePinForSecurity) {
            requestPin.launch(Intent(requireActivity(), EnterPinActivity::class.java))
        } else {
            startExportAccounts()
        }
    }
    private val requestReadPermission = requestPermissionsAbs(
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    ) {
        startImportAccounts()
    }
    private var empty: TextView? = null
    private var mRecyclerView: RecyclerView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAdapter: AccountAdapter? = null
    private var temp_to_show = 0
    private val requestEnterPin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val restore = Gson().fromJson(
                Settings.get().accounts().getLogin(temp_to_show),
                SaveAccount::class.java
            )
            val password = requireActivity().getString(
                R.string.restore_login_info,
                restore.login,
                restore.password,
                Settings.get().accounts().getAccessToken(temp_to_show),
                restore.two_factor_auth
            )
            MaterialAlertDialogBuilder(requireActivity())
                .setMessage(password)
                .setTitle(R.string.login_password_hint)
                .setPositiveButton(R.string.button_ok, null)
                .setNeutralButton(R.string.full_data) { _: DialogInterface?, _: Int ->
                    val clipboard = requireActivity().getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager?
                    val clip = ClipData.newPlainText("response", password)
                    clipboard?.setPrimaryClip(clip)
                    CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard)
                }
                .setNegativeButton(R.string.copy_data) { _: DialogInterface?, _: Int ->
                    val clipboard = requireActivity().getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager?
                    val clip =
                        ClipData.newPlainText("response", restore.login + " " + restore.password)
                    clipboard?.setPrimaryClip(clip)
                    CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard)
                }
                .setCancelable(true)
                .show()
        }
    }
    private var mData: ArrayList<Account>? = null
    private var mOwnersInteractor: IOwnersRepository = owners
    private val requestLoginWeb = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val uid = result.data?.extras?.getInt(Extra.USER_ID)
            val token = result.data?.getStringExtra(Extra.TOKEN)
            val Login = result.data?.getStringExtra(Extra.LOGIN)
            val Password = result.data?.getStringExtra(Extra.PASSWORD)
            val TwoFA = result.data?.getStringExtra(Extra.TWO_FA)
            val isSave = result.data?.getBooleanExtra(Extra.SAVE, false)
            if (uid != null) {
                if (isSave != null) {
                    processNewAccount(
                        uid,
                        token,
                        DEFAULT_ACCOUNT_TYPE,
                        Login,
                        Password,
                        TwoFA,
                        true,
                        isSave
                    )
                }
            }
        }
    }

    private val importAccounts = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            try {
                val jbld = StringBuilder()
                val file = File(
                    result.data?.getStringExtra(Extra.PATH) ?: return@registerForActivityResult
                )
                if (file.exists()) {
                    val dataFromServerStream = FileInputStream(file)
                    val d = BufferedReader(
                        InputStreamReader(
                            dataFromServerStream,
                            StandardCharsets.UTF_8
                        )
                    )
                    while (d.ready()) jbld.append(d.readLine())
                    d.close()
                    val obj = JsonParser.parseString(jbld.toString()).asJsonObject
                    val reader = obj.getAsJsonArray("fenrir_accounts")
                    for (i in reader) {
                        val elem = i.asJsonObject
                        val id = elem["user_id"].asInt
                        if (Settings.get().accounts().registered.contains(id)) continue
                        val token = elem["access_token"].asString
                        val Type = elem["type"].asInt
                        processNewAccount(
                            id, token, Type, null, null, "fenrir_app",
                            isCurrent = false,
                            needSave = false
                        )
                        if (elem.has("login")) {
                            Settings.get().accounts().storeLogin(id, elem["login"].asString)
                        }
                        if (elem.has("device")) {
                            Settings.get().accounts().storeDevice(id, elem["device"].asString)
                        }
                    }
                    if (obj.has("settings")) {
                        SettingsBackup().doRestore(obj.getAsJsonObject("settings"))
                        CreateCustomToast(requireActivity()).setDuration(Toast.LENGTH_LONG)
                            .showToastSuccessBottom(
                                R.string.need_restart
                            )
                    }
                }
                CreateCustomToast(requireActivity()).showToast(
                    R.string.accounts_restored,
                    file.absolutePath
                )
            } catch (e: Exception) {
                CreateCustomToast(requireActivity()).showToastError(e.localizedMessage)
            }
        }
    }

    private val exportAccounts = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val file = File(
                result.data?.getStringExtra(Extra.PATH),
                "fenrir_accounts_backup.json"
            )
            appendDisposable(
                mOwnersInteractor.findBaseOwnersDataAsBundle(
                    Settings.get().accounts().current,
                    Settings.get().accounts().registered,
                    IOwnersRepository.MODE_ANY
                )
                    .fromIOToMain()
                    .subscribe({
                        SaveAccounts(
                            file,
                            it
                        )
                    }) { SaveAccounts(file, null) })
        }
    }


    private val accountsInteractor: IAccountsInteractor =
        InteractorFactory.createAccountInteractor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelableArrayList(SAVE_DATA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_accounts, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))
        empty = root.findViewById(R.id.empty)
        mRecyclerView = root.findViewById(R.id.list)
        mRecyclerView?.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
        mSwipeRefreshLayout = root.findViewById(R.id.refresh)
        mSwipeRefreshLayout?.setOnRefreshListener { load(true) }
        setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout)
        ItemTouchHelper(MessagesReplyItemCallback { o: Int ->
            if (mAdapter?.checkPosition(o) == true) {
                val account = mAdapter?.getByPosition(o) ?: return@MessagesReplyItemCallback
                val idCurrent = account.id == Settings.get()
                    .accounts()
                    .current
                if (!idCurrent) {
                    setAsActive(account)
                }
            }
        }).attachToRecyclerView(mRecyclerView)
        root.findViewById<View>(R.id.fab).setOnClickListener(this)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var firstRun = false
        if (mData == null) {
            mData = ArrayList()
            firstRun = true
        }
        mAdapter = AccountAdapter(requireActivity(), mData ?: Collections.emptyList(), this)
        mRecyclerView?.adapter = mAdapter
        if (firstRun) {
            load(false)
        }
        resolveEmptyText()

        parentFragmentManager.setFragmentResultListener(
            DirectAuthDialog.ACTION_LOGIN_VIA_WEB,
            this
        ) { _: String?, _: Bundle? -> startLoginViaWeb() }
        parentFragmentManager.setFragmentResultListener(
            DirectAuthDialog.ACTION_VALIDATE_VIA_WEB,
            this
        ) { _: String?, result: Bundle ->
            val url = result.getString(Extra.URL)
            val Login = result.getString(Extra.LOGIN)
            val Password = result.getString(Extra.PASSWORD)
            val TwoFA = result.getString(Extra.TWO_FA)
            val isSave = result.getBoolean(Extra.SAVE)
            startValidateViaWeb(url, Login, Password, TwoFA, isSave)
        }
        parentFragmentManager.setFragmentResultListener(
            DirectAuthDialog.ACTION_LOGIN_COMPLETE,
            this
        ) { _: String?, result: Bundle ->
            val uid = result.getInt(Extra.USER_ID)
            val token = result.getString(Extra.TOKEN)
            val Login = result.getString(Extra.LOGIN)
            val Password = result.getString(Extra.PASSWORD)
            val TwoFA = result.getString(Extra.TWO_FA)
            val isSave = result.getBoolean(Extra.SAVE)
            processNewAccount(
                uid,
                token,
                DEFAULT_ACCOUNT_TYPE,
                Login,
                Password,
                TwoFA,
                true,
                isSave
            )
        }
    }

    private fun resolveEmptyText() {
        if (!isAdded || empty == null) return
        empty?.visibility =
            if (mData.isNullOrEmpty()) View.VISIBLE else View.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        val actionBar = supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.title = null
            actionBar.subtitle = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(SAVE_DATA, mData)
    }

    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
    }

    private fun load(refresh: Boolean) {
        if (!refresh) {
            mSwipeRefreshLayout?.isRefreshing = true
        }
        mCompositeDisposable.add(accountsInteractor
            .getAll(refresh)
            .fromIOToMain()
            .subscribe({
                mSwipeRefreshLayout?.isRefreshing = false
                val sz = mData?.size ?: 0
                mData?.clear()
                mAdapter?.notifyItemRangeRemoved(0, sz)
                mData?.addAll(it)
                mAdapter?.notifyItemRangeInserted(0, it.size)
                resolveEmptyText()
                if (isAdded && mData.isNullOrEmpty()) {
                    requireActivity().invalidateOptionsMenu()
                    startDirectLogin()
                }
            }) { mSwipeRefreshLayout?.isRefreshing = false })
    }

    @Suppress("DEPRECATION")
    private fun startExportAccounts() {
        exportAccounts.launch(
            FileManagerSelectActivity.makeFileManager(
                requireActivity(),
                Environment.getExternalStorageDirectory().absolutePath,
                "dirs"
            )
        )
    }

    private fun indexOf(uid: Int): Int {
        mData.nonNullNoEmpty {
            for (i in it.indices) {
                if (it[i].id == uid) {
                    return i
                }
            }
        }
        return -1
    }

    private fun merge(account: Account) {
        val index = indexOf(account.id)
        mData?.let {
            if (index != -1) {
                it[index] = account
                mAdapter?.notifyItemChanged(index)
            } else {
                val sz = it.size
                it.add(account)
                mAdapter?.notifyItemInserted(sz)
            }
        }
        resolveEmptyText()
    }

    private fun processNewAccount(
        uid: Int,
        token: String?,
        @AccountType type: Int,
        Login: String?,
        Password: String?,
        TwoFA: String?,
        isCurrent: Boolean,
        needSave: Boolean
    ) {
        //Accounts account = new Accounts(token, uid);

        // важно!! Если мы получили новый токен, то необходимо удалить запись
        // о регистрации push-уведомлений
        //PushSettings.unregisterFor(getContext(), account);
        Settings.get()
            .accounts()
            .storeAccessToken(uid, token)
        Settings.get()
            .accounts().storeTokenType(uid, type)
        Settings.get()
            .accounts()
            .registerAccountId(uid, isCurrent)
        if (needSave) {
            val json = Gson().toJson(SaveAccount().set(Login, Password, TwoFA))
            Settings.get()
                .accounts()
                .storeLogin(uid, json)
        }
        merge(Account(uid, null))
        mCompositeDisposable.add(
            mOwnersInteractor.getBaseOwnerInfo(uid, uid, IOwnersRepository.MODE_ANY)
                .fromIOToMain()
                .subscribe({ merge(Account(uid, it)) }) { })
    }

    private fun startLoginViaWeb() {
        val intent = createIntent(requireActivity(), Constants.API_ID.toString(), scope)
        requestLoginWeb.launch(intent)
    }

    private fun startValidateViaWeb(
        url: String?,
        Login: String?,
        Password: String?,
        TwoFa: String?,
        needSave: Boolean
    ) {
        val intent = createIntent(requireActivity(), url, Login, Password, TwoFa, needSave)
        requestLoginWeb.launch(intent)
    }

    private fun startDirectLogin() {
        val auth = newInstance()
        auth.show(parentFragmentManager, "direct-login")
    }

    override fun onClick(v: View) {
        if (v.id == R.id.fab) {
            startDirectLogin()
        }
    }

    private fun delete(account: Account) {
        Settings.get()
            .accounts()
            .removeAccessToken(account.id)
        Settings.get()
            .accounts()
            .removeType(account.id)
        Settings.get()
            .accounts()
            .removeLogin(account.id)
        Settings.get()
            .accounts()
            .removeDevice(account.id)
        Settings.get()
            .accounts()
            .remove(account.id)
        DBHelper.removeDatabaseFor(requireActivity(), account.id)
        longpollManager.forceDestroy(account.id)
        val pos = indexOf(account.id)
        mData?.remove(account)
        mAdapter?.notifyItemRemoved(pos)
        resolveEmptyText()
    }

    private fun setAsActive(account: Account) {
        Settings.get()
            .accounts().current = account.id
        mAdapter?.notifyDataSetChanged()
    }

    override fun onClick(account: Account) {
        val idCurrent = account.id == Settings.get()
            .accounts()
            .current
        val menus = ModalBottomSheetDialogFragment.Builder()
        if (account.id > 0) {
            menus.add(
                OptionRequest(
                    0,
                    getString(R.string.delete),
                    R.drawable.ic_outline_delete,
                    true
                )
            )
            menus.add(
                OptionRequest(
                    1,
                    getString(R.string.add_to_home_screen),
                    R.drawable.plus,
                    false
                )
            )
            if (!Settings.get().accounts().getLogin(account.id).isNullOrEmpty()) {
                menus.add(
                    OptionRequest(
                        3,
                        getString(R.string.login_password_hint),
                        R.drawable.view,
                        true
                    )
                )
            }
            if (!idCurrent) {
                menus.add(
                    OptionRequest(
                        2,
                        getString(R.string.set_as_active),
                        R.drawable.account_circle,
                        false
                    )
                )
            }
        } else {
            menus.add(
                OptionRequest(
                    0,
                    getString(R.string.delete),
                    R.drawable.ic_outline_delete,
                    true
                )
            )
        }
        if (isHiddenAccount(account.id)) {
            menus.add(
                OptionRequest(
                    4,
                    getString(R.string.set_device),
                    R.drawable.ic_smartphone,
                    false
                )
            )
        }
        menus.header(
            account.displayName,
            R.drawable.account_circle,
            if (account.owner != null) account.owner.maxSquareAvatar else null
        )
        menus.show(
            childFragmentManager,
            "account_options",
            object : ModalBottomSheetDialogFragment.Listener {
                override fun onModalOptionSelected(option: Option) {
                    when (option.id) {
                        0 -> delete(account)
                        1 -> createShortcut(account)
                        2 -> setAsActive(account)
                        3 -> if (!Settings.get().security().isUsePinForSecurity) {
                            CreateCustomToast(requireActivity()).showToastError(R.string.not_supported_hide)
                        } else {
                            temp_to_show = account.id
                            requestEnterPin.launch(
                                Intent(
                                    requireActivity(),
                                    EnterPinActivity::class.java
                                )
                            )
                        }
                        4 -> {
                            val root =
                                View.inflate(requireActivity(), R.layout.dialog_enter_text, null)
                            (root.findViewById<View>(R.id.editText) as TextInputEditText).setText(
                                Settings.get().accounts().getDevice(account.id)
                            )
                            MaterialAlertDialogBuilder(requireActivity())
                                .setTitle(R.string.set_device)
                                .setCancelable(true)
                                .setView(root)
                                .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                                    Settings.get().accounts().storeDevice(
                                        account.id,
                                        (root.findViewById<View>(R.id.editText) as TextInputEditText).editableText.toString()
                                    )
                                }
                                .setNegativeButton(R.string.button_cancel, null)
                                .show()
                        }
                    }
                }
            })
    }

    @Suppress("DEPRECATION")
    private fun SaveAccounts(file: File, Users: IOwnersBundle?) {
        var out: FileOutputStream? = null
        try {
            val root = JsonObject()
            val arr = JsonArray()
            for (i in Settings.get().accounts().registered) {
                val temp = JsonObject()
                val owner = Users?.getById(i)
                temp.addProperty("user_name", owner?.fullName)
                temp.addProperty("user_id", i)
                temp.addProperty("type", Settings.get().accounts().getType(i))
                temp.addProperty("domain", owner?.domain)
                temp.addProperty("access_token", Settings.get().accounts().getAccessToken(i))
                temp.addProperty("avatar", owner?.maxSquareAvatar)
                val login = Settings.get().accounts().getLogin(i)
                val device = Settings.get().accounts().getDevice(i)
                if (!login.isNullOrEmpty()) {
                    temp.addProperty("login", login)
                }
                if (!device.isNullOrEmpty()) {
                    temp.addProperty("device", device)
                }
                arr.add(temp)
            }
            val app = JsonObject()
            app.addProperty("version", getAppVersionName(requireActivity()))
            app.addProperty("api_type", DEFAULT_ACCOUNT_TYPE)
            root.add("app", app)
            root.add("fenrir_accounts", arr)
            val settings = SettingsBackup().doBackup()
            if (settings != null) root.add("settings", settings)
            val bytes = GsonBuilder().setPrettyPrinting().create().toJson(root).toByteArray(
                StandardCharsets.UTF_8
            )
            out = FileOutputStream(file)
            out.write(bytes)
            out.flush()
            provideApplicationContext().sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)
                )
            )
            CreateCustomToast(requireActivity()).showToast(
                R.string.saved_to_param_file_name,
                file.absolutePath
            )
        } catch (e: Exception) {
            CreateCustomToast(requireActivity()).showToastError(e.localizedMessage)
        } finally {
            safelyClose(out)
        }
    }

    @Suppress("DEPRECATION")
    private fun startImportAccounts() {
        importAccounts.launch(
            FileManagerSelectActivity.makeFileManager(
                requireActivity(),
                Environment.getExternalStorageDirectory().absolutePath,
                "json"
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_proxy -> {
                startProxySettings()
                return true
            }
            R.id.action_preferences -> {
                getPreferencesPlace(
                    Settings.get().accounts().current
                ).tryOpenWith(requireActivity())
                return true
            }
            R.id.entry_account -> {
                val root = View.inflate(requireActivity(), R.layout.entry_account, null)
                MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.entry_account)
                    .setCancelable(true)
                    .setView(root)
                    .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                        try {
                            val id =
                                (root.findViewById<View>(R.id.edit_user_id) as TextInputEditText).text.toString()
                                    .trim { it <= ' ' }
                                    .toInt()
                            val access_token =
                                (root.findViewById<View>(R.id.edit_access_token) as TextInputEditText).text.toString()
                                    .trim { it <= ' ' }
                            val selected =
                                (root.findViewById<View>(R.id.access_token_type) as Spinner).selectedItemPosition
                            val types = intArrayOf(
                                AccountType.VK_ANDROID,
                                AccountType.KATE,
                                AccountType.VK_ANDROID_HIDDEN,
                                AccountType.KATE_HIDDEN
                            )
                            if (access_token.isNotEmpty() && id != 0 && selected >= 0 && selected < 3) {
                                processNewAccount(
                                    id,
                                    access_token,
                                    types[selected],
                                    null,
                                    null,
                                    "fenrir_app",
                                    isCurrent = false,
                                    needSave = false
                                )
                            }
                        } catch (ignored: NumberFormatException) {
                        }
                    }
                    .setNegativeButton(R.string.button_cancel, null)
                    .show()
                return true
            }
            R.id.export_accounts -> {
                if (Settings.get()
                        .accounts().registered.isEmpty()
                ) return true
                if (!hasReadWriteStoragePermission(requireActivity())) {
                    requestWritePermission.launch()
                    return true
                }
                if (Settings.get().security().isUsePinForSecurity) {
                    requestPin.launch(Intent(requireActivity(), EnterPinActivity::class.java))
                } else startExportAccounts()
                return true
            }
            R.id.import_accounts -> {
                if (!hasReadStoragePermission(requireActivity())) {
                    requestReadPermission.launch()
                    return true
                }
                startImportAccounts()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun startProxySettings() {
        startActivity(Intent(requireActivity(), ProxyManagerActivity::class.java))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_accounts, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.export_accounts).isVisible = ((mData?.size ?: 0) > 0)
    }

    private fun createShortcut(account: Account) {
        if (account.id < 0) {
            return  // this is community
        }
        val user = account.owner as User
        appendDisposable(
            createAccountShortcutRx(
                requireActivity(),
                account.id,
                account.displayName,
                user.maxSquareAvatar
            ).fromIOToMain().subscribe(
                {
                    Snackbar.make(
                        requireView(),
                        R.string.success,
                        BaseTransientBottomBar.LENGTH_LONG
                    ).setAnchorView(mRecyclerView).show()
                }
            ) { t ->
                t.localizedMessage?.let {
                    Snackbar.make(requireView(), it, BaseTransientBottomBar.LENGTH_LONG)
                        .setTextColor(
                            Color.WHITE
                        ).setBackgroundTint(Color.parseColor("#eeff0000"))
                        .setAnchorView(mRecyclerView)
                        .show()
                }
            })
    }

    companion object {
        private const val SAVE_DATA = "save_data"
    }
}