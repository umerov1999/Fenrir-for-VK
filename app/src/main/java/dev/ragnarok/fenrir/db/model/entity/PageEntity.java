package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class PageEntity extends Entity {

    private int id;

    private int ownerId;

    private int creatorId;

    private String title;

    private String source;

    private long editionTime;

    private long creationTime;

    private String parent;

    private String parent2;

    private int views;

    private String viewUrl;

    public PageEntity set(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public PageEntity setCreatorId(int creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PageEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSource() {
        return source;
    }

    public PageEntity setSource(String source) {
        this.source = source;
        return this;
    }

    public long getEditionTime() {
        return editionTime;
    }

    public PageEntity setEditionTime(long editionTime) {
        this.editionTime = editionTime;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public PageEntity setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public PageEntity setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getParent2() {
        return parent2;
    }

    public PageEntity setParent2(String parent2) {
        this.parent2 = parent2;
        return this;
    }

    public int getViews() {
        return views;
    }

    public PageEntity setViews(int views) {
        this.views = views;
        return this;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public PageEntity setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
        return this;
    }
}