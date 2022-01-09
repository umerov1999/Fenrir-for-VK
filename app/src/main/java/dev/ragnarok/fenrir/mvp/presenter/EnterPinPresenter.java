package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IEnterPinView;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;

public class EnterPinPresenter extends RxSupportPresenter<IEnterPinView> {

    private static final String SAVE_VALUE = "save_value";
    private static final int LAST_CIRCLE_VISIBILITY_DELAY = 200;
    private static final int NO_VALUE = -1;
    private static final int MAX_ATTEMPT_DELAY = 3 * 60 * 1000;
    private final IOwnersRepository ownersRepository;
    private final ISettings.ISecuritySettings securitySettings;
    private final int[] mValues;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mOnFullyEnteredRunnable = this::onFullyEntered;
    private boolean onShowedFirstFingerPrint;
    private Owner mOwner;

    public EnterPinPresenter(@Nullable Bundle savedState) {
        super(savedState);
        securitySettings = Settings.get().security();
        ownersRepository = Repository.INSTANCE.getOwners();

        if (savedState != null) {
            mValues = savedState.getIntArray(SAVE_VALUE);
        } else {
            mValues = new int[Constants.PIN_DIGITS_COUNT];
            resetPin();
        }

        if (Objects.isNull(mOwner)) {
            loadOwnerInfo();
        }
    }

    private void loadOwnerInfo() {
        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId != ISettings.IAccountsSettings.INVALID_ID) {
            appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, accountId, IOwnersRepository.MODE_ANY)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onOwnerInfoReceived, t -> {/*ignore*/}));
        }
    }

    private void onOwnerInfoReceived(Owner owner) {
        mOwner = owner;
        resolveAvatarView();
    }

    public void onFingerprintClicked() {
        if (!securitySettings.isEntranceByFingerprintAllowed()) {
            callView(v -> v.getCustomToast().showToastError(R.string.error_login_by_fingerprint_not_allowed));
            return;
        }
        callView(IEnterPinView::showBiometricPrompt);
    }

    private void resolveAvatarView() {
        String avatar = Objects.isNull(mOwner) ? null : mOwner.getMaxSquareAvatar();
        if (isEmpty(avatar)) {
            callView(IEnterPinView::displayDefaultAvatar);
        } else {
            callView(v -> v.displayAvatarFromUrl(avatar));
        }
    }

    private long getNextPinAttemptTimeout() {
        List<Long> history = Settings.get()
                .security()
                .getPinEnterHistory();

        if (history.size() < Settings.get().security().getPinHistoryDepth()) {
            return 0;
        }

        long howLongAgoWasFirstAttempt = System.currentTimeMillis() - history.get(0);
        return howLongAgoWasFirstAttempt < MAX_ATTEMPT_DELAY ? MAX_ATTEMPT_DELAY - howLongAgoWasFirstAttempt : 0;
    }

    private void refreshViewCirclesVisibility() {
        callView(v -> v.displayPin(mValues, NO_VALUE));
    }

    public void onBackspaceClicked() {
        int currentIndex = getCurrentIndex();
        if (currentIndex == -1) {
            mValues[mValues.length - 1] = NO_VALUE;
        } else if (currentIndex > 0) {
            mValues[currentIndex - 1] = NO_VALUE;
        }

        refreshViewCirclesVisibility();
    }

    private void onFullyEntered() {
        if (!isFullyEntered()) return;

        long timeout = getNextPinAttemptTimeout();
        if (timeout > 0) {
            callView(view -> view.showError(R.string.limit_exceeded_number_of_attempts_message, timeout / 1000));

            resetPin();
            refreshViewCirclesVisibility();
            return;
        }

        Settings.get()
                .security()
                .firePinAttemptNow();

        if (Settings.get().security().isPinValid(mValues)) {
            onEnteredRightPin();
        } else {
            onEnteredWrongPin();
        }
    }

    private void onEnteredRightPin() {
        Settings.get()
                .security()
                .clearPinHistory();

        callView(IEnterPinView::sendSuccessAndClose);
    }

    private void onEnteredWrongPin() {
        resetPin();
        refreshViewCirclesVisibility();

        callView(v -> {
            v.showError(R.string.pin_is_invalid_message);
            v.displayErrorAnimation();
        });
    }

    public void onNumberClicked(int value) {
        if (isFullyEntered()) return;

        mValues[getCurrentIndex()] = value;
        refreshViewCirclesVisibility();

        if (isFullyEntered()) {
            mHandler.removeCallbacks(mOnFullyEnteredRunnable);
            mHandler.postDelayed(mOnFullyEnteredRunnable, LAST_CIRCLE_VISIBILITY_DELAY);
        }
    }

    private boolean isFullyEntered() {
        for (int value : mValues) {
            if (value == NO_VALUE) {
                return false;
            }
        }

        return true;
    }

    private int getCurrentIndex() {
        for (int i = 0; i < mValues.length; i++) {
            if (mValues[i] == NO_VALUE) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public void onGuiCreated(@NonNull IEnterPinView view) {
        super.onGuiCreated(view);
        if (!onShowedFirstFingerPrint) {
            onShowedFirstFingerPrint = true;
            if (securitySettings.isEntranceByFingerprintAllowed()) {
                view.showBiometricPrompt();
            }
        }
        resolveAvatarView();
        refreshViewCirclesVisibility();
    }

    private void resetPin() {
        Arrays.fill(mValues, NO_VALUE);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putIntArray(SAVE_VALUE, mValues);
    }
}