package dev.ragnarok.fenrir.api.model.database;

import androidx.annotation.Nullable;

/**
 * A city object describes a SchoolClazz.
 */
public class SchoolClazzDto {

    /**
     * SchoolClazz ID.
     */
    public int id;

    /**
     * SchoolClazz name
     */
    @Nullable
    public String title;
}