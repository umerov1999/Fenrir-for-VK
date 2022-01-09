package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

public class UploadChatPhotoResponse {

    @SerializedName("message_id")
    public int message_id;

    @SerializedName("chat")
    public ChatPhoto chat;

    public static class ChatPhoto {
        @SerializedName("photo_50")
        public String photo_50;

        @SerializedName("photo_100")
        public String photo_100;

        @SerializedName("photo_200")
        public String photo_200;
    }
}
