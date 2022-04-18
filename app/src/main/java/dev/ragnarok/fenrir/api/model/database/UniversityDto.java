package dev.ragnarok.fenrir.api.model.database;

import androidx.annotation.Nullable;

/**
 * A city object describes a University.
 */
public class UniversityDto {

    /**
     * University ID.
     */
    public int id;

    /**
     * University name
     */
    @Nullable
    public String title;
}