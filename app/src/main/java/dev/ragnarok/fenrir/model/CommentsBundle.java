package dev.ragnarok.fenrir.model;

import java.util.List;


public class CommentsBundle {

    private final List<Comment> comments;

    private Integer firstCommentId;

    private Integer lastCommentId;

    private Integer adminLevel;

    private Poll topicPoll;

    public CommentsBundle(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public Integer getFirstCommentId() {
        return firstCommentId;
    }

    public CommentsBundle setFirstCommentId(Integer firstCommentId) {
        this.firstCommentId = firstCommentId;
        return this;
    }

    public Integer getLastCommentId() {
        return lastCommentId;
    }

    public CommentsBundle setLastCommentId(Integer lastCommentId) {
        this.lastCommentId = lastCommentId;
        return this;
    }

    public Integer getAdminLevel() {
        return adminLevel;
    }

    public CommentsBundle setAdminLevel(Integer adminLevel) {
        this.adminLevel = adminLevel;
        return this;
    }

    public Poll getTopicPoll() {
        return topicPoll;
    }

    public CommentsBundle setTopicPoll(Poll topicPoll) {
        this.topicPoll = topicPoll;
        return this;
    }
}
