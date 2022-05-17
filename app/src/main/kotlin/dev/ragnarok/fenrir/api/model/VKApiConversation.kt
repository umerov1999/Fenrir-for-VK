package dev.ragnarok.fenrir.api.model

import com.google.gson.annotations.SerializedName

class VKApiConversation {
    @SerializedName("peer")
    var peer: Peer? = null

    /**
     * идентификатор последнего прочтенного входящего сообщения.
     */
    @SerializedName("in_read")
    var inRead = 0

    /**
     * идентификатор последнего прочтенного исходящего сообщения.
     */
    @SerializedName("out_read")
    var outRead = 0

    @SerializedName("last_message_id")
    var lastMessageId = 0

    /**
     * число непрочитанных сообщений.
     */
    @SerializedName("unread_count")
    var unreadCount = 0

    @SerializedName("sort_id")
    var sort_id: SortElement? = null

    /**
     * true, если диалог помечен как важный (только для сообщений сообществ).
     */
    @SerializedName("important")
    var important = false

    @SerializedName("current_keyboard")
    var current_keyboard: CurrentKeyboard? = null

    /**
     * true, если диалог помечен как неотвеченный (только для сообщений сообществ).
     */
    @SerializedName("unanswered")
    var unanswered = false

    /**
     * информация о том, может ли пользователь писать в диалог.
     */
    @SerializedName("can_write")
    var canWrite: CanWrite? = null

    @SerializedName("chat_settings")
    var settings: Settings? = null

    class CanWrite {
        /**
         * true, если пользователь может писать в диалог;
         */
        @SerializedName("allowed")
        var allowed = false

        /**
         * 18 — пользователь заблокирован или удален;
         * 900 — нельзя отправить сообщение пользователю, который в чёрном списке;
         * 901 — пользователь запретил сообщения от сообщества;
         * 902 — пользователь запретил присылать ему сообщения с помощью настроек приватности;
         * 915 — в сообществе отключены сообщения;
         * 916 — в сообществе заблокированы сообщения;
         * 917 — нет доступа к чату;
         * 918 — нет доступа к e-mail;
         * 203 — нет доступа к сообществу.
         */
        @SerializedName("reason")
        var reason = 0
    }

    class Peer {
        /**
         * идентификатор назначения.
         */
        @SerializedName("id")
        var id = 0

        /**
         * local_id (integer) — локальный идентификатор назначения. Для чатов — id - 2000000000, для сообществ — -id, для e-mail — -(id+2000000000).
         * для контактов - (id-1900000000)
         */
        @SerializedName("local_id")
        var local_id = 0

        @SerializedName("type")
        var type: String? = null
    }

    class Settings {
        @SerializedName("pinned_message")
        var pinnedMesage: VKApiMessage? = null

        @SerializedName("title")
        var title: String? = null

        @SerializedName("members_count")
        var membersCount = 0

        @SerializedName("photo")
        var photo: Photo? = null

        @SerializedName("active_ids")
        var activeIds: IntArray? = null

        @SerializedName("state")
        var state: String? = null

        @SerializedName("is_group_channel")
        var is_group_channel = false

        @SerializedName("acl")
        var acl: Acl? = null
    }

    class Acl {
        @SerializedName("can_invite")
        var can_invite = false

        @SerializedName("can_change_info")
        var can_change_info = false

        @SerializedName("can_change_pin")
        var can_change_pin = false

        @SerializedName("can_promote_users")
        var can_promote_users = false

        @SerializedName("can_see_invite_link")
        var can_see_invite_link = false

        @SerializedName("can_change_invite_link")
        var can_change_invite_link = false
    }

    class Photo {
        @SerializedName("photo_50")
        var photo50: String? = null

        @SerializedName("photo_100")
        var photo100: String? = null

        @SerializedName("photo200")
        var photo200: String? = null
    }

    class Keyboard_Action {
        @SerializedName("type")
        var type: String? = null

        @SerializedName("label")
        var label: String? = null

        @SerializedName("link")
        var link: String? = null

        @SerializedName("payload")
        var payload: String? = null
    }

    class ButtonElement {
        @SerializedName("action")
        var action: Keyboard_Action? = null

        @SerializedName("color")
        var color: String? = null
    }

    class SortElement {
        @SerializedName("major_id")
        var major_id = 0

        @SerializedName("minor_id")
        var minor_id = 0
    }

    class ContactElement {
        @SerializedName("id")
        var id = 0

        @SerializedName("name")
        var name: String? = null

        @SerializedName("phone")
        var phone: String? = null

        @SerializedName("photo_50")
        var photo_50: String? = null

        @SerializedName("photo_100")
        var photo_100: String? = null

        @SerializedName("photo_200")
        var photo_200: String? = null

        @SerializedName("photo_max_orig")
        var photo_max_orig: String? = null

        @SerializedName("last_seen_status")
        var last_seen_status: String? = null
    }

    class CurrentKeyboard {
        @SerializedName("one_time")
        var one_time = false

        @SerializedName("inline")
        var inline = false

        @SerializedName("author_id")
        var author_id = 0

        @SerializedName("buttons")
        var buttons: List<List<ButtonElement>>? = null
    }
}