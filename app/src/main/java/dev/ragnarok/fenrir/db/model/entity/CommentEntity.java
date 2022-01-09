package dev.ragnarok.fenrir.db.model.entity;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.model.CommentedType;


public class CommentEntity {

    @SerializedName("sourceId")
    private int sourceId;
    @SerializedName("sourceOwnerId")
    private int sourceOwnerId;
    @SerializedName("sourceType")
    private @CommentedType
    int sourceType;
    @SerializedName("sourceAccessKey")
    private String sourceAccessKey;
    @SerializedName("id")
    private int id;
    @SerializedName("fromId")
    private int fromId;
    @SerializedName("date")
    private long date;
    @SerializedName("text")
    private String text;
    @SerializedName("replyToUserId")
    private int replyToUserId;
    @SerializedName("replyToComment")
    private int replyToComment;
    @SerializedName("likesCount")
    private int likesCount;
    @SerializedName("userLikes")
    private boolean userLikes;
    @SerializedName("canLike")
    private boolean canLike;
    @SerializedName("canEdit")
    private boolean canEdit;
    @SerializedName("deleted")
    private boolean deleted;
    @SerializedName("attachmentsCount")
    private int attachmentsCount;
    @SerializedName("threads_count")
    private int threads_count;
    @SerializedName("pid")
    private int pid;
    @SerializedName("attachments")
    private AttachmentsEntity attachments;
    @SerializedName("threads")
    private List<CommentEntity> threads;

    public CommentEntity set(int sourceId, int sourceOwnerId, @CommentedType int sourceType, String sourceAccessKey, int id) {
        this.sourceId = sourceId;
        this.sourceOwnerId = sourceOwnerId;
        this.sourceType = sourceType;
        this.id = id;
        this.sourceAccessKey = sourceAccessKey;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getFromId() {
        return fromId;
    }

    public CommentEntity setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public List<CommentEntity> getThreads() {
        return threads;
    }

    public CommentEntity setThreads(List<CommentEntity> threads) {
        this.threads = threads;
        return this;
    }

    public int getThreadsCount() {
        return threads_count;
    }

    public CommentEntity setThreadsCount(int threads_count) {
        this.threads_count = threads_count;
        return this;
    }

    public long getDate() {
        return date;
    }

    public CommentEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public String getText() {
        return text;
    }

    public CommentEntity setText(String text) {
        this.text = text;
        return this;
    }

    public int getReplyToUserId() {
        return replyToUserId;
    }

    public CommentEntity setReplyToUserId(int replyToUserId) {
        this.replyToUserId = replyToUserId;
        return this;
    }

    public int getReplyToComment() {
        return replyToComment;
    }

    public CommentEntity setReplyToComment(int replyToComment) {
        this.replyToComment = replyToComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public CommentEntity setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public CommentEntity setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public CommentEntity setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public CommentEntity setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public CommentEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public CommentEntity setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
        return this;
    }

    public @Nullable
    List<Entity> getAttachments() {
        return nonNull(attachments) ? attachments.getEntities() : null;
    }

    public CommentEntity setAttachments(@Nullable List<Entity> entities) {
        attachments = AttachmentsEntity.from(entities);
        return this;
    }

    public int getPid() {
        return pid;
    }

    public CommentEntity setPid(int pid) {
        this.pid = pid;
        return this;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getSourceOwnerId() {
        return sourceOwnerId;
    }

    public @CommentedType
    int getSourceType() {
        return sourceType;
    }

    public String getSourceAccessKey() {
        return sourceAccessKey;
    }
}