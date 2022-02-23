package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.Constants.DEFAULT_ACCOUNT_TYPE;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.EnterPinActivity;
import dev.ragnarok.fenrir.activity.LoginActivity;
import dev.ragnarok.fenrir.activity.ProxyManagerActivity;
import dev.ragnarok.fenrir.adapter.AccountAdapter;
import dev.ragnarok.fenrir.api.Auth;
import dev.ragnarok.fenrir.db.DBHelper;
import dev.ragnarok.fenrir.dialog.DirectAuthDialog;
import dev.ragnarok.fenrir.domain.IAccountsInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.filepicker.model.DialogConfigs;
import dev.ragnarok.fenrir.filepicker.model.DialogProperties;
import dev.ragnarok.fenrir.filepicker.view.FilePickerDialog;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.longpoll.LongpollInstance;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Account;
import dev.ragnarok.fenrir.model.IOwnersBundle;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.backup.SettingsBackup;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.MessagesReplyItemCallback;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.ShortcutUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AccountsFragment extends BaseFragment implements View.OnClickListener, AccountAdapter.Callback {

    private static final String SAVE_DATA = "save_data";
    private static final String REQUEST_EXPORT_ACCOUNT = "export_account";
    private static final String REQUEST_IMPORT_ACCOUNT = "import_account";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final ActivityResultLauncher<Intent> requestPin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    startExportAccounts();
                }
            });
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> {
                if (Settings.get().security().isUsePinForSecurity()) {
                    requestPin.launch(new Intent(requireActivity(), EnterPinActivity.class));
                } else {
                    startExportAccounts();
                }
            });
    private final AppPerms.doRequestPermissions requestReadPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
            this::startImportAccounts);
    private TextView empty;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AccountAdapter mAdapter;
    private int temp_to_show;
    private final ActivityResultLauncher<Intent> requestEnterPin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    SaveAccount restore = new Gson().fromJson(Settings.get().accounts().getLogin(temp_to_show), SaveAccount.class);

                    String password = requireActivity().getString(R.string.restore_login_info, restore.login, restore.password, Settings.get().accounts().getAccessToken(temp_to_show), restore.two_factor_auth);
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setMessage(password)
                            .setTitle(R.string.login_password_hint)
                            .setPositiveButton(R.string.button_ok, null)
                            .setNeutralButton(R.string.full_data, (dialog, which) -> {
                                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("response", password);
                                clipboard.setPrimaryClip(clip);
                                CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard);
                            })
                            .setNegativeButton(R.string.copy_data, (dialog, which) -> {
                                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("response", restore.login + " " + restore.password);
                                clipboard.setPrimaryClip(clip);
                                CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.copied_to_clipboard);
                            })
                            .setCancelable(true)
                            .show();
                }
            });
    private ArrayList<Account> mData;
    private IOwnersRepository mOwnersInteractor;
    private final ActivityResultLauncher<Intent> requestLoginWeb = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    int uid = result.getData().getExtras().getInt(Extra.USER_ID);
                    String token = result.getData().getStringExtra(Extra.TOKEN);
                    String Login = result.getData().getStringExtra(Extra.LOGIN);
                    String Password = result.getData().getStringExtra(Extra.PASSWORD);
                    String TwoFA = result.getData().getStringExtra(Extra.TWO_FA);
                    boolean isSave = result.getData().getBooleanExtra(Extra.SAVE, false);
                    processNewAccount(uid, token, DEFAULT_ACCOUNT_TYPE, Login, Password, TwoFA, true, isSave);
                }
            });
    private IAccountsInteractor accountsInteractor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mOwnersInteractor = Repository.INSTANCE.getOwners();
        accountsInteractor = InteractorFactory.createAccountInteractor();

        if (savedInstanceState != null) {
            mData = savedInstanceState.getParcelableArrayList(SAVE_DATA);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_accounts, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        empty = root.findViewById(R.id.empty);
        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> load(true));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        new ItemTouchHelper(new MessagesReplyItemCallback(o -> {
            if (mAdapter.checkPosition(o)) {
                Account account = mAdapter.getByPosition(o);
                boolean idCurrent = account.getId() == Settings.get()
                        .accounts()
                        .getCurrent();
                if (!idCurrent) {
                    setAsActive(account);
                }
            }
        })).attachToRecyclerView(mRecyclerView);

        root.findViewById(R.id.fab).setOnClickListener(this);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new AccountAdapter(requireActivity(), mData, this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            load(false);
        }

        resolveEmptyText();

        getParentFragmentManager().setFragmentResultListener(REQUEST_EXPORT_ACCOUNT, this, (requestKey, result) -> {
            File file = new File(result.getStringArray(FilePickerDialog.RESULT_VALUE)[0], "fenrir_accounts_backup.json");
            appendDisposable(mOwnersInteractor.findBaseOwnersDataAsBundle(Settings.get().accounts().getCurrent(), Settings.get().accounts().getRegistered(), IOwnersRepository.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(userInfo -> SaveAccounts(file, userInfo), throwable -> SaveAccounts(file, null)));
        });

        getParentFragmentManager().setFragmentResultListener(REQUEST_IMPORT_ACCOUNT, this, (requestKey, result) -> {
            try {
                StringBuilder jbld = new StringBuilder();
                File file = new File(result.getStringArray(FilePickerDialog.RESULT_VALUE)[0]);
                if (file.exists()) {
                    FileInputStream dataFromServerStream = new FileInputStream(file);
                    BufferedReader d = new BufferedReader(new InputStreamReader(dataFromServerStream, StandardCharsets.UTF_8));
                    while (d.ready())
                        jbld.append(d.readLine());
                    d.close();
                    JsonObject obj = JsonParser.parseString(jbld.toString()).getAsJsonObject();
                    JsonArray reader = obj.getAsJsonArray("fenrir_accounts");
                    for (JsonElement i : reader) {
                        JsonObject elem = i.getAsJsonObject();
                        int id = elem.get("user_id").getAsInt();
                        if (Settings.get().accounts().getRegistered().contains(id))
                            continue;
                        String token = elem.get("access_token").getAsString();
                        int Type = elem.get("type").getAsInt();
                        processNewAccount(id, token, Type, null, null, "fenrir_app", false, false);
                        if (elem.has("login")) {
                            Settings.get().accounts().storeLogin(id, elem.get("login").getAsString());
                        }
                        if (elem.has("device")) {
                            Settings.get().accounts().storeDevice(id, elem.get("device").getAsString());
                        }
                    }
                    if (obj.has("settings")) {
                        new SettingsBackup().doRestore(obj.getAsJsonObject("settings"));
                        CustomToast.CreateCustomToast(requireActivity()).setDuration(Toast.LENGTH_LONG).showToastSuccessBottom((R.string.need_restart));
                    }
                }
                CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.accounts_restored, file.getAbsolutePath());
            } catch (Exception e) {
                CustomToast.CreateCustomToast(requireActivity()).showToastError(e.getLocalizedMessage());
            }
        });
        getParentFragmentManager().setFragmentResultListener(DirectAuthDialog.ACTION_LOGIN_VIA_WEB, this, (requestKey, result) -> startLoginViaWeb());
        getParentFragmentManager().setFragmentResultListener(DirectAuthDialog.ACTION_VALIDATE_VIA_WEB, this, (requestKey, result) -> {
            String url = result.getString(Extra.URL);
            String Login = result.getString(Extra.LOGIN);
            String Password = result.getString(Extra.PASSWORD);
            String TwoFA = result.getString(Extra.TWO_FA);
            boolean isSave = result.getBoolean(Extra.SAVE);
            startValidateViaWeb(url, Login, Password, TwoFA, isSave);
        });
        getParentFragmentManager().setFragmentResultListener(DirectAuthDialog.ACTION_LOGIN_COMPLETE, this, (requestKey, result) -> {
            int uid = result.getInt(Extra.USER_ID);
            String token = result.getString(Extra.TOKEN);
            String Login = result.getString(Extra.LOGIN);
            String Password = result.getString(Extra.PASSWORD);
            String TwoFA = result.getString(Extra.TWO_FA);
            boolean isSave = result.getBoolean(Extra.SAVE);
            processNewAccount(uid, token, DEFAULT_ACCOUNT_TYPE, Login, Password, TwoFA, true, isSave);
        });
    }

    private void resolveEmptyText() {
        if (!isAdded() || empty == null) return;
        empty.setVisibility(Utils.safeIsEmpty(mData) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setSubtitle(null);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_DATA, mData);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    private void load(boolean refresh) {
        if (!refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        mCompositeDisposable.add(accountsInteractor
                .getAll(refresh)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(appAccounts -> {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mData.clear();
                    mData.addAll(appAccounts);

                    if (Objects.nonNull(mAdapter)) {
                        mAdapter.notifyDataSetChanged();
                    }

                    resolveEmptyText();
                    if (isAdded() && Utils.safeIsEmpty(mData)) {
                        requireActivity().invalidateOptionsMenu();
                        startDirectLogin();
                    }
                }, e -> mSwipeRefreshLayout.setRefreshing(false)));
    }

    private void startExportAccounts() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.error_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.offset = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.extensions = null;
        properties.show_hidden_files = true;
        properties.tittle = R.string.export_accounts;
        properties.request = REQUEST_EXPORT_ACCOUNT;
        FilePickerDialog.newInstance(properties).show(getParentFragmentManager(), "ExportAccount");
    }

    private int indexOf(int uid) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).getId() == uid) {
                return i;
            }
        }

        return -1;
    }

    private void merge(Account account) {
        int index = indexOf(account.getId());

        if (index != -1) {
            mData.set(index, account);
        } else {
            mData.add(account);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }

        resolveEmptyText();
    }

    private void processNewAccount(int uid, String token, @AccountType int type, String Login, String Password, String TwoFA, boolean isCurrent, boolean needSave) {
        //Accounts account = new Accounts(token, uid);

        // важно!! Если мы получили новый токен, то необходимо удалить запись
        // о регистрации push-уведомлений
        //PushSettings.unregisterFor(getContext(), account);

        Settings.get()
                .accounts()
                .storeAccessToken(uid, token);

        Settings.get()
                .accounts().storeTokenType(uid, type);

        Settings.get()
                .accounts()
                .registerAccountId(uid, isCurrent);

        if (needSave) {
            String json = new Gson().toJson(new SaveAccount().set(Login, Password, TwoFA));
            Settings.get()
                    .accounts()
                    .storeLogin(uid, json);
        }

        merge(new Account(uid, null));

        mCompositeDisposable.add(mOwnersInteractor.getBaseOwnerInfo(uid, uid, IOwnersRepository.MODE_ANY)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owner -> merge(new Account(uid, owner)), t -> {/*ignored*/}));
    }

    private void startLoginViaWeb() {
        Intent intent = LoginActivity.createIntent(requireActivity(), String.valueOf(Constants.API_ID), Auth.getScope());
        requestLoginWeb.launch(intent);
    }

    private void startValidateViaWeb(String url, String Login, String Password, String TwoFa, boolean needSave) {
        Intent intent = LoginActivity.createIntent(requireActivity(), url, Login, Password, TwoFa, needSave);
        requestLoginWeb.launch(intent);
    }

    private void startDirectLogin() {
        DirectAuthDialog auth = DirectAuthDialog.newInstance();
        auth.show(getParentFragmentManager(), "direct-login");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) {
            startDirectLogin();
        }
    }

    private void delete(Account account) {
        Settings.get()
                .accounts()
                .removeAccessToken(account.getId());

        Settings.get()
                .accounts()
                .removeType(account.getId());

        Settings.get()
                .accounts()
                .removeLogin(account.getId());

        Settings.get()
                .accounts()
                .removeDevice(account.getId());

        Settings.get()
                .accounts()
                .remove(account.getId());

        DBHelper.removeDatabaseFor(requireActivity(), account.getId());

        LongpollInstance.get().forceDestroy(account.getId());

        mData.remove(account);
        mAdapter.notifyDataSetChanged();
        resolveEmptyText();
    }

    private void setAsActive(Account account) {
        Settings.get()
                .accounts()
                .setCurrent(account.getId());

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Account account) {
        boolean idCurrent = account.getId() == Settings.get()
                .accounts()
                .getCurrent();

        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        if (account.getId() > 0) {
            menus.add(new OptionRequest(0, getString(R.string.delete), R.drawable.ic_outline_delete, true));
            menus.add(new OptionRequest(1, getString(R.string.add_to_home_screen), R.drawable.plus, false));
            if (!Utils.isEmpty(Settings.get().accounts().getLogin(account.getId()))) {
                menus.add(new OptionRequest(3, getString(R.string.login_password_hint), R.drawable.view, true));
            }
            if (!idCurrent) {
                menus.add(new OptionRequest(2, getString(R.string.set_as_active), R.drawable.account_circle, false));
            }
        } else {
            menus.add(new OptionRequest(0, getString(R.string.delete), R.drawable.ic_outline_delete, true));
        }
        if (Utils.isHiddenAccount(account.getId())) {
            menus.add(new OptionRequest(4, getString(R.string.set_device), R.drawable.ic_smartphone, false));
        }
        menus.header(account.getDisplayName(), R.drawable.account_circle, account.getOwner() != null ? account.getOwner().getMaxSquareAvatar() : null);
        menus.show(getChildFragmentManager(), "account_options", option -> {
            switch (option.getId()) {
                case 0:
                    delete(account);
                    break;
                case 1:
                    createShortcut(account);
                    break;
                case 2:
                    setAsActive(account);
                    break;
                case 3:
                    if (!Settings.get().security().isUsePinForSecurity()) {
                        CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.not_supported_hide);
                    } else {
                        temp_to_show = account.getId();
                        requestEnterPin.launch(new Intent(requireActivity(), EnterPinActivity.class));
                    }
                    break;
                case 4:
                    View root = View.inflate(requireActivity(), R.layout.dialog_enter_text, null);
                    ((TextInputEditText) root.findViewById(R.id.editText)).setText(Settings.get().accounts().getDevice(account.getId()));
                    new MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(R.string.set_device)
                            .setCancelable(true)
                            .setView(root)
                            .setPositiveButton(R.string.button_ok, (dialog, which) -> Settings.get().accounts().storeDevice(account.getId(), ((TextInputEditText) root.findViewById(R.id.editText)).getEditableText().toString()))
                            .setNegativeButton(R.string.button_cancel, null)
                            .show();
                    break;
            }
        });
    }

    private void SaveAccounts(File file, IOwnersBundle Users) {
        FileOutputStream out = null;
        try {
            JsonObject root = new JsonObject();
            JsonArray arr = new JsonArray();
            for (int i : Settings.get().accounts().getRegistered()) {
                JsonObject temp = new JsonObject();

                Owner owner = Users.getById(i);
                temp.addProperty("user_name", owner.getFullName());
                temp.addProperty("user_id", i);
                temp.addProperty("type", Settings.get().accounts().getType(i));
                temp.addProperty("domain", owner.getDomain());
                temp.addProperty("access_token", Settings.get().accounts().getAccessToken(i));
                temp.addProperty("avatar", owner.getMaxSquareAvatar());

                String login = Settings.get().accounts().getLogin(i);
                String device = Settings.get().accounts().getDevice(i);
                if (!Utils.isEmpty(login)) {
                    temp.addProperty("login", login);
                }
                if (!Utils.isEmpty(device)) {
                    temp.addProperty("device", device);
                }
                arr.add(temp);
            }
            JsonObject app = new JsonObject();
            app.addProperty("version", Utils.getAppVersionName(requireActivity()));
            app.addProperty("api_type", DEFAULT_ACCOUNT_TYPE);
            root.add("app", app);
            root.add("fenrir_accounts", arr);
            JsonObject settings = new SettingsBackup().doBackup();
            if (settings != null)
                root.add("settings", settings);
            byte[] bytes = new GsonBuilder().setPrettyPrinting().create().toJson(root).getBytes(StandardCharsets.UTF_8);
            out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();
            Injection.provideApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.saved_to_param_file_name, file.getAbsolutePath());
        } catch (Exception e) {
            CustomToast.CreateCustomToast(requireActivity()).showToastError(e.getLocalizedMessage());
        } finally {
            Utils.safelyClose(out);
        }
    }

    private void startImportAccounts() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.error_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.offset = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.extensions = new String[]{"json"};
        properties.show_hidden_files = true;
        properties.tittle = R.string.import_accounts;
        properties.request = REQUEST_IMPORT_ACCOUNT;
        FilePickerDialog.newInstance(properties).show(getParentFragmentManager(), "ImportAccount");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_proxy) {
            startProxySettings();
            return true;
        } else if (item.getItemId() == R.id.action_preferences) {
            PlaceFactory.getPreferencesPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
            return true;
        } else if (item.getItemId() == R.id.entry_account) {
            View root = View.inflate(requireActivity(), R.layout.entry_account, null);
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.entry_account)
                    .setCancelable(true)
                    .setView(root)
                    .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                        try {
                            int id = Integer.parseInt(((TextInputEditText) root.findViewById(R.id.edit_user_id)).getText().toString().trim());
                            String access_token = ((TextInputEditText) root.findViewById(R.id.edit_access_token)).getText().toString().trim();
                            int selected = ((Spinner) root.findViewById(R.id.access_token_type)).getSelectedItemPosition();
                            int[] types = {AccountType.VK_ANDROID, AccountType.KATE, AccountType.VK_ANDROID_HIDDEN, AccountType.KATE_HIDDEN};
                            if (!Utils.isEmpty(access_token) && id != 0 && selected >= 0 && selected < 3) {
                                processNewAccount(id, access_token, types[selected], null, null, "fenrir_app", false, false);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, null)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.export_accounts) {
            if (Settings.get().accounts() == null || Settings.get().accounts().getRegistered() == null || Settings.get().accounts().getRegistered().size() <= 0)
                return true;
            if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                requestWritePermission.launch();
                return true;
            }
            if (Settings.get().security().isUsePinForSecurity()) {
                requestPin.launch(new Intent(requireActivity(), EnterPinActivity.class));
            } else
                startExportAccounts();
            return true;
        } else if (item.getItemId() == R.id.import_accounts) {
            if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                requestReadPermission.launch();
                return true;
            }
            startImportAccounts();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startProxySettings() {
        startActivity(new Intent(requireActivity(), ProxyManagerActivity.class));
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_accounts, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.export_accounts).setVisible(mData.size() > 0);
    }

    private void createShortcut(Account account) {
        if (account.getId() < 0) {
            return; // this is community
        }

        User user = (User) account.getOwner();

        appendDisposable(ShortcutUtils.createAccountShortcutRx(requireActivity(), account.getId(), account.getDisplayName(), user == null ? null : user.getMaxSquareAvatar()).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> Snackbar.make(requireView(), R.string.success, BaseTransientBottomBar.LENGTH_LONG).setAnchorView(mRecyclerView).show(),
                t -> Snackbar.make(requireView(), t.getLocalizedMessage(), BaseTransientBottomBar.LENGTH_LONG).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000")).setAnchorView(mRecyclerView).show()));
    }

    @Keep
    private static class SaveAccount {
        @SerializedName("login")
        String login;
        @SerializedName("password")
        String password;
        @SerializedName("two_factor_auth")
        String two_factor_auth;

        SaveAccount set(String login, String password, String two_factor_auth) {
            this.login = login;
            this.password = password;
            this.two_factor_auth = two_factor_auth;
            return this;
        }
    }
}
