package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadStoryDto {

    @Nullable
    @SerializedName("upload_result")
    public String upload_result;

    @NonNull
    @Override
    public String toString() {
        return "UploadStoryDto{" +
                "upload_result='" + upload_result + '\'' +
                '}';
    }
}
