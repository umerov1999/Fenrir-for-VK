package dev.ragnarok.fenrir.api.model;

import java.util.List;

import dev.ragnarok.fenrir.api.util.VKStringUtils;

/**
 * User object describes a user profile.
 */
public class VKApiUser extends VKApiOwner {

    public static final int SEX_UNKNOWN = 0;
    public static final int SEX_MAN = 2;
    public static final int SEX_WOMAN = 1;

    public static final String CAMERA_50 = "http://vk.com/images/camera_c.gif";
    public static final int FRIEND_STATUS_IS_NOT_FRIEDND = 0;
    public static final int FRIEND_STATUS_REQUEST_SENT = 1;
    public static final int FRIEND_STATUS_HAS_INPUT_REQUEST = 2;
    public static final int FRIEND_STATUS_IS_FRIEDND = 3;
    public static final String ALL_FIELDS = "sex,bdate,city,country,photo_50,photo_100,photo_200_orig," +
            "photo_200,photo_400_orig,photo_max,photo_max_orig,photo_id,online,online_mobile,domain," +
            "has_mobile,contacts,connections,site,education,universities,schools,can_post,can_see_all_posts," +
            "can_see_audio,can_write_private_message,status,last_seen,common_count,relation,relatives," +
            "counters,screen_name,maiden_name,timezone,occupation,activities,interests,music,movies,tv," +
            "books,games,about,quotes,personal,friend_status,military,career,is_friend,blacklisted_by_me,blacklisted,can_access_closed,verified,is_favorite,is_subscribed";
    /**
     * First name of user.
     */
    public String first_name;
    /**
     * Last name of user.
     */
    public String last_name;
    /**
     * Last visit date(in Unix time).
     */
    public long last_seen;
    /**
     * Last visit paltform(in Unix time).
     * 1    mobile	Мобильная версия сайта или неопознанное мобильное приложение
     * 2	iphone	Официальное приложение для iPhone
     * 3	ipad	Официальное приложение для iPad
     * 4	android	Официальное приложение для Android
     * 5	wphone	Официальное приложение для Windows Phone
     * 6	windows	Официальное приложение для Windows 8
     * 7	web	    Полная версия сайта или неопознанное приложение
     */
    public int platform;
    /**
     * Information whether the user is online.
     */
    public boolean online;
    /**
     * If user utilizes a mobile application or site mobile version, it returns online_mobile as additional.
     */
    public boolean online_mobile;
    public int online_app;
    /**
     * URL of default square photo of the user with 50 pixels in width.
     */
    public String photo_50;
    /**
     * URL of default square photo of the user with 100 pixels in width.
     */
    public String photo_100;
    /**
     * URL of default square photo of the user with 200 pixels in width.
     */
    public String photo_200;
    /**
     * URL of default square photo of the user with max pixels in width.
     */
    public String photo_max_orig;
    /**
     * статус пользователя. Возвращается строка,
     * содержащая текст статуса, расположенного в профиле под именем пользователя.
     * Если у пользователя включена опция «Транслировать в статус играющую музыку»,
     * будет возвращено дополнительное поле status_audio,
     * содержащее информацию о транслируемой композиции.
     */
    public String status;
    /**
     * Text of user status.
     */
    public String activity;
    /**
     * Audio which broadcasting to status.
     */
    public VKApiAudio status_audio;
    /**
     * User's date of birth.  Returned as DD.MM.YYYY or DD.MM (if birth year is hidden).
     */
    public String bdate;
    /**
     * City specified on user's page in "Contacts" section.
     */
    public VKApiCity city;
    /**
     * Country specified on user's page in "Contacts" section.
     */
    public VKApiCountry country;
    /**
     * List of user's universities
     */
    public List<VKApiUniversity> universities;
    /**
     * List of user's schools
     */
    public List<VKApiSchool> schools;
    /**
     * List of user's schools
     */
    public List<VKApiMilitary> militaries;
    /**
     * List of user's schools
     */
    public List<VKApiCareer> careers;
    /**
     * Views on smoking.
     *
     * @see {@link VKApiUser.Attitude}
     */
    public int smoking;
    /**
     * Views on alcohol.
     *
     * @see {@link VKApiUser.Attitude}
     */
    public int alcohol;
    /**
     * Views on policy.
     *
     * @see {@link VKApiUser.Political}
     */
    public int political;
    /**
     * Life main stuffs.
     *
     * @see {@link VKApiUser.LifeMain}
     */
    public int life_main;
    /**
     * People main stuffs.
     *
     * @see {@link VKApiUser.PeopleMain}
     */
    public int people_main;
    /**
     * Stuffs that inspire the user.
     */
    public String inspired_by;
    /**
     * List of user's languages
     */
    public String[] langs;
    /**
     * Religion of user
     */
    public String religion;
    /**
     * Name of user's account in Facebook
     */
    public String facebook;
    /**
     * ID of user's facebook
     */
    public String facebook_name;
    /**
     * Name of user's account in LiveJournal
     */
    public String livejournal;
    /**
     * Name of user's account in Skype
     */
    public String skype;
    /**
     * URL of user's site
     */
    public String site;
    /**
     * Name of user's account in Twitter
     */
    public String twitter;
    /**
     * Name of user's account in Instagram
     */
    public String instagram;
    /**
     * User's mobile phone number
     */
    public String mobile_phone;
    /**
     * User's home phone number
     */
    public String home_phone;
    /**
     * Page screen name.
     */
    public String screen_name;
    /**
     * Nickname of user.
     */
    public String nickname;
    /**
     * User's activities
     */
    public String activities;
    /**
     * User's interests
     */
    public String interests;
    /**
     * User's favorite movies
     */
    public String movies;
    /**
     * User's favorite TV Shows
     */
    public String tv;
    /**
     * User's favorite books
     */
    public String books;
    /**
     * User's favorite games
     */
    public String games;
    /**
     * User's about information
     */
    public String about;
    /**
     * User's favorite quotes
     */
    public String quotes;
    /**
     * Information whether others can posts on user's wall.
     */
    public boolean can_post;
    /**
     * Information whether others' posts on user's wall can be viewed
     */
    public boolean can_see_all_posts;
    /**
     * Information whether private messages can be sent to this user.
     */
    public boolean can_write_private_message;

