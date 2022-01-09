package dev.ragnarok.fenrir.db.column;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.content.ContentValues;
import android.provider.BaseColumns;

import dev.ragnarok.fenrir.api.model.VKApiTopic;

public class TopicsColumns implements BaseColumns {

    public static final String TABLENAME = "topics";

    public static final String TOPIC_ID = "topic_id";
    public static final String OWNER_ID = "owner_id";
    public static final String TITLE = "title";
    public static final String CREATED = "created";
    public static final String CREATED_BY = "created_by";
    public static final String UPDATED = "updated";
    public static final String UPDATED_BY = "updated_by";
    public static final String IS_CLOSED = "is_closed";
    public static final String IS_FIXED = "is_fixed";
    public static final String COMMENTS = "comments";
    public static final String FIRST_COMMENT = "first_comment";
    public static final String LAST_COMMENT = "last_comment";
    public static final String ATTACHED_POLL = "attached_poll";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_TOPIC_ID = TABLENAME + "." + TOPIC_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_CREATED = TABLENAME + "." + CREATED;
    public static final String FULL_CREATED_BY = TABLENAME + "." + CREATED_BY;
    public static final String FULL_UPDATED = TABLENAME + "." + UPDATED;
    public static final String FULL_UPDATED_BY = TABLENAME + "." + UPDATED_BY;
    public static final String FULL_IS_CLOSED = TABLENAME + "." + IS_CLOSED;
    public static final String FULL_IS_FIXED = TABLENAME + "." + IS_FIXED;
    public static final String FULL_COMMENTS = TABLENAME + "." + COMMENTS;
    public static final String FULL_FIRST_COMMENT = TABLENAME + "." + FIRST_COMMENT;
    public static final String FULL_LAST_COMMENT = TABLENAME + "." + LAST_COMMENT;
    public static final String FULL_ATTACHED_POLL = TABLENAME + "." + ATTACHED_POLL;

    // This class cannot be instantiated
    private TopicsColumns() {
    }

    public static ContentValues getCV(VKApiTopic p) {
        ContentValues cv = new ContentValues();
        cv.put(TOPIC_ID, p.id);
        cv.put(OWNER_ID, p.owner_id);
        cv.put(TITLE, p.title);
        cv.put(CREATED, p.created);
        cv.put(CREATED_BY, p.created_by);
        cv.put(UPDATED, p.updated);
        cv.put(UPDATED_BY, p.updated_by);
        cv.put(IS_CLOSED, p.is_closed);
        cv.put(IS_FIXED, p.is_fixed);
        cv.put(COMMENTS, isNull(p.comments) ? 0 : p.comments.count);
        cv.put(FIRST_COMMENT, p.first_comment);
        cv.put(LAST_COMMENT, p.last_comment);
        return cv;
    }
}
