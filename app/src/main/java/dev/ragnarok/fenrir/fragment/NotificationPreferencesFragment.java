package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.view.MySearchView;

public class NotificationPreferencesFragment extends PreferenceFragmentCompat {

    private static final String TAG = NotificationPreferencesFragment.class.getSimpleName();
    private final ActivityResultLauncher<Intent> requestRingTone = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String uri = result.getData().getData().getPath();
                    Settings.get()
                            .notifications()
                            .setNotificationRingtoneUri(uri);
                }
            });
    private Ringtone current;
    private int selection;

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

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
        addPreferencesFromResource(R.xml.notication_settings);

        findPreference("notif_sound").setOnPreferenceClickListener(preference -> {
            showAlertDialog();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
    }

    private void stopRingtoneIfExist() {
        if (current != null && current.isPlaying()) {
            current.stop();
        }
    }

    private void showAlertDialog() {
        Map<String, String> ringrones = getNotifications();

        Set<String> keys = ringrones.keySet();
        String[] array = keys.toArray(new String[0]);

        String selectionKey = getKeyByValue(ringrones, Settings.get()
                .notifications()
                .getNotificationRingtone());

        selection = Arrays.asList(array).indexOf(selectionKey);

        new MaterialAlertDialogBuilder(requireActivity()).setSingleChoiceItems(array, selection, (dialog, which) -> {
            selection = which;
            stopRingtoneIfExist();
            String title = array[which];
            String uri = ringrones.get(title);
            Ringtone r = RingtoneManager.getRingtone(requireActivity(), Uri.parse(uri));
            current = r;
            r.play();
        }).setPositiveButton(R.string.button_ok, (dialog, which) -> {
            if (selection == -1) {
                Toast.makeText(requireActivity(), R.string.ringtone_not_selected, Toast.LENGTH_SHORT).show();
            } else {
                String title = array[selection];
                Settings.get()
                        .notifications()
                        .setNotificationRingtoneUri(ringrones.get(title));
                stopRingtoneIfExist();
            }
        })
                .setNegativeButton(R.string.cancel, (dialog, which) -> stopRingtoneIfExist())
                .setNeutralButton(R.string.ringtone_custom, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file/audio");
                    requestRingTone.launch(intent);
                }).setOnDismissListener(dialog -> stopRingtoneIfExist()).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRingtoneIfExist();
    }

    public Map<String, String> getNotifications() {
        RingtoneManager manager = new RingtoneManager(requireActivity());
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();
        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            Uri notificationUri = manager.getRingtoneUri(cursor.getPosition());
            list.put(notificationTitle, notificationUri.toString());
        }

        list.put(getString(R.string.ringtone_vk), Settings.get()
                .notifications()
                .getDefNotificationRingtone());
        return list;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setSubtitle(R.string.notif_setting_title);
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
}
