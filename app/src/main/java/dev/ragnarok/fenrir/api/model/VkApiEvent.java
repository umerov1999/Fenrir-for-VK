package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;


public class VkApiEvent implements VKApiAttachment {

    @SerializedName("id")
    public int id;

    @SerializedName("button_text")
    public String button_text;

    @SerializedName("text")
    public String text;

    @Override
    public String getType() {
        return TYPE_EVENT;
    }

}
