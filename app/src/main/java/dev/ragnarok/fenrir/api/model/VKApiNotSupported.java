package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiNotSupported implements VKApiAttachment {

    @SerializedName("type")
    public String type;
    @SerializedName("body")
    public String body;

    public VKApiNotSupported(String type, String body) {
        this.type = type;
        this.body = body;
    }

    @Override
    public String getType() {
        return TYPE_NOT_SUPPORT;
    }
}
