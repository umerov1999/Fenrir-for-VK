package dev.ragnarok.fenrir.dialog.base;

import androidx.fragment.app.DialogFragment;

import dev.ragnarok.fenrir.util.ViewUtils;

public abstract class BaseDialogFragment extends DialogFragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        ViewUtils.keyboardHide(requireActivity());
    }
}