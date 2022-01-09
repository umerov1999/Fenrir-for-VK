package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiOwnerPhotoUploadServer implements Parcelable, UploadServer {

    public static final Creator<VkApiOwnerPhotoUploadServer> CREATOR = new Creator<VkApiOwnerPhotoUploadServer>() {
        @Override
        public VkApiOwnerPhotoUploadServer createFromParcel(Parcel in) {
            return new VkApiOwnerPhotoUploadServer(in);
        }

        @Override
        public VkApiOwnerPhotoUploadServer[] newArray(int size) {
            return new VkApiOwnerPhotoUploadServer[size];
        }
    };
    public String upload_url;

    public VkApiOwnerPhotoUploadServer() {

    }

    protected VkApiOwnerPhotoUploadServer(Parcel in) {
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
