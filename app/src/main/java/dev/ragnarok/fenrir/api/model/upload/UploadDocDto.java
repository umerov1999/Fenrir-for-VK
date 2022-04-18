package dev.ragnarok.fenrir.api.model.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadDocDto {

    @Nullable
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
