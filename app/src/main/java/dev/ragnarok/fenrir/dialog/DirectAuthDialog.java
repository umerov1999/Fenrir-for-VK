package dev.ragnarok.fenrir.dialog;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseMvpDialogFragment;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.DirectAuthPresenter;
import dev.ragnarok.fenrir.mvp.view.IDirectAuthView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class DirectAuthDialog extends BaseMvpDialogFragment<DirectAuthPresenter, IDirectAuthView> implements IDirectAuthView {

    public static final String ACTION_LOGIN_COMPLETE = "ACTION_LOGIN_COMPLETE";
    public static final String ACTION_LOGIN_VIA_WEB = "ACTION_LOGIN_VIA_WEB";
    public static final String ACTION_VALIDATE_VIA_WEB = "ACTION_VALIDATE_VIA_WEB";
    private TextInputEditText mLogin;
    private TextInputEditText mPassword;
    private TextInputEditText mCaptcha;
    private TextInputEditText mSmsCode;
    private MaterialCheckBox mSavePassword;
    private View mSmsCodeRoot;
    private View mContentRoot;
    private View mLoadingRoot;
    private View mCaptchaRoot;
    private ImageView mCaptchaImage;
    private View mEnterAppCodeRoot;
    private TextInputEditText mAppCode;

    public static DirectAuthDialog newInstance() {
        Bundle args = new Bundle();
        DirectAuthDialog fragment = new DirectAuthDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());

        View view = View.inflate(requireActivity(), R.layout.dialog_direct_auth, null);

        mLogin = view.findViewById(R.id.field_username);
        mLogin.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireLoginEdit(s));
            }
        });

        mPassword = view.findViewById(R.id.field_password);
        mPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.firePasswordEdit(s));
            }
        });

        mEnterAppCodeRoot = view.findViewById(R.id.field_app_code_root);
        mAppCode = view.findViewById(R.id.field_app_code);
        mAppCode.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireAppCodeEdit(s));
            }
        });

        view.findViewById(R.id.button_send_code_via_sms).setOnClickListener(view1 -> callPresenter(DirectAuthPresenter::fireButtonSendCodeViaSmsClick));

        mSmsCodeRoot = view.findViewById(R.id.field_sms_code_root);
        mSmsCode = view.findViewById(R.id.field_sms_code);
        mSmsCode.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireSmsCodeEdit(s));
            }
        });

        mContentRoot = view.findViewById(R.id.content_root);
        mLoadingRoot = view.findViewById(R.id.loading_root);
        mCaptchaRoot = view.findViewById(R.id.captcha_root);
        mCaptcha = view.findViewById(R.id.field_captcha);
        mCaptcha.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireCaptchaEdit(s));
            }
        });
        mCaptchaImage = view.findViewById(R.id.captcha_img);

        mSavePassword = view.findViewById(R.id.save_password);
        mSavePassword.setOnCheckedChangeListener((buttonView, isChecked) -> callPresenter(p -> p.fireSaveEdit(isChecked)));

        builder.setView(view);
        builder.setPositiveButton(R.string.button_login, null);
        if (Constants.IS_HAS_LOGIN_WEB)
            builder.setNeutralButton(R.string.button_login_via_web, (dialogInterface, i) -> callPresenter(DirectAuthPresenter::fireLoginViaWebClick));
        builder.setTitle(R.string.login_title);
        builder.setIcon(R.drawable.logo_vk);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(true);

        fireViewCreated();
        return dialog;
    }

    @NonNull
    @Override
    public IPresenterFactory<DirectAuthPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new DirectAuthPresenter(saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Button buttonLogin = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        buttonLogin.setOnClickListener(view -> callPresenter(DirectAuthPresenter::fireLoginClick));
    }

    @Override
    public void setLoginButtonEnabled(boolean enabled) {
        Button buttonLogin = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);

        if (nonNull(buttonLogin)) {
            buttonLogin.setEnabled(enabled);
        }
        if (nonNull(mSavePassword)) {
            mSavePassword.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setSmsRootVisible(boolean visible) {
        if (nonNull(mSmsCodeRoot)) {
            mSmsCodeRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setAppCodeRootVisible(boolean visible) {
        if (nonNull(mEnterAppCodeRoot)) {
            mEnterAppCodeRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void moveFocusToSmsCode() {
        if (nonNull(mSmsCode)) {
            mSmsCode.requestFocus();
        }
    }

    @Override
    public void moveFocusToAppCode() {
        if (nonNull(mSmsCode)) {
            mAppCode.requestFocus();
        }
    }

    @Override
    public void displayLoading(boolean loading) {
        if (nonNull(mLoadingRoot)) {
            mLoadingRoot.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        if (nonNull(mContentRoot)) {
            mContentRoot.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public void setCaptchaRootVisible(boolean visible) {
        if (nonNull(mCaptchaRoot)) {
            mCaptchaRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayCaptchaImage(String img) {
        if (nonNull(mCaptchaImage)) {
            PicassoInstance.with()
                    .load(img)
                    .placeholder(R.drawable.background_gray)
                    .into(mCaptchaImage);
        }
    }

    @Override
    public void moveFocusToCaptcha() {
        if (nonNull(mCaptcha)) {
            mCaptcha.requestFocus();
        }
    }

    @Override
    public void hideKeyboard() {
        try {
            InputMethodManager im = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(mLogin.getWindowToken(), 0);
            im.hideSoftInputFromWindow(mPassword.getWindowToken(), 0);
            im.hideSoftInputFromWindow(mCaptcha.getWindowToken(), 0);
            im.hideSoftInputFromWindow(mSmsCode.getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void returnSuccessToParent(int userId, String accessToken, String Login, String Password, String twoFA, boolean isSave) {
        Bundle data = new Bundle();
        data.putString(Extra.TOKEN, accessToken);
        data.putInt(Extra.USER_ID, userId);
        data.putString(Extra.LOGIN, Login);
        data.putString(Extra.PASSWORD, Password);
        data.putString(Extra.TWO_FA, twoFA);
        data.putBoolean(Extra.SAVE, isSave);
        returnResultAndDismiss(ACTION_LOGIN_COMPLETE, data);
    }

    @Override
    public void returnSuccessValidation(String url, String Login, String Password, String twoFA, boolean isSave) {
        Bundle data = new Bundle();
        data.putString(Extra.URL, url);
        data.putString(Extra.LOGIN, Login);
        data.putString(Extra.PASSWORD, Password);
        data.putString(Extra.TWO_FA, twoFA);
        data.putBoolean(Extra.SAVE, isSave);
        returnResultAndDismiss(ACTION_VALIDATE_VIA_WEB, data);
    }

    private void returnResultAndDismiss(@NonNull String key, @NonNull Bundle data) {
        getParentFragmentManager().setFragmentResult(key, data);
        dismiss();
    }

    @Override
    public void returnLoginViaWebAction() {
        returnResultAndDismiss(ACTION_LOGIN_VIA_WEB, new Bundle());
    }
}
