package dev.ragnarok.fenrir.db.column;

import android.content.ContentValues;
import android.provider.BaseColumns;

public class RelationshipColumns implements BaseColumns {

    public static final int TYPE_FRIEND = 1;
    public static final int TYPE_FOLLOWER = 2;
    public static final int TYPE_BLACK_LIST = 3;
    public static final int TYPE_MEMBER = 4;
    public static final int TYPE_REQUESTS = 5;
    public static final String TABLENAME = "relationship";
    public static final String OBJECT_ID = "object_id";
    public static final String SUBJECT_ID = "subject_id";
    public static final String TYPE = "type";
    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_OBJECT_ID = TABLENAME + "." + OBJECT_ID;
    public static final String FULL_SUBJECT_ID = TABLENAME + "." + SUBJECT_ID;
    public static final String FULL_TYPE = TABLENAME + "." + TYPE;
    public static final String FOREIGN_SUBJECT_USER_FIRST_NAME = "subject_user_first_name";
    public static final String FOREIGN_SUBJECT_USER_LAST_NAME = "subject_user_last_name";
    public static final String FOREIGN_SUBJECT_USER_ONLINE = "subject_user_online";
    public static final String FOREIGN_SUBJECT_USER_ONLINE_MOBILE = "subject_user_online_mobile";
    public static final String FOREIGN_SUBJECT_USER_ONLINE_APP = "subject_user_online_app";
    public static final String FOREIGN_SUBJECT_USER_PHOTO_50 = "subject_user_photo_50";
    public static final String FOREIGN_SUBJECT_USER_PHOTO_100 = "subject_user_photo_100";
    public static final String FOREIGN_SUBJECT_USER_PHOTO_200 = "subject_user_photo_200";
    public static final String FOREIGN_SUBJECT_USER_PHOTO_MAX = "subject_user_photo_max";
    public static final String FOREIGN_SUBJECT_USER_LAST_SEEN = "subject_user_last_seen";
    public static final String FOREIGN_SUBJECT_USER_PLATFORM = "subject_user_platform";
    public static final String FOREIGN_SUBJECT_USER_STATUS = "subject_user_status";
    public static final String FOREIGN_SUBJECT_USER_SEX = "subject_user_sex";
    public static final String FOREIGN_SUBJECT_USER_IS_FRIEND = "subject_user_is_friend";
    public static final String FOREIGN_SUBJECT_USER_FRIEND_STATUS = "subject_user_friend_status";
    public static final String FOREIGN_SUBJECT_WRITE_MESSAGE_STATUS = "subject_write_message_status";
    public static final String FOREIGN_SUBJECT_IS_USER_BLACK_LIST = "subject_is_user_in_black_list";
    public static final String FOREIGN_SUBJECT_IS_BLACK_LISTED = "subject_is_black_listed";
    public static final String FOREIGN_SUBJECT_IS_CAN_ACCESS_CLOSED = "subject_is_can_access_closed";
    public static final String FOREIGN_SUBJECT_IS_VERIFIED = "subject_is_verified";
    public static final String FOREIGN_SUBJECT_MAIDEN_NAME = "subject_maiden_name";
    public static final String FOREIGN_SUBJECT_GROUP_NAME = "subject_group_name";
    public static final String FOREIGN_SUBJECT_GROUP_SCREEN_NAME = "subject_group_screen_name";
    public static final String FOREIGN_SUBJECT_GROUP_PHOTO_50 = "subject_group_photo_50";
    public static final String FOREIGN_SUBJECT_GROUP_PHOTO_100 = "subject_group_photo_100";
    public static final String FOREIGN_SUBJECT_GROUP_PHOTO_200 = "subject_group_photo_200";
    public static final String FOREIGN_SUBJECT_GROUP_IS_CLOSED = "subject_group_is_closed";
    public static final String FOREIGN_SUBJECT_GROUP_IS_VERIFIED = "subject_group_is_verified";
    public static final String FOREIGN_SUBJECT_GROUP_IS_ADMIN = "subject_group_is_admin";
    public static final String FOREIGN_SUBJECT_GROUP_ADMIN_LEVEL = "subject_group_admin_level";
    public static final String FOREIGN_SUBJECT_GROUP_IS_MEMBER = "subject_group_is_member";
    public static final String FOREIGN_SUBJECT_GROUP_MEMBERS_COUNT = "subject_group_members_count";
    public static final String FOREIGN_SUBJECT_GROUP_MEMBER_STATUS = "subject_group_member_status";
    public static final String FOREIGN_SUBJECT_GROUP_TYPE = "subject_group_type";

    private RelationshipColumns() {
    }

    public static ContentValues getCV(int objectId, int subjectId, int type) {
        ContentValues cv = new ContentValues();
        cv.put(OBJECT_ID, objectId);
        cv.put(SUBJECT_ID, subjectId);
        cv.put(TYPE, type);
        return cv;
    }
}