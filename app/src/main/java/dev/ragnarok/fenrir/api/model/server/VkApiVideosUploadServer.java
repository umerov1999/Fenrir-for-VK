package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiVideosUploadServer implements UploadServer, Parcelable {

    public static final Creator<VKApiVideosUploadServer> CREATOR = new Creator<VKApiVideosUploadServer>() {
        @Override
        public VKApiVideosUploadServer createFromParcel(Parcel in) {
            return new VKApiVideosUploadServer(in);
        }

        @Override
        public VKApiVideosUploadServer[] newArray(int size) {
            return new VKApiVideosUploadServer[size];
        }
    };
    @Nullable
    private String upload_url;

    public VKApiVideosUploadServer() {

    }

    protected VKApiVideosUploadServer(Parcel in) {
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
