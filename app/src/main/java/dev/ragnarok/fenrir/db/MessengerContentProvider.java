package dev.ragnarok.fenrir.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.db.column.CommentsColumns;
import dev.ragnarok.fenrir.db.column.CountriesColumns;
import dev.ragnarok.fenrir.db.column.DialogsColumns;
import dev.ragnarok.fenrir.db.column.DocColumns;
import dev.ragnarok.fenrir.db.column.FaveArticlesColumns;
import dev.ragnarok.fenrir.db.column.FaveLinksColumns;
import dev.ragnarok.fenrir.db.column.FavePageColumns;
import dev.ragnarok.fenrir.db.column.FavePhotosColumns;
import dev.ragnarok.fenrir.db.column.FavePostsColumns;
import dev.ragnarok.fenrir.db.column.FaveProductColumns;
import dev.ragnarok.fenrir.db.column.FaveVideosColumns;
import dev.ragnarok.fenrir.db.column.FeedListsColumns;
import dev.ragnarok.fenrir.db.column.FriendListsColumns;
import dev.ragnarok.fenrir.db.column.GroupColumns;
import dev.ragnarok.fenrir.db.column.GroupsDetColumns;
import dev.ragnarok.fenrir.db.column.KeyColumns;
import dev.ragnarok.fenrir.db.column.MessageColumns;
import dev.ragnarok.fenrir.db.column.NewsColumns;
import dev.ragnarok.fenrir.db.column.NotificationColumns;
import dev.ragnarok.fenrir.db.column.PeersColumns;
import dev.ragnarok.fenrir.db.column.PhotoAlbumsColumns;
import dev.ragnarok.fenrir.db.column.PhotosColumns;
import dev.ragnarok.fenrir.db.column.PostsColumns;
import dev.ragnarok.fenrir.db.column.RelationshipColumns;
import dev.ragnarok.fenrir.db.column.TopicsColumns;
import dev.ragnarok.fenrir.db.column.UserColumns;
import dev.ragnarok.fenrir.db.column.UsersDetColumns;
import dev.ragnarok.fenrir.db.column.VideoAlbumsColumns;
import dev.ragnarok.fenrir.db.column.VideoColumns;
import dev.ragnarok.fenrir.db.column.attachments.CommentsAttachmentsColumns;
import dev.ragnarok.fenrir.db.column.attachments.MessagesAttachmentsColumns;
import dev.ragnarok.fenrir.db.column.attachments.WallAttachmentsColumns;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Utils;

public class MessengerContentProvider extends ContentProvider {

