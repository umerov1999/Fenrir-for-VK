package dev.ragnarok.fenrir.db.model.entity;


import androidx.annotation.Nullable;

public class EntityWrapper {

    private Entity entity;

    public EntityWrapper wrap(@Nullable Entity entity) {
        this.entity = entity;
        return this;
    }

    public @Nullable
    Entity get() {
        return entity;
    }
}