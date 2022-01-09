package dev.ragnarok.fenrir.api.model;

/**
 * An audio object describes an audio file and contains the following fields.
 */
@SuppressWarnings("unused")
public class VKApiTopic implements Commentable {

    public int id;

    public int owner_id;

    public String title;

    public long created;

    public int created_by;

    public long updated;

    public int updated_by;

    public boolean is_closed;

    public boolean is_fixed;

    public CommentsDto comments;

    public String first_comment;

    public String last_comment;
}