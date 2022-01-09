package dev.ragnarok.fenrir.api.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Community object describes a community.
 */
public class VKApiCommunity extends VKApiOwner {

    public static final String TYPE_GROUP = "group";
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_EVENT = "event";

    public static final String PHOTO_50 = "http://vk.com/images/community_50.gif";
    public static final String PHOTO_100 = "http://vk.com/images/community_100.gif";
    public static final String IS_FAVORITE = "is_favorite";
    public static final String IS_SUBSCRIBED = "is_subscribed";
    public static final String MAIN_ALBUM_ID = "main_album_id";
    public static final String CAN_UPLOAD_DOC = "can_upload_doc";
    public static final String CAN_CTARE_TOPIC = "can_upload_video";
    public static final String CAN_UPLOAD_VIDEO = "can_create_topic";
    public static final String BAN_INFO = "ban_info";
    /**
     * Filed city from VK fields set
     */
    public static final String CITY = "city";
    /**
     * Filed country from VK fields set
     */
    public static final String COUNTRY = "country";
    /**
     * Filed place from VK fields set
     */
    public static final String PLACE = "place";
    /**
     * Filed description from VK fields set
     */
    public static final String DESCRIPTION = "description";
    /**
     * Filed wiki_page from VK fields set
     */
    public static final String WIKI_PAGE = "wiki_page";
    /**
     * Filed members_count from VK fields set
     */
    public static final String MEMBERS_COUNT = "members_count";
    /**
     * Filed counters from VK fields set
     */
    public static final String COUNTERS = "counters";
    /**
     * Filed start_date from VK fields set
     */
    public static final String START_DATE = "start_date";
    /**
     * Filed end_date from VK fields set
     */
    public static final String FINISH_DATE = "finish_date";
    /**
     * Filed can_post from VK fields set
     */
    public static final String CAN_POST = "can_post";
    /**
     * Filed can_see_all_posts from VK fields set
     */
    public static final String CAN_SEE_ALL_POSTS = "can_see_all_posts";
    /**
     * Filed status from VK fields set
     */
    public static final String STATUS = "status";
    /**
     * Filed contacts from VK fields set
     */
    public static final String CONTACTS = "contacts";
    /**
     * Filed links from VK fields set
     */
    public static final String LINKS = "links";
    /**
     * Filed fixed_post from VK fields set
     */
    public static final String FIXED_POST = "fixed_post";
    /**
     * Filed verified from VK fields set
     */
    public static final String VERIFIED = "verified";
    /**
     * Filed blacklisted from VK fields set
     */
    public static final String BLACKLISTED = "blacklisted";
    /**
     * Filed site from VK fields set
     */
    public static final String SITE = "site";
    /**
     * Filed activity from VK fields set
     */
    public static final String ACTIVITY = "activity";
    /**
     * Community name
     */
    public String name;
    /**
     * Screen name of the community page (e.g. apiclub or club1).
     */
    public String screen_name;
    /**
     * Whether the community is closed
     *
     * @see {@link VKApiCommunity.Status}
     */
    public int is_closed;
    /**
     * Whether a user is the community manager
     */
    public boolean is_admin;
    /**
     * Rights of the user
     *
     * @see {@link VKApiCommunity.AdminLevel}
     */
    public int admin_level;
    /**
     * Whether a user is a community member
     */
    public boolean is_member;
    /**
     * статус участника текущего пользователя в сообществе:
     * 0 — не является участником;
     * 1 — является участником;
     * 2 — не уверен; возможно not_sure опциональный параметр, учитываемый, если group_id принадлежит встрече. 1 — Возможно пойду. 0 — Точно пойду. По умолчанию 0.
     * 3 — отклонил приглашение;
     * 4 — подал заявку на вступление;
     * 5 — приглашен.
     */
    public int member_status;
    /**
     * City specified in information about community.
     */
    public VKApiCity city;
    /**
     * Country specified in information about community.
     */
    public VKApiCountry country;
    /**
     * Audio which broadcasting to status.
     */
    public VKApiAudio status_audio;
    /**
     * The location which specified in information about community
     */
    public VKApiPlace place;
    /**
     * срок окончания блокировки в формате unixtime;
     */
    public long ban_end_date;
    /**
     * комментарий к блокировке.
     */
    public String ban_comment;
    /**
     * Community description text.
     */
    public String description;
    /**
     * Name of the home wiki-page of the community.
     */
    public String wiki_page;
    /**
     * Number of community members.
     */
    public int members_count;
    /**
     * Counters object with community counters.
     */
    public Counters counters;
    /**
     * Returned only for meeting and contain start time of the meeting as unixtime.
     */
    public long start_date;
    /**
     * Returned only for meeting and contain end time of the meeting as unixtime.
     */
    public long finish_date;
    /**
     * Whether the current user can post on the community's wall
     */
    public boolean can_post;
    /**
     * Whether others' posts on the community's wall can be viewed
     */
    public boolean can_see_all_posts;
    /**
     * Group status.
     */
    public String status;
    /**
     * Information from public page contact module.
     */
    public List<Contact> contacts;
    /**
     * Information from public page links module.
     */
    public List<Link> links;
    /**
     * ID of canDelete post of this community.
     */
    public int fixed_post;
    /**
     * идентификатор основного альбома сообщества.
     */
    public int main_album_id;
    /**
     * Information whether the community has a verified page in VK
     */
    public boolean verified;
    /**
     * информация о том, может ли текущий пользователь загружать документы в группу.
     */
    public boolean can_upload_doc;
    /**
     * информация о том, может ли текущий пользователь загружать видеозаписи в группу
     */
    public boolean can_upload_video;
    /**
     * информация о том, может ли текущий пользователь создать тему обсуждения в группе, используя метод board.addTopic
     */
    public boolean can_create_topic;
    /**
     * возвращается 1, если сообщество находится в закладках у текущего пользователя.
     */
    public boolean is_favorite;

