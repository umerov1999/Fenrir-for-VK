package dev.ragnarok.fenrir.db.model.entity

import kotlinx.serialization.Serializable

@Serializable
sealed interface DboEntity {
    fun getAttachmentKey(): String
}