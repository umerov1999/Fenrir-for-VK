package dev.ragnarok.fenrir.api.model.response;

import com.google.gson.annotations.SerializedName;

import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiUser;

public class FavePageResponse {

    @SerializedName("description")
    public String description;

    @SerializedName("type")
    public String type;

    @SerializedName("updated_date")
    public long updated_date;

    @SerializedName("user")
    public VKApiUser user;

    @SerializedName("group")
    public VKApiCommunity group;
}
