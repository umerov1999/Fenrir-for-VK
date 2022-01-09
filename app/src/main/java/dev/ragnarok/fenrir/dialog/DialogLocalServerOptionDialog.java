package dev.ragnarok.fenrir.dialog;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import dev.ragnarok.fenrir.R;

public class DialogLocalServerOptionDialog extends BottomSheetDialogFragment {

    private boolean isDiscography;
    private boolean isReverse;
    private MaterialButton toggleDiscography;
    private DialogLocalServerOptionListener listener;

    public static DialogLocalServerOptionDialog newInstance(boolean isDiscography, boolean isReverse, DialogLocalServerOptionListener listener) {
        DialogLocalServerOptionDialog dialog = new DialogLocalServerOptionDialog();
        dialog.listener = listener;
        dialog.isDiscography = isDiscography;
        dialog.isReverse = isReverse;
        return dialog;
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = View.inflate(requireActivity(), R.layout.dialog_local_server_options, null);

        SwitchMaterial scReverse = root.findViewById(R.id.reverse_time);
        toggleDiscography = root.findViewById(R.id.go_discography);
        MaterialButton sync = root.findViewById(R.id.local_server_sync);
        sync.setOnClickListener(v -> listener.onSync());
        scReverse.setChecked(isReverse);
        scReverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isReverse = isChecked;
            if (nonNull(listener)) {
                listener.onReverse(isChecked);
            }
        });
        toggleDiscography.setOnClickListener(v -> {
            isDiscography = !isDiscography;
            if (nonNull(listener)) {
                listener.onDiscography(isDiscography);
            }
            resolve();
        });
        resolve();
        return root;
    }

    private void resolve() {
        toggleDiscography.setText(isDiscography ? R.string.return_away : R.string.go_discography);
    }

    public interface DialogLocalServerOptionListener {
        void onReverse(boolean reverse);

        void onDiscography(boolean discography);

        void onSync();
    }
}
