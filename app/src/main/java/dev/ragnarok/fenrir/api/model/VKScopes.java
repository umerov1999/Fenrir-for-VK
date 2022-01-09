package dev.ragnarok.fenrir.api.model;

import java.util.ArrayList;

/**
 * Application Access Permissions
 *
 * @see <a href="http://vk.com/dev/permissions">http://vk.com/dev/permissions</a>
 */
@SuppressWarnings("unused")
public class VKScopes {

    /**
     * User allowed to send notifications to him/her.
     */
    public static final String NOTIFY = "notify";
    /**
     * Access to friends.
     */
    public static final String FRIENDS = "friends";
    /**
     * Access to photos.
     */
    public static final String PHOTOS = "photos";
    /**
     * Access to audios.
     */
    public static final String AUDIO = "audio";
    /**
     * Access to videos.
     */
    public static final String VIDEO = "video";
    /**
     * Access to documents.
     */
    public static final String DOCS = "docs";
    /**
     * Access to user notes.
     */
    public static final String NOTES = "notes";
    /**
     * Access to wiki pages.
     */
    public static final String PAGES = "pages";
    /**
     * Access to user status.
     */
    public static final String STATUS = "status";
    /**
     * Access to offers (obsolete methods).
     */
    @Deprecated
    public static final String OFFERS = "offers";
    /**
     * Access to questions (obsolete methods).
     */
    @Deprecated
    public static final String QUESTIONS = "questions";
    /**
     * Access to standard and advanced methods for the wall.
     */
    public static final String WALL = "wall";
    /**
     * Access to user groups.
     */
    public static final String GROUPS = "groups";
    /**
     * Access to advanced methods for messaging.
     */
    public static final String MESSAGES = "messages";
    /**
     * Access to notifications about answers to the user.
     */
    public static final String NOTIFICATIONS = "notifications";
    /**
     * Access to statistics of user groups and applications where he/she is an administrator.
     */
    public static final String STATS = "stats";
    /**
     * Access to advanced methods for <a href="http://vk.com/dev/ads">Ads API</a>.
     */
    public static final String ADS = "ads";
    /**
     * Access to API at any time from a third party server.
     */
    public static final String OFFLINE = "offline";
    /**
     * Possibility to make API requests without HTTPS. <br />
     * <b>Note that this functionality is under testing and can be changed.</b>
     */
    public static final String NOHTTPS = "nohttps";

    private VKScopes() {
    }

    /**
     * Converts integer value of permissions into arraylist of constants
     *
     * @param permissions integer permissions value
     * @return ArrayList contains string constants of permissions (scope)
     */
    public static ArrayList<String> parse(int permissions) {
        ArrayList<String> result = new ArrayList<>();
        if ((permissions & 1) > 0) result.add(NOTIFY);
        if ((permissions & 2) > 0) result.add(FRIENDS);
        if ((permissions & 4) > 0) result.add(PHOTOS);
        if ((permissions & 8) > 0) result.add(AUDIO);
        if ((permissions & 16) > 0) result.add(VIDEO);
        if ((permissions & 128) > 0) result.add(PAGES);
        if ((permissions & 1024) > 0) result.add(STATUS);
        if ((permissions & 2048) > 0) result.add(NOTES);
        if ((permissions & 4096) > 0) result.add(MESSAGES);
        if ((permissions & 8192) > 0) result.add(WALL);
        if ((permissions & 32768) > 0) result.add(ADS);
        if ((permissions & 65536) > 0) result.add(OFFLINE);
        if ((permissions & 131072) > 0) result.add(DOCS);
        if ((permissions & 262144) > 0) result.add(GROUPS);
        if ((permissions & 524288) > 0) result.add(NOTIFICATIONS);
        if ((permissions & 1048576) > 0) result.add(STATS);
        return result;
    }

}
