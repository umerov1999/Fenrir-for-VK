package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

public final class VkApiBanned {

    @SerializedName("type")
    public String type;

    @SerializedName("profile")
    public VKApiUser profile;

    @SerializedName("group")
    public VKApiCommunity group;

    @SerializedName("ban_info")
    public Info banInfo;

    public static final class Info {

        /**
         * идентификатор администратора, который добавил пользователя в черный список.
         */
        @SerializedName("admin_id")
        public int adminId;

        /**
         * дата добавления пользователя в черный список в формате Unixtime.
         */
        @SerializedName("date")
        public long date;

        /**
         * причина добавления пользователя в черный список. Возможные значения:
         * 0 — другое (по умолчанию);
         * 1 — спам;
         * 2 — оскорбление участников;
         * 3 — нецензурные выражения;
         * 4 — сообщения не по теме.
         */
        @SerializedName("reason")
        public int reason;

        /**
         * текст комментария.
         */
        @SerializedName("comment")
        public String comment;

        /**
         * дата окончания блокировки (0 — блокировка вечная).
         */
        @SerializedName("end_date")
        public long endDate;

        @SerializedName("comment_visible")
        public boolean commentVisible;
    }
}