package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;


public class VkApiMarketAlbum implements VKApiAttachment {

    @SerializedName("id")
    public int id;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("access_key")
    public String access_key;

    @SerializedName("title")
    public String title;

    @SerializedName("photo")
    public VKApiPhoto photo;

    @SerializedName("count")
    public int count;

    @SerializedName("updated_time")
    public int updated_time;

    @Override
    public String getType() {
        return TYPE_MARKET_ALBUM;
    }

}