    public boolean can_access_closed;

    /**
     * Information whether user can comment wall posts.
     */
    public boolean wall_comments;
    /**
     * Information whether the user is banned in VK.
     */
    public boolean is_banned;
    /**
     * Information whether the user is deleted in VK.
     */
    public boolean is_deleted;
    /**
     * Information whether the user's post of wall shows by default.
     */
    public boolean wall_default_owner;
    /**
     * Information whether the user has a verified page in VK
     */
    public boolean verified;
    /**
     * User sex.
     *
     * @see {@link VKApiUser.Sex}
     */
    public int sex;
    /**
     * Set of user's counters.
     */
    public Counters counters;
    /**
     * Relationship status.
     *
     * @see {@link VKApiUser.Relation}
     */
    public int relation;
    /**
     * List of user's relatives
     */
    public List<Relative> relatives;
    /**
     * Information whether the current user has add this user to the blacklist.
     */
    public boolean blacklisted_by_me;
    /**
     * короткий адрес страницы.
     * Возвращается строка, содержащая короткий адрес страницы
     * (возвращается только сам поддомен, например, andrew).
     * Если он не назначен, возвращается "id"+uid, например, id35828305.
     */
    public String domain;
    public String home_town;
    /**
     * id главной фотографии профиля пользователя в формате user_id+photo_id,
     * например, 6492_192164258. В некоторых случаях (если фотография была установлена очень давно)
     * это поле не возвращается.
     */
    public String photo_id;
    /**
     * возвращается 1, если текущий пользователь находится в черном списке у запрашиваемого.
     */
    public boolean blacklisted;
    /**
     * url фотографии пользователя, имеющей ширину 200 пикселей.
     * В случае отсутствия у пользователя фотографии возвращается http://vk.com/images/camera_a.gif.
     */
    public String photo_200_orig;
    /**
     * url фотографии пользователя, имеющей ширину 400 пикселей.
     * Если у пользователя отсутствует фотография такого размера, ответ не будет содержать этого поля.
     */
    public String photo_400_orig;
    /**
     * url квадратной фотографии пользователя с максимальной шириной.
     * Может быть возвращена фотография, имеющая ширину как 200, так и 100 пикселей.
     * В случае отсутствия у пользователя фотографии возвращается http://vk.com/images/camera_b.gif.
     */
    public String photo_max;
    /**
     * информация о том, известен ли номер мобильного телефона пользователя.
     * Возвращаемые значения: 1 — известен, 0 — не известен.
     * Рекомендуется использовать перед вызовом метода secure.sendSMSNotification.
     */
    public boolean has_mobile;
    /**
     * информация о текущем роде занятия пользователя. Возвращаются
     */
    public Occupation occupation;
    /**
     * Если в семейном положении указан другой пользователь,
     * дополнительно возвращается объект relation_partner, содержащий id и имя этого человека.
     */
    public VKApiUser relation_partner;
    /**
     * любимая музыка.
     */
    public String music;
    /**
     * информация о том, разрешено ли видеть чужие аудиозаписи на стене пользователя.
     * Возвращаемые значения: 1 —разрешено, 0 — не разрешено.
     */
    public boolean can_see_audio;
    /**
     * информация о том, будет ли отправлено уведомление пользователю о заявке в друзья.
     * Возвращаемые значения: 1 — уведомление будет отправлено, 0 — уведомление не будет оптравлено.
     */
    public boolean can_send_friend_request;
    /**
     * возвращается 1, если пользователь находится в закладках у текущего пользователя.
     */
    public boolean is_favorite;

