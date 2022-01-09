package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiStoryUploadServer implements Parcelable, UploadServer {

    public static final Creator<VkApiStoryUploadServer> CREATOR = new Creator<VkApiStoryUploadServer>() {
        @Override
        public VkApiStoryUploadServer createFromParcel(Parcel in) {
            return new VkApiStoryUploadServer(in);
        }

        @Override
        public VkApiStoryUploadServer[] newArray(int size) {
            return new VkApiStoryUploadServer[size];
        }
    };
    public String upload_url;

    public VkApiStoryUploadServer() {
    }

    protected VkApiStoryUploadServer(Parcel in) {
        upload_url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(upload_url);
    }

    @Override
    public String getUrl() {
        return upload_url;
    }
}
