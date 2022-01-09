package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.CallSuper;


public abstract class AbsModel implements Parcelable {

    public AbsModel() {

    }

    @SuppressWarnings("unused")
    public AbsModel(Parcel in) {

    }

    @CallSuper
    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}