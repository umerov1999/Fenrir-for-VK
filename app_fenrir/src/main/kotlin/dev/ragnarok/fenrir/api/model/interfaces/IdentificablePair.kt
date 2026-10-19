package dev.ragnarok.fenrir.api.model.interfaces

interface IdentificablePair {
    fun getObjectOwnerId(): Long
    fun getObjectId(): Int
}