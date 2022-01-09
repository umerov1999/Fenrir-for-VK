package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UploadPhotoToAlbumDto {

    @SerializedName("server")
    public int server;

    @SerializedName("photos_list")
    public String photos_list;

    @SerializedName("aid")
    public int aid;

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
