package dev.ragnarok.fenrir.db.model.entity

class EntityWrapper {
    private var entity: Entity? = null
    fun wrap(entity: Entity?): EntityWrapper {
        this.entity = entity
        return this
    }

    fun get(): Entity? {
        return entity
    }
}