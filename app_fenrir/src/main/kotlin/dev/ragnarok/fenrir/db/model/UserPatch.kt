package dev.ragnarok.fenrir.db.model

class UserPatch(val userId: Long) {
    var status: Status? = null
        private set

    fun setStatus(status: Status?): UserPatch {
        this.status = status
        return this
    }

    class Status(val status: String?)
}