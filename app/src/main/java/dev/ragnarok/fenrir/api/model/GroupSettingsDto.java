package dev.ragnarok.fenrir.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GroupSettingsDto {

    public String title;

    public String description;

    public String address;

    public Place place;

    public int country_id;

    public int city_id;

    /**
     * 3 - закрытая
     * 0 - выключена
     * 1 - открытая
     * 2 - ограниченная
     */
    public int wall;

    public int photos;

    public int video;

    public int audio;

    public int docs;

    public int topics;

    public int wiki;

    public boolean obscene_filter;

    public boolean obscene_stopwords;

    public String[] obscene_words;

    public int access;

    public int subject;

    /**
     * "public_date": "13.0.2014"
     */
    public String public_date;

    public String public_date_label;

    /**
     * Может быть как int, так и String
     */
    public String public_category;

    /**
     * Может быть как int, так и String
     */
    public String public_subcategory;

    public List<PublicCategory> public_category_list;

    public int contacts;

    public int links;

    public int events;

    public int places;

    public boolean rss;

    public String website;

    public int age_limits;

    public Market market;

    public static final class Place {

        @SerializedName("id")
        public int id;

        @SerializedName("title")
        public String title;

        @SerializedName("latitude")
        public double latitude;

        @SerializedName("longitude")
        public double longitude;

        @SerializedName("created")
        public long created;

        @SerializedName("icon")
        public String icon;

        @SerializedName("group_id")
        public int group_id;

        @SerializedName("group_photo")
        public String group_photo;

        @SerializedName("checkins")
        public int checkins;

        @SerializedName("type")
        public int type;

        @SerializedName("country")
        public int country;

        @SerializedName("city")
        public int city;

        @SerializedName("address")
        public String address;
    }

    public static final class PublicCategory {

        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;

        @SerializedName("subtypes_list")
        public List<PublicCategory> subtypes_list;
    }

    public static final class Market {

        @SerializedName("enabled")
        public boolean enabled;

        @SerializedName("comments_enabled")
        public boolean comments_enabled;

        /**
         * -1 Полный список
         * -2 Весь мир
         * -3 Страны СНГ
         */
        @SerializedName("country_ids")
        public int[] country_ids;

        @SerializedName("city_ids")
        public int[] city_ids;

        /**
         * Если в сообщения сообщества, то меньше ноля (напр. -72124992)
         */
        @SerializedName("contact_id")
        public int contact_id;

        @SerializedName("currency")
        public Currency currency;
    }

    public static final class Currency {

        @SerializedName("id")
        public int id;

        @SerializedName("name")
        public String name;
    }
}