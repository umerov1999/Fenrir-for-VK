package dev.ragnarok.fenrir.api.model

/**
 * Describes a photo album
 */
class VKApiVideoAlbum {
    /**
     * Album ID.
     */
    var id = 0

    /**
     * Album title.
     */
    var title: String? = null

    /**
     * ID of the user or community that owns the album.
     */
    var owner_id = 0
    var count = 0

    /**
     * Date (in Unix time) the album was last updated.
     */
    var updated_time: Long = 0
    var image: String? = null
    var privacy: VKApiPrivacy? = null
}