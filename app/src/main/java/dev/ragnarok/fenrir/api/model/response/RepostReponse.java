package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

public class RepostReponse {

    @Nullable
    @SerializedName("post_id")
    public Integer postId;

    @Nullable
    @SerializedName("reposts_count")
    public Integer repostsCount;

    @Nullable
    @SerializedName("likes_count")
    public Integer likesCount;

}
