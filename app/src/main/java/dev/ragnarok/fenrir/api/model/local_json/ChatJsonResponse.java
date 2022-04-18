package dev.ragnarok.fenrir.api.model.local_json;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiMessage;

public class ChatJsonResponse {
    @Nullable
    public String type;
    @Nullable
    public List<VKApiMessage> messages;
    @Nullable
    public Version version;
    public int page_id;
    @Nullable
    public String page_title;
    @Nullable
    public String page_avatar;
    @Nullable
    public String page_phone_number;
    @Nullable
    public String page_instagram;
    @Nullable
    public String page_site;

    public static class Version {
        @SerializedName("float")
        public float floatValue;

        @SerializedName("string")
        public float stringValue;
    }
}
