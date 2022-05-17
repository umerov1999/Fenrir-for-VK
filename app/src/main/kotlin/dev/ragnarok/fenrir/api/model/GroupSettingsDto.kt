package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class GroupSettingsDto {
    var title: String? = null
    var description: String? = null
    var address: String? = null
    var place: Place? = null
    var country_id = 0
    var city_id = 0

    /**
     * 3 - закрытая
     * 0 - выключена
     * 1 - открытая
     * 2 - ограниченная
     */
    var wall = 0
    var photos = 0
    var video = 0
    var audio = 0
    var docs = 0
    var topics = 0
    var wiki = 0
    var obscene_filter = false
    var obscene_stopwords = false
    var obscene_words: Array<String>? = null
    var access = 0
    var subject = 0

    /**
     * "public_date": "13.0.2014"
     */
    var public_date: String? = null
    var public_date_label: String? = null

    /**
     * Может быть как int, так и String
     */
    var public_category: String? = null

    /**
     * Может быть как int, так и String
     */
    var public_subcategory: String? = null
    var public_category_list: List<PublicCategory>? = null
    var contacts = 0
    var links = 0
    var events = 0
    var places = 0
    var rss = false
    var website: String? = null
    var age_limits = 0
    var market: Market? = null

    class Place {
        @SerializedName("id")
        var id = 0

        @SerializedName("title")
        var title: String? = null

        @SerializedName("latitude")
        var latitude = 0.0

        @SerializedName("longitude")
        var longitude = 0.0

        @SerializedName("created")
        var created: Long = 0

        @SerializedName("icon")
        var icon: String? = null

        @SerializedName("group_id")
        var group_id = 0

        @SerializedName("group_photo")
        var group_photo: String? = null

        @SerializedName("checkins")
        var checkins = 0

        @SerializedName("type")
        var type = 0

        @SerializedName("country")
        var country = 0

        @SerializedName("city")
        var city = 0

        @SerializedName("address")
        var address: String? = null
    }

    class PublicCategory {
        @SerializedName("id")
        var id = 0

        @SerializedName("name")
        var name: String? = null

        @SerializedName("subtypes_list")
        var subtypes_list: List<PublicCategory>? = null
    }

    class Market {
        @SerializedName("enabled")
        var enabled = false

        @SerializedName("comments_enabled")
        var comments_enabled = false

        /**
         * -1 Полный список
         * -2 Весь мир
         * -3 Страны СНГ
         */
        @SerializedName("country_ids")
        var country_ids: IntArray? = null

        @SerializedName("city_ids")
        var city_ids: IntArray? = null

        /**
         * Если в сообщения сообщества, то меньше ноля (напр. -72124992)
         */
        @SerializedName("contact_id")
        var contact_id = 0

        @SerializedName("currency")
        var currency: Currency? = null
    }

    class Currency {
        @SerializedName("id")
        var id = 0

        @SerializedName("name")
        var name: String? = null
    }
}