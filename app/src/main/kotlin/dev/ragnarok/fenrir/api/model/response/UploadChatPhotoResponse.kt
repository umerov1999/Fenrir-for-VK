package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName

class UploadChatPhotoResponse {
    @SerializedName("message_id")
    var message_id = 0

    @SerializedName("chat")
    var chat: ChatPhoto? = null

    class ChatPhoto {
        @SerializedName("photo_50")
        var photo_50: String? = null

        @SerializedName("photo_100")
        var photo_100: String? = null

        @SerializedName("photo_200")
        var photo_200: String? = null
    }
}