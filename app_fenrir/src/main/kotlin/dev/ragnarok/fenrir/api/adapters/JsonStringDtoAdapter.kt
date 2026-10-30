package dev.ragnarok.fenrir.api.adapters

import dev.ragnarok.fenrir.api.model.VKApiJsonString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

class JsonStringDtoAdapter : AbsDtoAdapter<VKApiJsonString>("VKApiJsonString") {
    @Throws(Exception::class)
    override fun deserialize(
        json: JsonElement
    ): VKApiJsonString {
        val content = VKApiJsonString()
        if (!checkObject(json)) {
            return content
        }
        val root = json.jsonObject
        content.json_data = root.toString()
        return content
    }
}