    // Uri authority
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".providers.Messages";
    static final int URI_USERS = 1;
    static final int URI_USERS_ID = 2;
    static final int URI_MESSAGES = 3;
    static final int URI_MESSAGES_ID = 4;
    static final int URI_MESSAGES_ATTACHMENTS = 5;
    static final int URI_MESSAGES_ATTACHMENTS_ID = 6;
    static final int URI_PHOTOS = 7;
    static final int URI_PHOTOS_ID = 8;
    static final int URI_DIALOGS = 9;
    static final int URI_DOCS = 10;
    static final int URI_DOCS_ID = 11;
    static final int URI_VIDEOS = 12;
    static final int URI_VIDEOS_ID = 13;
    static final int URI_POSTS = 14;
    static final int URI_POSTS_ID = 15;
    static final int URI_POST_ATTACHMENTS = 16;
    static final int URI_POST_ATTACHMENTS_ID = 17;
    static final int URI_GROUPS = 18;
    static final int URI_GROUPS_ID = 19;
    static final int URI_RELATIVESHIP = 20;
    static final int URI_COMMENTS = 21;
    static final int URI_COMMENTS_ID = 22;
    static final int URI_COMMENTS_ATTACHMENTS = 23;
    static final int URI_COMMENTS_ATTACHMENTS_ID = 24;
    static final int URI_PHOTO_ALBUMS = 25;
    static final int URI_NEWS = 26;
    static final int URI_GROUPS_DET = 27;
    static final int URI_GROUPS_DET_ID = 28;
    static final int URI_VIDEO_ALBUMS = 29;
    static final int URI_TOPICS = 30;
    static final int URI_NOTIFICATIONS = 31;
    static final int URI_USER_DET = 32;
    static final int URI_USER_DET_ID = 33;
    static final int URI_FAVE_PHOTOS = 34;
    static final int URI_FAVE_VIDEOS = 35;
    static final int URI_FAVE_PAGES = 36;
    static final int URI_FAVE_GROUPS = 37;
    static final int URI_FAVE_LINKS = 38;
    static final int URI_FAVE_POSTS = 39;
    static final int URI_FAVE_ARTICLES = 40;
    static final int URI_FAVE_PRODUCTS = 41;
    static final int URI_COUNTRIES = 42;
    static final int URI_FEED_LISTS = 43;
    static final int URI_FRIEND_LISTS = 44;
    static final int URI_KEYS = 45;
    static final int URI_PEERS = 46;
    // path
    static final String USER_PATH = "users";
    static final String MESSAGES_PATH = "messages";
    static final String MESSAGES_ATTACHMENTS_PATH = "messages_attachments";
    static final String PHOTOS_PATH = "photos";
    static final String DIALOGS_PATH = "dialogs";
    static final String PEERS_PATH = "peers";
    static final String DOCS_PATH = "docs";
    static final String VIDEOS_PATH = "videos";
    static final String POSTS_PATH = "posts";
    static final String POSTS_ATTACHMENTS_PATH = "wall_attachments";
    static final String GROUPS_PATH = "groups";
    static final String RELATIVESHIP_PATH = "relativeship";
    static final String COMMENTS_PATH = "comments";
    static final String COMMENTS_ATTACHMENTS_PATH = "comments_attachments";
    static final String PHOTO_ALBUMS_PATH = "photo_albums";
    static final String NEWS_PATH = "news";
    static final String GROUPS_DET_PATH = "groups_det";
    static final String VIDEO_ALBUMS_PATH = "video_albums";
    static final String TOPICS_PATH = "topics";
    static final String NOTIFICATIONS_PATH = "notifications";
    static final String USER_DET_PATH = "user_det";
    static final String FAVE_PHOTOS_PATH = "fave_photos";
    static final String FAVE_VIDEOS_PATH = "fave_videos";
    static final String FAVE_PAGES_PATH = "fave_pages";
    static final String FAVE_GROUPS_PATH = "fave_groups";
    static final String FAVE_ARTICLES_PATH = "fave_articles";
    static final String FAVE_PRODUCTS_PATH = "fave_products";
    static final String FAVE_LINKS_PATH = "fave_links";
    static final String FAVE_POSTS_PATH = "fave_posts";
    static final String COUNTRIES_PATH = "countries";
    static final String FEED_LISTS_PATH = "feed_lists";
    static final String FRIEND_LISTS_PATH = "friends_lists";
    static final String KEYS_PATH = "keys";
    // Типы данных
    static final String USER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + USER_PATH;
    static final String USER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + USER_PATH;
    static final String MESSAGE_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + MESSAGES_PATH;
    static final String MESSAGE_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + MESSAGES_PATH;
    static final String MESSAGES_ATTACHMENTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + MESSAGES_ATTACHMENTS_PATH;
    static final String MESSAGES_ATTACHMENTS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + MESSAGES_ATTACHMENTS_PATH;
    static final String PHOTOS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PHOTOS_PATH;
    static final String PHOTOS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + PHOTOS_PATH;
    static final String DIALOGS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DIALOGS_PATH;
    static final String PEERS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PEERS_PATH;
    static final String DOCS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + DOCS_PATH;
    static final String DOCS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + DOCS_PATH;
    static final String VIDEOS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + VIDEOS_PATH;
    static final String VIDEOS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + VIDEOS_PATH;
    static final String POSTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + POSTS_PATH;
    static final String POSTS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + POSTS_PATH;
    static final String POSTS_ATTACHMENTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + POSTS_ATTACHMENTS_PATH;
    static final String POSTS_ATTACHMENTS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + POSTS_ATTACHMENTS_PATH;
    static final String GROUPS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + GROUPS_PATH;
    static final String GROUPS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + GROUPS_PATH;
    static final String RELATIVESHIP_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + RELATIVESHIP_PATH;
    static final String COMMENTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + COMMENTS_PATH;
    static final String COMMENTS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + COMMENTS_PATH;
    static final String COMMENTS_ATTACHMENTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + COMMENTS_ATTACHMENTS_PATH;
    static final String COMMENTS_ATTACHMENTS_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + COMMENTS_ATTACHMENTS_PATH;
    static final String PHOTO_ALBUMS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + PHOTO_ALBUMS_PATH;
    static final String NEWS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + NEWS_PATH;
    static final String GROUPS_DET_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + GROUPS_DET_PATH;
    static final String GROUPS_DET_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + GROUPS_DET_PATH;
    static final String VIDEO_ALBUMS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + VIDEO_ALBUMS_PATH;
    static final String TOPICS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + TOPICS_PATH;
    static final String NOTIFICATIONS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + NOTIFICATIONS_PATH;
    static final String USER_DET_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + USER_DET_PATH;
    static final String USER_DET_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + USER_DET_PATH;
    static final String FAVE_PHOTOS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_PHOTOS_PATH;
    static final String FAVE_VIDEOS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_VIDEOS_PATH;
    static final String FAVE_ARTICLES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_ARTICLES_PATH;
    static final String FAVE_PRODUCTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_PRODUCTS_PATH;
    static final String FAVE_PAGES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_PAGES_PATH;
    static final String FAVE_GROUPS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_GROUPS_PATH;
    static final String FAVE_LINKS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_LINKS_PATH;
    static final String FAVE_POSTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FAVE_POSTS_PATH;
    static final String COUNTRIES_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + COUNTRIES_PATH;
    static final String FEED_LISTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FEED_LISTS_PATH;
    static final String FRIEND_LISTS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + FRIEND_LISTS_PATH;
    static final String KEYS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + KEYS_PATH;
    // описание и создание UriMatcher
    private static final UriMatcher sUriMatcher;
    // Общий Uri
    private static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + USER_PATH);
    private static final Uri MESSAGE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGES_PATH);
    private static final Uri MESSAGES_ATTACHMENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + MESSAGES_ATTACHMENTS_PATH);
    private static final Uri PHOTOS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PHOTOS_PATH);
    private static final Uri DIALOGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DIALOGS_PATH);
    private static final Uri PEERS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PEERS_PATH);
    private static final Uri DOCS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DOCS_PATH);
    private static final Uri VIDEOS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + VIDEOS_PATH);
    private static final Uri POSTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + POSTS_PATH);
    private static final Uri POSTS_ATTACHMENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + POSTS_ATTACHMENTS_PATH);
    private static final Uri GROUPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GROUPS_PATH);
    private static final Uri RELATIVESHIP_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + RELATIVESHIP_PATH);
    private static final Uri COMMENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + COMMENTS_PATH);
    private static final Uri COMMENTS_ATTACHMENTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + COMMENTS_ATTACHMENTS_PATH);
    private static final Uri PHOTO_ALBUMS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PHOTO_ALBUMS_PATH);
    private static final Uri NEWS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NEWS_PATH);
    private static final Uri GROUPS_DET_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + GROUPS_DET_PATH);
    private static final Uri VIDEO_ALBUMS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + VIDEO_ALBUMS_PATH);
    private static final Uri TOPICS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TOPICS_PATH);
    private static final Uri NOTIFICATIONS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + NOTIFICATIONS_PATH);
    private static final Uri USER_DET_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + USER_DET_PATH);
    private static final Uri FAVE_PHOTOS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_PHOTOS_PATH);
    private static final Uri FAVE_VIDEOS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_VIDEOS_PATH);
    private static final Uri FAVE_PAGES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_PAGES_PATH);
    private static final Uri FAVE_GROUPS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_GROUPS_PATH);
    private static final Uri FAVE_ARTICLES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_ARTICLES_PATH);
    private static final Uri FAVE_PRODUCTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_PRODUCTS_PATH);
    private static final Uri FAVE_LINKS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_LINKS_PATH);
    private static final Uri FAVE_POSTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FAVE_POSTS_PATH);
    private static final Uri COUNTRIES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + COUNTRIES_PATH);
    private static final Uri FEED_LISTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FEED_LISTS_PATH);
    private static final Uri FRIEND_LISTS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + FRIEND_LISTS_PATH);
    private static final Uri KEYS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + KEYS_PATH);
    private static final String AID = "aid";
    private static final Map<String, String> sUsersProjectionMap;
    private static final Map<String, String> sMessagesProjectionMap;
    private static final Map<String, String> sMessagesAttachmentsProjectionMap;
    private static final Map<String, String> sPhotosProjectionMap;
    private static final Map<String, String> sDialogsProjectionMap;
    private static final Map<String, String> sPeersProjectionMap;
    private static final Map<String, String> sDocsProjectionMap;
    private static final Map<String, String> sVideosProjectionMap;
    private static final Map<String, String> sPostsProjectionMap;
    private static final Map<String, String> sPostsMessagesAttachmentsProjectionMap;
    private static final Map<String, String> sGroupsProjectionMap;
    private static final Map<String, String> sGroupsDetProjectionMap;
    private static final Map<String, String> sRelativeshipProjectionMap;
    private static final Map<String, String> sCommentsProjectionMap;
    private static final Map<String, String> sCommentsMessagesAttachmentsProjectionMap;
    private static final Map<String, String> sPhotoAlbumsProjectionMap;
    private static final Map<String, String> sNewsProjectionMap;
    private static final Map<String, String> sVideoAlbumsProjectionMap;
    private static final Map<String, String> sTopicsProjectionMap;
    private static final Map<String, String> sNoticationsProjectionMap;
    private static final Map<String, String> sUserDetProjectionMap;
    private static final Map<String, String> sFavePhotosProjectionMap;
    private static final Map<String, String> sFaveVideosProjectionMap;
    private static final Map<String, String> sFaveUsersProjectionMap;
    private static final Map<String, String> sFaveGroupsProjectionMap;
    private static final Map<String, String> sFaveLinksProjectionMap;
    private static final Map<String, String> sFavePostsProjectionMap;
    private static final Map<String, String> sFaveArticlesProjectionMap;
    private static final Map<String, String> sFaveProductsProjectionMap;
    private static final Map<String, String> sCountriesProjectionMap;
    private static final Map<String, String> sFeedListsProjectionMap;
    private static final Map<String, String> sFriendListsProjectionMap;
    private static final Map<String, String> sKeysProjectionMap;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, USER_PATH, URI_USERS);
        sUriMatcher.addURI(AUTHORITY, USER_PATH + "/#", URI_USERS_ID);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_PATH, URI_MESSAGES);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_PATH + "/#", URI_MESSAGES_ID);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_ATTACHMENTS_PATH, URI_MESSAGES_ATTACHMENTS);
        sUriMatcher.addURI(AUTHORITY, MESSAGES_ATTACHMENTS_PATH + "/#", URI_MESSAGES_ATTACHMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, PHOTOS_PATH, URI_PHOTOS);
        sUriMatcher.addURI(AUTHORITY, PHOTOS_PATH + "/#", URI_PHOTOS_ID);
        sUriMatcher.addURI(AUTHORITY, DIALOGS_PATH, URI_DIALOGS);
        sUriMatcher.addURI(AUTHORITY, PEERS_PATH, URI_PEERS);
        sUriMatcher.addURI(AUTHORITY, DOCS_PATH, URI_DOCS);
        sUriMatcher.addURI(AUTHORITY, DOCS_PATH + "/#", URI_DOCS_ID);
        sUriMatcher.addURI(AUTHORITY, VIDEOS_PATH, URI_VIDEOS);
        sUriMatcher.addURI(AUTHORITY, VIDEOS_PATH + "/#", URI_VIDEOS_ID);
        sUriMatcher.addURI(AUTHORITY, POSTS_PATH, URI_POSTS);
        sUriMatcher.addURI(AUTHORITY, POSTS_PATH + "/#", URI_POSTS_ID);
        sUriMatcher.addURI(AUTHORITY, POSTS_ATTACHMENTS_PATH, URI_POST_ATTACHMENTS);
        sUriMatcher.addURI(AUTHORITY, POSTS_ATTACHMENTS_PATH + "/#", URI_POST_ATTACHMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, GROUPS_PATH, URI_GROUPS);
        sUriMatcher.addURI(AUTHORITY, GROUPS_PATH + "/#", URI_GROUPS_ID);
        sUriMatcher.addURI(AUTHORITY, RELATIVESHIP_PATH, URI_RELATIVESHIP);
        sUriMatcher.addURI(AUTHORITY, COMMENTS_PATH, URI_COMMENTS);
        sUriMatcher.addURI(AUTHORITY, COMMENTS_PATH + "/#", URI_COMMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, COMMENTS_ATTACHMENTS_PATH, URI_COMMENTS_ATTACHMENTS);
        sUriMatcher.addURI(AUTHORITY, COMMENTS_ATTACHMENTS_PATH + "/#", URI_COMMENTS_ATTACHMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, PHOTO_ALBUMS_PATH, URI_PHOTO_ALBUMS);
        sUriMatcher.addURI(AUTHORITY, NEWS_PATH, URI_NEWS);
        sUriMatcher.addURI(AUTHORITY, GROUPS_DET_PATH, URI_GROUPS_DET);
        sUriMatcher.addURI(AUTHORITY, GROUPS_DET_PATH + "/#", URI_GROUPS_DET_ID);
        sUriMatcher.addURI(AUTHORITY, VIDEO_ALBUMS_PATH, URI_VIDEO_ALBUMS);
        sUriMatcher.addURI(AUTHORITY, TOPICS_PATH, URI_TOPICS);
        sUriMatcher.addURI(AUTHORITY, NOTIFICATIONS_PATH, URI_NOTIFICATIONS);
        sUriMatcher.addURI(AUTHORITY, USER_DET_PATH, URI_USER_DET);
        sUriMatcher.addURI(AUTHORITY, USER_DET_PATH + "/#", URI_USER_DET_ID);
        sUriMatcher.addURI(AUTHORITY, FAVE_PHOTOS_PATH, URI_FAVE_PHOTOS);
        sUriMatcher.addURI(AUTHORITY, FAVE_VIDEOS_PATH, URI_FAVE_VIDEOS);
        sUriMatcher.addURI(AUTHORITY, FAVE_PAGES_PATH, URI_FAVE_PAGES);
        sUriMatcher.addURI(AUTHORITY, FAVE_GROUPS_PATH, URI_FAVE_GROUPS);
        sUriMatcher.addURI(AUTHORITY, FAVE_LINKS_PATH, URI_FAVE_LINKS);
        sUriMatcher.addURI(AUTHORITY, FAVE_ARTICLES_PATH, URI_FAVE_ARTICLES);
        sUriMatcher.addURI(AUTHORITY, FAVE_PRODUCTS_PATH, URI_FAVE_PRODUCTS);
        sUriMatcher.addURI(AUTHORITY, FAVE_POSTS_PATH, URI_FAVE_POSTS);
        sUriMatcher.addURI(AUTHORITY, COUNTRIES_PATH, URI_COUNTRIES);
        sUriMatcher.addURI(AUTHORITY, FEED_LISTS_PATH, URI_FEED_LISTS);
        sUriMatcher.addURI(AUTHORITY, FRIEND_LISTS_PATH, URI_FRIEND_LISTS);
        sUriMatcher.addURI(AUTHORITY, KEYS_PATH, URI_KEYS);
    }

    static {
        //Setup projection maps
        sUsersProjectionMap = new HashMap<>();
        sUsersProjectionMap.put(BaseColumns._ID, UserColumns.FULL_ID);
        sUsersProjectionMap.put(UserColumns.FIRST_NAME, UserColumns.FULL_FIRST_NAME);
        sUsersProjectionMap.put(UserColumns.LAST_NAME, UserColumns.FULL_LAST_NAME);
        sUsersProjectionMap.put(UserColumns.ONLINE, UserColumns.FULL_ONLINE);
        sUsersProjectionMap.put(UserColumns.ONLINE_MOBILE, UserColumns.FULL_ONLINE_MOBILE);
        sUsersProjectionMap.put(UserColumns.ONLINE_APP, UserColumns.FULL_ONLINE_APP);
        sUsersProjectionMap.put(UserColumns.PHOTO_50, UserColumns.FULL_PHOTO_50);
        sUsersProjectionMap.put(UserColumns.PHOTO_100, UserColumns.FULL_PHOTO_100);
        sUsersProjectionMap.put(UserColumns.PHOTO_200, UserColumns.FULL_PHOTO_200);
        sUsersProjectionMap.put(UserColumns.PHOTO_MAX, UserColumns.FULL_PHOTO_MAX);
        sUsersProjectionMap.put(UserColumns.LAST_SEEN, UserColumns.FULL_LAST_SEEN);
        sUsersProjectionMap.put(UserColumns.PLATFORM, UserColumns.FULL_PLATFORM);
        sUsersProjectionMap.put(UserColumns.USER_STATUS, UserColumns.FULL_USER_STATUS);
        sUsersProjectionMap.put(UserColumns.SEX, UserColumns.FULL_SEX);
        sUsersProjectionMap.put(UserColumns.DOMAIN, UserColumns.FULL_DOMAIN);
        sUsersProjectionMap.put(UserColumns.IS_FRIEND, UserColumns.FULL_IS_FRIEND);
        sUsersProjectionMap.put(UserColumns.FRIEND_STATUS, UserColumns.FULL_FRIEND_STATUS);
        sUsersProjectionMap.put(UserColumns.WRITE_MESSAGE_STATUS, UserColumns.FULL_WRITE_MESSAGE_STATUS);
        sUsersProjectionMap.put(UserColumns.IS_USER_BLACK_LIST, UserColumns.FULL_IS_USER_BLACK_LIST);
        sUsersProjectionMap.put(UserColumns.IS_BLACK_LISTED, UserColumns.FULL_IS_BLACK_LISTED);
        sUsersProjectionMap.put(UserColumns.IS_CAN_ACCESS_CLOSED, UserColumns.FULL_IS_CAN_ACCESS_CLOSED);
        sUsersProjectionMap.put(UserColumns.IS_VERIFIED, UserColumns.FULL_IS_VERIFIED);
        sUsersProjectionMap.put(UserColumns.MAIDEN_NAME, UserColumns.FULL_MAIDEN_NAME);

        sRelativeshipProjectionMap = new HashMap<>();
        sRelativeshipProjectionMap.put(BaseColumns._ID, RelationshipColumns.FULL_ID);
        sRelativeshipProjectionMap.put(RelationshipColumns.OBJECT_ID, RelationshipColumns.FULL_OBJECT_ID);
        sRelativeshipProjectionMap.put(RelationshipColumns.SUBJECT_ID, RelationshipColumns.FULL_SUBJECT_ID);
        sRelativeshipProjectionMap.put(RelationshipColumns.TYPE, RelationshipColumns.FULL_TYPE);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_FIRST_NAME, UserColumns.FULL_FIRST_NAME + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_FIRST_NAME);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_NAME, UserColumns.FULL_LAST_NAME + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_NAME);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE, UserColumns.FULL_ONLINE + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE_MOBILE, UserColumns.FULL_ONLINE_MOBILE + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE_MOBILE);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE_APP, UserColumns.FULL_ONLINE_APP + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_ONLINE_APP);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_50, UserColumns.FULL_PHOTO_50 + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_50);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_100, UserColumns.FULL_PHOTO_100 + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_100);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_200, UserColumns.FULL_PHOTO_200 + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_200);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_MAX, UserColumns.FULL_PHOTO_MAX + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_PHOTO_MAX);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_SEEN, UserColumns.FULL_LAST_SEEN + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_LAST_SEEN);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_PLATFORM, UserColumns.FULL_PLATFORM + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_PLATFORM);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_STATUS, UserColumns.FULL_USER_STATUS + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_STATUS);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_SEX, UserColumns.FULL_SEX + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_SEX);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_IS_FRIEND, UserColumns.FULL_IS_FRIEND + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_IS_FRIEND);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_USER_FRIEND_STATUS, UserColumns.FULL_FRIEND_STATUS + " AS " + RelationshipColumns.FOREIGN_SUBJECT_USER_FRIEND_STATUS);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_WRITE_MESSAGE_STATUS, UserColumns.FULL_WRITE_MESSAGE_STATUS + " AS " + RelationshipColumns.FOREIGN_SUBJECT_WRITE_MESSAGE_STATUS);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_IS_USER_BLACK_LIST, UserColumns.FULL_IS_USER_BLACK_LIST + " AS " + RelationshipColumns.FOREIGN_SUBJECT_IS_USER_BLACK_LIST);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_IS_BLACK_LISTED, UserColumns.FULL_IS_BLACK_LISTED + " AS " + RelationshipColumns.FOREIGN_SUBJECT_IS_BLACK_LISTED);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_IS_CAN_ACCESS_CLOSED, UserColumns.FULL_IS_CAN_ACCESS_CLOSED + " AS " + RelationshipColumns.FOREIGN_SUBJECT_IS_CAN_ACCESS_CLOSED);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_IS_VERIFIED, UserColumns.FULL_IS_VERIFIED + " AS " + RelationshipColumns.FOREIGN_SUBJECT_IS_VERIFIED);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_MAIDEN_NAME, UserColumns.FULL_MAIDEN_NAME + " AS " + RelationshipColumns.FOREIGN_SUBJECT_MAIDEN_NAME);

        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_NAME, GroupColumns.FULL_NAME + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_NAME);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_SCREEN_NAME, GroupColumns.FULL_SCREEN_NAME + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_SCREEN_NAME);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_50, GroupColumns.FULL_PHOTO_50 + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_50);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_100, GroupColumns.FULL_PHOTO_100 + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_100);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_200, GroupColumns.FULL_PHOTO_200 + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_PHOTO_200);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_CLOSED, GroupColumns.FULL_IS_CLOSED + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_CLOSED);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_VERIFIED, GroupColumns.FULL_IS_VERIFIED + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_VERIFIED);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_ADMIN, GroupColumns.FULL_IS_ADMIN + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_ADMIN);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_ADMIN_LEVEL, GroupColumns.FULL_ADMIN_LEVEL + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_ADMIN_LEVEL);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_MEMBER, GroupColumns.FULL_IS_MEMBER + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_IS_MEMBER);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBERS_COUNT, GroupColumns.FULL_MEMBERS_COUNT + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBERS_COUNT);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBER_STATUS, GroupColumns.FULL_MEMBER_STATUS + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_MEMBER_STATUS);
        sRelativeshipProjectionMap.put(RelationshipColumns.FOREIGN_SUBJECT_GROUP_TYPE, GroupColumns.FULL_TYPE + " AS " + RelationshipColumns.FOREIGN_SUBJECT_GROUP_TYPE);

        sMessagesProjectionMap = new HashMap<>();
        sMessagesProjectionMap.put(MessageColumns._ID, MessageColumns.FULL_ID);
        sMessagesProjectionMap.put(MessageColumns.PEER_ID, MessageColumns.FULL_PEER_ID);
        sMessagesProjectionMap.put(MessageColumns.FROM_ID, MessageColumns.FULL_FROM_ID);
        sMessagesProjectionMap.put(MessageColumns.DATE, MessageColumns.FULL_DATE);
        //sMessagesProjectionMap.put(MessageColumns.READ_STATE, MessageColumns.FULL_READ_STATE);
        sMessagesProjectionMap.put(MessageColumns.OUT, MessageColumns.FULL_OUT);
        //sMessagesProjectionMap.put(MessageColumns.TITLE, MessageColumns.FULL_TITLE);
        sMessagesProjectionMap.put(MessageColumns.BODY, MessageColumns.FULL_BODY);
        sMessagesProjectionMap.put(MessageColumns.ENCRYPTED, MessageColumns.FULL_ENCRYPTED);
        sMessagesProjectionMap.put(MessageColumns.DELETED, MessageColumns.FULL_DELETED);
        sMessagesProjectionMap.put(MessageColumns.DELETED_FOR_ALL, MessageColumns.FULL_DELETED_FOR_ALL);
        sMessagesProjectionMap.put(MessageColumns.IMPORTANT, MessageColumns.FULL_IMPORTANT);
        sMessagesProjectionMap.put(MessageColumns.FORWARD_COUNT, MessageColumns.FULL_FORWARD_COUNT);
        sMessagesProjectionMap.put(MessageColumns.HAS_ATTACHMENTS, MessageColumns.FULL_HAS_ATTACHMENTS);
        sMessagesProjectionMap.put(MessageColumns.STATUS, MessageColumns.FULL_STATUS);
        sMessagesProjectionMap.put(MessageColumns.ATTACH_TO, MessageColumns.FULL_ATTACH_TO);
        sMessagesProjectionMap.put(MessageColumns.ORIGINAL_ID, MessageColumns.FULL_ORIGINAL_ID);
        sMessagesProjectionMap.put(MessageColumns.UPDATE_TIME, MessageColumns.FULL_UPDATE_TIME);
        sMessagesProjectionMap.put(MessageColumns.ACTION, MessageColumns.FULL_ACTION);
        sMessagesProjectionMap.put(MessageColumns.ACTION_MID, MessageColumns.FULL_ACTION_MID);
        sMessagesProjectionMap.put(MessageColumns.ACTION_EMAIL, MessageColumns.FULL_ACTION_EMAIL);
        sMessagesProjectionMap.put(MessageColumns.ACTION_TEXT, MessageColumns.FULL_ACTION_TEXT);
        sMessagesProjectionMap.put(MessageColumns.PHOTO_50, MessageColumns.FULL_PHOTO_50);
        sMessagesProjectionMap.put(MessageColumns.PHOTO_100, MessageColumns.FULL_PHOTO_100);
        sMessagesProjectionMap.put(MessageColumns.PHOTO_200, MessageColumns.FULL_PHOTO_200);
        sMessagesProjectionMap.put(MessageColumns.RANDOM_ID, MessageColumns.FULL_RANDOM_ID);
        sMessagesProjectionMap.put(MessageColumns.EXTRAS, MessageColumns.FULL_EXTRAS);
        sMessagesProjectionMap.put(MessageColumns.PAYLOAD, MessageColumns.FULL_PAYLOAD);
        sMessagesProjectionMap.put(MessageColumns.KEYBOARD, MessageColumns.FULL_KEYBOARD);

        sMessagesAttachmentsProjectionMap = new HashMap<>();
        sMessagesAttachmentsProjectionMap.put(BaseColumns._ID, MessagesAttachmentsColumns.FULL_ID);
        sMessagesAttachmentsProjectionMap.put(MessagesAttachmentsColumns.M_ID, MessagesAttachmentsColumns.FULL_M_ID);
        sMessagesAttachmentsProjectionMap.put(MessagesAttachmentsColumns.TYPE, MessagesAttachmentsColumns.FULL_TYPE);
        sMessagesAttachmentsProjectionMap.put(MessagesAttachmentsColumns.DATA, MessagesAttachmentsColumns.FULL_DATA);

        sPhotosProjectionMap = new HashMap<>();
        sPhotosProjectionMap.put(BaseColumns._ID, PhotosColumns.FULL_ID);
        sPhotosProjectionMap.put(PhotosColumns.PHOTO_ID, PhotosColumns.FULL_PHOTO_ID);
        sPhotosProjectionMap.put(PhotosColumns.ALBUM_ID, PhotosColumns.FULL_ALBUM_ID);
        sPhotosProjectionMap.put(PhotosColumns.OWNER_ID, PhotosColumns.FULL_OWNER_ID);
        sPhotosProjectionMap.put(PhotosColumns.WIDTH, PhotosColumns.FULL_WIDTH);
        sPhotosProjectionMap.put(PhotosColumns.HEIGHT, PhotosColumns.FULL_HEIGHT);
        sPhotosProjectionMap.put(PhotosColumns.TEXT, PhotosColumns.FULL_TEXT);
        sPhotosProjectionMap.put(PhotosColumns.DATE, PhotosColumns.FULL_DATE);
        sPhotosProjectionMap.put(PhotosColumns.SIZES, PhotosColumns.FULL_SIZES);
        sPhotosProjectionMap.put(PhotosColumns.USER_LIKES, PhotosColumns.FULL_USER_LIKES);
        sPhotosProjectionMap.put(PhotosColumns.CAN_COMMENT, PhotosColumns.FULL_CAN_COMMENT);
        sPhotosProjectionMap.put(PhotosColumns.LIKES, PhotosColumns.FULL_LIKES);
        sPhotosProjectionMap.put(PhotosColumns.COMMENTS, PhotosColumns.FULL_COMMENTS);
        sPhotosProjectionMap.put(PhotosColumns.TAGS, PhotosColumns.FULL_TAGS);
        sPhotosProjectionMap.put(PhotosColumns.ACCESS_KEY, PhotosColumns.FULL_ACCESS_KEY);
        sPhotosProjectionMap.put(PhotosColumns.DELETED, PhotosColumns.FULL_DELETED);

        sDialogsProjectionMap = new HashMap<>();
        sDialogsProjectionMap.put(BaseColumns._ID, DialogsColumns.FULL_ID);
        sDialogsProjectionMap.put(DialogsColumns.UNREAD, DialogsColumns.FULL_UNREAD);
        sDialogsProjectionMap.put(DialogsColumns.TITLE, DialogsColumns.FULL_TITLE);
        sDialogsProjectionMap.put(DialogsColumns.IN_READ, DialogsColumns.FULL_IN_READ);
        sDialogsProjectionMap.put(DialogsColumns.OUT_READ, DialogsColumns.FULL_OUT_READ);
        sDialogsProjectionMap.put(DialogsColumns.PHOTO_50, DialogsColumns.FULL_PHOTO_50);
        sDialogsProjectionMap.put(DialogsColumns.PHOTO_100, DialogsColumns.FULL_PHOTO_100);
        sDialogsProjectionMap.put(DialogsColumns.PHOTO_200, DialogsColumns.FULL_PHOTO_200);
        sDialogsProjectionMap.put(DialogsColumns.ACL, DialogsColumns.FULL_ACL);
        sDialogsProjectionMap.put(DialogsColumns.LAST_MESSAGE_ID, DialogsColumns.FULL_LAST_MESSAGE_ID);
        sDialogsProjectionMap.put(DialogsColumns.IS_GROUP_CHANNEL, DialogsColumns.FULL_IS_GROUP_CHANNEL);
        sDialogsProjectionMap.put(DialogsColumns.MAJOR_ID, DialogsColumns.FULL_MAJOR_ID);
        sDialogsProjectionMap.put(DialogsColumns.MINOR_ID, DialogsColumns.FULL_MINOR_ID);

        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_FROM_ID, MessageColumns.FULL_FROM_ID + " AS " + DialogsColumns.FOREIGN_MESSAGE_FROM_ID);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_BODY, MessageColumns.FULL_BODY + " AS " + DialogsColumns.FOREIGN_MESSAGE_BODY);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_DATE, MessageColumns.FULL_DATE + " AS " + DialogsColumns.FOREIGN_MESSAGE_DATE);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_OUT, MessageColumns.FULL_OUT + " AS " + DialogsColumns.FOREIGN_MESSAGE_OUT);
        //sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_READ_STATE, MessageColumns.FULL_READ_STATE + " AS " + DialogsColumns.FOREIGN_MESSAGE_READ_STATE);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_HAS_ATTACHMENTS, MessageColumns.FULL_HAS_ATTACHMENTS + " AS " + DialogsColumns.FOREIGN_MESSAGE_HAS_ATTACHMENTS);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_FWD_COUNT, MessageColumns.FULL_FORWARD_COUNT + " AS " + DialogsColumns.FOREIGN_MESSAGE_FWD_COUNT);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_ACTION, MessageColumns.FULL_ACTION + " AS " + DialogsColumns.FOREIGN_MESSAGE_ACTION);
        sDialogsProjectionMap.put(DialogsColumns.FOREIGN_MESSAGE_ENCRYPTED, MessageColumns.FULL_ENCRYPTED + " AS " + DialogsColumns.FOREIGN_MESSAGE_ENCRYPTED);

        sPeersProjectionMap = new HashMap<>();
        sPeersProjectionMap.put(BaseColumns._ID, PeersColumns.FULL_ID);
        sPeersProjectionMap.put(PeersColumns.UNREAD, PeersColumns.FULL_UNREAD);
        sPeersProjectionMap.put(PeersColumns.TITLE, PeersColumns.FULL_TITLE);
        sPeersProjectionMap.put(PeersColumns.IN_READ, PeersColumns.FULL_IN_READ);
        sPeersProjectionMap.put(PeersColumns.OUT_READ, PeersColumns.FULL_OUT_READ);
        sPeersProjectionMap.put(PeersColumns.PHOTO_50, PeersColumns.FULL_PHOTO_50);
        sPeersProjectionMap.put(PeersColumns.PHOTO_100, PeersColumns.FULL_PHOTO_100);
        sPeersProjectionMap.put(PeersColumns.PHOTO_200, PeersColumns.FULL_PHOTO_200);
        sPeersProjectionMap.put(PeersColumns.PINNED, PeersColumns.FULL_PINNED);
        sPeersProjectionMap.put(PeersColumns.LAST_MESSAGE_ID, PeersColumns.FULL_LAST_MESSAGE_ID);
        sPeersProjectionMap.put(PeersColumns.ACL, PeersColumns.FULL_ACL);
        sPeersProjectionMap.put(PeersColumns.IS_GROUP_CHANNEL, PeersColumns.FULL_IS_GROUP_CHANNEL);
        sPeersProjectionMap.put(PeersColumns.KEYBOARD, PeersColumns.FULL_KEYBOARD);
        sPeersProjectionMap.put(PeersColumns.MAJOR_ID, PeersColumns.FULL_MAJOR_ID);
        sPeersProjectionMap.put(PeersColumns.MINOR_ID, PeersColumns.FULL_MINOR_ID);

        sDocsProjectionMap = new HashMap<>();
        sDocsProjectionMap.put(BaseColumns._ID, DocColumns.FULL_ID);
        sDocsProjectionMap.put(DocColumns.DOC_ID, DocColumns.FULL_DOC_ID);
        sDocsProjectionMap.put(DocColumns.OWNER_ID, DocColumns.FULL_OWNER_ID);
        sDocsProjectionMap.put(DocColumns.TITLE, DocColumns.FULL_TITLE);
        sDocsProjectionMap.put(DocColumns.SIZE, DocColumns.FULL_SIZE);
        sDocsProjectionMap.put(DocColumns.EXT, DocColumns.FULL_EXT);
        sDocsProjectionMap.put(DocColumns.URL, DocColumns.FULL_URL);

        sDocsProjectionMap.put(DocColumns.PHOTO, DocColumns.FULL_PHOTO);
        sDocsProjectionMap.put(DocColumns.GRAFFITI, DocColumns.FULL_GRAFFITI);
        sDocsProjectionMap.put(DocColumns.VIDEO, DocColumns.FULL_VIDEO);

        sDocsProjectionMap.put(DocColumns.DATE, DocColumns.FULL_DATE);
        sDocsProjectionMap.put(DocColumns.TYPE, DocColumns.FULL_TYPE);
        sDocsProjectionMap.put(DocColumns.ACCESS_KEY, DocColumns.FULL_ACCESS_KEY);

        sVideosProjectionMap = new HashMap<>();
        sVideosProjectionMap.put(BaseColumns._ID, VideoColumns.FULL_ID);
        sVideosProjectionMap.put(VideoColumns.VIDEO_ID, VideoColumns.FULL_VIDEO_ID);
        sVideosProjectionMap.put(VideoColumns.OWNER_ID, VideoColumns.FULL_OWNER_ID);
        sVideosProjectionMap.put(VideoColumns.ORIGINAL_OWNER_ID, VideoColumns.FULL_ORIGINAL_OWNER_ID);
        sVideosProjectionMap.put(VideoColumns.ALBUM_ID, VideoColumns.FULL_ALBUM_ID);
        sVideosProjectionMap.put(VideoColumns.TITLE, VideoColumns.FULL_TITLE);
        sVideosProjectionMap.put(VideoColumns.DESCRIPTION, VideoColumns.FULL_DESCRIPTION);
        sVideosProjectionMap.put(VideoColumns.DURATION, VideoColumns.FULL_DURATION);
        sVideosProjectionMap.put(VideoColumns.LINK, VideoColumns.FULL_LINK);
        sVideosProjectionMap.put(VideoColumns.DATE, VideoColumns.FULL_DATE);
        sVideosProjectionMap.put(VideoColumns.ADDING_DATE, VideoColumns.FULL_ADDING_DATE);
        sVideosProjectionMap.put(VideoColumns.VIEWS, VideoColumns.FULL_VIEWS);
        sVideosProjectionMap.put(VideoColumns.PLAYER, VideoColumns.FULL_PLAYER);
        sVideosProjectionMap.put(VideoColumns.IMAGE, VideoColumns.FULL_IMAGE);
        sVideosProjectionMap.put(VideoColumns.ACCESS_KEY, VideoColumns.FULL_ACCESS_KEY);
        sVideosProjectionMap.put(VideoColumns.COMMENTS, VideoColumns.FULL_COMMENTS);
        sVideosProjectionMap.put(VideoColumns.CAN_COMENT, VideoColumns.FULL_CAN_COMENT);
        sVideosProjectionMap.put(VideoColumns.CAN_REPOST, VideoColumns.FULL_CAN_REPOST);
        sVideosProjectionMap.put(VideoColumns.USER_LIKES, VideoColumns.FULL_USER_LIKES);
        sVideosProjectionMap.put(VideoColumns.REPEAT, VideoColumns.FULL_REPEAT);
        sVideosProjectionMap.put(VideoColumns.LIKES, VideoColumns.FULL_LIKES);
        sVideosProjectionMap.put(VideoColumns.PRIVACY_VIEW, VideoColumns.FULL_PRIVACY_VIEW);
        sVideosProjectionMap.put(VideoColumns.PRIVACY_COMMENT, VideoColumns.FULL_PRIVACY_COMMENT);
        sVideosProjectionMap.put(VideoColumns.MP4_240, VideoColumns.FULL_MP4_240);
        sVideosProjectionMap.put(VideoColumns.MP4_360, VideoColumns.FULL_MP4_360);
        sVideosProjectionMap.put(VideoColumns.MP4_480, VideoColumns.FULL_MP4_480);
        sVideosProjectionMap.put(VideoColumns.MP4_720, VideoColumns.FULL_MP4_720);
        sVideosProjectionMap.put(VideoColumns.MP4_1080, VideoColumns.FULL_MP4_1080);
        sVideosProjectionMap.put(VideoColumns.EXTERNAL, VideoColumns.FULL_EXTERNAL);
        sVideosProjectionMap.put(VideoColumns.HLS, VideoColumns.FULL_HLS);
        sVideosProjectionMap.put(VideoColumns.LIVE, VideoColumns.FULL_LIVE);
        sVideosProjectionMap.put(VideoColumns.PLATFORM, VideoColumns.FULL_PLATFORM);
        sVideosProjectionMap.put(VideoColumns.CAN_EDIT, VideoColumns.FULL_CAN_EDIT);
        sVideosProjectionMap.put(VideoColumns.CAN_ADD, VideoColumns.FULL_CAN_ADD);

        sPostsProjectionMap = new HashMap<>();
        sPostsProjectionMap.put(BaseColumns._ID, PostsColumns.FULL_ID);
        sPostsProjectionMap.put(PostsColumns.POST_ID, PostsColumns.FULL_POST_ID);
        sPostsProjectionMap.put(PostsColumns.OWNER_ID, PostsColumns.FULL_OWNER_ID);
        sPostsProjectionMap.put(PostsColumns.FROM_ID, PostsColumns.FULL_FROM_ID);
        sPostsProjectionMap.put(PostsColumns.DATE, PostsColumns.FULL_DATE);
        sPostsProjectionMap.put(PostsColumns.TEXT, PostsColumns.FULL_TEXT);
        sPostsProjectionMap.put(PostsColumns.REPLY_OWNER_ID, PostsColumns.FULL_REPLY_OWNER_ID);
        sPostsProjectionMap.put(PostsColumns.REPLY_POST_ID, PostsColumns.FULL_REPLY_POST_ID);
        sPostsProjectionMap.put(PostsColumns.FRIENDS_ONLY, PostsColumns.FULL_FRIENDS_ONLY);
        sPostsProjectionMap.put(PostsColumns.COMMENTS_COUNT, PostsColumns.FULL_COMMENTS_COUNT);
        sPostsProjectionMap.put(PostsColumns.CAN_POST_COMMENT, PostsColumns.FULL_CAN_POST_COMMENT);
        sPostsProjectionMap.put(PostsColumns.LIKES_COUNT, PostsColumns.FULL_LIKES_COUNT);
        sPostsProjectionMap.put(PostsColumns.USER_LIKES, PostsColumns.FULL_USER_LIKES);
        sPostsProjectionMap.put(PostsColumns.CAN_LIKE, PostsColumns.FULL_CAN_LIKE);
        sPostsProjectionMap.put(PostsColumns.CAN_PUBLISH, PostsColumns.FULL_CAN_PUBLISH);
        sPostsProjectionMap.put(PostsColumns.CAN_EDIT, PostsColumns.FULL_CAN_EDIT);
        sPostsProjectionMap.put(PostsColumns.REPOSTS_COUNT, PostsColumns.FULL_REPOSTS_COUNT);
        sPostsProjectionMap.put(PostsColumns.USER_REPOSTED, PostsColumns.FULL_USER_REPOSTED);
        sPostsProjectionMap.put(PostsColumns.POST_TYPE, PostsColumns.FULL_POST_TYPE);
        sPostsProjectionMap.put(PostsColumns.ATTACHMENTS_MASK, PostsColumns.FULL_ATTACHMENTS_MASK);
        sPostsProjectionMap.put(PostsColumns.SIGNED_ID, PostsColumns.FULL_SIGNED_ID);
        sPostsProjectionMap.put(PostsColumns.CREATED_BY, PostsColumns.FULL_CREATED_BY);
        sPostsProjectionMap.put(PostsColumns.CAN_PIN, PostsColumns.FULL_CAN_PIN);
        sPostsProjectionMap.put(PostsColumns.IS_PINNED, PostsColumns.FULL_IS_PINNED);
        sPostsProjectionMap.put(PostsColumns.DELETED, PostsColumns.FULL_DELETED);
        sPostsProjectionMap.put(PostsColumns.POST_SOURCE, PostsColumns.FULL_POST_SOURCE);
        sPostsProjectionMap.put(PostsColumns.VIEWS, PostsColumns.FULL_VIEWS);

        sPostsMessagesAttachmentsProjectionMap = new HashMap<>();
        sPostsMessagesAttachmentsProjectionMap.put(BaseColumns._ID, WallAttachmentsColumns.FULL_ID);
        sPostsMessagesAttachmentsProjectionMap.put(WallAttachmentsColumns.P_ID, WallAttachmentsColumns.FULL_P_ID);
        sPostsMessagesAttachmentsProjectionMap.put(WallAttachmentsColumns.TYPE, WallAttachmentsColumns.FULL_TYPE);
        sPostsMessagesAttachmentsProjectionMap.put(WallAttachmentsColumns.DATA, WallAttachmentsColumns.FULL_DATA);

        sGroupsProjectionMap = new HashMap<>();
        sGroupsProjectionMap.put(BaseColumns._ID, GroupColumns.FULL_ID);
        sGroupsProjectionMap.put(GroupColumns.NAME, GroupColumns.FULL_NAME);
        sGroupsProjectionMap.put(GroupColumns.SCREEN_NAME, GroupColumns.FULL_SCREEN_NAME);
        sGroupsProjectionMap.put(GroupColumns.IS_CLOSED, GroupColumns.FULL_IS_CLOSED);
        sGroupsProjectionMap.put(GroupColumns.IS_VERIFIED, GroupColumns.FULL_IS_VERIFIED);
        sGroupsProjectionMap.put(GroupColumns.IS_ADMIN, GroupColumns.FULL_IS_ADMIN);
        sGroupsProjectionMap.put(GroupColumns.ADMIN_LEVEL, GroupColumns.FULL_ADMIN_LEVEL);
        sGroupsProjectionMap.put(GroupColumns.IS_MEMBER, GroupColumns.FULL_IS_MEMBER);
        sGroupsProjectionMap.put(GroupColumns.MEMBERS_COUNT, GroupColumns.FULL_MEMBERS_COUNT);
        sGroupsProjectionMap.put(GroupColumns.MEMBER_STATUS, GroupColumns.FULL_MEMBER_STATUS);
        sGroupsProjectionMap.put(GroupColumns.TYPE, GroupColumns.FULL_TYPE);
        sGroupsProjectionMap.put(GroupColumns.PHOTO_50, GroupColumns.FULL_PHOTO_50);
        sGroupsProjectionMap.put(GroupColumns.PHOTO_100, GroupColumns.FULL_PHOTO_100);
        sGroupsProjectionMap.put(GroupColumns.PHOTO_200, GroupColumns.FULL_PHOTO_200);
        sGroupsProjectionMap.put(GroupColumns.CAN_ADD_TOPICS, GroupColumns.FULL_CAN_ADD_TOPICS);
        sGroupsProjectionMap.put(GroupColumns.TOPICS_ORDER, GroupColumns.FULL_TOPICS_ORDER);

        sCommentsProjectionMap = new HashMap<>();
        sCommentsProjectionMap.put(BaseColumns._ID, CommentsColumns.FULL_ID);
        sCommentsProjectionMap.put(CommentsColumns.COMMENT_ID, CommentsColumns.FULL_COMMENT_ID);
        sCommentsProjectionMap.put(CommentsColumns.FROM_ID, CommentsColumns.FULL_FROM_ID);
        sCommentsProjectionMap.put(CommentsColumns.DATE, CommentsColumns.FULL_DATE);
        sCommentsProjectionMap.put(CommentsColumns.TEXT, CommentsColumns.FULL_TEXT);
        sCommentsProjectionMap.put(CommentsColumns.REPLY_TO_USER, CommentsColumns.FULL_REPLY_TO_USER);
        sCommentsProjectionMap.put(CommentsColumns.REPLY_TO_COMMENT, CommentsColumns.FULL_REPLY_TO_COMMENT);
        sCommentsProjectionMap.put(CommentsColumns.THREADS_COUNT, CommentsColumns.FULL_THREADS_COUNT);
        sCommentsProjectionMap.put(CommentsColumns.THREADS, CommentsColumns.FULL_THREADS);
        sCommentsProjectionMap.put(CommentsColumns.LIKES, CommentsColumns.FULL_LIKES);
        sCommentsProjectionMap.put(CommentsColumns.USER_LIKES, CommentsColumns.FULL_USER_LIKES);
        sCommentsProjectionMap.put(CommentsColumns.CAN_LIKE, CommentsColumns.FULL_CAN_LIKE);
        sCommentsProjectionMap.put(CommentsColumns.CAN_EDIT, CommentsColumns.FULL_CAN_EDIT);
        sCommentsProjectionMap.put(CommentsColumns.ATTACHMENTS_COUNT, CommentsColumns.FULL_ATTACHMENTS_COUNT);
        sCommentsProjectionMap.put(CommentsColumns.DELETED, CommentsColumns.FULL_DELETED);
        sCommentsProjectionMap.put(CommentsColumns.SOURCE_ID, CommentsColumns.FULL_SOURCE_ID);
        sCommentsProjectionMap.put(CommentsColumns.SOURCE_OWNER_ID, CommentsColumns.FULL_SOURCE_OWNER_ID);
        sCommentsProjectionMap.put(CommentsColumns.SOURCE_TYPE, CommentsColumns.FULL_SOURCE_TYPE);
        sCommentsProjectionMap.put(CommentsColumns.SOURCE_ACCESS_KEY, CommentsColumns.FULL_SOURCE_ACCESS_KEY);

        sCommentsMessagesAttachmentsProjectionMap = new HashMap<>();
        sCommentsMessagesAttachmentsProjectionMap.put(BaseColumns._ID, CommentsAttachmentsColumns.FULL_ID);
        sCommentsMessagesAttachmentsProjectionMap.put(CommentsAttachmentsColumns.C_ID, CommentsAttachmentsColumns.FULL_C_ID);
        sCommentsMessagesAttachmentsProjectionMap.put(CommentsAttachmentsColumns.TYPE, CommentsAttachmentsColumns.FULL_TYPE);
        sCommentsMessagesAttachmentsProjectionMap.put(CommentsAttachmentsColumns.DATA, CommentsAttachmentsColumns.FULL_DATA);

        sPhotoAlbumsProjectionMap = new HashMap<>();
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.ALBUM_ID, PhotoAlbumsColumns.FULL_ALBUM_ID);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.OWNER_ID, PhotoAlbumsColumns.FULL_OWNER_ID);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.TITLE, PhotoAlbumsColumns.FULL_TITLE);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.SIZE, PhotoAlbumsColumns.FULL_SIZE);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.PRIVACY_VIEW, PhotoAlbumsColumns.FULL_PRIVACY_VIEW);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.PRIVACY_COMMENT, PhotoAlbumsColumns.FULL_PRIVACY_COMMENT);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.DESCRIPTION, PhotoAlbumsColumns.FULL_DESCRIPTION);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.CAN_UPLOAD, PhotoAlbumsColumns.FULL_CAN_UPLOAD);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.UPDATED, PhotoAlbumsColumns.FULL_UPDATED);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.CREATED, PhotoAlbumsColumns.FULL_CREATED);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.SIZES, PhotoAlbumsColumns.FULL_SIZES);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.UPLOAD_BY_ADMINS, PhotoAlbumsColumns.FULL_UPLOAD_BY_ADMINS);
        sPhotoAlbumsProjectionMap.put(PhotoAlbumsColumns.COMMENTS_DISABLED, PhotoAlbumsColumns.FULL_COMMENTS_DISABLED);

        //sPollProjectionMap = new HashMap<>();
        //sPollProjectionMap.put(PollColumns._ID, PollColumns.FULL_ID);
        //sPollProjectionMap.put(PollColumns.POLL_ID, PollColumns.FULL_POLL_ID);
        //sPollProjectionMap.put(PollColumns.OWNER_ID, PollColumns.FULL_OWNER_ID);
        //sPollProjectionMap.put(PollColumns.CREATED, PollColumns.FULL_CREATED);
        //sPollProjectionMap.put(PollColumns.QUESTION, PollColumns.FULL_QUESTION);
        //sPollProjectionMap.put(PollColumns.VOTES, PollColumns.FULL_VOTES);
        //sPollProjectionMap.put(PollColumns.ANSWER_ID, PollColumns.FULL_ANSWER_ID);
        //sPollProjectionMap.put(PollColumns.ANSWER_COUNT, PollColumns.FULL_ANSWER_COUNT);
        //sPollProjectionMap.put(PollColumns.ANONYMOUS, PollColumns.FULL_ANONYMOUS);
        //sPollProjectionMap.put(PollColumns.IS_BOARD, PollColumns.FULL_IS_BOARD);
        //sPollProjectionMap.put(PollColumns.ANSWERS, PollColumns.FULL_ANSWERS);

        sNewsProjectionMap = new HashMap<>();
        sNewsProjectionMap.put(BaseColumns._ID, NewsColumns.FULL_ID);
        sNewsProjectionMap.put(NewsColumns.TYPE, NewsColumns.FULL_TYPE);
        sNewsProjectionMap.put(NewsColumns.SOURCE_ID, NewsColumns.FULL_SOURCE_ID);
        sNewsProjectionMap.put(NewsColumns.DATE, NewsColumns.FULL_DATE);
        sNewsProjectionMap.put(NewsColumns.POST_ID, NewsColumns.FULL_POST_ID);
        sNewsProjectionMap.put(NewsColumns.POST_TYPE, NewsColumns.FULL_POST_TYPE);
        sNewsProjectionMap.put(NewsColumns.FINAL_POST, NewsColumns.FULL_FINAL_POST);
        sNewsProjectionMap.put(NewsColumns.COPY_OWNER_ID, NewsColumns.FULL_COPY_OWNER_ID);
        sNewsProjectionMap.put(NewsColumns.COPY_POST_ID, NewsColumns.FULL_COPY_POST_ID);
        sNewsProjectionMap.put(NewsColumns.COPY_POST_DATE, NewsColumns.FULL_COPY_POST_DATE);
        sNewsProjectionMap.put(NewsColumns.TEXT, NewsColumns.FULL_TEXT);
        sNewsProjectionMap.put(NewsColumns.CAN_EDIT, NewsColumns.FULL_CAN_EDIT);
        sNewsProjectionMap.put(NewsColumns.CAN_DELETE, NewsColumns.FULL_CAN_DELETE);
        sNewsProjectionMap.put(NewsColumns.COMMENT_COUNT, NewsColumns.FULL_COMMENT_COUNT);
        sNewsProjectionMap.put(NewsColumns.COMMENT_CAN_POST, NewsColumns.FULL_COMMENT_CAN_POST);
        sNewsProjectionMap.put(NewsColumns.LIKE_COUNT, NewsColumns.FULL_LIKE_COUNT);
        sNewsProjectionMap.put(NewsColumns.USER_LIKE, NewsColumns.FULL_USER_LIKE);
        sNewsProjectionMap.put(NewsColumns.CAN_LIKE, NewsColumns.FULL_CAN_LIKE);
        sNewsProjectionMap.put(NewsColumns.CAN_PUBLISH, NewsColumns.FULL_CAN_PUBLISH);
        sNewsProjectionMap.put(NewsColumns.REPOSTS_COUNT, NewsColumns.FULL_REPOSTS_COUNT);
        sNewsProjectionMap.put(NewsColumns.USER_REPOSTED, NewsColumns.FULL_USER_REPOSTED);
        //sNewsProjectionMap.put(NewsColumns.ATTACHMENTS_MASK, NewsColumns.FULL_ATTACHMENTS_COUNT);
        sNewsProjectionMap.put(NewsColumns.GEO_ID, NewsColumns.FULL_GEO_ID);
        sNewsProjectionMap.put(NewsColumns.TAG_FRIENDS, NewsColumns.FULL_TAG_FRIENDS);
        sNewsProjectionMap.put(NewsColumns.ATTACHMENTS_JSON, NewsColumns.FULL_ATTACHMENTS_JSON);
        sNewsProjectionMap.put(NewsColumns.VIEWS, NewsColumns.FULL_VIEWS);
        //sNewsProjectionMap.put(NewsColumns.HAS_COPY_HISTORY, NewsColumns.FULL_HAS_COPY_HISTORY);

        sGroupsDetProjectionMap = new HashMap<>();
        sGroupsDetProjectionMap.put(BaseColumns._ID, GroupsDetColumns.FULL_ID);
        sGroupsDetProjectionMap.put(GroupsDetColumns.DATA, GroupsDetColumns.FULL_DATA);

        sVideoAlbumsProjectionMap = new HashMap<>();
        sVideoAlbumsProjectionMap.put(BaseColumns._ID, VideoAlbumsColumns.FULL_ID);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.ALBUM_ID, VideoAlbumsColumns.FULL_ALBUM_ID);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.OWNER_ID, VideoAlbumsColumns.FULL_OWNER_ID);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.TITLE, VideoAlbumsColumns.FULL_TITLE);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.IMAGE, VideoAlbumsColumns.FULL_IMAGE);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.COUNT, VideoAlbumsColumns.FULL_COUNT);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.UPDATE_TIME, VideoAlbumsColumns.FULL_UPDATE_TIME);
        sVideoAlbumsProjectionMap.put(VideoAlbumsColumns.PRIVACY, VideoAlbumsColumns.FULL_PRIVACY);

        sTopicsProjectionMap = new HashMap<>();
        sTopicsProjectionMap.put(BaseColumns._ID, TopicsColumns.FULL_ID);
        sTopicsProjectionMap.put(TopicsColumns.TOPIC_ID, TopicsColumns.FULL_TOPIC_ID);
        sTopicsProjectionMap.put(TopicsColumns.OWNER_ID, TopicsColumns.FULL_OWNER_ID);
        sTopicsProjectionMap.put(TopicsColumns.TITLE, TopicsColumns.FULL_TITLE);
        sTopicsProjectionMap.put(TopicsColumns.CREATED, TopicsColumns.FULL_CREATED);
        sTopicsProjectionMap.put(TopicsColumns.CREATED_BY, TopicsColumns.FULL_CREATED_BY);
        sTopicsProjectionMap.put(TopicsColumns.UPDATED, TopicsColumns.FULL_UPDATED);
        sTopicsProjectionMap.put(TopicsColumns.UPDATED_BY, TopicsColumns.FULL_UPDATED_BY);
        sTopicsProjectionMap.put(TopicsColumns.IS_CLOSED, TopicsColumns.FULL_IS_CLOSED);
        sTopicsProjectionMap.put(TopicsColumns.IS_FIXED, TopicsColumns.FULL_IS_FIXED);
        sTopicsProjectionMap.put(TopicsColumns.COMMENTS, TopicsColumns.FULL_COMMENTS);
        sTopicsProjectionMap.put(TopicsColumns.FIRST_COMMENT, TopicsColumns.FULL_FIRST_COMMENT);
        sTopicsProjectionMap.put(TopicsColumns.LAST_COMMENT, TopicsColumns.FULL_LAST_COMMENT);
        sTopicsProjectionMap.put(TopicsColumns.ATTACHED_POLL, TopicsColumns.FULL_ATTACHED_POLL);
        //sTopicsProjectionMap.put(TopicsColumns.POLL_ID, TopicsColumns.FULL_POLL_ID);

        sNoticationsProjectionMap = new HashMap<>();
        sNoticationsProjectionMap.put(BaseColumns._ID, NotificationColumns.FULL_ID);
        sNoticationsProjectionMap.put(NotificationColumns.TYPE, NotificationColumns.FULL_TYPE);
        sNoticationsProjectionMap.put(NotificationColumns.DATE, NotificationColumns.FULL_DATE);
        sNoticationsProjectionMap.put(NotificationColumns.DATA, NotificationColumns.FULL_DATA);

        sUserDetProjectionMap = new HashMap<>();
        sUserDetProjectionMap.put(BaseColumns._ID, UsersDetColumns.FULL_ID);
        sUserDetProjectionMap.put(UsersDetColumns.DATA, UsersDetColumns.FULL_DATA);

        sFavePhotosProjectionMap = new HashMap<>();
        sFavePhotosProjectionMap.put(BaseColumns._ID, FavePhotosColumns.FULL_ID);
        sFavePhotosProjectionMap.put(FavePhotosColumns.PHOTO_ID, FavePhotosColumns.FULL_PHOTO_ID);
        sFavePhotosProjectionMap.put(FavePhotosColumns.OWNER_ID, FavePhotosColumns.FULL_OWNER_ID);
        sFavePhotosProjectionMap.put(FavePhotosColumns.POST_ID, FavePhotosColumns.FULL_POST_ID);
        sFavePhotosProjectionMap.put(FavePhotosColumns.PHOTO, FavePhotosColumns.FULL_PHOTO);

        sFaveVideosProjectionMap = new HashMap<>();
        sFaveVideosProjectionMap.put(BaseColumns._ID, FaveVideosColumns.FULL_ID);
        sFaveVideosProjectionMap.put(FaveVideosColumns.VIDEO, FaveVideosColumns.FULL_VIDEO);

        sFaveArticlesProjectionMap = new HashMap<>();
        sFaveArticlesProjectionMap.put(BaseColumns._ID, FaveArticlesColumns.FULL_ID);
        sFaveArticlesProjectionMap.put(FaveArticlesColumns.ARTICLE, FaveArticlesColumns.FULL_ARTICLE);

        sFaveProductsProjectionMap = new HashMap<>();
        sFaveProductsProjectionMap.put(BaseColumns._ID, FaveProductColumns.FULL_ID);
        sFaveProductsProjectionMap.put(FaveProductColumns.PRODUCT, FaveProductColumns.FULL_PRODUCT);

        sFaveUsersProjectionMap = new HashMap<>();
        sFaveUsersProjectionMap.put(BaseColumns._ID, FavePageColumns.FULL_ID);
        sFaveUsersProjectionMap.put(FavePageColumns.UPDATED_TIME, FavePageColumns.UPDATED_TIME);
        sFaveUsersProjectionMap.put(FavePageColumns.DESCRIPTION, FavePageColumns.DESCRIPTION);
        sFaveUsersProjectionMap.put(FavePageColumns.FAVE_TYPE, FavePageColumns.FAVE_TYPE);

        sFaveGroupsProjectionMap = new HashMap<>();
        sFaveGroupsProjectionMap.put(BaseColumns._ID, FavePageColumns.FULL_GROUPS_ID);
        sFaveGroupsProjectionMap.put(FavePageColumns.UPDATED_TIME, FavePageColumns.UPDATED_TIME);
        sFaveGroupsProjectionMap.put(FavePageColumns.DESCRIPTION, FavePageColumns.DESCRIPTION);
        sFaveGroupsProjectionMap.put(FavePageColumns.FAVE_TYPE, FavePageColumns.FAVE_TYPE);

        sFaveLinksProjectionMap = new HashMap<>();
        sFaveLinksProjectionMap.put(BaseColumns._ID, FaveLinksColumns.FULL_ID);
        sFaveLinksProjectionMap.put(FaveLinksColumns.LINK_ID, FaveLinksColumns.FULL_LINK_ID);
        sFaveLinksProjectionMap.put(FaveLinksColumns.URL, FaveLinksColumns.FULL_URL);
        sFaveLinksProjectionMap.put(FaveLinksColumns.TITLE, FaveLinksColumns.FULL_TITLE);
        sFaveLinksProjectionMap.put(FaveLinksColumns.DESCRIPTION, FaveLinksColumns.FULL_DESCRIPTION);
        sFaveLinksProjectionMap.put(FaveLinksColumns.PHOTO, FaveLinksColumns.FULL_PHOTO);

        sFavePostsProjectionMap = new HashMap<>();
        sFavePostsProjectionMap.put(BaseColumns._ID, FavePostsColumns.FULL_ID);
        sFavePostsProjectionMap.put(FavePostsColumns.POST, FavePostsColumns.FULL_POST);

        sCountriesProjectionMap = new HashMap<>();
        sCountriesProjectionMap.put(BaseColumns._ID, CountriesColumns.FULL_ID);
        sCountriesProjectionMap.put(CountriesColumns.NAME, CountriesColumns.FULL_NAME);

        sFeedListsProjectionMap = new HashMap<>();
        sFeedListsProjectionMap.put(BaseColumns._ID, FeedListsColumns.FULL_ID);
        sFeedListsProjectionMap.put(FeedListsColumns.TITLE, FeedListsColumns.FULL_TITLE);
        sFeedListsProjectionMap.put(FeedListsColumns.NO_REPOSTS, FeedListsColumns.FULL_NO_REPOSTS);
        sFeedListsProjectionMap.put(FeedListsColumns.SOURCE_IDS, FeedListsColumns.FULL_SOURCE_IDS);

        sFriendListsProjectionMap = new HashMap<>();
        sFriendListsProjectionMap.put(BaseColumns._ID, FriendListsColumns.FULL_ID);
        sFriendListsProjectionMap.put(FriendListsColumns.USER_ID, FriendListsColumns.FULL_USER_ID);
        sFriendListsProjectionMap.put(FriendListsColumns.LIST_ID, FriendListsColumns.FULL_LIST_ID);
        sFriendListsProjectionMap.put(FriendListsColumns.NAME, FriendListsColumns.FULL_NAME);

        sKeysProjectionMap = new HashMap<>();
        sKeysProjectionMap.put(BaseColumns._ID, KeyColumns.FULL_ID);
        sKeysProjectionMap.put(KeyColumns.VERSION, KeyColumns.FULL_VERSION);
        sKeysProjectionMap.put(KeyColumns.PEER_ID, KeyColumns.FULL_PEER_ID);
        sKeysProjectionMap.put(KeyColumns.SESSION_ID, KeyColumns.FULL_SESSION_ID);
        sKeysProjectionMap.put(KeyColumns.DATE, KeyColumns.FULL_DATE);
        sKeysProjectionMap.put(KeyColumns.START_SESSION_MESSAGE_ID, KeyColumns.FULL_START_SESSION_MESSAGE_ID);
        sKeysProjectionMap.put(KeyColumns.END_SESSION_MESSAGE_ID, KeyColumns.FULL_END_SESSION_MESSAGE_ID);
        sKeysProjectionMap.put(KeyColumns.OUT_KEY, KeyColumns.FULL_OUT_KEY);
        sKeysProjectionMap.put(KeyColumns.IN_KEY, KeyColumns.FULL_IN_KEY);
    }

    public static Uri getKeysContentUriFor(int aid) {
        return appendAccountId(KEYS_CONTENT_URI, aid);
    }

    public static Uri getGroupsDetContentUriFor(int aid) {
        return appendAccountId(GROUPS_DET_CONTENT_URI, aid);
    }

    public static Uri getFavePostsContentUriFor(int aid) {
        return appendAccountId(FAVE_POSTS_CONTENT_URI, aid);
    }

    public static Uri getFaveLinksContentUriFor(int aid) {
        return appendAccountId(FAVE_LINKS_CONTENT_URI, aid);
    }

    public static Uri getFavePhotosContentUriFor(int aid) {
        return appendAccountId(FAVE_PHOTOS_CONTENT_URI, aid);
    }

    public static Uri getFaveUsersContentUriFor(int aid) {
        return appendAccountId(FAVE_PAGES_CONTENT_URI, aid);
    }

    public static Uri getFaveGroupsContentUriFor(int aid) {
        return appendAccountId(FAVE_GROUPS_CONTENT_URI, aid);
    }

    public static Uri getFaveVideosContentUriFor(int aid) {
        return appendAccountId(FAVE_VIDEOS_CONTENT_URI, aid);
    }

    public static Uri getFaveArticlesContentUriFor(int aid) {
        return appendAccountId(FAVE_ARTICLES_CONTENT_URI, aid);
    }

    public static Uri getFaveProductsContentUriFor(int aid) {
        return appendAccountId(FAVE_PRODUCTS_CONTENT_URI, aid);
    }

    public static Uri getTopicsContentUriFor(int aid) {
        return appendAccountId(TOPICS_CONTENT_URI, aid);
    }

    //public static Uri getPollContentUriFor(int aid){
    //    return appendAccountId(POLL_CONTENT_URI, aid);
    //}

    public static Uri getMessagesAttachmentsContentUriFor(int aid) {
        return appendAccountId(MESSAGES_ATTACHMENTS_CONTENT_URI, aid);
    }

    public static Uri getPostsAttachmentsContentUriFor(int aid) {
        return appendAccountId(POSTS_ATTACHMENTS_CONTENT_URI, aid);
    }

    public static Uri getPostsContentUriFor(int aid) {
        return appendAccountId(POSTS_CONTENT_URI, aid);
    }

    public static Uri getVideosContentUriFor(int aid) {
        return appendAccountId(VIDEOS_CONTENT_URI, aid);
    }

    public static Uri getVideoAlbumsContentUriFor(int aid) {
        return appendAccountId(VIDEO_ALBUMS_CONTENT_URI, aid);
    }

    public static Uri getDocsContentUriFor(int aid) {
        return appendAccountId(DOCS_CONTENT_URI, aid);
    }

    public static Uri getPhotosContentUriFor(int aid) {
        return appendAccountId(PHOTOS_CONTENT_URI, aid);
    }

    public static Uri getCommentsContentUriFor(int aid) {
        return appendAccountId(COMMENTS_CONTENT_URI, aid);
    }

    public static Uri getCommentsAttachmentsContentUriFor(int aid) {
        return appendAccountId(COMMENTS_ATTACHMENTS_CONTENT_URI, aid);
    }

    public static Uri getDialogsContentUriFor(int aid) {
        return appendAccountId(DIALOGS_CONTENT_URI, aid);
    }

    public static Uri getPeersContentUriFor(int aid) {
        return appendAccountId(PEERS_CONTENT_URI, aid);
    }

    public static Uri getRelativeshipContentUriFor(int aid) {
        return appendAccountId(RELATIVESHIP_CONTENT_URI, aid);
    }

    public static Uri getUserContentUriFor(int aid) {
        return appendAccountId(USER_CONTENT_URI, aid);
    }

    public static Uri getUserDetContentUriFor(int aid) {
        return appendAccountId(USER_DET_CONTENT_URI, aid);
    }

    public static Uri getGroupsContentUriFor(int aid) {
        return appendAccountId(GROUPS_CONTENT_URI, aid);
    }

    public static Uri getNewsContentUriFor(int aid) {
        return appendAccountId(NEWS_CONTENT_URI, aid);
    }

    public static Uri getMessageContentUriFor(int aid) {
        return appendAccountId(MESSAGE_CONTENT_URI, aid);
    }

    public static Uri getCountriesContentUriFor(int aid) {
        return appendAccountId(COUNTRIES_CONTENT_URI, aid);
    }

    public static Uri getNotificationsContentUriFor(int aid) {
        return appendAccountId(NOTIFICATIONS_CONTENT_URI, aid);
    }

    public static Uri getFeedListsContentUriFor(int aid) {
        return appendAccountId(FEED_LISTS_CONTENT_URI, aid);
    }

    public static Uri getPhotoAlbumsContentUriFor(int aid) {
        return appendAccountId(PHOTO_ALBUMS_CONTENT_URI, aid);
    }

    public static Uri getFriendListsContentUriFor(int aid) {
        return appendAccountId(FRIEND_LISTS_CONTENT_URI, aid);
    }

    private static Uri appendAccountId(@NonNull Uri uri, int aid) {
        return new Uri.Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .path(uri.getPath())
                .appendQueryParameter(AID, String.valueOf(aid))
                .build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @NonNull
    private DBHelper getDbHelper(int aid) {
        return DBHelper.getInstance(getContext(), aid);
    }

    private int extractAidFromUri(@NonNull Uri uri) {
        String said = uri.getQueryParameter(AID);
        if (TextUtils.isEmpty(said)) {
            throw new IllegalArgumentException("AID query parameter not found, uri: " + uri);
        }

        int targetAid = Integer.parseInt(said);
        if (targetAid == 0) {
            throw new IllegalArgumentException("Invalid account id=0, uri: " + uri);
        }

        return targetAid;
    }

    /**
     * Проверяем все операции на соответствие aid
     * Потому что будет открыватся транзакция только к одной базе данных
     */
    private void validateUris(@NonNull List<ContentProviderOperation> operations) {
        Integer aid = null;
        for (ContentProviderOperation operation : operations) {
            Uri uri = operation.getUri();
            if (aid == null) {
                aid = extractAidFromUri(uri);
            }

            int thisAid = extractAidFromUri(uri);
            if (aid != thisAid) {
                throw new IllegalArgumentException("There are different aids in operations");
            }
        }
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations) {
        if (Utils.safeIsEmpty(operations)) {
            return new ContentProviderResult[0];
        }

        validateUris(operations);

        int aid = extractAidFromUri(operations.get(0).getUri());

        ContentProviderResult[] result = new ContentProviderResult[operations.size()];
        int i = 0;
        // Opens the database object in "write" mode.
        SQLiteDatabase db = getDbHelper(aid).getWritableDatabase();
        // Begin a transaction
        db.beginTransaction();
        try {
            for (ContentProviderOperation operation : operations) {
                // Chain the result for back references
                result[i++] = operation.apply(this, result, i);
            }

            db.setTransactionSuccessful();
        } catch (OperationApplicationException e) {
            Logger.d("DATABASE", "batch failed: " + e.getLocalizedMessage());
        } finally {
            db.endTransaction();
        }

        return result;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = getDbHelper(uri).getWritableDatabase();

        long rowId;
        Uri resultUri;
        int matchUri = sUriMatcher.match(uri);

        switch (matchUri) {
            case URI_USERS:
                rowId = db.replace(UserColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(USER_CONTENT_URI, rowId);
                break;
            case URI_MESSAGES:
                rowId = db.replace(MessageColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGE_CONTENT_URI, rowId);
                break;
            case URI_MESSAGES_ATTACHMENTS:
                rowId = db.replace(MessagesAttachmentsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(MESSAGES_ATTACHMENTS_CONTENT_URI, rowId);
                break;
            case URI_PHOTOS:
                rowId = db.replace(PhotosColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(PHOTOS_CONTENT_URI, rowId);
                break;
            case URI_DIALOGS:
                rowId = db.replace(DialogsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(DIALOGS_CONTENT_URI, rowId);
                break;
            case URI_PEERS:
                rowId = db.replace(PeersColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(PEERS_CONTENT_URI, rowId);
                break;
            case URI_DOCS:
                rowId = db.replace(DocColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(DOCS_CONTENT_URI, rowId);
                break;
            case URI_VIDEOS:
                rowId = db.replace(VideoColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(VIDEOS_CONTENT_URI, rowId);
                break;
            case URI_POSTS:
                rowId = db.replace(PostsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(POSTS_CONTENT_URI, rowId);
                break;
            case URI_POST_ATTACHMENTS:
                rowId = db.replace(WallAttachmentsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(POSTS_ATTACHMENTS_CONTENT_URI, rowId);
                break;
            case URI_GROUPS:
                rowId = db.replace(GroupColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(GROUPS_CONTENT_URI, rowId);
                break;
            case URI_RELATIVESHIP:
                rowId = db.replace(RelationshipColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(RELATIVESHIP_CONTENT_URI, rowId);
                break;
            case URI_COMMENTS:
                rowId = db.replace(CommentsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(COMMENTS_CONTENT_URI, rowId);
                break;
            case URI_COMMENTS_ATTACHMENTS:
                rowId = db.replace(CommentsAttachmentsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(COMMENTS_ATTACHMENTS_CONTENT_URI, rowId);
                break;
            case URI_PHOTO_ALBUMS:
                rowId = db.replace(PhotoAlbumsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(PHOTO_ALBUMS_CONTENT_URI, rowId);
                break;
            //case URI_POLL:
            //    rowId = db.insert(PollColumns.TABLENAME, null, values);
            //    resultUri = ContentUris.withAppendedId(POLL_CONTENT_URI, rowId);
            //    break;
            case URI_NEWS:
                rowId = db.replace(NewsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(NEWS_CONTENT_URI, rowId);
                break;
            case URI_GROUPS_DET:
                rowId = db.replace(GroupsDetColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(GROUPS_DET_CONTENT_URI, rowId);
                break;
            case URI_VIDEO_ALBUMS:
                rowId = db.replace(VideoAlbumsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(VIDEO_ALBUMS_CONTENT_URI, rowId);
                break;
            case URI_TOPICS:
                rowId = db.replace(TopicsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(TOPICS_CONTENT_URI, rowId);
                break;
            case URI_NOTIFICATIONS:
                rowId = db.replace(NotificationColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(NOTIFICATIONS_CONTENT_URI, rowId);
                break;
            case URI_USER_DET:
                rowId = db.replace(UsersDetColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(USER_DET_CONTENT_URI, rowId);
                break;
            case URI_FAVE_PHOTOS:
                rowId = db.replace(FavePhotosColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_PHOTOS_CONTENT_URI, rowId);
                break;
            case URI_FAVE_VIDEOS:
                rowId = db.replace(FaveVideosColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_VIDEOS_CONTENT_URI, rowId);
                break;
            case URI_FAVE_PAGES:
                rowId = db.replace(FavePageColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_PAGES_CONTENT_URI, rowId);
                break;
            case URI_FAVE_GROUPS:
                rowId = db.replace(FavePageColumns.GROUPSTABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_GROUPS_CONTENT_URI, rowId);
                break;
            case URI_FAVE_LINKS:
                rowId = db.replace(FaveLinksColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_LINKS_CONTENT_URI, rowId);
                break;
            case URI_FAVE_ARTICLES:
                rowId = db.replace(FaveArticlesColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_ARTICLES_CONTENT_URI, rowId);
                break;
            case URI_FAVE_PRODUCTS:
                rowId = db.replace(FaveProductColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_PRODUCTS_CONTENT_URI, rowId);
                break;
            case URI_FAVE_POSTS:
                rowId = db.replace(FavePostsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FAVE_POSTS_CONTENT_URI, rowId);
                break;

            case URI_COUNTRIES:
                rowId = db.replace(CountriesColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(COUNTRIES_CONTENT_URI, rowId);
                break;
            case URI_FEED_LISTS:
                rowId = db.replace(FeedListsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FEED_LISTS_CONTENT_URI, rowId);
                break;
            case URI_FRIEND_LISTS:
                rowId = db.replace(FriendListsColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(FRIEND_LISTS_CONTENT_URI, rowId);
                break;
            case URI_KEYS:
                rowId = db.replace(KeyColumns.TABLENAME, null, values);
                resultUri = ContentUris.withAppendedId(KEYS_CONTENT_URI, rowId);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        safeNotifyChange(resultUri);

        if (matchUri == URI_MESSAGES) {
            int peerId = values.getAsInteger(MessageColumns.PEER_ID);

            Uri dUri = ContentUris.withAppendedId(DIALOGS_CONTENT_URI, peerId);
            safeNotifyChange(dUri);
        }

        return resultUri;
    }

    private void safeNotifyChange(Uri uri) {
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder _QB = new SQLiteQueryBuilder();
        int _TableType;

        switch (sUriMatcher.match(uri)) {
            case URI_USERS:
                _QB.setTables(UserColumns.TABLENAME);
                _QB.setProjectionMap(sUsersProjectionMap);
                _TableType = URI_USERS;
                break;

            case URI_USERS_ID:
                _QB.setTables(UserColumns.TABLENAME);
                _QB.setProjectionMap(sUsersProjectionMap);
                _QB.appendWhere(UserColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_USERS;
                break;

            case URI_GROUPS:
                _QB.setTables(GroupColumns.TABLENAME);
                _QB.setProjectionMap(sGroupsProjectionMap);
                _TableType = URI_GROUPS;
                break;

            case URI_GROUPS_ID:
                _QB.setTables(GroupColumns.TABLENAME);
                _QB.setProjectionMap(sGroupsProjectionMap);
                _QB.appendWhere(GroupColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_GROUPS;
                break;

            case URI_MESSAGES:
                _QB.setTables(MessageColumns.TABLENAME);
                //" LEFT OUTER JOIN " + PeerColumns.TABLENAME + " ON " + MessageColumns.FULL_FROM_ID + " = " + PeerColumns.FULL_ID +
                //" LEFT OUTER JOIN " + UserColumns.TABLENAME + " ON " + MessageColumns.FULL_ACTION_MID + " = " + UserColumns.FULL_ID);
                _QB.setProjectionMap(sMessagesProjectionMap);
                _TableType = URI_MESSAGES;
                break;

            case URI_MESSAGES_ID:
                _QB.setTables(MessageColumns.TABLENAME);
                //" LEFT OUTER JOIN " + PeerColumns.TABLENAME + " ON " + MessageColumns.FULL_FROM_ID + " = " + PeerColumns.FULL_ID +
                //" LEFT OUTER JOIN " + UserColumns.TABLENAME + " ON " + MessageColumns.FULL_ACTION_MID + " = " + UserColumns.FULL_ID);
                _QB.setProjectionMap(sMessagesProjectionMap);
                _QB.appendWhere(MessageColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_MESSAGES;
                break;

            case URI_MESSAGES_ATTACHMENTS:
                _QB.setTables(MessagesAttachmentsColumns.TABLENAME);
                //" LEFT OUTER JOIN " + AudiosColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + AudiosColumns.FULL_AUDIO_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + AudiosColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + StickersColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + StickersColumns.FULL_ID +
                //" LEFT OUTER JOIN " + DocColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + DocColumns.FULL_DOC_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + DocColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + VideoColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + VideoColumns.FULL_VIDEO_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + VideoColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PostsColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PostsColumns.FULL_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PostsColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PhotosColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PhotosColumns.FULL_PHOTO_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PhotosColumns.FULL_OWNER_ID);
                _QB.setProjectionMap(sMessagesAttachmentsProjectionMap);
                _TableType = URI_MESSAGES_ATTACHMENTS;
                break;

            case URI_MESSAGES_ATTACHMENTS_ID:
                _QB.setTables(MessagesAttachmentsColumns.TABLENAME);
                //" LEFT OUTER JOIN " + AudiosColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + AudiosColumns.FULL_AUDIO_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + AudiosColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + StickersColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + StickersColumns.FULL_ID +
                //" LEFT OUTER JOIN " + DocColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + DocColumns.FULL_DOC_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + DocColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + VideoColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + VideoColumns.FULL_VIDEO_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + VideoColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PostsColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PostsColumns.FULL_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PostsColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PhotosColumns.TABLENAME + " ON " + AttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PhotosColumns.FULL_PHOTO_ID + " AND " + AttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PhotosColumns.FULL_OWNER_ID);
                _QB.setProjectionMap(sMessagesAttachmentsProjectionMap);
                _QB.appendWhere(MessagesAttachmentsColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_MESSAGES_ATTACHMENTS;
                break;

            case URI_PHOTOS:
                _QB.setTables(PhotosColumns.TABLENAME);
                _QB.setProjectionMap(sPhotosProjectionMap);
                _TableType = URI_PHOTOS;
                break;

            case URI_PHOTOS_ID:
                _QB.setTables(PhotosColumns.TABLENAME);
                _QB.setProjectionMap(sPhotosProjectionMap);
                _QB.appendWhere(PhotosColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_PHOTOS;
                break;

            case URI_DIALOGS:
                _QB.setTables(DialogsColumns.TABLENAME + " LEFT OUTER JOIN " + MessageColumns.TABLENAME + " ON " + DialogsColumns.FULL_LAST_MESSAGE_ID + " = " + MessageColumns.FULL_ID);
                _QB.setProjectionMap(sDialogsProjectionMap);
                _TableType = URI_DIALOGS;
                break;

            case URI_PEERS:
                _QB.setTables(PeersColumns.TABLENAME);
                _QB.setProjectionMap(sPeersProjectionMap);
                _TableType = URI_PEERS;
                break;

            case URI_DOCS:
                _QB.setTables(DocColumns.TABLENAME);
                _QB.setProjectionMap(sDocsProjectionMap);
                _TableType = URI_DOCS;
                break;

            case URI_DOCS_ID:
                _QB.setTables(DocColumns.TABLENAME);
                _QB.setProjectionMap(sDocsProjectionMap);
                _QB.appendWhere(DocColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_DOCS;
                break;

            case URI_VIDEOS:
                _QB.setTables(VideoColumns.TABLENAME);
                _QB.setProjectionMap(sVideosProjectionMap);
                _TableType = URI_VIDEOS;
                break;

            case URI_VIDEOS_ID:
                _QB.setTables(VideoColumns.TABLENAME);
                _QB.setProjectionMap(sVideosProjectionMap);
                _QB.appendWhere(VideoColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_DOCS;
                break;

            case URI_POSTS:
                _QB.setTables(PostsColumns.TABLENAME);
                _QB.setProjectionMap(sPostsProjectionMap);
                _TableType = URI_POSTS;
                break;

            case URI_POSTS_ID:
                _QB.setTables(PostsColumns.TABLENAME);
                _QB.setProjectionMap(sPostsProjectionMap);
                _QB.appendWhere(PostsColumns.FULL_ID + " = " + uri.getPathSegments().get(1));
                _TableType = URI_POSTS;
                break;

            case URI_POST_ATTACHMENTS:
                _QB.setTables(WallAttachmentsColumns.TABLENAME);
                _QB.setProjectionMap(sPostsMessagesAttachmentsProjectionMap);
                _TableType = URI_POST_ATTACHMENTS;
                break;

            case URI_POST_ATTACHMENTS_ID:
                _QB.setTables(WallAttachmentsColumns.TABLENAME);
                _QB.setProjectionMap(sPostsMessagesAttachmentsProjectionMap);
                _QB.appendWhere(WallAttachmentsColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_POST_ATTACHMENTS;
                break;

            case URI_RELATIVESHIP:
                _QB.setTables(RelationshipColumns.TABLENAME +
                        " LEFT OUTER JOIN " + UserColumns.TABLENAME + " ON " + RelationshipColumns.FULL_SUBJECT_ID + " = " + UserColumns.FULL_ID +
                        " LEFT OUTER JOIN " + GroupColumns.TABLENAME + " ON -" + RelationshipColumns.FULL_SUBJECT_ID + " = " + GroupColumns.FULL_ID);
                _QB.setProjectionMap(sRelativeshipProjectionMap);
                _TableType = URI_RELATIVESHIP;
                break;

            case URI_COMMENTS:
                _QB.setTables(CommentsColumns.TABLENAME);
                _QB.setProjectionMap(sCommentsProjectionMap);
                _TableType = URI_COMMENTS;
                break;

            case URI_COMMENTS_ID:
                _QB.setTables(CommentsColumns.TABLENAME);
                _QB.setProjectionMap(sCommentsProjectionMap);
                _QB.appendWhere(BaseColumns._ID + " = " + uri.getPathSegments().get(1));
                _TableType = URI_COMMENTS;
                break;

            case URI_COMMENTS_ATTACHMENTS:
                _QB.setTables(CommentsAttachmentsColumns.TABLENAME);
                //" LEFT OUTER JOIN " + AudiosColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + AudiosColumns.FULL_AUDIO_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + AudiosColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + StickersColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + StickersColumns.FULL_ID +
                //" LEFT OUTER JOIN " + DocColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + DocColumns.FULL_DOC_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + DocColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + VideoColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + VideoColumns.FULL_VIDEO_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + VideoColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PostsColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PostsColumns.FULL_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PostsColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PhotosColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PhotosColumns.FULL_PHOTO_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PhotosColumns.FULL_OWNER_ID);
                _QB.setProjectionMap(sCommentsMessagesAttachmentsProjectionMap);
                _TableType = URI_COMMENTS_ATTACHMENTS;
                break;

            case URI_COMMENTS_ATTACHMENTS_ID:
                _QB.setTables(CommentsAttachmentsColumns.TABLENAME);
                //" LEFT OUTER JOIN " + AudiosColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + AudiosColumns.FULL_AUDIO_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + AudiosColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + StickersColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + StickersColumns.FULL_ID +
                //" LEFT OUTER JOIN " + DocColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + DocColumns.FULL_DOC_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + DocColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + VideoColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + VideoColumns.FULL_VIDEO_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + VideoColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PostsColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PostsColumns.FULL_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PostsColumns.FULL_OWNER_ID +
                //" LEFT OUTER JOIN " + PhotosColumns.TABLENAME + " ON " + CommentsAttachmentsColumns.FULL_ATTACHMENT_ID + " = " + PhotosColumns.FULL_PHOTO_ID + " AND " + CommentsAttachmentsColumns.FULL_ATTACHMENT_OWNER_ID + " = " + PhotosColumns.FULL_OWNER_ID);
                _QB.setProjectionMap(sCommentsMessagesAttachmentsProjectionMap);
                _QB.appendWhere(CommentsAttachmentsColumns.FULL_ID + "=" + uri.getPathSegments().get(1));
                _TableType = URI_COMMENTS_ATTACHMENTS;
                break;

            case URI_PHOTO_ALBUMS:
                _QB.setTables(PhotoAlbumsColumns.TABLENAME);
                _QB.setProjectionMap(sPhotoAlbumsProjectionMap);
                _TableType = URI_PHOTO_ALBUMS;
                break;

            /*case URI_POLL:
                _QB.setTables(PollColumns.TABLENAME);
                _QB.setProjectionMap(sPollProjectionMap);
                _TableType = URI_POLL;
                break;

            case URI_POLL_ID:
                _QB.setTables(PollColumns.TABLENAME);
                _QB.setProjectionMap(sPollProjectionMap);
                _QB.appendWhere(PollColumns._ID + " = " + uri.getPathSegments().get(1));
                _TableType = URI_POLL;
                break;*/

            case URI_NEWS:
                _QB.setTables(NewsColumns.TABLENAME);
                _QB.setProjectionMap(sNewsProjectionMap);
                _TableType = URI_NEWS;
                break;

            case URI_GROUPS_DET:
                _QB.setTables(GroupsDetColumns.TABLENAME);
                _QB.setProjectionMap(sGroupsDetProjectionMap);
                _TableType = URI_GROUPS_DET;
                break;

            case URI_GROUPS_DET_ID:
                _QB.setTables(GroupsDetColumns.TABLENAME);
                _QB.setProjectionMap(sGroupsDetProjectionMap);
                _QB.appendWhere(GroupsDetColumns.FULL_ID + " = " + uri.getPathSegments().get(1));
                _TableType = URI_GROUPS_DET;
                break;

            case URI_VIDEO_ALBUMS:
                _QB.setTables(VideoAlbumsColumns.TABLENAME);
                _QB.setProjectionMap(sVideoAlbumsProjectionMap);
                _TableType = URI_VIDEO_ALBUMS;
                break;

            case URI_TOPICS:
                _QB.setTables(TopicsColumns.TABLENAME);
                _QB.setProjectionMap(sTopicsProjectionMap);
                _TableType = URI_TOPICS;
                break;

            case URI_NOTIFICATIONS:
                _QB.setTables(NotificationColumns.TABLENAME);
                _QB.setProjectionMap(sNoticationsProjectionMap);
                _TableType = URI_NOTIFICATIONS;
                break;

            case URI_USER_DET:
                _QB.setTables(UsersDetColumns.TABLENAME);
                _QB.setProjectionMap(sUserDetProjectionMap);
                _TableType = URI_USER_DET;
                break;

            case URI_USER_DET_ID:
                _QB.setTables(UsersDetColumns.TABLENAME);
                _QB.setProjectionMap(sUserDetProjectionMap);
                _QB.appendWhere(UsersDetColumns.FULL_ID + " = " + uri.getPathSegments().get(1));
                _TableType = URI_USER_DET;
                break;
            case URI_FAVE_PHOTOS:
                _QB.setTables(FavePhotosColumns.TABLENAME);
                _QB.setProjectionMap(sFavePhotosProjectionMap);
                _TableType = URI_FAVE_PHOTOS;
                break;
            case URI_FAVE_VIDEOS:
                _QB.setTables(FaveVideosColumns.TABLENAME);
                _QB.setProjectionMap(sFaveVideosProjectionMap);
                _TableType = URI_FAVE_VIDEOS;
                break;
            case URI_FAVE_ARTICLES:
                _QB.setTables(FaveArticlesColumns.TABLENAME);
                _QB.setProjectionMap(sFaveArticlesProjectionMap);
                _TableType = URI_FAVE_ARTICLES;
                break;
            case URI_FAVE_PRODUCTS:
                _QB.setTables(FaveProductColumns.TABLENAME);
                _QB.setProjectionMap(sFaveProductsProjectionMap);
                _TableType = URI_FAVE_PRODUCTS;
                break;
            case URI_FAVE_PAGES:
                _QB.setTables(FavePageColumns.TABLENAME +
                        " LEFT OUTER JOIN " + UserColumns.TABLENAME +
                        " users ON " + FavePageColumns.FULL_ID + " = users." + BaseColumns._ID);
                _QB.setProjectionMap(sFaveUsersProjectionMap);
                _TableType = URI_FAVE_PAGES;
                break;
            case URI_FAVE_GROUPS:
                _QB.setTables(FavePageColumns.GROUPSTABLENAME +
                        " LEFT OUTER JOIN " + GroupColumns.TABLENAME +
                        " groups ON " + FavePageColumns.FULL_GROUPS_ID + " = groups." + BaseColumns._ID);
                _QB.setProjectionMap(sFaveGroupsProjectionMap);
                _TableType = URI_FAVE_GROUPS;
                break;
            case URI_FAVE_LINKS:
                _QB.setTables(FaveLinksColumns.TABLENAME);
                _QB.setProjectionMap(sFaveLinksProjectionMap);
                _TableType = URI_FAVE_LINKS;
                break;
            case URI_FAVE_POSTS:
                _QB.setTables(FavePostsColumns.TABLENAME);
                _QB.setProjectionMap(sFavePostsProjectionMap);
                _TableType = URI_FAVE_POSTS;
                break;

            case URI_COUNTRIES:
                _QB.setTables(CountriesColumns.TABLENAME);
                _QB.setProjectionMap(sCountriesProjectionMap);
                _TableType = URI_COUNTRIES;
                break;
            case URI_FEED_LISTS:
                _QB.setTables(FeedListsColumns.TABLENAME);
                _QB.setProjectionMap(sFeedListsProjectionMap);
                _TableType = URI_FEED_LISTS;
                break;
            case URI_FRIEND_LISTS:
                _QB.setTables(FriendListsColumns.TABLENAME);
                _QB.setProjectionMap(sFriendListsProjectionMap);
                _TableType = URI_FRIEND_LISTS;
                break;
            case URI_KEYS:
                _QB.setTables(KeyColumns.TABLENAME);
                _QB.setProjectionMap(sKeysProjectionMap);
                _TableType = URI_KEYS;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        //Set your sort order here
        String _OrderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            // If no sort order is specified use the default
            switch (_TableType) {
                case URI_USERS:
                    _OrderBy = UserColumns.FULL_LAST_NAME + " ASC";
                    break;

                case URI_GROUPS:
                    _OrderBy = GroupColumns.FULL_NAME + " ASC";
                    break;

                case URI_MESSAGES:
                    _OrderBy = MessageColumns.FULL_STATUS + ", " + MessageColumns.FULL_ID + " ASC";
                    break;

                case URI_MESSAGES_ATTACHMENTS:
                    _OrderBy = MessagesAttachmentsColumns.FULL_ID + " ASC";
                    break;

                case URI_PHOTOS:
                    _OrderBy = PhotosColumns.FULL_ID + " ASC";
                    break;

                case URI_DIALOGS:
                    _OrderBy = MessageColumns.FULL_DATE + " DESC";
                    break;

                case URI_PEERS:
                    _OrderBy = PeersColumns.FULL_ID + " DESC";
                    break;

                case URI_DOCS:
                    _OrderBy = DocColumns.FULL_ID + " ASC";
                    break;

                case URI_VIDEOS:
                    _OrderBy = VideoColumns.FULL_ID + " ASC";
                    break;

                case URI_POSTS:
                    _OrderBy = PostsColumns.FULL_ID + " ASC";
                    break;

                case URI_POST_ATTACHMENTS:
                    _OrderBy = WallAttachmentsColumns.FULL_ID + " ASC";
                    break;

                case URI_RELATIVESHIP:
                    _OrderBy = RelationshipColumns.FULL_ID + " ASC";
                    break;

                case URI_COMMENTS:
                    _OrderBy = CommentsColumns.FULL_COMMENT_ID + " ASC";
                    break;

                case URI_COMMENTS_ATTACHMENTS:
                    _OrderBy = CommentsAttachmentsColumns.FULL_ID + " ASC";
                    break;

                case URI_PHOTO_ALBUMS:
                    _OrderBy = PhotoAlbumsColumns.FULL_ID + " ASC";
                    break;

                // case URI_POLL:
                //    _OrderBy = PollColumns.FULL_ID + " ASC";
                //    break;

                case URI_NEWS:
                    _OrderBy = NewsColumns.FULL_ID + " ASC";
                    break;

                case URI_GROUPS_DET:
                    _OrderBy = GroupsDetColumns.FULL_ID + " ASC";
                    break;

                case URI_VIDEO_ALBUMS:
                    _OrderBy = VideoAlbumsColumns.FULL_ID + " ASC";
                    break;

                case URI_TOPICS:
                    _OrderBy = TopicsColumns.FULL_ID + " ASC";
                    break;

                case URI_NOTIFICATIONS:
                    _OrderBy = NotificationColumns.FULL_ID + " ASC";
                    break;

                case URI_USER_DET:
                    _OrderBy = UsersDetColumns.FULL_ID + " ASC";
                    break;
                case URI_FAVE_PHOTOS:
                    _OrderBy = FavePhotosColumns.FULL_ID + " ASC";
                    break;
                case URI_FAVE_VIDEOS:
                    _OrderBy = FaveVideosColumns.FULL_ID + " ASC";
                    break;
                case URI_FAVE_ARTICLES:
                    _OrderBy = FaveArticlesColumns.FULL_ID + " ASC";
                    break;
                case URI_FAVE_PRODUCTS:
                    _OrderBy = FaveProductColumns.FULL_ID + " ASC";
                    break;
                case URI_FAVE_PAGES:
                    _OrderBy = FavePageColumns.UPDATED_TIME + " DESC";
                    break;
                case URI_FAVE_GROUPS:
                    _OrderBy = FavePageColumns.UPDATED_TIME + " DESC";
                    break;
                case URI_FAVE_LINKS:
                    _OrderBy = FaveLinksColumns.FULL_ID + " ASC";
                    break;
                case URI_FAVE_POSTS:
                    _OrderBy = FavePostsColumns.FULL_ID + " ASC";
                    break;
                case URI_COUNTRIES:
                    _OrderBy = CountriesColumns.FULL_ID + " ASC";
                    break;
                case URI_FEED_LISTS:
                    _OrderBy = FeedListsColumns.FULL_ID + " ASC";
                    break;
                case URI_FRIEND_LISTS:
                    _OrderBy = FriendListsColumns.FULL_ID + " ASC";
                    break;
                case URI_KEYS:
                    _OrderBy = KeyColumns.FULL_ID + " ASC";
                    break;
                default:
                    throw new UnknownError("Unknown table type for sort order");
            }
        } else {
            _OrderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase _DB = getDbHelper(uri).getReadableDatabase();
        Cursor _Result = _QB.query(_DB, projection, selection, selectionArgs, null, null, _OrderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        if (getContext() != null) {
            _Result.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return _Result;
    }

    @NonNull
    private DBHelper getDbHelper(Uri uri) {
        return getDbHelper(extractAidFromUri(uri));
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case URI_USERS:
                return USER_CONTENT_TYPE;
            case URI_USERS_ID:
                return USER_CONTENT_ITEM_TYPE;
            case URI_MESSAGES:
                return MESSAGE_CONTENT_TYPE;
            case URI_MESSAGES_ID:
                return MESSAGE_CONTENT_ITEM_TYPE;
            case URI_MESSAGES_ATTACHMENTS:
                return MESSAGES_ATTACHMENTS_CONTENT_TYPE;
            case URI_MESSAGES_ATTACHMENTS_ID:
                return MESSAGES_ATTACHMENTS_CONTENT_ITEM_TYPE;
            case URI_PHOTOS:
                return PHOTOS_CONTENT_TYPE;
            case URI_PHOTOS_ID:
                return PHOTOS_CONTENT_ITEM_TYPE;
            case URI_DIALOGS:
                return DIALOGS_CONTENT_TYPE;
            case URI_PEERS:
                return PEERS_CONTENT_TYPE;
            case URI_DOCS:
                return DOCS_CONTENT_TYPE;
            case URI_DOCS_ID:
                return DOCS_CONTENT_ITEM_TYPE;
            case URI_VIDEOS:
                return VIDEOS_CONTENT_TYPE;
            case URI_VIDEOS_ID:
                return VIDEOS_CONTENT_ITEM_TYPE;
            case URI_POSTS:
                return POSTS_CONTENT_TYPE;
            case URI_POSTS_ID:
                return POSTS_CONTENT_ITEM_TYPE;
            case URI_POST_ATTACHMENTS:
                return POSTS_ATTACHMENTS_CONTENT_TYPE;
            case URI_POST_ATTACHMENTS_ID:
                return POSTS_ATTACHMENTS_CONTENT_ITEM_TYPE;
            case URI_GROUPS:
                return GROUPS_CONTENT_TYPE;
            case URI_GROUPS_ID:
                return GROUPS_CONTENT_ITEM_TYPE;
            case URI_RELATIVESHIP:
                return RELATIVESHIP_CONTENT_TYPE;
            case URI_COMMENTS:
                return COMMENTS_CONTENT_TYPE;
            case URI_COMMENTS_ID:
                return COMMENTS_CONTENT_ITEM_TYPE;
            case URI_COMMENTS_ATTACHMENTS:
                return COMMENTS_ATTACHMENTS_CONTENT_TYPE;
            case URI_COMMENTS_ATTACHMENTS_ID:
                return COMMENTS_ATTACHMENTS_CONTENT_ITEM_TYPE;
            case URI_PHOTO_ALBUMS:
                return PHOTO_ALBUMS_CONTENT_TYPE;
            //case URI_POLL:
            //    return POLL_CONTENT_TYPE;
            //case URI_POLL_ID:
            //    return POLL_CONTENT_ITEM_TYPE;
            case URI_NEWS:
                return NEWS_CONTENT_TYPE;
            case URI_GROUPS_DET:
                return GROUPS_DET_CONTENT_TYPE;
            case URI_GROUPS_DET_ID:
                return GROUPS_DET_CONTENT_ITEM_TYPE;
            case URI_VIDEO_ALBUMS:
                return VIDEO_ALBUMS_CONTENT_TYPE;
            case URI_TOPICS:
                return TOPICS_CONTENT_TYPE;
            case URI_NOTIFICATIONS:
                return NOTIFICATIONS_CONTENT_TYPE;
            case URI_USER_DET:
                return USER_DET_CONTENT_TYPE;
            case URI_USER_DET_ID:
                return USER_DET_CONTENT_ITEM_TYPE;
            case URI_FAVE_PHOTOS:
                return FAVE_PHOTOS_CONTENT_TYPE;
            case URI_FAVE_VIDEOS:
                return FAVE_VIDEOS_CONTENT_TYPE;
            case URI_FAVE_ARTICLES:
                return FAVE_ARTICLES_CONTENT_TYPE;
            case URI_FAVE_PRODUCTS:
                return FAVE_PRODUCTS_CONTENT_TYPE;
            case URI_FAVE_PAGES:
                return FAVE_PAGES_CONTENT_TYPE;
            case URI_FAVE_GROUPS:
                return FAVE_GROUPS_CONTENT_TYPE;
            case URI_FAVE_LINKS:
                return FAVE_LINKS_CONTENT_TYPE;
            case URI_FAVE_POSTS:
                return FAVE_POSTS_CONTENT_TYPE;
            case URI_COUNTRIES:
                return COUNTRIES_CONTENT_TYPE;
            case URI_FEED_LISTS:
                return FEED_LISTS_CONTENT_TYPE;
            case URI_FRIEND_LISTS:
                return FRIEND_LISTS_CONTENT_TYPE;
            case URI_KEYS:
                return KEYS_CONTENT_TYPE;
        }
        return null;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        String tbName;
        switch (sUriMatcher.match(uri)) {
            case URI_MESSAGES:
                tbName = MessageColumns.TABLENAME;
                break;
            case URI_MESSAGES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = MessageColumns._ID + " = " + id;
                } else {
                    selection = selection + " AND " + MessageColumns._ID + " = " + id;
                }
                tbName = MessageColumns.TABLENAME;
                break;
            case URI_DIALOGS:
                tbName = DialogsColumns.TABLENAME;
                break;
            case URI_RELATIVESHIP:
                tbName = RelationshipColumns.TABLENAME;
                break;
            case URI_POSTS:
                tbName = PostsColumns.TABLENAME;
                break;
            case URI_POSTS_ID:
                String postId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + postId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + postId;
                }
                tbName = PostsColumns.TABLENAME;
                break;
            case URI_PHOTOS:
                tbName = PhotosColumns.TABLENAME;
                break;
            case URI_MESSAGES_ATTACHMENTS:
                tbName = MessagesAttachmentsColumns.TABLENAME;
                break;
            case URI_COMMENTS:
                tbName = CommentsColumns.TABLENAME;
                break;
            case URI_PHOTO_ALBUMS:
                tbName = PhotoAlbumsColumns.TABLENAME;
                break;
            case URI_POST_ATTACHMENTS:
                tbName = WallAttachmentsColumns.TABLENAME;
                break;
            case URI_COMMENTS_ATTACHMENTS:
                tbName = CommentsAttachmentsColumns.TABLENAME;
                break;
            /*case URI_POLL:
                tbName = PollColumns.TABLENAME;
                break;
            case URI_POLL_ID:
                String pollId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = PollColumns._ID + " = " + pollId;
                } else {
                    selection = selection + " AND " + PollColumns._ID + " = " + pollId;
                }
                tbName = PollColumns.TABLENAME;
                break;*/

            case URI_DOCS:
                tbName = DocColumns.TABLENAME;
                break;

            case URI_NEWS:
                tbName = NewsColumns.TABLENAME;
                break;

            case URI_GROUPS_DET:
                tbName = GroupsDetColumns.TABLENAME;
                break;
            case URI_GROUPS_DET_ID:
                String groupDetId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + groupDetId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + groupDetId;
                }
                tbName = GroupsDetColumns.TABLENAME;
                break;
            case URI_VIDEO_ALBUMS:
                tbName = VideoAlbumsColumns.TABLENAME;
                break;
            case URI_VIDEOS:
                tbName = VideoColumns.TABLENAME;
                break;
            case URI_TOPICS:
                tbName = TopicsColumns.TABLENAME;
                break;
            case URI_NOTIFICATIONS:
                tbName = NotificationColumns.TABLENAME;
                break;
            case URI_USER_DET:
                tbName = UsersDetColumns.TABLENAME;
                break;
            case URI_USER_DET_ID:
                String userDetId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + userDetId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + userDetId;
                }
                tbName = UsersDetColumns.TABLENAME;
                break;
            case URI_FAVE_PHOTOS:
                tbName = FavePhotosColumns.TABLENAME;
                break;
            case URI_FAVE_VIDEOS:
                tbName = FaveVideosColumns.TABLENAME;
                break;
            case URI_FAVE_ARTICLES:
                tbName = FaveArticlesColumns.TABLENAME;
                break;
            case URI_FAVE_PRODUCTS:
                tbName = FaveProductColumns.TABLENAME;
                break;
            case URI_FAVE_PAGES:
                tbName = FavePageColumns.TABLENAME;
                break;
            case URI_FAVE_GROUPS:
                tbName = FavePageColumns.GROUPSTABLENAME;
                break;
            case URI_FAVE_LINKS:
                tbName = FaveLinksColumns.TABLENAME;
                break;
            case URI_FAVE_POSTS:
                tbName = FavePostsColumns.TABLENAME;
                break;
            case URI_COUNTRIES:
                tbName = CountriesColumns.TABLENAME;
                break;
            case URI_FEED_LISTS:
                tbName = FeedListsColumns.TABLENAME;
                break;
            case URI_FRIEND_LISTS:
                tbName = FriendListsColumns.TABLENAME;
                break;
            case URI_KEYS:
                tbName = KeyColumns.TABLENAME;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        SQLiteDatabase db = getDbHelper(uri).getWritableDatabase();
        int cnt = db.delete(tbName, selection, selectionArgs);

        safeNotifyChange(uri);
        return cnt;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String tbName;
        switch (sUriMatcher.match(uri)) {
            case URI_MESSAGES:
                tbName = MessageColumns.TABLENAME;
                break;
            case URI_MESSAGES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = MessageColumns._ID + " = " + id;
                } else {
                    selection = selection + " AND " + MessageColumns._ID + " = " + id;
                }
                tbName = MessageColumns.TABLENAME;
                break;
            case URI_USERS:
                tbName = UserColumns.TABLENAME;
                break;
            case URI_USERS_ID:
                String userID = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + userID;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + userID;
                }
                tbName = UserColumns.TABLENAME;
                break;
            case URI_GROUPS:
                tbName = GroupColumns.TABLENAME;
                break;
            case URI_GROUPS_ID:
                String groupID = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + groupID;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + groupID;
                }
                tbName = GroupColumns.TABLENAME;
                break;
            case URI_DIALOGS:
                tbName = DialogsColumns.TABLENAME;
                break;
            case URI_PEERS:
                tbName = PeersColumns.TABLENAME;
                break;
            case URI_POSTS:
                tbName = PostsColumns.TABLENAME;
                break;
            case URI_POSTS_ID:
                String postId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + postId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + postId;
                }
                tbName = PostsColumns.TABLENAME;
                break;
            case URI_PHOTOS:
                tbName = PhotosColumns.TABLENAME;
                break;
            case URI_PHOTOS_ID:
                String photoId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + photoId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + photoId;
                }
                tbName = PhotosColumns.TABLENAME;
                break;
            case URI_VIDEOS:
                tbName = VideoColumns.TABLENAME;
                break;
            case URI_COMMENTS:
                tbName = CommentsColumns.TABLENAME;
                break;
            case URI_COMMENTS_ID:
                String commentId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + commentId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + commentId;
                }
                tbName = CommentsColumns.TABLENAME;
                break;
            case URI_RELATIVESHIP:
                tbName = RelationshipColumns.TABLENAME;
                break;
            case URI_PHOTO_ALBUMS:
                tbName = PhotoAlbumsColumns.TABLENAME;
                break;
            /*case URI_POLL:
                tbName = PollColumns.TABLENAME;
                break;
            case URI_POLL_ID:
                String pollId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = PollColumns._ID + " = " + pollId;
                } else {
                    selection = selection + " AND " + PollColumns._ID + " = " + pollId;
                }
                tbName = PollColumns.TABLENAME;
                break;*/

            case URI_NEWS:
                tbName = NewsColumns.TABLENAME;
                break;

            case URI_GROUPS_DET:
                tbName = GroupsDetColumns.TABLENAME;
                break;
            case URI_GROUPS_DET_ID:
                String groupDetId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + groupDetId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + groupDetId;
                }
                tbName = GroupsDetColumns.TABLENAME;
                break;

            case URI_VIDEO_ALBUMS:
                tbName = VideoAlbumsColumns.TABLENAME;
                break;

            case URI_TOPICS:
                tbName = TopicsColumns.TABLENAME;
                break;
            case URI_NOTIFICATIONS:
                tbName = NotificationColumns.TABLENAME;
                break;

            case URI_USER_DET:
                tbName = UsersDetColumns.TABLENAME;
                break;
            case URI_USER_DET_ID:
                String userDetId = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = BaseColumns._ID + " = " + userDetId;
                } else {
                    selection = selection + " AND " + BaseColumns._ID + " = " + userDetId;
                }
                tbName = UsersDetColumns.TABLENAME;
                break;
            case URI_FAVE_PHOTOS:
                tbName = FavePhotosColumns.TABLENAME;
                break;
            case URI_FAVE_VIDEOS:
                tbName = FaveVideosColumns.TABLENAME;
                break;
            case URI_FAVE_ARTICLES:
                tbName = FaveArticlesColumns.TABLENAME;
                break;
            case URI_FAVE_PRODUCTS:
                tbName = FaveProductColumns.TABLENAME;
                break;
            case URI_FAVE_PAGES:
                tbName = FavePageColumns.TABLENAME;
                break;
            case URI_FAVE_GROUPS:
                tbName = FavePageColumns.GROUPSTABLENAME;
                break;
            case URI_FAVE_LINKS:
                tbName = FaveLinksColumns.TABLENAME;
                break;
            case URI_FAVE_POSTS:
                tbName = FavePostsColumns.TABLENAME;
                break;
            case URI_COUNTRIES:
                tbName = CountriesColumns.TABLENAME;
                break;
            case URI_FEED_LISTS:
                tbName = FeedListsColumns.TABLENAME;
                break;
            case URI_FRIEND_LISTS:
                tbName = FriendListsColumns.TABLENAME;
                break;
            case URI_KEYS:
                tbName = KeyColumns.TABLENAME;
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        SQLiteDatabase db = getDbHelper(uri).getWritableDatabase();

        int cnt = db.update(tbName, values, selection, selectionArgs);
        safeNotifyChange(uri);

        if (tbName.equals(MessageColumns.TABLENAME)) {
            safeNotifyChange(DIALOGS_CONTENT_URI);
        }

        return cnt;
    }
}
