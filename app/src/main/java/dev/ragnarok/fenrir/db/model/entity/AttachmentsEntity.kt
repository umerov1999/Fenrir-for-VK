package dev.ragnarok.fenrir.db.model.entity

class AttachmentsEntity {
    lateinit var entities: List<Entity>
        private set

    private fun wrap(entities: List<Entity>): AttachmentsEntity {
        this.entities = entities
        return this
    }

    val isEmpty: Boolean
        get() = entities.isEmpty()

    companion object {
        @JvmStatic
        fun from(entities: List<Entity>?): AttachmentsEntity? {
            return if (entities.isNullOrEmpty()) {
                null
            } else AttachmentsEntity().wrap(entities)
        }
    }
}