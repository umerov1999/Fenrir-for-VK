package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UploadStoryDto {

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
