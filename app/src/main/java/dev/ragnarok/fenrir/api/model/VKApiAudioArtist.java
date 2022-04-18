package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VKApiAudioArtist implements VKApiAttachment {
    @Nullable
    @SerializedName("id")
    public String id;

    @Nullable
    @SerializedName("name")
    public String name;

    @Nullable
    @SerializedName("photo")
    public List<Image> photo;

    public VKApiAudioArtist() {

    }

    @NonNull
    @Override
    public String getType() {
        return TYPE_ARTIST;
    }

    public static final class Image {
        @Nullable
        @SerializedName("url")
        public String url;

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;
    }
}
