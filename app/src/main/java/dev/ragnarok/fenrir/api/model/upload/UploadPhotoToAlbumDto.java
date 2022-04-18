package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadPhotoToAlbumDto {

    @SerializedName("server")
    public int server;

    @Nullable
    @SerializedName("photos_list")
    public String photos_list;

    @SerializedName("aid")
    public int aid;

    @Nullable
    @SerializedName("hash")
    public String hash;

    @NonNull
    @Override
    public String toString() {
        return "UploadPhotoToAlbumDto{" +
                "server=" + server +
                ", photos_list='" + photos_list + '\'' +
                ", aid=" + aid +
                ", hash='" + hash + '\'' +
                '}';
    }
}
