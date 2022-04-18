package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;


public class VKApiMarketAlbum implements VKApiAttachment {

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

    @NonNull
    @Override
    public String getType() {
        return TYPE_MARKET_ALBUM;
    }

}
