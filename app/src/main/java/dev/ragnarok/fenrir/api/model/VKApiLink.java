package dev.ragnarok.fenrir.api.model;

/**
 * A link object describes a link attachment
 */
public class VKApiLink implements VKApiAttachment {

    /**
     * Link URL
     */
    public String url;

    /**
     * Link title
     */
    public String title;

    /**
     * Link description;
     */
    public String description;

    /**
     * Image preview URL for the link (if any).
     */
    public VKApiPhoto photo;

    /**
     * ID wiki page with content for the preview of the page contents
     * ID is returned as "ownerid_pageid".
     */
    public String preview_page;

    /* адрес страницы для предпросмотра содержимого страницы. */
    public String preview_url;

    public String preview_photo;

    public String caption;

    public VKApiLink() {

    }

    @Override
    public String getType() {
        return VKApiAttachment.TYPE_LINK;
    }
}