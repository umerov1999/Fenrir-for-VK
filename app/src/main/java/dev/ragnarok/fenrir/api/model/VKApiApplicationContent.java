package dev.ragnarok.fenrir.api.model;

/**
 * Describes information about application in the post.
 */
public class VKApiApplicationContent implements VKApiAttachment {

    /**
     * ID of the application that posted on the wall;
     */
    public int id;

    /**
     * Application name
     */
    public String name;

    /**
     * Image URL for preview with maximum width in 130px
     */
    public String photo_130;

    /**
     * Image URL for preview with maximum width in 130px
     */
    public String photo_604;

    @Override
    public String getType() {
        return TYPE_APP;
    }
}