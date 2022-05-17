package dev.ragnarok.fenrir.api.model.feedback

import com.google.gson.annotations.SerializedName

class Copies {
    @SerializedName("count")
    var count = 0

    @SerializedName("items")
    var pairs: List<IdPair>? = null

    class IdPair {
        @SerializedName("id")
        var id = 0

        @SerializedName("from_id")
        var owner_id = 0
    }
}