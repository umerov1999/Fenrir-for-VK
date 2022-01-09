package dev.ragnarok.fenrir.model.drawer;

import android.os.Parcel;

public class NoIconMenuItem extends SectionMenuItem {

    public static Creator<NoIconMenuItem> CREATOR = new Creator<NoIconMenuItem>() {
        public NoIconMenuItem createFromParcel(Parcel source) {
            return new NoIconMenuItem(source);
        }

        public NoIconMenuItem[] newArray(int size) {
            return new NoIconMenuItem[size];
        }
    };

    public NoIconMenuItem(int section, int title) {
        super(TYPE_WITHOUT_ICON, section, title);
    }

    public NoIconMenuItem(Parcel in) {
        super(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
