package dev.ragnarok.fenrir.dialog;

import static dev.ragnarok.fenrir.settings.NotificationsPrefs.FLAG_HIGH_PRIORITY;
import static dev.ragnarok.fenrir.settings.NotificationsPrefs.FLAG_LED;
import static dev.ragnarok.fenrir.settings.NotificationsPrefs.FLAG_SHOW_NOTIF;
import static dev.ragnarok.fenrir.settings.NotificationsPrefs.FLAG_SOUND;
import static dev.ragnarok.fenrir.settings.NotificationsPrefs.FLAG_VIBRO;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.hasFlag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.settings.Settings;

public class DialogNotifOptionsDialog extends BottomSheetDialogFragment {

    protected int mask;
    private int peerId;
    private int accountId;
    private SwitchMaterial scEnable;
    private SwitchMaterial scHighPriority;
    private SwitchMaterial scSound;
    private SwitchMaterial scVibro;
    private SwitchMaterial scLed;
    private Listener listener;

    public static DialogNotifOptionsDialog newInstance(int aid, int peerId, Listener listener) {
        Bundle args = new Bundle();
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        DialogNotifOptionsDialog dialog = new DialogNotifOptionsDialog();
        dialog.listener = listener;
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        peerId = requireArguments().getInt(Extra.PEER_ID);

        mask = Settings.get()
                .notifications()
                .getNotifPref(accountId, peerId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = View.inflate(requireActivity(), R.layout.dialog_dialog_options, null);

        scEnable = root.findViewById(R.id.enable);
        scHighPriority = root.findViewById(R.id.priority);
        scSound = root.findViewById(R.id.sound);
        scVibro = root.findViewById(R.id.vibro);
        scLed = root.findViewById(R.id.led);
        MaterialButton save = root.findViewById(R.id.buttonSave);
        MaterialButton restore = root.findViewById(R.id.button_restore);

        scEnable.setChecked(hasFlag(mask, FLAG_SHOW_NOTIF));
        scEnable.setOnCheckedChangeListener((buttonView, isChecked) -> resolveOtherSwitches());

        scSound.setChecked(hasFlag(mask, FLAG_SOUND));
        scHighPriority.setChecked(hasFlag(mask, FLAG_HIGH_PRIORITY));
        scVibro.setChecked(hasFlag(mask, FLAG_VIBRO));
        scLed.setChecked(hasFlag(mask, FLAG_LED));
        save.setOnClickListener(v -> {
            onSaveClick();
            if (nonNull(listener)) {
                listener.onSelected();
            }
            dismiss();
        });
        restore.setOnClickListener(v -> {
            Settings.get()
                    .notifications()
                    .setDefault(accountId, peerId);
            if (nonNull(listener)) {
                listener.onSelected();
            }
            dismiss();
        });
        resolveOtherSwitches();
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    private void onSaveClick() {
        int newMask = 0;
        if (scEnable.isChecked()) {
            newMask += FLAG_SHOW_NOTIF;
        }

        if (scHighPriority.isChecked()) {
            newMask += FLAG_HIGH_PRIORITY;
        }

        if (scSound.isChecked()) {
            newMask += FLAG_SOUND;
        }

        if (scVibro.isChecked()) {
            newMask += FLAG_VIBRO;
        }

        if (scLed.isChecked()) {
            newMask += FLAG_LED;
        }

        Settings.get()
                .notifications()
                .setNotifPref(accountId, peerId, newMask);
    }

    private void resolveOtherSwitches() {
        boolean enable = scEnable.isChecked();
        scHighPriority.setEnabled(enable);
        scSound.setEnabled(enable);
        scVibro.setEnabled(enable);
        scLed.setEnabled(enable);
    }

    public interface Listener {
        void onSelected();
    }
}
