package dev.ragnarok.fenrir.model

import dev.ragnarok.fenrir.api.adapters.AnswerVKOfficialDtoAdapter
import kotlinx.serialization.Serializable

@Serializable(with = AnswerVKOfficialDtoAdapter::class)
class AnswerVKOfficialList {
    var items: ArrayList<AnswerVKOfficial>? = null
    var fields: ArrayList<AnswerField>? = null
    fun getAvatar(id: Int): String? {
        if (fields.isNullOrEmpty()) return null
        for (i in fields.orEmpty()) {
            if (i.id == id) return i.photo
        }
        return null
    }

    class AnswerField(val id: Int, val photo: String)
}