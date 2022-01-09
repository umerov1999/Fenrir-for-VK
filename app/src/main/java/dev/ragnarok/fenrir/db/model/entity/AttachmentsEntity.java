package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.util.Utils;


public class AttachmentsEntity {

    private List<Entity> entities;

    public static @Nullable
    AttachmentsEntity from(@Nullable List<Entity> entities) {
        if (Utils.isEmpty(entities)) {
            return null;
        }
        return new AttachmentsEntity().wrap(entities);
    }

    private AttachmentsEntity wrap(@NonNull List<Entity> entities) {
        this.entities = entities;
        return this;
    }

    public boolean isEmpty() {
        return Utils.isEmpty(entities);
    }

    public List<Entity> getEntities() {
        return entities;
    }
}