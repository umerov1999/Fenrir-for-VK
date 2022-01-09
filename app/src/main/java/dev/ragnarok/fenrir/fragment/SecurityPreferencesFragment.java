package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collection;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.CreatePinActivity;
import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.SecuritySettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.view.MySearchView;

public class SecurityPreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private final ActivityResultLauncher<Intent> requestChangePin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    int[] values = CreatePinFragment.extractValueFromIntent(result.getData());
                    Settings.get()
                            .security()
                            .setPin(values);
                }
            });
    private SwitchPreference mUsePinForSecurityPreference;
    private final ActivityResultLauncher<Intent> requestCreatePin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    int[] values = CreatePinFragment.extractValueFromIntent(result.getData());
                    Settings.get()
                            .security()
                            .setPin(values);
                    mUsePinForSecurityPreference.setChecked(true);
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        assert root != null;
        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Preference pref = findPreferenceByName(query);
                if (nonNull(pref)) {
                    scrollToPreference(pref);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Preference pref = findPreferenceByName(newText);
                if (nonNull(pref)) {
                    scrollToPreference(pref);
                }
                return false;
            }
        });
        searchView.setRightButtonVisibility(false);
        searchView.setLeftIcon(R.drawable.magnify);
        searchView.setQuery("", true);
        return root;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.preference_fenrir_list_fragment;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.security_settings);

        mUsePinForSecurityPreference = findPreference(SecuritySettings.KEY_USE_PIN_FOR_SECURITY);
        mUsePinForSecurityPreference.setOnPreferenceChangeListener(this);

        Preference changePinPreference = findPreference(SecuritySettings.KEY_CHANGE_PIN);
        changePinPreference.setOnPreferenceClickListener(preference -> {
            requestChangePin.launch(new Intent(requireActivity(), CreatePinActivity.class));
            return true;
        });

        Preference clearKeysPreference = findPreference(SecuritySettings.KEY_DELETE_KEYS);
        AssertUtils.requireNonNull(clearKeysPreference);
        clearKeysPreference.setOnPreferenceClickListener(preference -> {
            onClearKeysClick();
            return true;
        });

        findPreference("encryption_terms_of_use").setOnPreferenceClickListener(preference -> {
            View view = View.inflate(requireActivity(), R.layout.content_encryption_terms_of_use, null);
            new MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .setTitle(R.string.fenrir_encryption)
                    .setNegativeButton(R.string.button_cancel, null)
                    .show();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
    }

    private void onClearKeysClick() {
        String[] items = {getString(R.string.for_the_current_account), getString(R.string.for_all_accounts)};
        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialog, which) -> onClearKeysClick(which == 1))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void onClearKeysClick(boolean allAccount) {
        if (allAccount) {
            Collection<Integer> accountIds = Settings.get()
                    .accounts()
                    .getRegistered();

            for (Integer accountId : accountIds) {
                removeKeysFor(accountId);
            }
        } else {
            int currentAccountId = Settings.get()
                    .accounts()
                    .getCurrent();

            if (ISettings.IAccountsSettings.INVALID_ID != currentAccountId) {
                removeKeysFor(currentAccountId);
            }
        }

        Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show();
    }

    private void removeKeysFor(int accountId) {
        Stores.getInstance()
                .keys(KeyLocationPolicy.PERSIST)
                .deleteAll(accountId)
                .blockingAwait();

        Stores.getInstance()
                .keys(KeyLocationPolicy.RAM)
                .deleteAll(accountId)
                .blockingAwait();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setSubtitle(R.string.security);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_SETTINGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    private void startCreatePinActivity() {
        requestCreatePin.launch(new Intent(requireActivity(), CreatePinActivity.class));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (SecuritySettings.KEY_USE_PIN_FOR_SECURITY.equals(preference.getKey())) {
            Boolean usePinForSecurity = (Boolean) newValue;
            if (usePinForSecurity) {
                if (!Settings.get().security().hasPinHash()) {
                    startCreatePinActivity();
                    return false;
                } else {
                    // при вызове mUsePinForSecurityPreference.setChecked(true) мы опять попадем в этот блок
                    return true;
                }
            } else {
                Settings.get().security().setPin(null);
                return true;
            }
        }

        return false;
    }
}
