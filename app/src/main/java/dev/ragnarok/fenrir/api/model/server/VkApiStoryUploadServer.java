package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiStoryUploadServer implements Parcelable, UploadServer {

    public static final Creator<VKApiStoryUploadServer> CREATOR = new Creator<VKApiStoryUploadServer>() {
        @Override
        public VKApiStoryUploadServer createFromParcel(Parcel in) {
            return new VKApiStoryUploadServer(in);
        }

        @Override
        public VKApiStoryUploadServer[] newArray(int size) {
            return new VKApiStoryUploadServer[size];
        }
    };
    @Nullable
    public String upload_url;

    public VKApiStoryUploadServer() {
    }

    protected VKApiStoryUploadServer(Parcel in) {
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
