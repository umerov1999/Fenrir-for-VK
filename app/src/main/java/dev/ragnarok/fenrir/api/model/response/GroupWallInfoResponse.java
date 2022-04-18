package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;

public class GroupWallInfoResponse {

    @Nullable
    @SerializedName("group_info")
    public List<VKApiCommunity> groups;

    @Nullable
    @SerializedName("all_wall_count")
    public Integer allWallCount;

    @Nullable
    @SerializedName("owner_wall_count")
    public Integer ownerWallCount;

    @Nullable
    @SerializedName("suggests_wall_count")
    public Integer suggestsWallCount;

    @Nullable
    @SerializedName("postponed_wall_count")
    public Integer postponedWallCount;
}
