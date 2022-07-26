package dev.ragnarok.fenrir.api.model

import kotlinx.serialization.Serializable

/**
 * A place object describes a location.
 */
@Serializable
class VKApiPlace
/**
 * Creates empty Place instance.
 */
{
    /**
     * Location ID.
     */
    var id = 0

    /**
     * Location title.
     */
    var title: String? = null

    /**
     * Geographical latitude, in degrees (from -90 to 90).
     */
    var latitude = 0.0

    /**
     * Geographical longitude, in degrees (from -180 to 180)
     */
    var longitude = 0.0

    /**
     * Date (in Unix time) when the location was added
     */
    var created: Long = 0

    /**
     * Numbers of checkins in this place
     */
    var checkins = 0

    /**
     * Date (in Unix time) when the location was last time updated
     */
    var updated: Long = 0

    /**
     * ID of the country the place is located in, positive number
     */
    var country_id = 0

    /**
     * ID of the city the place is located in, positive number
     */
    var city_id = 0

    /**
     * Location address.
     */
    var address: String? = null
}