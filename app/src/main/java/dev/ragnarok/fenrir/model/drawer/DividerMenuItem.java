package dev.ragnarok.fenrir.model.drawer;

import android.os.Parcel;

public class DividerMenuItem extends AbsMenuItem {

    public static Creator<DividerMenuItem> CREATOR = new Creator<DividerMenuItem>() {
        public DividerMenuItem createFromParcel(Parcel source) {
            return new DividerMenuItem(source);
        }

        public DividerMenuItem[] newArray(int size) {
            return new DividerMenuItem[size];
        }
    };

    public DividerMenuItem() {
        super(TYPE_DIVIDER);
    }

    public DividerMenuItem(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
