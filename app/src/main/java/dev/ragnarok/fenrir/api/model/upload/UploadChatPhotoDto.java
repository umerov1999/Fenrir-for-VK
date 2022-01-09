package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UploadChatPhotoDto {

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
