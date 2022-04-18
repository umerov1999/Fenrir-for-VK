package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadAudioDto {

    @Nullable
    @SerializedName("server")
    public String server;

    @Nullable
    @SerializedName("audio")
    public String audio;

    @Nullable
    @SerializedName("hash")
    public String hash;

    @NonNull
    @Override
    public String toString() {
        return "UploadAudioDto{" +
                "server='" + server + '\'' +
                ", audio='" + audio + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
