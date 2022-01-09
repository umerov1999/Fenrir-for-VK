package dev.ragnarok.fenrir.model.feedback;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({FeedbackType.NULL, FeedbackType.FOLLOW, FeedbackType.FRIEND_ACCEPTED, FeedbackType.MENTION, FeedbackType.MENTION_COMMENT_POST,
        FeedbackType.WALL, FeedbackType.WALL_PUBLISH, FeedbackType.COMMENT_POST, FeedbackType.COMMENT_PHOTO,
        FeedbackType.COMMENT_VIDEO, FeedbackType.REPLY_COMMENT, FeedbackType.REPLY_COMMENT_PHOTO,
        FeedbackType.REPLY_COMMENT_VIDEO, FeedbackType.REPLY_TOPIC, FeedbackType.LIKE_POST,
        FeedbackType.LIKE_COMMENT_POST, FeedbackType.LIKE_PHOTO, FeedbackType.LIKE_VIDEO, FeedbackType.LIKE_COMMENT_PHOTO,
        FeedbackType.LIKE_COMMENT_VIDEO, FeedbackType.LIKE_COMMENT_TOPIC, FeedbackType.COPY_POST,
        FeedbackType.COPY_PHOTO, FeedbackType.COPY_VIDEO, FeedbackType.MENTION_COMMENT_PHOTO, FeedbackType.MENTION_COMMENT_VIDEO})
@Retention(RetentionPolicy.SOURCE)
public @interface FeedbackType {
    int NULL = 0;
    /**
     * У пользователя появился один или несколько новых подписчиков
     */
    int FOLLOW = 1;

    /**
     * Заявка в друзья, отправленная пользователем, была принята
     */
    int FRIEND_ACCEPTED = 2;

    /**
     * Была создана запись на чужой стене, содержащая упоминание пользователя
     */
    int MENTION = 3;

    /**
     * Был оставлен комментарий, содержащий упоминание пользователя
     */
    int MENTION_COMMENT_POST = 4;

    /**
     * Была добавлена запись на стене пользователя
     */
    int WALL = 5;

    /**
     * Была опубликована новость, предложенная пользователем в публичной странице
     */
    int WALL_PUBLISH = 6;

    /**
     * Был добавлен новый комментарий к записи, созданной пользователем
     */
    int COMMENT_POST = 7;

    /**
     * Был добавлен новый комментарий к фотографии пользователя
     */
    int COMMENT_PHOTO = 8;

    /**
     * Был добавлен новый комментарий к видеозаписи пользователя
     */
    int COMMENT_VIDEO = 9;

    /**
     * Был добавлен новый ответ на комментарий пользователя
     */
    int REPLY_COMMENT = 10;

    /**
     * Был добавлен новый ответ на комментарий пользователя к фотографии
     */
    int REPLY_COMMENT_PHOTO = 11;

    /**
     * Был добавлен новый ответ на комментарий пользователя к видеозаписи
     */
    int REPLY_COMMENT_VIDEO = 12;

    /**
     * Был добавлен новый ответ пользователю в обсуждении
     */
    int REPLY_TOPIC = 13;

    /**
     * У записи пользователя появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_POST = 14;

    /**
     * У комментария пользователя появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_COMMENT_POST = 15;

    /**
     * У фотографии пользователя появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_PHOTO = 16;

    /**
     * У видеозаписи пользователя появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_VIDEO = 17;

    /**
     * У комментария пользователя к фотографии появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_COMMENT_PHOTO = 18;

    /**
     * У комментария пользователя к видеозаписи появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_COMMENT_VIDEO = 19;

    /**
     * У комментария пользователя в обсуждении появилась одна или несколько новых отметок «Мне нравится»
     */
    int LIKE_COMMENT_TOPIC = 20;

    /**
     * Один или несколько пользователей скопировали запись пользователя
     */
    int COPY_POST = 21;

    /**
     * Один или несколько пользователей скопировали фотографию пользователя
     */
    int COPY_PHOTO = 22;

    /**
     * Один или несколько пользователей скопировали видеозапись пользователя
     */
    int COPY_VIDEO = 23;

    /**
     * Под фотографией был оставлен комментарий, содержащий упоминание пользователя
     */
    int MENTION_COMMENT_PHOTO = 24;

    /**
     * Под видео был оставлен комментарий, содержащий упоминание пользователя
     */
    int MENTION_COMMENT_VIDEO = 25;
}