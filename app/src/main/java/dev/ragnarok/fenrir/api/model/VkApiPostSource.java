package dev.ragnarok.fenrir.api.model;

public class VkApiPostSource {

    /**
     * На данный момент поддерживаются следующие типы источников записи на стене, значение которых указываются в поле type:
     * vk — запись создана через основной интерфейс сайта (http://vk.com/);
     * widget — запись создана через виджет на стороннем сайте;
     * api — запись создана приложением через API;
     * rss— запись создана посредством импорта RSS-ленты со стороннего сайта;
     * sms — запись создана посредством отправки SMS-сообщения на специальный номер.
     */
    public int type;

    /**
     * может содержать название платформы, если оно доступно: android, iphone, wphone
     */
    public String platform;

    /**
     * Поле data является опциональным и содержит следующие данные в зависимости от значения поля type:
     * vk — содержит тип действия, из-за которого была создана запись:
     * profile_activity — изменение статуса под именем пользователя;
     * profile_photo — изменение профильной фотографии пользователя;
     * widget — содержит тип виджета, через который была создана запись:
     * comments — виджет комментариев;
     * like — виджет «Мне нравится»;
     * poll — виджет опросов;
     */
    public int data;
    /**
     * является опциональным и может содержать внешнюю ссылку на ресурс, с которого была опубликована запись.
     */
    public String url;

    public static class Type {

        public static final int VK = 1;
        public static final int WIDGET = 2;
        public static final int API = 3;
        public static final int RSS = 4;
        public static final int SMS = 5;

        public static int parse(String original) {
            if ("vk".equals(original)) {
                return VK;
            } else if ("widget".equals(original)) {
                return WIDGET;
            } else if ("api".equals(original)) {
                return API;
            } else if ("rss".equals(original)) {
                return RSS;
            } else if ("sms".equals(original)) {
                return SMS;
            } else {
                return 0;
            }
        }
    }

    public static class Data {

        public static final int VK = 1;
        public static final int PROFILE_ACTIVITY = 2;
        public static final int PROFILE_PHOTO = 3;
        public static final int WIDGET = 4;
        public static final int COMMENTS = 5;
        public static final int LIKE = 6;
        public static final int POLL = 7;

        public static int parse(String original) {
            if ("vk".equals(original)) {
                return VK;
            } else if ("profile_activity".equals(original)) {
                return PROFILE_ACTIVITY;
            } else if ("profile_photo".equals(original)) {
                return PROFILE_PHOTO;
            } else if ("widget".equals(original)) {
                return WIDGET;
            } else if ("like".equals(original)) {
                return LIKE;
            } else if ("poll".equals(original)) {
                return POLL;
            } else {
                return 0;
            }
        }
    }
}