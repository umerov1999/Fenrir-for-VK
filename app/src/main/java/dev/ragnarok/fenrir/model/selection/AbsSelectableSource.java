package dev.ragnarok.fenrir.model.selection;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.CallSuper;


public abstract class AbsSelectableSource implements Parcelable {

    @Types
    private final int type;

    AbsSelectableSource(@Types int type) {
        this.type = type;
    }

    AbsSelectableSource(Parcel in) {
        type = in.readInt();
    }

    @CallSuper
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
    }

    @Types
    public final int getType() {
        return type;
    }
}