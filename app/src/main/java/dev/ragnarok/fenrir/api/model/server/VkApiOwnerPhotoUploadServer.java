package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiOwnerPhotoUploadServer implements Parcelable, UploadServer {

    public static final Creator<VKApiOwnerPhotoUploadServer> CREATOR = new Creator<VKApiOwnerPhotoUploadServer>() {
        @Override
        public VKApiOwnerPhotoUploadServer createFromParcel(Parcel in) {
            return new VKApiOwnerPhotoUploadServer(in);
        }

        @Override
        public VKApiOwnerPhotoUploadServer[] newArray(int size) {
            return new VKApiOwnerPhotoUploadServer[size];
        }
    };
    @Nullable
    public String upload_url;

    public VKApiOwnerPhotoUploadServer() {

    }

    protected VKApiOwnerPhotoUploadServer(Parcel in) {
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

    @Nullable
    @Override
    public String getUrl() {
        return upload_url;
    }
}
