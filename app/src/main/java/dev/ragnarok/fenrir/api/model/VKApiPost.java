package dev.ragnarok.fenrir.api.model;

import java.util.List;

/**
 * A post object describes a wall post.
 */
public class VKApiPost implements VKApiAttachment, Commentable, Likeable, Copyable {

    /**
     * Post ID on the wall, positive number
     */
    public int id;

    /**
     * Wall owner ID.
     */
    public int owner_id;

    /**
     * ID of the user who posted.
     */
    public int from_id;

    /**
     * Date (in Unix time) the post was added.
     */
    public long date;

    /**
     * Text of the post.
     */
    public String text;

    /**
     * ID of the wall owner the post to which the reply is addressed (if the post is a reply to another wall post).
     */
    public int reply_owner_id;

    /**
     * ID of the wall post to which the reply is addressed (if the post is a reply to another wall post).
     */
    public int reply_post_id;

    /**
     * True, if the post was created with "Friends only" option.
     */
    public boolean friends_only;

    /**
     * Number of comments.
     */
    public CommentsDto comments;

    /**
     * Number of users who liked the post.
     */
    public int likes_count;

    /**
     * Whether the user liked the post (false — not liked, true — liked)
     */
    public boolean user_likes;

    /**
     * Whether the user can like the post (false — cannot, true — can).
     */
    public boolean can_like;

    /**
     * Whether the user can repost (false — cannot, true — can).
     */
    public boolean can_publish;

    /**
     * Number of users who copied the post.
     */
    public int reposts_count;

    /**
     * Whether the user reposted the post (false — not reposted, true — reposted).
     */
    public boolean user_reposted;

    /**
     * Type of the post, can be: post, copy, reply, postpone, suggest.
     */
    public int post_type;

    /**
     * Information about attachments to the post (photos, links, etc.), if any;
     */
    public VkApiAttachments attachments;

    /**
     * Information about location.
     */
    public VKApiPlace geo;

    /**
     * ID of the author (if the post was published by a community and signed by a user).
     */
    public int signer_id;

    /**
     * информация о том, может ли текущий пользователь закрепить запись. R.Kolbasa
     */
    public boolean can_pin;

    /**
     * информация о том, закреплена ли запись. R.Kolbasa.
     */
    public boolean is_pinned;

    /**
     * List of history of the reposts.
     */
    public List<VKApiPost> copy_history;

    public VkApiPostSource post_source;

    public int views;

    public int created_by;

    public boolean can_edit;

    @Override
    public String getType() {
        return TYPE_POST;
    }

    public int getAttachmentsCount() {
        return attachments == null ? 0 : attachments.size();
    }

    public boolean hasAttachments() {
        return getAttachmentsCount() > 0;
    }

    public boolean hasCopyHistory() {
        return copy_history != null && !copy_history.isEmpty();
    }

    public static class Type {

        public static final int POST = 1;
        public static final int COPY = 2;
        public static final int REPLY = 3;
        public static final int POSTPONE = 4;
        public static final int SUGGEST = 5;

        public static int parse(String type) {
            if (type == null) return 0;

            switch (type) {
                case "post":
                    return POST;
                case "copy":
                    return COPY;
                case "reply":
                    return REPLY;
                case "postpone":
                    return POSTPONE;
                case "suggest":
                    return SUGGEST;
                default:
                    return 0;
            }
        }
    }
}