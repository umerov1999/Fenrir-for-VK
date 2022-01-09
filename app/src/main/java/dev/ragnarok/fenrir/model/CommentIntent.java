package dev.ragnarok.fenrir.model;

import java.util.List;


public class CommentIntent {

    private final int authorId;
    private String message;
    private Integer replyToComment;
    private Integer draftMessageId;
    private Integer stickerId;

    private List<AbsModel> models;

    public CommentIntent(int authorId) {
        this.authorId = authorId;
    }

    public List<AbsModel> getModels() {
        return models;
    }

    public CommentIntent setModels(List<AbsModel> models) {
        this.models = models;
        return this;
    }

    public Integer getDraftMessageId() {
        return draftMessageId;
    }

    public CommentIntent setDraftMessageId(Integer draftMessageId) {
        this.draftMessageId = draftMessageId;
        return this;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Integer getReplyToComment() {
        return replyToComment;
    }

    public CommentIntent setReplyToComment(Integer replyToComment) {
        this.replyToComment = replyToComment;
        return this;
    }

    public Integer getStickerId() {
        return stickerId;
    }

    public CommentIntent setStickerId(Integer stickerId) {
        this.stickerId = stickerId;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public CommentIntent setMessage(String message) {
        this.message = message;
        return this;
    }
}