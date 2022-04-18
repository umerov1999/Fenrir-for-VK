package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiUser;

public class UserWallInfoResponse {

    @Nullable
    @SerializedName("user_info")
    public List<VKApiUser> users;

    @Nullable
    @SerializedName("all_wall_count")
    public Integer allWallCount;

    @Nullable
    @SerializedName("owner_wall_count")
    public Integer ownerWallCount;

    @Nullable
    @SerializedName("postponed_wall_count")
    public Integer postponedWallCount;

}
