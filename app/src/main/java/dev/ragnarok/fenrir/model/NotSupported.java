package dev.ragnarok.fenrir.model;

import android.os.Parcel;

public class NotSupported extends AbsModel {
    public static final Creator<NotSupported> CREATOR = new Creator<NotSupported>() {
        @Override
        public NotSupported createFromParcel(Parcel in) {
            return new NotSupported(in);
        }

        @Override
        public NotSupported[] newArray(int size) {
            return new NotSupported[size];
        }
    };
    private String type;
    private String body;

    public NotSupported() {

    }

    protected NotSupported(Parcel in) {
        super(in);
        type = in.readString();
        body = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(type);
        dest.writeString(body);
    }

    public String getType() {
        return type;
    }

    public NotSupported setType(String type) {
        this.type = type;
        return this;
    }

    public String getBody() {
        return body;
    }

    public NotSupported setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
