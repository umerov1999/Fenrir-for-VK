package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public final class CommentsDto {

    @SerializedName("count")
    public int count;

    @SerializedName("can_post")
    public boolean canPost;

    @SerializedName("list")
    public List<VKApiComment> list;
}