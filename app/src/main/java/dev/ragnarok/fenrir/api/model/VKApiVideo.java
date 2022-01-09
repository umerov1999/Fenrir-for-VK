package dev.ragnarok.fenrir.api.model;

/**
 * A video object describes an video file.
 */
@SuppressWarnings("unused")
public class VKApiVideo implements VKApiAttachment, Commentable, Likeable, Copyable {

    /**
     * Video ID.
     */
    public int id;

    /**
     * Video owner ID.
     */
    public int owner_id;

    /**
     * Video album ID.
     */
    public int album_id;

    /**
     * Video title.
     */
    public String title;

    /**
     * Text describing video.
     */
    public String description;

    /**
     * Duration of the video in seconds.
     */
    public int duration;

    /**
     * String with video+vid key.
     */
    public String link;

    /**
     * Date when the video was created, as unixtime.
     */
    public long date;

    /**
     * Date when the video was added, as unixtime.
     */
    public long adding_date;

    /**
     * Number of views of the video.
     */
    public int views;

    /**
     * URL of the page with a player that can be used to play a video in the browser.
     * Flash and HTML5 video players are supported; the player is always zoomed to fit
     * the window size.
     */
    public String player;

    /**
     * URL of the video cover image
     */
    public String image;

    /**
     * An access key using for get information about hidden objects.
     */
    public String access_key;

    /**
     * Number of comments on the video.
     */
    public CommentsDto comments;

    /**
     * Whether the current user can comment on the video
     */
    public boolean can_comment;

    /**
     * Whether the current user can repost this video
     */
    public boolean can_repost;

    /**
     * Information whether the current user liked the video.
     */
    public boolean user_likes;

    /**
     * Information whether the the video should be repeated.
     */
    public boolean repeat;

    /**
     * Number of likes on the video.
     */
    public int likes;

    /**
     * VkApiPrivacy to view of this video.
     */
    public VkApiPrivacy privacy_view;

    /**
     * VkApiPrivacy to comment of this video.
     */
    public VkApiPrivacy privacy_comment;

    /**
     * URL of video with height of 240 pixels. Returns only if you use direct auth.
     */
    public String mp4_240;

    /**
     * URL of video with height of 360 pixels. Returns only if you use direct auth.
     */
    public String mp4_360;

    /**
     * URL of video with height of 480 pixels. Returns only if you use direct auth.
     */
    public String mp4_480;

    /**
     * URL of video with height of 720 pixels. Returns only if you use direct auth.
     */
    public String mp4_720;

    /**
     * URL of video with height of 1080 pixels. Returns only if you use direct auth.
     */
    public String mp4_1080;

    /**
     * URL of the external video link.
     */
    public String external;

    public String hls;

    public String live;

    public String platform;

    public boolean can_edit;

    public boolean can_add;

    public boolean is_private;

    @Override
    public String getType() {
        return TYPE_VIDEO;
    }
}