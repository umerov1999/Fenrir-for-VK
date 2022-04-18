package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


public class VKApiEvent implements VKApiAttachment {

    @SerializedName("id")
    public int id;

    @SerializedName("button_text")
    public String button_text;

    @SerializedName("text")
    public String text;

    @NonNull
    @Override
    public String getType() {
        return TYPE_EVENT;
    }

}
