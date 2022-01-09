package dev.ragnarok.fenrir.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CreatePinPresenter;
import dev.ragnarok.fenrir.mvp.view.ICreatePinView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.view.KeyboardView;

public class CreatePinFragment extends BaseMvpFragment<CreatePinPresenter, ICreatePinView>
        implements ICreatePinView, KeyboardView.OnKeyboardClickListener, BackPressCallback {

    private static final String EXTRA_PIN_VALUE = "pin_value";
    private TextView mTitle;
    private View mValuesRoot;
    private View[] mValuesCircles;

    public static CreatePinFragment newInstance() {
        return new CreatePinFragment();
    }

    public static int[] extractValueFromIntent(Intent intent) {
        return intent.getIntArrayExtra(EXTRA_PIN_VALUE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_pin, container, false);

        KeyboardView keyboardView = root.findViewById(R.id.keyboard);
        keyboardView.setOnKeyboardClickListener(this);

        mTitle = root.findViewById(R.id.pin_title_text);

        mValuesRoot = root.findViewById(R.id.value_root);
        mValuesCircles = new View[Constants.PIN_DIGITS_COUNT];
        mValuesCircles[0] = root.findViewById(R.id.pincode_digit_0);
        mValuesCircles[1] = root.findViewById(R.id.pincode_digit_1);
        mValuesCircles[2] = root.findViewById(R.id.pincode_digit_2);
        mValuesCircles[3] = root.findViewById(R.id.pincode_digit_3);
        return root;
    }

    @Override
    public void displayTitle(@StringRes int titleRes) {
        if (Objects.nonNull(mTitle)) {
            mTitle.setText(titleRes);
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
    public void sendSuccessAndClose(int[] values) {
        Intent data = new Intent();
        data.putExtra(EXTRA_PIN_VALUE, values);
        requireActivity().setResult(Activity.RESULT_OK, data);
        requireActivity().finish();
    }

    @NonNull
    @Override
    public IPresenterFactory<CreatePinPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CreatePinPresenter(saveInstanceState);
    }

    @Override
    public void onButtonClick(int number) {
        callPresenter(p -> p.fireDigitClick(number));
    }

    @Override
    public void onBackspaceClick() {
        callPresenter(CreatePinPresenter::fireBackspaceClick);
    }

    @Override
    public void onFingerPrintClick() {
        callPresenter(CreatePinPresenter::fireFingerPrintClick);
    }

    @Override
    public boolean onBackPressed() {
        return callPresenter(CreatePinPresenter::fireBackButtonClick, false);
    }
}
