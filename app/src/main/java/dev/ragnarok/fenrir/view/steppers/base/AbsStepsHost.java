package dev.ragnarok.fenrir.view.steppers.base;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public abstract class AbsStepsHost<T extends AbsStepsHost.AbsState> {

    protected int currentStep;
    protected T state;

    public AbsStepsHost(@NonNull T state) {
        this.state = state;
    }

    public abstract int getStepsCount();

    @StringRes
    public abstract int getStepTitle(int index);

    public abstract boolean canMoveNext(int index, @NonNull T state);

    public final boolean canMoveNext(int index) {
        return canMoveNext(index, state);
    }

    @StringRes
    public abstract int getNextButtonText(int index);

    @StringRes
    public abstract int getCancelButtonText(int index);

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    @NonNull
    public T getState() {
        return state;
    }

    public void setState(@NonNull T state) {
        this.state = state;
    }

    public void saveState(@NonNull Bundle bundle) {
        bundle.putInt("host_current", currentStep);
        bundle.putParcelable("host_state", state);
    }

    public void restoreState(Bundle saveInstanceState) {
        currentStep = saveInstanceState.getInt("host_current");
        state = saveInstanceState.getParcelable("host_state");
    }

    public static class AbsState implements Parcelable {

        public static final Creator<AbsState> CREATOR = new Creator<AbsState>() {
            @Override
            public AbsState createFromParcel(Parcel in) {
                return new AbsState(in);
            }

            @Override
            public AbsState[] newArray(int size) {
                return new AbsState[size];
            }
        };

        public AbsState() {

        }

        @SuppressWarnings("unused")
        protected AbsState(Parcel in) {
        }

        public void reset() {

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @CallSuper
        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}