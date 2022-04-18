package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadChatPhotoDto {

    @Nullable
    @SerializedName("response")
    public String response;

    @NonNull
    @Override
    public String toString() {
        return "UploadChatPhotoDto{" +
                "response='" + response + '\'' +
                '}';
    }
}
