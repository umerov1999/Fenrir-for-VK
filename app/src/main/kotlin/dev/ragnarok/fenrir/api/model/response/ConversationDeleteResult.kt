package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class ConversationDeleteResult {
    @SerializedName("last_deleted_id")
    var lastDeletedId = 0
}