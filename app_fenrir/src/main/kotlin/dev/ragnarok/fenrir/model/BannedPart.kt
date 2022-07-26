package dev.ragnarok.fenrir.model

class BannedPart(val users: List<User>) {
    fun getTotalCount(): Int {
        return users.size
    }
}