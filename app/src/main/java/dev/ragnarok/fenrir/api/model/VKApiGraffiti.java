package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public class VKApiGraffiti implements VKApiAttachment {

    @SerializedName("id")
    public int id;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("url")
    public String url;

    @SerializedName("width")
    public int width;

    @SerializedName("height")
    public int height;

    @SerializedName("access_key")
    public String access_key;

    @Override
    public String getType() {
        return TYPE_GRAFFITI;
    }
}
