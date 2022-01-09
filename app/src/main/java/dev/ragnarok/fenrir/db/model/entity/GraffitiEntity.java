package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class GraffitiEntity extends Entity {
    private int id;
    private int owner_id;
    private String url;
    private int width;
    private int height;
    private String access_key;

    public int getId() {
        return id;
    }

    public GraffitiEntity setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwner_id() {
        return owner_id;
    }

    public GraffitiEntity setOwner_id(int owner_id) {
        this.owner_id = owner_id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public GraffitiEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public GraffitiEntity setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public GraffitiEntity setHeight(int height) {
        this.height = height;
        return this;
    }

    public String getAccess_key() {
        return access_key;
    }

    public GraffitiEntity setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }
}
