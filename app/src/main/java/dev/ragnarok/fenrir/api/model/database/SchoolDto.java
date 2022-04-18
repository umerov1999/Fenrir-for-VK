package dev.ragnarok.fenrir.api.model.database;

import androidx.annotation.Nullable;

/**
 * A city object describes a School.
 */
public class SchoolDto {

    /**
     * School ID.
     */
    public int id;

    /**
     * School name
     */
    @Nullable
    public String title;
}