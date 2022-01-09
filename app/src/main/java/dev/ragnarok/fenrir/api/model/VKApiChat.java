package dev.ragnarok.fenrir.api.model;

import java.util.List;

/**
 * Chat object describes a user's chat.
 */
public class VKApiChat {

    /**
     * Chat ID, positive number.
     */
    public int id;

    /**
     * Type of chat.
     */
    public String type;

    /**
     * Chat title.
     */
    public String title;

    /**
     * ID of the chat starter, positive number
     */
    public int admin_id;

    /**
     * List of chat participants' IDs.
     */
    public List<ChatUserDto> users;

    public String photo_50;

    public String photo_100;

    public String photo_200;

    /**
     * Creates empty Chat instance.
     */
    public VKApiChat() {

    }
}