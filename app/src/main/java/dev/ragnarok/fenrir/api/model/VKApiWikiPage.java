package dev.ragnarok.fenrir.api.model;

/**
 * Describes a Wiki page.
 */
public class VKApiWikiPage implements VKApiAttachment {

    /**
     * Wiki page ID;
     */
    public int id;

    /**
     * ID of the group the wiki page belongs to;
     */
    public int owner_id;

    /**
     * ID of the page creator.
     */
    public int creator_id;

    /**
     * Wiki page name.
     */
    public String title;

    /**
     * Text of the wiki page.
     */
    public String source;

    /**
     * Whether a user can edit page text (false — cannot, true — can).
     */
    public boolean current_user_can_edit;

    /**
     * Whether a user can edit page access permissions (false — cannot, true — can).
     */
    public boolean current_user_can_edit_access;

    /**
     * Who can view the wiki page(0 — only community managers, 1 — only community members, 2 — all users).
     */
    public int who_can_view;

    /**
     * Who can edit the wiki page (0 — only community managers, 1 — only community members, 2 — all users).
     */
    public int who_can_edit;

    /**
     * ID of the last user who edited the page.
     */
    public int editor_id;

    /**
     * Date of the last change.
     */
    public long edited;

    /**
     * Page creation date.
     */
    public long created;

    /**
     * Title of the parent page for navigation, if any.
     */
    public String parent;

    /**
     * Title of the second parent page for navigation, if any.
     */
    public String parent2;

    public int views;

    public String html;

    public String view_url;

    @Override
    public String getType() {
        return TYPE_WIKI_PAGE;
    }
}