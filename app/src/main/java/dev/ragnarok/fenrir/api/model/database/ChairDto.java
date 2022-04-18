package dev.ragnarok.fenrir.api.model.database;

import androidx.annotation.Nullable;

/**
 * A city object describes a Chair.
 */
public class ChairDto {

    /**
     * Chair ID.
     */
    public int id;

    /**
     * Chair name
     */
    @Nullable
    public String title;
}