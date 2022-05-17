package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.*

class NewsfeedCommentsResponse {
    @SerializedName("items")
    var items: List<Dto>? = null

    @SerializedName("profiles")
    var profiles: List<VKApiUser>? = null

    @SerializedName("groups")
    var groups: List<VKApiCommunity>? = null

    @SerializedName("next_from")
    var nextFrom: String? = null

    abstract class Dto
    class PostDto(val post: VKApiPost?) : Dto()
    class PhotoDto(val photo: VKApiPhoto?) : Dto()
    class VideoDto(val video: VKApiVideo?) : Dto()
    class TopicDto(val topic: VKApiTopic?) : Dto()
}