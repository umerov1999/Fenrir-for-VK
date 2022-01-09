package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

public class RepostReponse {

    @SerializedName("post_id")
    public Integer postId;

    @SerializedName("reposts_count")
    public Integer repostsCount;

    @SerializedName("likes_count")
    public Integer likesCount;

}
