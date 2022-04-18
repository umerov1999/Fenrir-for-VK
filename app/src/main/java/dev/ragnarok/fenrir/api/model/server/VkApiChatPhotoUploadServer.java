package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiChatPhotoUploadServer implements Parcelable, UploadServer {

    public static final Creator<VKApiChatPhotoUploadServer> CREATOR = new Creator<VKApiChatPhotoUploadServer>() {
        @Override
        public VKApiChatPhotoUploadServer createFromParcel(Parcel in) {
            return new VKApiChatPhotoUploadServer(in);
        }

        @Override
        public VKApiChatPhotoUploadServer[] newArray(int size) {
            return new VKApiChatPhotoUploadServer[size];
        }
    };
    @Nullable
    public String upload_url;

    public VKApiChatPhotoUploadServer() {

    }

    protected VKApiChatPhotoUploadServer(Parcel in) {
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
