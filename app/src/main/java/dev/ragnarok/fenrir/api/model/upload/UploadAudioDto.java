package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UploadAudioDto {

    @SerializedName("server")
    public String server;

    @SerializedName("audio")
    public String audio;

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
