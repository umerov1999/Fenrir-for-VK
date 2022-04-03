package dev.ragnarok.fenrir.db.serialize

interface ISerializeAdapter<T> {
    fun deserialize(raw: String): T
    fun serialize(data: T): String
}