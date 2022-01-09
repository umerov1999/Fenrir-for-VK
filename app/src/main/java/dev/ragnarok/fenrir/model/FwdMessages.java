package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import java.util.ArrayList;

public class FwdMessages extends AbsModel {

    public static final Creator<FwdMessages> CREATOR = new Creator<FwdMessages>() {
        @Override
        public FwdMessages createFromParcel(Parcel in) {
            return new FwdMessages(in);
        }

        @Override
        public FwdMessages[] newArray(int size) {
            return new FwdMessages[size];
        }
    };
    public final ArrayList<Message> fwds;

    public FwdMessages(ArrayList<Message> fwds) {
        this.fwds = fwds;
    }

    protected FwdMessages(Parcel in) {
        super(in);
        fwds = in.createTypedArrayList(Message.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeTypedList(fwds);
    }
}
