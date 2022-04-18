package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadOwnerPhotoDto {

    @Nullable
    @SerializedName("server")
    public String server;

    @Nullable
    @SerializedName("photo")
    public String photo;

    @Nullable
    @SerializedName("hash")
    public String hash;

    @NonNull
    @Override
    public String toString() {
        return "UploadOwnerPhotoDto{" +
                "server=" + server +
                ", photo='" + photo + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
