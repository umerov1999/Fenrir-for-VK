package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.EnterPinPresenter;
import dev.ragnarok.fenrir.mvp.view.IEnterPinView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.view.KeyboardView;

public class EnterPinFragment extends BaseMvpFragment<EnterPinPresenter, IEnterPinView>
        implements IEnterPinView, KeyboardView.OnKeyboardClickListener {

    private ImageView mAvatar;
    private View mValuesRoot;
    private View[] mValuesCircles;

    public static EnterPinFragment newInstance() {
        Bundle bundle = new Bundle();
        EnterPinFragment fragment = new EnterPinFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_enter_pin, container, false);

        KeyboardView keyboardView = root.findViewById(R.id.keyboard);
        keyboardView.setOnKeyboardClickListener(this);

        mAvatar = root.findViewById(R.id.avatar);
        mValuesRoot = root.findViewById(R.id.value_root);

        mValuesCircles = new View[Constants.PIN_DIGITS_COUNT];
        mValuesCircles[0] = root.findViewById(R.id.pincode_digit_0);
        mValuesCircles[1] = root.findViewById(R.id.pincode_digit_1);
        mValuesCircles[2] = root.findViewById(R.id.pincode_digit_2);
        mValuesCircles[3] = root.findViewById(R.id.pincode_digit_3);
        return root;
    }

    private BiometricPrompt.AuthenticationCallback getAuthenticationCallback() {
        return new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                sendSuccessAndClose();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                getCustomToast().showToastError(errString.toString());
            }
        };
    }

    @Override
    public void showBiometricPrompt() {
        if (BiometricManager.from(requireActivity()).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
            getCustomToast().showToastError(R.string.biometric_not_support);
            return;
        }
        BiometricPrompt.AuthenticationCallback authenticationCallback = getAuthenticationCallback();
        BiometricPrompt mBiometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(requireActivity()), authenticationCallback);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric))
                .setNegativeButtonText(getString(R.string.cancel))
                .build();
        mBiometricPrompt.authenticate(promptInfo);
    }

    @NonNull
    @Override
    public IPresenterFactory<EnterPinPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new EnterPinPresenter(saveInstanceState);
    }

    @Override
    public void displayPin(int[] values, int noValueConstant) {
        if (Objects.isNull(mValuesCircles)) return;

        if (values.length != mValuesCircles.length) {
            throw new IllegalStateException("Invalid pin length, view: " + mValuesCircles.length + ", target: " + values.length);
        }

        for (int i = 0; i < mValuesCircles.length; i++) {
            boolean visible = values[i] != noValueConstant;
            mValuesCircles[i].setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void sendSuccessAndClose() {
        if (isAdded()) {
            Settings.get().security().updateLastPinTime();
            requireActivity().setResult(Activity.RESULT_OK);
            requireActivity().finish();
        }
    }

    @Override
    public void displayErrorAnimation() {
        if (Objects.nonNull(mValuesRoot)) {
            Animation animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.anim_invalid_pin);
            mValuesRoot.startAnimation(animation);
        }
    }

    @Override
    public void displayAvatarFromUrl(@NonNull String url) {
        if (Objects.nonNull(mAvatar)) {
            PicassoInstance.with()
                    .load(url)
                    .error(R.drawable.ic_avatar_unknown)
                    .transform(CurrentTheme.createTransformationForAvatar())
                    .into(mAvatar);
        }
    }

    @Override
    public void displayDefaultAvatar() {
        if (Objects.nonNull(mAvatar)) {
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(CurrentTheme.createTransformationForAvatar())
                    .into(mAvatar);
        }
    }

    @Override
    public void onButtonClick(int number) {
        callPresenter(p -> p.onNumberClicked(number));
    }

    @Override
    public void onBackspaceClick() {
        callPresenter(EnterPinPresenter::onBackspaceClicked);
    }

    @Override
    public void onFingerPrintClick() {
        callPresenter(EnterPinPresenter::onFingerprintClicked);
    }
}
