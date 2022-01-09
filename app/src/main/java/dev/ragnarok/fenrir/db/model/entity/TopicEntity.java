package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class TopicEntity extends Entity {

    private int id;

    private int ownerId;

    private String title;

    private long createdTime;

    private int creatorId;

    private long lastUpdateTime;

    private int updatedBy;

    private boolean closed;

    private boolean fixed;

    private int commentsCount;

    private String firstComment;

    private String lastComment;

    private PollEntity poll;

    public TopicEntity set(int id, int ownerId) {
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

    public String getTitle() {
        return title;
    }

    public TopicEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public TopicEntity setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public TopicEntity setCreatorId(int creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public TopicEntity setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public int getUpdatedBy() {
        return updatedBy;
    }

    public TopicEntity setUpdatedBy(int updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public boolean isClosed() {
        return closed;
    }

    public TopicEntity setClosed(boolean closed) {
        this.closed = closed;
        return this;
    }

    public boolean isFixed() {
        return fixed;
    }

    public TopicEntity setFixed(boolean fixed) {
        this.fixed = fixed;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public TopicEntity setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public String getFirstComment() {
        return firstComment;
    }

    public TopicEntity setFirstComment(String firstComment) {
        this.firstComment = firstComment;
        return this;
    }

    public String getLastComment() {
        return lastComment;
    }

    public TopicEntity setLastComment(String lastComment) {
        this.lastComment = lastComment;
        return this;
    }

    public PollEntity getPoll() {
        return poll;
    }

    public TopicEntity setPoll(PollEntity poll) {
        this.poll = poll;
        return this;
    }
}