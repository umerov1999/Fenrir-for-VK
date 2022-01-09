package dev.ragnarok.fenrir.api.model;

import java.util.List;

/**
 * Describes a photo album
 */
public class VKApiPhotoAlbum implements VKApiAttachment {

    /**
     * URL for empty album cover with max width at 75px
     */
    public static final String COVER_S = "http://vk.com/images/s_noalbum.png";

    /**
     * URL of empty album cover with max width at 130px
     */
    public static final String COVER_M = "http://vk.com/images/m_noalbum.png";

    /**
     * URL of empty album cover with max width at 604px
     */
    public static final String COVER_X = "http://vk.com/images/x_noalbum.png";

    /**
     * Album ID.
     */
    public int id;

    /**
     * Album title.
     */
    public String title;

    /**
     * Number of photos in the album.
     */
    public int size;

    /**
     * Album description.
     */
    public String description;

    /**
     * ID of the user or community that owns the album.
     */
    public int owner_id;

    /**
     * Whether a user can upload photos to this album(false — cannot, true — can).
     */
    public boolean can_upload;

    /**
     * Date (in Unix time) the album was last updated.
     */
    public long updated;

    /**
     * Album creation date (in Unix time).
     */
    public long created;

    /**
     * ID of the photo which is the cover.
     */
    public int thumb_id;

    /**
     * Link to album cover photo.
     */
    public String thumb_src;

    /**
     * Links to to cover photo.
     */
    public List<PhotoSizeDto> photo;

    /**
     * Настройки приватности для просмотра альбома
     */
    public VkApiPrivacy privacy_view;

    /**
     * Настройки приватности для комментирования альбома
     */
    public VkApiPrivacy privacy_comment;

    /**
     * кто может загружать фотографии в альбом (только для альбома сообщества,
     * не приходит для системных альбомов);
     */
    public boolean upload_by_admins_only;

    /**
     * отключено ли комментирование альбома (только для альбома сообщества,
     * не приходит для системных альбомов);
     */
    public boolean comments_disabled;

    /**
     * Creates empty PhotoAlbum instance.
     */
    public VKApiPhotoAlbum() {

    }

    @Override
    public String getType() {
        return TYPE_ALBUM;
    }

    public boolean isSystem() {
        return id < 0;
    }
}