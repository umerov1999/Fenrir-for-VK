package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiChatPhotoUploadServer implements Parcelable, UploadServer {

    public static final Creator<VkApiChatPhotoUploadServer> CREATOR = new Creator<VkApiChatPhotoUploadServer>() {
        @Override
        public VkApiChatPhotoUploadServer createFromParcel(Parcel in) {
            return new VkApiChatPhotoUploadServer(in);
        }

        @Override
        public VkApiChatPhotoUploadServer[] newArray(int size) {
            return new VkApiChatPhotoUploadServer[size];
        }
    };
    public String upload_url;

    public VkApiChatPhotoUploadServer() {

    }

    protected VkApiChatPhotoUploadServer(Parcel in) {
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
