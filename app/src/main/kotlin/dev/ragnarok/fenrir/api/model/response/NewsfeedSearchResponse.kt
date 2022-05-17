package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.api.model.VKApiPost
import dev.ragnarok.fenrir.api.model.VKApiUser

class NewsfeedSearchResponse {
    @SerializedName("items")
    var items: List<VKApiPost>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("next_from")
    var nextFrom: String? = null //@SerializedName("count")
    //public Integer count;
    //@SerializedName("total_count")
    //public Integer totalCount;
}