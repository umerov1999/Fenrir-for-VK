package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiDocsUploadServer implements UploadServer, Parcelable {

    public static final Creator<VKApiDocsUploadServer> CREATOR = new Creator<VKApiDocsUploadServer>() {
        @Override
        public VKApiDocsUploadServer createFromParcel(Parcel in) {
            return new VKApiDocsUploadServer(in);
        }

        @Override
        public VKApiDocsUploadServer[] newArray(int size) {
            return new VKApiDocsUploadServer[size];
        }
    };
    @Nullable
    private String upload_url;

    public VKApiDocsUploadServer() {

    }

    protected VKApiDocsUploadServer(Parcel in) {
        upload_url = in.readString();
    }

    @Nullable
    @Override
    public String getUrl() {
        return upload_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(upload_url);
    }
}
