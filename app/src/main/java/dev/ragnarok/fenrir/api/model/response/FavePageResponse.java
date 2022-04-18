package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class FavePageResponse {
    @Nullable
    @SerializedName("description")
    public String description;

    @Nullable
    @SerializedName("type")
    public String type;

    @SerializedName("updated_date")
    public long updated_date;

    @Nullable
    @SerializedName("user")
    public VKApiUser user;

    @Nullable
    @SerializedName("group")
    public VKApiCommunity group;
}
