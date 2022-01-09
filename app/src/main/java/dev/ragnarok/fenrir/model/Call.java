package dev.ragnarok.fenrir.model;

import android.content.Context;
import android.os.Parcel;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;

public class Call extends AbsModel {

    public static final Creator<Call> CREATOR = new Creator<Call>() {
        @Override
        public Call createFromParcel(Parcel in) {
            return new Call(in);
        }

        @Override
        public Call[] newArray(int size) {
            return new Call[size];
        }
    };
    private int initiator_id;
    private int receiver_id;
    private String state;
    private long time;

    public Call() {

    }

    protected Call(Parcel in) {
        super(in);
        initiator_id = in.readInt();
        receiver_id = in.readInt();
        time = in.readLong();
        state = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(initiator_id);
        dest.writeInt(receiver_id);
        dest.writeLong(time);
        dest.writeString(state);
    }

    public int getInitiator_id() {
        return initiator_id;
    }

    public Call setInitiator_id(int initiator_id) {
        this.initiator_id = initiator_id;
        return this;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public Call setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
        return this;
    }

    public long getTime() {
        return time;
    }

    public Call setTime(long time) {
        this.time = time;
        return this;
    }

    public String getState() {
        return state;
    }

    public Call setState(String state) {
        this.state = state;
        return this;
    }

    public String getLocalizedState(@NonNull Context context) {
        switch (state) {
            case "canceled_by_receiver":
                return context.getString(R.string.canceled_by_receiver);
            case "canceled_by_initiator":
                return context.getString(R.string.canceled_by_initiator);
            case "reached":
                return context.getString(R.string.call_reached);
        }
        return state;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
