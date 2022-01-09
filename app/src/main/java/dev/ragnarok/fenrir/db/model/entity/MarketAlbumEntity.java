package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class MarketAlbumEntity extends Entity {
    private int id;
    private int owner_id;
    private String access_key;
    private String title;
    private PhotoEntity photo;
    private int count;
    private int updated_time;

    public MarketAlbumEntity set(int id, int owner_id) {
        this.id = id;
        this.owner_id = owner_id;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public String getAccess_key() {
        return access_key;
    }

    public MarketAlbumEntity setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MarketAlbumEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public PhotoEntity getPhoto() {
        return photo;
    }

    public MarketAlbumEntity setPhoto(PhotoEntity photo) {
        this.photo = photo;
        return this;
    }

    public int getCount() {
        return count;
    }

    public MarketAlbumEntity setCount(int count) {
        this.count = count;
        return this;
    }

    public int getUpdated_time() {
        return updated_time;
    }

    public MarketAlbumEntity setUpdated_time(int updated_time) {
        this.updated_time = updated_time;
        return this;
    }
}
