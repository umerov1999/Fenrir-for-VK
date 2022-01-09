package dev.ragnarok.fenrir.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;
import android.text.TextUtils;

import dev.ragnarok.fenrir.api.model.VKApiNews;

public final class NewsColumns implements BaseColumns {

    // Переменные для определения в какой массив добавлять вложения
    // (attachments, copy_history, photos, photo_tags и notes из API),
    // на них ссылаются идентификаторы из table NEWS_ATTACHMENTS, поле ARRAY_TYPE_CODE;
    //public static final int TYPE_CODE_ATTACHMENTS = 1;
    //public static final int TYPE_CODE_COPY_HISTORY = 2;
    //public static final int TYPE_CODE_PHOTOS = 3;
    //public static final int TYPE_CODE_PHOTOS_TAGS = 4;
    //public static final int TYPE_CODE_NOTES = 5;

    public static final String TABLENAME = "news";
    public static final String TYPE = "type";
    public static final String SOURCE_ID = "source_id";
    public static final String DATE = "date";
    public static final String POST_ID = "post_id";
    public static final String POST_TYPE = "post_type";
    public static final String FINAL_POST = "final_post";
    public static final String COPY_OWNER_ID = "copy_owner_id";
    public static final String COPY_POST_ID = "copy_post_id";
    public static final String COPY_POST_DATE = "copy_post_date";
    public static final String TEXT = "text";
    public static final String CAN_EDIT = "can_edit";
    public static final String CAN_DELETE = "can_delete";
    public static final String COMMENT_COUNT = "comment_count";
    public static final String COMMENT_CAN_POST = "comment_can_post";
    public static final String LIKE_COUNT = "like_count";
    public static final String USER_LIKE = "user_like";
    public static final String CAN_LIKE = "can_like";
    public static final String CAN_PUBLISH = "can_publish";
    public static final String REPOSTS_COUNT = "reposts_count";
    public static final String USER_REPOSTED = "user_reposted";
    //public static final String ATTACHMENTS_MASK = "attachments_mask";
    public static final String GEO_ID = "geo_id";
    public static final String TAG_FRIENDS = "friends_tag";
    public static final String ATTACHMENTS_JSON = "attachments_json";
    public static final String VIEWS = "views";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    //public static final String HAS_COPY_HISTORY = "has_copy_history";
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_SOURCE_ID = TABLENAME + "." + SOURCE_ID;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_POST_ID = TABLENAME + "." + POST_ID;
    public static final String FULL_POST_TYPE = TABLENAME + "." + POST_TYPE;
    public static final String FULL_FINAL_POST = TABLENAME + "." + FINAL_POST;
    public static final String FULL_COPY_OWNER_ID = TABLENAME + "." + COPY_OWNER_ID;
    public static final String FULL_COPY_POST_ID = TABLENAME + "." + COPY_POST_ID;
    public static final String FULL_COPY_POST_DATE = TABLENAME + "." + COPY_POST_DATE;
    public static final String FULL_TEXT = TABLENAME + "." + TEXT;
    public static final String FULL_CAN_EDIT = TABLENAME + "." + CAN_EDIT;
    public static final String FULL_CAN_DELETE = TABLENAME + "." + CAN_DELETE;
    public static final String FULL_COMMENT_COUNT = TABLENAME + "." + COMMENT_COUNT;
    public static final String FULL_COMMENT_CAN_POST = TABLENAME + "." + COMMENT_CAN_POST;
    public static final String FULL_LIKE_COUNT = TABLENAME + "." + LIKE_COUNT;
    public static final String FULL_USER_LIKE = TABLENAME + "." + USER_LIKE;
    public static final String FULL_CAN_LIKE = TABLENAME + "." + CAN_LIKE;
    public static final String FULL_CAN_PUBLISH = TABLENAME + "." + CAN_PUBLISH;
    public static final String FULL_REPOSTS_COUNT = TABLENAME + "." + REPOSTS_COUNT;
    public static final String FULL_USER_REPOSTED = TABLENAME + "." + USER_REPOSTED;
    //public static final String FULL_ATTACHMENTS_COUNT = TABLENAME + "." + ATTACHMENTS_MASK;
    public static final String FULL_GEO_ID = TABLENAME + "." + GEO_ID;
    public static final String FULL_TAG_FRIENDS = TABLENAME + "." + TAG_FRIENDS;
    public static final String FULL_ATTACHMENTS_JSON = TABLENAME + "." + ATTACHMENTS_JSON;
    public static final String FULL_VIEWS = TABLENAME + "." + VIEWS;

    private NewsColumns() {
    }

    public static ContentValues getCV(VKApiNews p) {
        ContentValues cv = new ContentValues();
        cv.put(TYPE, p.type);
        cv.put(SOURCE_ID, p.source_id);
        cv.put(DATE, p.date);
        cv.put(POST_ID, p.post_id);
        cv.put(POST_TYPE, p.post_type);
        cv.put(FINAL_POST, p.final_post);
        cv.put(COPY_OWNER_ID, p.copy_owner_id);
        cv.put(COPY_POST_ID, p.copy_post_id);
        cv.put(COPY_POST_DATE, p.copy_post_date);
        cv.put(TEXT, p.text);
        cv.put(CAN_EDIT, p.can_edit);
        cv.put(CAN_DELETE, p.can_delete);
        cv.put(COMMENT_COUNT, p.comment_count);
        cv.put(COMMENT_CAN_POST, p.comment_can_post);
        cv.put(LIKE_COUNT, p.like_count);
        cv.put(USER_LIKE, p.user_like);
        cv.put(CAN_LIKE, p.can_like);
        cv.put(CAN_PUBLISH, p.can_publish);
        cv.put(REPOSTS_COUNT, p.reposts_count);
        cv.put(USER_REPOSTED, p.user_reposted);
        //cv.put(ATTACHMENTS_MASK, Attachments.genAttachmentsMask(p));
        cv.put(GEO_ID, p.geo == null ? 0 : p.geo.id);
        cv.put(TAG_FRIENDS, p.friends != null ? TextUtils.join(",", p.friends) : null);
        cv.put(VIEWS, p.views);

        //cv.put(HAS_COPY_HISTORY, !Utils.safeIsEmpty(p.copy_history));
        return cv;
    }
    //public static final String FULL_HAS_COPY_HISTORY = TABLENAME + "." + HAS_COPY_HISTORY;
}