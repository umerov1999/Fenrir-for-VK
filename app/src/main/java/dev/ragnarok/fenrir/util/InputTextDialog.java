package dev.ragnarok.fenrir.util;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.R;

public class InputTextDialog {

    private Context context;
    private int inputType;
    private int titleRes;
    private String value;
    private boolean allowEmpty;
    private TextView target;
    private Callback callback;
    private Validator validator;
    private Integer hint;
    private DialogInterface.OnDismissListener onDismissListener;

    private InputTextDialog() {
        // not instantiate class
    }

    public void show() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle(titleRes);
        View view = View.inflate(context, R.layout.dialog_enter_text, null);

        TextInputEditText input = view.findViewById(R.id.editText);
        input.setText(value);
        input.setSelection(input.getText().length());

        if (hint != null) {
            input.setHint(hint);
        }

        input.setInputType(inputType);
        builder.setView(view);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> {
            Button b = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

            b.setOnClickListener(view1 -> {
                input.setError(null);
                String newValue = input.getText().toString().trim();

                if (TextUtils.isEmpty(newValue) && !allowEmpty) {
                    input.setError(context.getString(R.string.field_is_required));
                    input.requestFocus();
                } else {
                    try {
                        if (validator != null) {
                            validator.validate(newValue);
                        }

                        if (callback != null) {
                            callback.onChanged(newValue);
                        }

                        if (target != null) {
                            target.setText(newValue);
                        }

                        alertDialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        input.setError(e.getMessage());
                        input.requestFocus();
                    }
                }
            });
        });

        alertDialog.setOnDismissListener(onDismissListener);
        alertDialog.show();

        input.post(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });
    }

    public interface Callback {
        void onChanged(String newValue);
    }

    public interface Validator {
        void validate(String value) throws IllegalArgumentException;
    }

    public static class Builder {

        private final Context context;
        private int inputType;
        private int titleRes;
        private String value;
        private boolean allowEmpty;
        private TextView target;
        private Callback callback;
        private Validator validator;
        private Integer hint;
        private DialogInterface.OnDismissListener onDismissListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setInputType(int inputType) {
            this.inputType = inputType;
            return this;
        }

        public Builder setTitleRes(int titleRes) {
            this.titleRes = titleRes;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setAllowEmpty(boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
            return this;
        }

        public Builder setTarget(TextView target) {
            this.target = target;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setValidator(Validator validator) {
            this.validator = validator;
            return this;
        }

        public InputTextDialog create() {
            InputTextDialog inputTextDialog = new InputTextDialog();
            inputTextDialog.context = context;
            inputTextDialog.inputType = inputType;
            inputTextDialog.titleRes = titleRes;
            inputTextDialog.value = value;
            inputTextDialog.allowEmpty = allowEmpty;
            inputTextDialog.target = target;
            inputTextDialog.callback = callback;
            inputTextDialog.validator = validator;
            inputTextDialog.hint = hint;
            inputTextDialog.onDismissListener = onDismissListener;
            return inputTextDialog;
        }

        public Builder setHint(Integer hint) {
            this.hint = hint;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

        public void show() {
            create().show();
        }
    }
}