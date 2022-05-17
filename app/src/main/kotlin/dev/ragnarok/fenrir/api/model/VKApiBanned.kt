package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiBanned {
    @SerializedName("type")
    var type: String? = null

    @SerializedName("profile")
    var profile: VKApiUser? = null

    @SerializedName("group")
    var group: VKApiCommunity? = null

    @SerializedName("ban_info")
    var banInfo: Info? = null

    class Info {
        /**
         * идентификатор администратора, который добавил пользователя в черный список.
         */
        @SerializedName("admin_id")
        var adminId = 0

        /**
         * дата добавления пользователя в черный список в формате Unixtime.
         */
        @SerializedName("date")
        var date: Long = 0

        /**
         * причина добавления пользователя в черный список. Возможные значения:
         * 0 — другое (по умолчанию);
         * 1 — спам;
         * 2 — оскорбление участников;
         * 3 — нецензурные выражения;
         * 4 — сообщения не по теме.
         */
        @SerializedName("reason")
        var reason = 0

        /**
         * текст комментария.
         */
        @SerializedName("comment")
        var comment: String? = null

        /**
         * дата окончания блокировки (0 — блокировка вечная).
         */
        @SerializedName("end_date")
        var endDate: Long = 0

        @SerializedName("comment_visible")
        var commentVisible = false
    }
}