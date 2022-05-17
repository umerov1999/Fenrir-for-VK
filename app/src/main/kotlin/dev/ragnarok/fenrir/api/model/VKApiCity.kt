package dev.ragnarok.fenrir.api.model

/**
 * A city object describes a city.
 */
class VKApiCity {
    /**
     * City ID.
     */
    var id = 0

    /**
     * City name
     */
    var title: String? = null
    var important = false
    var area: String? = null
    var region: String? = null
}