package dev.ragnarok.fenrir.api.model;

/**
 * A note object describes a note.
 */
public class VKApiNote implements VKApiAttachment {

    /**
     * Note ID, positive number
     */
    public int id;

    /**
     * Note owner ID.
     */
    public int user_id;

    /**
     * Note title.
     */
    public String title;

    /**
     * Note text.
     */
    public String text;

    /**
     * Date (in Unix time) when the note was created.
     */
    public long date;

    /**
     * Number of comments.
     */
    public int comments;

    /**
     * Number of read comments (only if owner_id is the current user).
     */
    public int read_comments;

    /**
     * Creates empty Note instance.
     */
    public VKApiNote() {

    }

    @Override
    public String getType() {
        return TYPE_NOTE;
    }
}