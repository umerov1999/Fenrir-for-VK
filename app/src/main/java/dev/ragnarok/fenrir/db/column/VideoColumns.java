package dev.ragnarok.fenrir.db.column;

import android.provider.BaseColumns;

public final class VideoColumns implements BaseColumns {

    public static final String TABLENAME = "videos";

    public static final String VIDEO_ID = "video_id";
    public static final String OWNER_ID = "owner_id";
    public static final String ORIGINAL_OWNER_ID = "original_owner_id";
    public static final String ALBUM_ID = "album_id";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String DURATION = "duration";
    public static final String LINK = "link";
    public static final String DATE = "date";
    public static final String ADDING_DATE = "adding_date";
    public static final String VIEWS = "views";
    public static final String PLAYER = "player";
    public static final String IMAGE = "image";
    public static final String ACCESS_KEY = "access_key";
    public static final String COMMENTS = "comments";
    public static final String CAN_COMENT = "can_comment";
    public static final String CAN_REPOST = "can_repost";
    public static final String USER_LIKES = "user_likes";
    public static final String REPEAT = "repeat";
    public static final String LIKES = "likes";
    public static final String PRIVACY_VIEW = "privacy_view";
    public static final String PRIVACY_COMMENT = "privacy_comment";
    public static final String MP4_240 = "mp4_240";
    public static final String MP4_360 = "mp4_360";
    public static final String MP4_480 = "mp4_480";
    public static final String MP4_720 = "mp4_720";
    public static final String MP4_1080 = "mp4_1080";
    public static final String EXTERNAL = "external";
    public static final String HLS = "hls";
    public static final String LIVE = "live";
    public static final String PLATFORM = "platform";
    public static final String CAN_EDIT = "can_edit";
    public static final String CAN_ADD = "can_add";

    public static final String FULL_ID = TABLENAME + "." + _ID;
    public static final String FULL_VIDEO_ID = TABLENAME + "." + VIDEO_ID;
    public static final String FULL_OWNER_ID = TABLENAME + "." + OWNER_ID;
    public static final String FULL_ORIGINAL_OWNER_ID = TABLENAME + "." + ORIGINAL_OWNER_ID;
    public static final String FULL_ALBUM_ID = TABLENAME + "." + ALBUM_ID;
    public static final String FULL_TITLE = TABLENAME + "." + TITLE;
    public static final String FULL_DESCRIPTION = TABLENAME + "." + DESCRIPTION;
    public static final String FULL_DURATION = TABLENAME + "." + DURATION;
    public static final String FULL_LINK = TABLENAME + "." + LINK;
    public static final String FULL_DATE = TABLENAME + "." + DATE;
    public static final String FULL_ADDING_DATE = TABLENAME + "." + ADDING_DATE;
    public static final String FULL_VIEWS = TABLENAME + "." + VIEWS;
    public static final String FULL_PLAYER = TABLENAME + "." + PLAYER;
    public static final String FULL_IMAGE = TABLENAME + "." + IMAGE;
    public static final String FULL_ACCESS_KEY = TABLENAME + "." + ACCESS_KEY;
    public static final String FULL_COMMENTS = TABLENAME + "." + COMMENTS;
    public static final String FULL_CAN_COMENT = TABLENAME + "." + CAN_COMENT;
    public static final String FULL_CAN_REPOST = TABLENAME + "." + CAN_REPOST;
    public static final String FULL_USER_LIKES = TABLENAME + "." + USER_LIKES;
    public static final String FULL_REPEAT = TABLENAME + "." + REPEAT;
    public static final String FULL_LIKES = TABLENAME + "." + LIKES;
    public static final String FULL_PRIVACY_VIEW = TABLENAME + "." + PRIVACY_VIEW;
    public static final String FULL_PRIVACY_COMMENT = TABLENAME + "." + PRIVACY_COMMENT;
    public static final String FULL_MP4_240 = TABLENAME + "." + MP4_240;
    public static final String FULL_MP4_360 = TABLENAME + "." + MP4_360;
    public static final String FULL_MP4_480 = TABLENAME + "." + MP4_480;
    public static final String FULL_MP4_720 = TABLENAME + "." + MP4_720;
    public static final String FULL_MP4_1080 = TABLENAME + "." + MP4_1080;
    public static final String FULL_EXTERNAL = TABLENAME + "." + EXTERNAL;
    public static final String FULL_HLS = TABLENAME + "." + HLS;
    public static final String FULL_LIVE = TABLENAME + "." + LIVE;
    public static final String FULL_PLATFORM = TABLENAME + "." + PLATFORM;
    public static final String FULL_CAN_EDIT = TABLENAME + "." + CAN_EDIT;
    public static final String FULL_CAN_ADD = TABLENAME + "." + CAN_ADD;

}
