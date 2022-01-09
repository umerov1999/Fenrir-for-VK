package dev.ragnarok.fenrir.api.model;

/**
 * Describes a photo album
 */
@SuppressWarnings("unused")
public class VKApiVideoAlbum {

    /**
     * Album ID.
     */
    public int id;

    /**
     * Album title.
     */
    public String title;

    /**
     * ID of the user or community that owns the album.
     */
    public int owner_id;

    public int count;

    /**
     * Date (in Unix time) the album was last updated.
     */
    public long updated_time;

    public String image;

    public VkApiPrivacy privacy;
}