package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiUser;

public class UserWallInfoResponse {

    @SerializedName("user_info")
    public List<VKApiUser> users;

    @SerializedName("all_wall_count")
    public Integer allWallCount;

    @SerializedName("owner_wall_count")
    public Integer ownerWallCount;

    @SerializedName("postponed_wall_count")
    public Integer postponedWallCount;

}
