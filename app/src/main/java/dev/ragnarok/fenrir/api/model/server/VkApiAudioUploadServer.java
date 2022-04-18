package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class VKApiAudioUploadServer implements Parcelable, UploadServer {

    public static final Creator<VKApiAudioUploadServer> CREATOR = new Creator<VKApiAudioUploadServer>() {
        @Override
        public VKApiAudioUploadServer createFromParcel(Parcel in) {
            return new VKApiAudioUploadServer(in);
        }

        @Override
        public VKApiAudioUploadServer[] newArray(int size) {
            return new VKApiAudioUploadServer[size];
        }
    };
    @Nullable
    public String upload_url;

    public VKApiAudioUploadServer() {
    }

    public VKApiAudioUploadServer(@Nullable String url) {
        upload_url = url;
    }

    protected VKApiAudioUploadServer(Parcel in) {
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
