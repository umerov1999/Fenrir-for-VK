package dev.ragnarok.fenrir.api.model.server;

import android.os.Parcel;
import android.os.Parcelable;

public class VkApiAudioUploadServer implements Parcelable, UploadServer {

    public static final Creator<VkApiAudioUploadServer> CREATOR = new Creator<VkApiAudioUploadServer>() {
        @Override
        public VkApiAudioUploadServer createFromParcel(Parcel in) {
            return new VkApiAudioUploadServer(in);
        }

        @Override
        public VkApiAudioUploadServer[] newArray(int size) {
            return new VkApiAudioUploadServer[size];
        }
    };
    public String upload_url;

    public VkApiAudioUploadServer() {
    }

    public VkApiAudioUploadServer(String url) {
        upload_url = url;
    }

    protected VkApiAudioUploadServer(Parcel in) {
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