    public boolean is_subscribed;
    /**
     * временная зона пользователя. Возвращается только при запросе информации о текущем пользователе.
     */
    public int timezone;
    /**
     * девичья фамилия.
     */
    public String maiden_name;
    /**
     * 1 – пользователь друг, 2 – пользователь не в друзьях.
     */
    public boolean is_friend;
    /**
     * статус дружбы с пользователем:
     * 0 – пользователь не является другом,
     * 1 – отправлена заявка/подписка пользователю,
     * 2 – имеется входящая заявка/подписка от пользователя,
     * 3 – пользователь является другом;
     */
    public int friend_status;
    /**
     * Содержит уровень полномочий руководителя сообщества
     */
    public String role;

    /**
     * Creates empty User instance.
     */
    public VKApiUser() {
        super(Type.USER);
    }

    public VKApiUser(int id) {
        this();
        this.id = id;
    }

    public static VKApiUser create(int id) {
        VKApiUser user = new VKApiUser();
        user.id = id;
        return user;
    }

    @Override
    public String getMaxSquareAvatar() {
        if (photo_200 != null && photo_200.length() > 0) {
            return photo_200;
        } else if (photo_100 != null && photo_100.length() > 0) {
            return photo_100;
        } else {
            return photo_50;
        }
    }

    @Override
    public String getFullName() {
        return VKStringUtils.isEmpty(first_name) && VKStringUtils.isEmpty(last_name)
                ? "[id" + id + "]" : first_name + " " + last_name;
    }

    public static final class Field {

        /**
         * Field name for {@link #online} param.
         */
        public static final String ONLINE = "online";

        /**
         * Field name for {@link #online_mobile} param.
         */
        public static final String ONLINE_MOBILE = "online_mobile";

        /**
         * Field name for {@link #photo_50} param.
         */
        public static final String PHOTO_50 = "photo_50";

        /**
         * Field name for {@link #photo_100} param.
         */
        public static final String PHOTO_100 = "photo_100";

        /**
         * Field name for {@link #photo_200} param.
         */
        public static final String PHOTO_200 = "photo_200";

        /**
         * Filed last_seen from VK fields set
         */
        public static final String LAST_SEEN = "last_seen";

