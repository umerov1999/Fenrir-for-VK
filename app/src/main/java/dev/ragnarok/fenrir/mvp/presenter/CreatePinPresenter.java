package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.Arrays;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.ICreatePinView;

public class CreatePinPresenter extends RxSupportPresenter<ICreatePinView> {

    private static final int LAST_CIRCLE_VISIBILITY_DELAY = 200;
    private static final int NO_VALUE = -1;

    private static final String SAVE_STEP = "save_step";
    private static final String SAVE_CREATED_PIN = "save_created_pin";
    private static final String SAVE_REPEATED_PIN = "save_repeated_pin";

    private static final int STEP_CREATE = 1;
    private static final int STEP_REPEAT = 2;
    private final int[] mCreatedPin;
    private final int[] mRepeatedPin;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mCurrentStep;
    private final Runnable mOnFullyEnteredRunnable = () -> {
        if (mCurrentStep == STEP_CREATE) {
            onCreatedPinFullyEntered();
        } else {
            onRepeatedPinFullyEntered();
        }
    };

    public CreatePinPresenter(Bundle savedInstanceState) {
        super(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentStep = savedInstanceState.getInt(SAVE_STEP);
            mCreatedPin = savedInstanceState.getIntArray(SAVE_CREATED_PIN);
            mRepeatedPin = savedInstanceState.getIntArray(SAVE_REPEATED_PIN);
        } else {
            mCurrentStep = STEP_CREATE;
            mCreatedPin = new int[Constants.PIN_DIGITS_COUNT];
            mRepeatedPin = new int[Constants.PIN_DIGITS_COUNT];
            resetPin(mCreatedPin);
            resetPin(mRepeatedPin);
        }
    }

    public boolean fireBackButtonClick() {
        if (mCurrentStep == STEP_CREATE) {
            return true;
        }

        mCurrentStep = STEP_CREATE;
        resetPin(mCreatedPin);
        resetPin(mRepeatedPin);
        resolveAllViews();
        return false;
    }

    public void fireFingerPrintClick() {
        callView(v -> v.showError(R.string.not_yet_implemented_message));
    }

    public void fireBackspaceClick() {
        int[] targetPin = mCurrentStep == STEP_CREATE ? mCreatedPin : mRepeatedPin;
        int currentIndex = getNextNoEnteredIndex(targetPin);
        if (currentIndex == -1) {
            targetPin[targetPin.length - 1] = NO_VALUE;
        } else if (currentIndex > 0) {
            targetPin[currentIndex - 1] = NO_VALUE;
        }

        refreshViewCirclesVisibility();
    }

    private void resolveAllViews() {
        refreshViewCirclesVisibility();
        resolveTitles();
    }

    private void resolveTitles() {
        if (mCurrentStep == STEP_CREATE) {
            callView(v -> v.displayTitle(R.string.create_pin_code_title));
        } else {
            callView(v -> v.displayTitle(R.string.repeat_pin_code_title));
        }
    }

    public void fireDigitClick(int digit) {
        int[] targetPin = mCurrentStep == STEP_CREATE ? mCreatedPin : mRepeatedPin;
        if (isFullyEntered(targetPin)) {
            return;
        }

        appendDigit(targetPin, digit);
        refreshViewCirclesVisibility();

        if (isFullyEntered(targetPin)) {
            mHandler.removeCallbacks(mOnFullyEnteredRunnable);
            mHandler.postDelayed(mOnFullyEnteredRunnable, LAST_CIRCLE_VISIBILITY_DELAY);
        }
    }

    private void appendDigit(int[] pin, int digit) {
        pin[getNextNoEnteredIndex(pin)] = digit;
    }

    private void onRepeatedPinFullyEntered() {
        if (!isPinsMatch()) {
            resetPin(mRepeatedPin);
            resetPin(mCreatedPin);
            mCurrentStep = STEP_CREATE;
            callView(v -> v.showError(R.string.entered_pin_codes_do_not_match));
            resolveAllViews();
            callView(ICreatePinView::displayErrorAnimation);
            return;
        }
        callView(v -> v.sendSuccessAndClose(mCreatedPin));
    }

    private void onCreatedPinFullyEntered() {
        resetPin(mRepeatedPin);
        mCurrentStep = STEP_REPEAT;
        resolveAllViews();
    }

    private boolean isPinsMatch() {
        for (int i = 0; i < Constants.PIN_DIGITS_COUNT; i++) {
            if (mCreatedPin[i] == NO_VALUE || mRepeatedPin[i] == NO_VALUE || mCreatedPin[i] != mRepeatedPin[i]) {
                return false;
            }
        }

        return true;
    }

    private int getNextNoEnteredIndex(int[] pin) {
        for (int i = 0; i < pin.length; i++) {
            if (pin[i] == NO_VALUE) {
                return i;
            }
        }

        return -1;
    }

    private boolean isFullyEntered(int[] pin) {
        for (int value : pin) {
            if (value == NO_VALUE) {
                return false;
            }
        }

        return true;
    }

    private void refreshViewCirclesVisibility() {
        if (mCurrentStep == STEP_CREATE) {
            callView(v -> v.displayPin(mCreatedPin, NO_VALUE));
        } else {
            callView(v -> v.displayPin(mRepeatedPin, NO_VALUE));
        }
    }

    @Override
    public void onGuiCreated(@NonNull ICreatePinView view) {
        super.onGuiCreated(view);

        resolveTitles();
        refreshViewCirclesVisibility();
    }

    private void resetPin(int[] pin) {
        Arrays.fill(pin, NO_VALUE);
    }
}
