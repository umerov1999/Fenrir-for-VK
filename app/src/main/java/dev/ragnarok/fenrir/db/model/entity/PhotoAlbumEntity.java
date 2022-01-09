package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class PhotoAlbumEntity extends Entity {

    private int id;

    private int ownerId;

    private int size;

    private String title;

    private String description;

    private boolean canUpload;

    private long updatedTime;

    private long createdTime;

    private PhotoSizeEntity sizes;

    private boolean uploadByAdminsOnly;

    private boolean commentsDisabled;

    private PrivacyEntity privacyView;

    private PrivacyEntity privacyComment;

    public PhotoAlbumEntity set(int id, int ownerId) {
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

    public int getSize() {
        return size;
    }

    public PhotoAlbumEntity setSize(int size) {
        this.size = size;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PhotoAlbumEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PhotoAlbumEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isCanUpload() {
        return canUpload;
    }

    public PhotoAlbumEntity setCanUpload(boolean canUpload) {
        this.canUpload = canUpload;
        return this;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public PhotoAlbumEntity setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public PhotoAlbumEntity setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public PhotoSizeEntity getSizes() {
        return sizes;
    }

    public PhotoAlbumEntity setSizes(PhotoSizeEntity sizes) {
        this.sizes = sizes;
        return this;
    }

    public boolean isUploadByAdminsOnly() {
        return uploadByAdminsOnly;
    }

    public PhotoAlbumEntity setUploadByAdminsOnly(boolean uploadByAdminsOnly) {
        this.uploadByAdminsOnly = uploadByAdminsOnly;
        return this;
    }

    public boolean isCommentsDisabled() {
        return commentsDisabled;
    }

    public PhotoAlbumEntity setCommentsDisabled(boolean commentsDisabled) {
        this.commentsDisabled = commentsDisabled;
        return this;
    }

    public PrivacyEntity getPrivacyView() {
        return privacyView;
    }

    public PhotoAlbumEntity setPrivacyView(PrivacyEntity privacyView) {
        this.privacyView = privacyView;
        return this;
    }

    public PrivacyEntity getPrivacyComment() {
        return privacyComment;
    }

    public PhotoAlbumEntity setPrivacyComment(PrivacyEntity privacyComment) {
        this.privacyComment = privacyComment;
        return this;
    }
}