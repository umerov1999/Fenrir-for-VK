package dev.ragnarok.fenrir.model

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