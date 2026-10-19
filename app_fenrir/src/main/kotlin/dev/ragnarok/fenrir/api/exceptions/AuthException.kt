package dev.ragnarok.fenrir.api.exceptions

import dev.ragnarok.fenrir.nonNullNoEmpty

class AuthException(val code: String, message: String?) : Exception(message) {
    override val message: String
        get() {
            val desc = super.message
            return if (desc.nonNullNoEmpty()) {
                desc
            } else "Unexpected auth error, code: [$code]"
        }
}