package dev.ragnarok.fenrir.api.model;

import java.util.List;

/**
 * Describes poll on the wall on board.
 */
@SuppressWarnings("unused")
public class VKApiPoll implements VKApiAttachment {

    /**
     * Poll ID to get information about it using polls.getById method;
     */
    public int id;

    /**
     * ID of the user or community that owns this poll.
     */
    public int owner_id;

    /**
     * Date (in Unix time) the poll was created.
     */
    public long created;

    /**
     * Question in the poll.
     */
    public String question;

    /**
     * The total number of users answered.
     */
    public int votes;

    /**
     * Response ID of the current user(if the current user has not yet posted in this poll, it contains 0)
     */
    public int[] answer_ids;

    /* возвращется для анонимных опросов. */
    public boolean anonymous;

    /**
     * Array of answers for this question.
     */
    public List<Answer> answers;

    /**
     * true – опрос находится в обсуждении,
     * false – опрос прикреплен к стене.
     */
    public boolean is_board;

    public boolean closed;

    public int author_id;

    public boolean can_vote;

    public boolean can_edit;

    public boolean can_report;

    public boolean can_share;

    public long end_date;

    public boolean multiple;

    public Photo photo;


    /**
     * Creates empty Country instance.
     */
    public VKApiPoll() {

    }

    @Override
    public String getType() {
        return TYPE_POLL;
    }

    /**
     * Represents answer for the poll
     */
    public static final class Answer {

        /**
         * ID of the answer for the question
         */
        public int id;

        /**
         * Text of the answer
         */
        public String text;

        /**
         * Number of users that voted for this answer
         */
        public int votes;

        /**
         * Rate of this answer in percent
         */
        public double rate;
    }

    public static final class Photo {
        public String color;
        public int id;
        public List<Image> images;
    }

    public static final class Image {
        public int width;
        public int height;
        public String url;
    }
}