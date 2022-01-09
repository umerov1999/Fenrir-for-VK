package dev.ragnarok.fenrir.db.model.entity;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class PostEntity extends Entity {

    private static final int NO_STORED = -1;
    private int id;
    private int ownerId;
    private int dbid = NO_STORED;
    private int fromId;

    private long date;

    private String text;

    private int replyOwnerId;

    private int replyPostId;

    private boolean friendsOnly;

    private int commentsCount;

    private boolean canPostComment;

    private int likesCount;

    private boolean userLikes;

    private boolean canLike;

    private boolean canEdit;

    private boolean canPublish;

    private int repostCount;

    private boolean userReposted;

    private int postType;

    private int attachmentsCount;

    private int signedId;

    private int createdBy;

    private boolean canPin;

    private boolean pinned;

    private boolean deleted;

    private int views;

    private SourceDbo source;

    private AttachmentsEntity attachments;

    private List<PostEntity> copyHierarchy;

    public PostEntity set(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        return this;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public PostEntity setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
        return this;
    }

    public int getDbid() {
        return dbid;
    }

    public PostEntity setDbid(int dbid) {
        this.dbid = dbid;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getFromId() {
        return fromId;
    }

    public PostEntity setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public PostEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public String getText() {
        return text;
    }

    public PostEntity setText(String text) {
        this.text = text;
        return this;
    }

    public int getReplyOwnerId() {
        return replyOwnerId;
    }

    public PostEntity setReplyOwnerId(int replyOwnerId) {
        this.replyOwnerId = replyOwnerId;
        return this;
    }

    public int getReplyPostId() {
        return replyPostId;
    }

    public PostEntity setReplyPostId(int replyPostId) {
        this.replyPostId = replyPostId;
        return this;
    }

    public boolean isFriendsOnly() {
        return friendsOnly;
    }

    public PostEntity setFriendsOnly(boolean friendsOnly) {
        this.friendsOnly = friendsOnly;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public PostEntity setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public boolean isCanPostComment() {
        return canPostComment;
    }

    public PostEntity setCanPostComment(boolean canPostComment) {
        this.canPostComment = canPostComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public PostEntity setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public PostEntity setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public PostEntity setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public PostEntity setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public PostEntity setRepostCount(int repostCount) {
        this.repostCount = repostCount;
        return this;
    }

    public boolean isUserReposted() {
        return userReposted;
    }

    public PostEntity setUserReposted(boolean userReposted) {
        this.userReposted = userReposted;
        return this;
    }

    public int getPostType() {
        return postType;
    }

    public PostEntity setPostType(int postType) {
        this.postType = postType;
        return this;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public PostEntity setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
        return this;
    }

    public int getSignedId() {
        return signedId;
    }

    public PostEntity setSignedId(int signedId) {
        this.signedId = signedId;
        return this;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public PostEntity setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public boolean isCanPin() {
        return canPin;
    }

    public PostEntity setCanPin(boolean canPin) {
        this.canPin = canPin;
        return this;
    }

    public boolean isPinned() {
        return pinned;
    }

    public PostEntity setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public PostEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getViews() {
        return views;
    }

    public PostEntity setViews(int views) {
        this.views = views;
        return this;
    }

    public SourceDbo getSource() {
        return source;
    }

    public PostEntity setSource(SourceDbo source) {
        this.source = source;
        return this;
    }

    @Nullable
    public List<Entity> getAttachments() {
        return nonNull(attachments) ? attachments.getEntities() : null;
    }

    public PostEntity setAttachments(@Nullable List<Entity> entities) {
        attachments = AttachmentsEntity.from(entities);
        return this;
    }

    public @Nullable
    List<PostEntity> getCopyHierarchy() {
        return copyHierarchy;
    }

    public PostEntity setCopyHierarchy(@Nullable List<PostEntity> copyHierarchy) {
        this.copyHierarchy = copyHierarchy;
        return this;
    }

    public static final class SourceDbo {
        @SerializedName("type")
        private int type;
        @SerializedName("platform")
        private String platform;
        @SerializedName("data")
        private int data;
        @SerializedName("url")
        private String url;

        public SourceDbo set(int type, String platform, int data, String url) {
            this.type = type;
            this.platform = platform;
            this.data = data;
            this.url = url;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public int getType() {
            return type;
        }

        public int getData() {
            return data;
        }

        public String getPlatform() {
            return platform;
        }
    }
}