        /**
         * Filed photo_max_orig
         */
        public static final String PHOTO_MAX_ORIG = "photo_max_orig";

        /**
         * Filed photo_max_orig
         */
        public static final String STATUS = "status";

        /**
         * Filed bdate from VK fields set
         */
        public static final String BDATE = "bdate";

        /**
         * Filed city from VK fields set
         */
        public static final String CITY = "city";

        /**
         * Filed country from VK fields set
         */
        public static final String COUNTRY = "country";

        /**
         * Filed universities from VK fields set
         */
        public static final String UNIVERSITIES = "universities";

        /**
         * Filed schools from VK fields set
         */
        public static final String SCHOOLS = "schools";

        /**
         * Filed military from VK fields set
         */
        public static final String MILITARY = "military";

        /**
         * Filed military from VK fields set
         */
        public static final String CAREER = "career";

        /**
         * Filed activity from VK fields set
         */
        public static final String ACTIVITY = "activity";

        /**
         * Filed personal from VK fields set
         */
        public static final String PERSONAL = "personal";

        /**
         * Filed sex from VK fields set
         */
        public static final String SEX = "sex";

        /**
         * Filed site from VK fields set
         */
        public static final String SITE = "site";

        /**
         * Filed contacts from VK fields set
         */
        public static final String CONTACTS = "contacts";

        /**
         * Filed can_post from VK fields set
         */
        public static final String CAN_POST = "can_post";

        /**
         * Filed can_see_all_posts from VK fields set
         */
        public static final String CAN_SEE_ALL_POSTS = "can_see_all_posts";

        /**
         * Filed can_write_private_message from VK fields set
         */
        public static final String CAN_WRITE_PRIVATE_MESSAGE = "can_write_private_message";

        /**
         * Filed relation from VK fields set
         */
        public static final String RELATION = "relation";

        /**
         * Filed counters from VK fields set
         */
        public static final String COUNTERS = "counters";

        /**
         * Filed activities from VK fields set
         */
        public static final String ACTIVITIES = "activities";

        /**
         * Filed interests from VK fields set
         */
        public static final String INTERESTS = "interests";

        /**
         * Filed movies from VK fields set
         */
        public static final String MOVIES = "movies";

        /**
         * Filed tv from VK fields set
         */
        public static final String TV = "tv";

        /**
         * Filed books from VK fields set
         */
        public static final String BOOKS = "books";

        /**
         * Filed games from VK fields set
         */
        public static final String GAMES = "games";

        /**
         * Filed about from VK fields set
         */
        public static final String ABOUT = "about";

        /**
         * Filed quotes from VK fields set
         */
        public static final String QUOTES = "quotes";

        /**
         * Filed connections from VK fields set
         */
        public static final String CONNECTIONS = "connections";

        /**
         * Filed relatives from VK fields set
         */
        public static final String RELATIVES = "relatives";

        /**
         * Filed wall_default from VK fields set
         */
        public static final String WALL_DEFAULT = "wall_default";

        /**
         * Filed verified from VK fields set
         */
        public static final String VERIFIED = "verified";

        /**
         * Filed screen_name from VK fields set
         */
        public static final String SCREEN_NAME = "screen_name";

        /**
         * Filed blacklisted_by_me from VK fields set
         */
        public static final String BLACKLISTED_BY_ME = "blacklisted_by_me";

        /**
         * Filed blacklisted_by_me from VK fields set
         */
        public static final String DOMAIN = "domain";

        /**
         * Filed blacklisted_by_me from VK fields set
         */
        public static final String HOME_TOWN = "home_town";
    }

    public static class Platform {

        public static final int MOBILE = 1;
        public static final int IPHONE = 2;
        public static final int IPAD = 3;
        public static final int ANDROID = 4;
        public static final int WPHONE = 5;
        public static final int WINDOWS = 6;
        public static final int WEB = 7;

        private Platform() {
        }
    }

    public static class Occupation {

