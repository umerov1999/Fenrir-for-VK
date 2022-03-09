package dev.ragnarok.fenrir.view.steppers.base

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.view.steppers.base.AbsStepsHost.AbsState

abstract class AbsStepsHost<T : AbsState>(var state: T) {
    var currentStep = 0
    abstract val stepsCount: Int

    @StringRes
    abstract fun getStepTitle(index: Int): Int
    abstract fun canMoveNext(index: Int, state: T): Boolean
    fun canMoveNext(index: Int): Boolean {
        return canMoveNext(index, state)
    }

    @StringRes
    abstract fun getNextButtonText(index: Int): Int

    @StringRes
    abstract fun getCancelButtonText(index: Int): Int
    fun saveState(bundle: Bundle) {
        bundle.putInt("host_current", currentStep)
        bundle.putParcelable("host_state", state)
    }

    fun restoreState(saveInstanceState: Bundle) {
        currentStep = saveInstanceState.getInt("host_current")
        saveInstanceState.getParcelable<T>("host_state")?.let {
            state = it
        }
    }

    open class AbsState : Parcelable {
        fun reset() {}
        override fun describeContents(): Int {
            return 0
        }

        @CallSuper
        override fun writeToParcel(dest: Parcel, flags: Int) {

        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<AbsState> {
                override fun createFromParcel(p: Parcel): AbsState {
                    return AbsState()
                }

                override fun newArray(size: Int): Array<AbsState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}