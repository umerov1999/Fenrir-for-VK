package dev.ragnarok.fenrir.db.model.entity;


public class VideoAlbumEntity extends Entity {

    private final int id;

    private final int ownerId;

    private String title;

    private String image;

    private int count;

    private long updateTime;

    private PrivacyEntity privacy;

    public VideoAlbumEntity(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public VideoAlbumEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getImage() {
        return image;
    }

    public VideoAlbumEntity setImage(String image) {
        this.image = image;
        return this;
    }

    public int getCount() {
        return count;
    }

    public VideoAlbumEntity setCount(int count) {
        this.count = count;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public VideoAlbumEntity setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public PrivacyEntity getPrivacy() {
        return privacy;
    }

    public VideoAlbumEntity setPrivacy(PrivacyEntity privacy) {
        this.privacy = privacy;
        return this;
    }
}