        /**
         * может принимать значения work, school, university
         */
        public String type;
        /**
         * идентификатор школы, вуза, группы компании (в которой пользователь работает);
         */
        public int id;
        /**
         * название школы, вуза или места работы;
         */
        public String name;
    }

    public static class Counters {

        /**
         * Count was not in server response.
         */
        public static final int NO_COUNTER = -1;

        public int albums = NO_COUNTER;
        public int videos = NO_COUNTER;
        public int audios = NO_COUNTER;
        public int notes = NO_COUNTER;
        public int friends = NO_COUNTER;
        public int photos = NO_COUNTER;
        public int groups = NO_COUNTER;
        public int online_friends = NO_COUNTER;
        public int mutual_friends = NO_COUNTER;
        public int user_videos = NO_COUNTER;
        public int user_photos = NO_COUNTER;
        public int followers = NO_COUNTER;
        public int subscriptions = NO_COUNTER;
        public int pages = NO_COUNTER;
        public int all_wall = NO_COUNTER;
        public int owner_wall = NO_COUNTER;
        public int postponed_wall = NO_COUNTER;
        public int articles = NO_COUNTER;
        public int market = NO_COUNTER;
        public int gifts = NO_COUNTER;

        public Counters() {

        }
    }

    public static class Sex {
        public static final int FEMALE = 1;
        public static final int MALE = 2;

        private Sex() {
        }
    }

    public static class Relation {
        public static final int SINGLE = 1;
        public static final int RELATIONSHIP = 2;
        public static final int ENGAGED = 3;
        public static final int MARRIED = 4;
        public static final int COMPLICATED = 5;
        public static final int SEARCHING = 6;
        public static final int IN_LOVE = 7;
        public static final int IN_A_CIVIL_UNION = 8;

        private Relation() {
        }
    }

    public static class Attitude {
        public static final int VERY_NEGATIVE = 1;
        public static final int NEGATIVE = 2;
        public static final int COMPROMISABLE = 3;
        public static final int NEUTRAL = 4;
        public static final int POSITIVE = 5;

        private Attitude() {
        }
    }

    public static class Political {
        public static final int COMMUNNIST = 1;
        public static final int SOCIALIST = 2;
        public static final int CENTRIST = 3;
        public static final int LIBERAL = 4;
        public static final int CONSERVATIVE = 5;
        public static final int MONARCHIST = 6;
        public static final int ULTRACONSERVATIVE = 7;
        public static final int LIBERTARIAN = 8;
        public static final int APATHETIC = 9;

        private Political() {
        }
    }

    public static class LifeMain {
        public static final int FAMILY_AND_CHILDREN = 1;
        public static final int CAREER_AND_MONEY = 2;
        public static final int ENTERTAINMENT_AND_LEISURE = 3;
        public static final int SCIENCE_AND_RESEARCH = 4;
        public static final int IMPROOVING_THE_WORLD = 5;
        public static final int PERSONAL_DEVELOPMENT = 6;
        public static final int BEAUTY_AND_ART = 7;
        public static final int FAME_AND_INFLUENCE = 8;

        private LifeMain() {
        }
    }

    public static class PeopleMain {
        public static final int INTELLECT_AND_CREATIVITY = 1;
        public static final int KINDNESS_AND_HONESTLY = 2;
        public static final int HEALTH_AND_BEAUTY = 3;
        public static final int WEALTH_AND_POWER = 4;
        public static final int COURAGE_AND_PERSISTENCE = 5;
        public static final int HUMOR_AND_LOVE_FOR_LIFE = 6;

        private PeopleMain() {
        }
    }

    public static class RelativeType {
        public static final String PARTNER = "partner";
        public static final String GRANDCHILD = "grandchild";
        public static final String GRANDPARENT = "grandparent";
        public static final String CHILD = "child";
        public static final String SUBLING = "sibling";
        public static final String PARENT = "parent";

        private RelativeType() {
        }
    }

    public static class Relative {

        public String type;
        public int id;
        public String name;

        public Relative() {
        }
    }
}
