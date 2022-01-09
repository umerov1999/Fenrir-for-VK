package dev.ragnarok.fenrir.api.model;

import java.util.List;

/**
 * Describes a photo object from VK.
 */
public class VKApiPhoto implements VKApiAttachment, Commentable, Likeable, Copyable {

    /**
     * Photo ID, positive number
     */
    public int id;

    /**
     * Photo album ID.
     */
    public int album_id;

    /**
     * ID of the user or community that owns the photo.
     */
    public int owner_id;

    /**
     * Width (in pixels) of the original photo.
     */
    public int width;

    /**
     * Height (in pixels) of the original photo.
     */
    public int height;

    /**
     * Text describing the photo.
     */
    public String text;

    /**
     * Date (in Unix time) the photo was added.
     */
    public long date;

    /**
     * Information whether the current user liked the photo.
     */
    public boolean user_likes;

    /**
     * Whether the current user can comment on the photo
     */
    public boolean can_comment;

    /**
     * Number of likes on the photo.
     */
    public int likes;

    /**
     * Number of comments on the photo.
     */
    public CommentsDto comments;

    /**
     * Number of tags on the photo.
     */
    public int tags;

    /**
     * An access key using for get information about hidden objects.
     */
    public String access_key;

    public int post_id;

    public List<PhotoSizeDto> sizes;

    /**
     * Creates empty Photo instance.
     */
    public VKApiPhoto() {

    }

    @Override
    public String getType() {
        return TYPE_PHOTO;
    }
}