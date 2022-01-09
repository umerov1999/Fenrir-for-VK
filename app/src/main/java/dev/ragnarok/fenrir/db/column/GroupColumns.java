package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class GroupColumns implements BaseColumns {

    public static final String TABLENAME = "groups";
    public static final String NAME = "name";
    public static final String SCREEN_NAME = "screen_name";
    public static final String IS_CLOSED = "is_closed";
    public static final String IS_ADMIN = "is_admin";
    public static final String ADMIN_LEVEL = "admin_level";
    public static final String IS_MEMBER = "is_member";
    public static final String MEMBER_STATUS = "member_status";
    public static final String MEMBERS_COUNT = "members_count";
    public static final String IS_VERIFIED = "is_verified";
    public static final String TYPE = "type";
    public static final String PHOTO_50 = "photo_50";
    public static final String PHOTO_100 = "photo_100";
    public static final String PHOTO_200 = "photo_200";
    public static final String CAN_ADD_TOPICS = "can_add_topics";
    public static final String TOPICS_ORDER = "topics_order";
    public static final String API_FIELDS = "name,screen_name,is_closed,verified,members_count,is_admin,admin_level," +
            "is_member,member_status,type,photo_50,photo_100,photo_200";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_NAME = TABLENAME + "." + NAME;
    public static final String FULL_SCREEN_NAME = TABLENAME + "." + SCREEN_NAME;
    public static final String FULL_IS_CLOSED = TABLENAME + "." + IS_CLOSED;
    public static final String FULL_IS_ADMIN = TABLENAME + "." + IS_ADMIN;
    public static final String FULL_ADMIN_LEVEL = TABLENAME + "." + ADMIN_LEVEL;
    public static final String FULL_IS_MEMBER = TABLENAME + "." + IS_MEMBER;
    public static final String FULL_MEMBER_STATUS = TABLENAME + "." + MEMBER_STATUS;
    public static final String FULL_MEMBERS_COUNT = TABLENAME + "." + MEMBERS_COUNT;
    public static final String FULL_IS_VERIFIED = TABLENAME + "." + IS_VERIFIED;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FULL_PHOTO_50 = TABLENAME + "." + PHOTO_50;
    public static final String FULL_PHOTO_100 = TABLENAME + "." + PHOTO_100;
    public static final String FULL_PHOTO_200 = TABLENAME + "." + PHOTO_200;
    public static final String FULL_CAN_ADD_TOPICS = TABLENAME + "." + CAN_ADD_TOPICS;
    public static final String FULL_TOPICS_ORDER = TABLENAME + "." + TOPICS_ORDER;

    private GroupColumns() {
    }
}