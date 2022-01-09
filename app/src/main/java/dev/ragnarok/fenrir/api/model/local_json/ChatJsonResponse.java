package dev.ragnarok.fenrir.api.model.local_json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiMessage;

public class ChatJsonResponse {
    public String type;
    public List<VKApiMessage> messages;
    public Version version;
    public int page_id;
    public String page_title;
    public String page_avatar;
    public String page_phone_number;
    public String page_instagram;
    public String page_site;

    public static class Version {
        @SerializedName("float")
        public float floatValue;

        @SerializedName("string")
        public float stringValue;
    }
}
