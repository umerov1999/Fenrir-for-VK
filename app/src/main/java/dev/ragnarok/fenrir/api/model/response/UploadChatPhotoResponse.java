package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class UploadChatPhotoResponse {

    @SerializedName("message_id")
    public int message_id;

    @Nullable
    @SerializedName("chat")
    public ChatPhoto chat;

    public static class ChatPhoto {
        @Nullable
        @SerializedName("photo_50")
        public String photo_50;

        @Nullable
        @SerializedName("photo_100")
        public String photo_100;

        @Nullable
        @SerializedName("photo_200")
        public String photo_200;
    }
}