    public boolean is_subscribed;
    /**
     * URL of community site
     */
    public String site;
    /**
     * строка состояния публичной страницы. У групп возвращается строковое значение, открыта ли группа или нет, а у событий дата начала.
     */
    public String activity;
    /**
     * Information whether the current community has add current user to the blacklist.
     */
    public boolean blacklisted;
    /**
     * информация о том, может ли текущий пользователь написать сообщение сообществу. Возможные значения:
     */
    public boolean can_message;

    public VkApiCover cover;
    /**
     * Community type
     *
     * @see {@link VKApiCommunity.Type}
     */
    public int type;
    /**
     * URL of the 50px-wide community logo.
     */
    public String photo_50;
    /**
     * URL of the 100px-wide community logo.
     */
    public String photo_100;
    /**
     * URL of the 200px-wide community logo.
     */
    public String photo_200;

    /**
     * Creates empty Community instance.
     */
    public VKApiCommunity() {
        super(VKApiOwner.Type.COMMUNITY);
    }

    public static VKApiCommunity create(int id) {
        VKApiCommunity community = new VKApiCommunity();
        community.id = id;
        return community;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public String getMaxSquareAvatar() {
        if (!TextUtils.isEmpty(photo_200)) {
            return photo_200;
        }

        if (!TextUtils.isEmpty(photo_100)) {
            return photo_100;
        }

        return photo_50;
    }

    @NonNull
    @Override
    public String toString() {
        return "id: " + id + ", name: '" + name + "'";
    }

    /**
     * VkApiPrivacy status of the group.
     */
    public static class MemberStatus {
        public static final int IS_NOT_MEMBER = 0;
        public static final int IS_MEMBER = 1;
        public static final int NOT_SURE = 2;
        public static final int DECLINED_INVITATION = 3;
        public static final int SENT_REQUEST = 4;
        public static final int INVITED = 5;

        private MemberStatus() {
        }
    }

    /**
     * Access level to manage community.
     */
    public static class AdminLevel {
        public static final int MODERATOR = 1;
        public static final int EDITOR = 2;
        public static final int ADMIN = 3;

        private AdminLevel() {
        }
    }

    /**
     * VkApiPrivacy status of the group.
     */
    public static class Status {
        public static final int OPEN = 0;
        public static final int CLOSED = 1;
        public static final int PRIVATE = 2;

        private Status() {
        }
    }

    /**
     * Types of communities.
     */
    public static class Type {
        public static final int GROUP = 0;
        public static final int PAGE = 1;
        public static final int EVENT = 2;

        private Type() {
        }
    }

    public static class Counters {

        /**
         * Значение в том случае, если счетчик не был явно указан.
         */
        public static final int NO_COUNTER = -1;

        public int photos = NO_COUNTER;
        public int albums = NO_COUNTER;
        public int audios = NO_COUNTER;
        public int videos = NO_COUNTER;
        public int topics = NO_COUNTER;
        public int docs = NO_COUNTER;
        public int articles = NO_COUNTER;
        public int market = NO_COUNTER;
        public int chats = NO_COUNTER;

        public int all_wall = NO_COUNTER;
        public int owner_wall = NO_COUNTER;
        public int suggest_wall = NO_COUNTER;
        public int postponed_wall = NO_COUNTER;

        public Counters() {

        }
    }

    public static final class Contact implements Identificable {

        public int user_id;
        public String email;
        public String phone;
        public String desc;

        public Contact() {

        }

        @Override
        public int getId() {
            return user_id;
        }
    }

    public static class Link {

        public int id;
        public String url;
        public String name;
        public String desc;
        public String photo_50;
        public String photo_100;
        public boolean edit_title;

        public Link() {

        }
    }
}
