package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiVideosUploadServer implements UploadServer, Parcelable {

    public static final Creator<VkApiVideosUploadServer> CREATOR = new Creator<VkApiVideosUploadServer>() {
        @Override
        public VkApiVideosUploadServer createFromParcel(Parcel in) {
            return new VkApiVideosUploadServer(in);
        }

        @Override
        public VkApiVideosUploadServer[] newArray(int size) {
            return new VkApiVideosUploadServer[size];
        }
    };
    private String upload_url;

    public VkApiVideosUploadServer() {

    }

    protected VkApiVideosUploadServer(Parcel in) {
        upload_url = in.readString();
    }

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
