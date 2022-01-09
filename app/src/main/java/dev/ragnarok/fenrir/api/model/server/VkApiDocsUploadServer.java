package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiDocsUploadServer implements UploadServer, Parcelable {

    public static final Creator<VkApiDocsUploadServer> CREATOR = new Creator<VkApiDocsUploadServer>() {
        @Override
        public VkApiDocsUploadServer createFromParcel(Parcel in) {
            return new VkApiDocsUploadServer(in);
        }

        @Override
        public VkApiDocsUploadServer[] newArray(int size) {
            return new VkApiDocsUploadServer[size];
        }
    };
    private String upload_url;

    public VkApiDocsUploadServer() {

    }

    protected VkApiDocsUploadServer(Parcel in) {
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
