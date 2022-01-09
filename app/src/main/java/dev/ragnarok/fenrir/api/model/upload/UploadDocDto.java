package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class UploadDocDto {

    @SerializedName("file")
    public String file;

    @NonNull
    @Override
    public String toString() {
        return "UploadDocDto{" +
                "file='" + file + '\'' +
                '}';
    }
}
