package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class LinkEntity extends Entity {

    private String url;

    private String title;

    private String caption;

    private String description;

    private String preview_photo;

    private PhotoEntity photo;

    public String getUrl() {
        return url;
    }

    public LinkEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public LinkEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getCaption() {
        return caption;
    }

    public LinkEntity setCaption(String caption) {
        this.caption = caption;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public LinkEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public PhotoEntity getPhoto() {
        return photo;
    }

    public LinkEntity setPhoto(PhotoEntity photo) {
        this.photo = photo;
        return this;
    }

    public String getPreviewPhoto() {
        return preview_photo;
    }

    public LinkEntity setPreviewPhoto(String photo) {
        preview_photo = photo;
        return this;
    }
}