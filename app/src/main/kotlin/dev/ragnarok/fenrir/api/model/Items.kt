package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class Items<I> {
    @SerializedName("count")
    var count = 0

    @SerializedName("items")
    var items: ArrayList<I>? = null
}