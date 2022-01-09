package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

public class isLikeResponse {

    @SerializedName("liked")
    public int liked;

    @SerializedName("copied")
    public int copied;

}
