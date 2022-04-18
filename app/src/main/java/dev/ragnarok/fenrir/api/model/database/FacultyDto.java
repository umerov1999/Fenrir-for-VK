package dev.ragnarok.fenrir.api.model.database;

import androidx.annotation.Nullable;

/**
 * A city object describes a Faculty.
 */
public class FacultyDto {

    /**
     * Faculty ID.
     */
    public int id;

    /**
     * Faculty name
     */
    @Nullable
    public String title;
}