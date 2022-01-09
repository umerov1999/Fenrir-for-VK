package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UploadVideoDto {

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("video_id")
    public int video_id;

    @SerializedName("video_hash")
    public String video_hash;

    @NonNull
    @Override
    public String toString() {
        return "UploadVideoDto{" +
                "owner_id=" + owner_id +
                ", video_id='" + video_id + '\'' +
                ", video_hash='" + video_hash + '\'' +
                '}';
    }
}
