package dev.ragnarok.fenrir.db.model.entity;

public class FaveLinkEntity {

    private final String id;

    private final String url;

    private String title;

    private String description;

    private PhotoEntity photo;

    public FaveLinkEntity(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public FaveLinkEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public FaveLinkEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public PhotoEntity getPhoto() {
        return photo;
    }

    public FaveLinkEntity setPhoto(PhotoEntity photo) {
        this.photo = photo;
        return this;
    }
}