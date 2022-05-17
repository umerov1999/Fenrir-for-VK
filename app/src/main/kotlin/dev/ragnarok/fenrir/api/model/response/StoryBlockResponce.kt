package dev.ragnarok.fenrir.api.model.response

import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.api.model.VKApiStory

class StoryBlockResponce {
    @SerializedName("stories")
    var stories: List<VKApiStory>? = null

    @SerializedName("grouped")
    var grouped: List<StoryBlockResponce>? = null
}