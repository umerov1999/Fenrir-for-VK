package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Nullable;

import java.util.List;


public class NewsEntity extends Entity {

    private String type;

    private int sourceId;

    private long date;

    private int postId;

    private String postType;

    private boolean finalPost;

    private int copyOwnerId;

    private int copyPostId;

    private long copyPostDate;

    private String text;

    private boolean canEdit;

    private boolean canDelete;

    private int commentCount;

    private boolean canPostComment;

    private int likesCount;

    private boolean userLikes;

    private boolean canLike;

    private boolean canPublish;

    private int repostCount;

    private boolean userReposted;

    private int geoId;

    private List<Integer> friendsTags;

    private int views;

    private List<Entity> attachments;

    private List<PostEntity> copyHistory;

    public String getType() {
        return type;
    }

    public NewsEntity setType(String type) {
        this.type = type;
        return this;
    }

    public int getSourceId() {
        return sourceId;
    }

    public NewsEntity setSourceId(int sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public NewsEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public int getPostId() {
        return postId;
    }

    public NewsEntity setPostId(int postId) {
        this.postId = postId;
        return this;
    }

    public String getPostType() {
        return postType;
    }

    public NewsEntity setPostType(String postType) {
        this.postType = postType;
        return this;
    }

    public boolean isFinalPost() {
        return finalPost;
    }

    public NewsEntity setFinalPost(boolean finalPost) {
        this.finalPost = finalPost;
        return this;
    }

    public int getCopyOwnerId() {
        return copyOwnerId;
    }

    public NewsEntity setCopyOwnerId(int copyOwnerId) {
        this.copyOwnerId = copyOwnerId;
        return this;
    }

    public int getCopyPostId() {
        return copyPostId;
    }

    public NewsEntity setCopyPostId(int copyPostId) {
        this.copyPostId = copyPostId;
        return this;
    }

    public long getCopyPostDate() {
        return copyPostDate;
    }

    public NewsEntity setCopyPostDate(long copyPostDate) {
        this.copyPostDate = copyPostDate;
        return this;
    }

    public String getText() {
        return text;
    }

    public NewsEntity setText(String text) {
        this.text = text;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public NewsEntity setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public NewsEntity setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
        return this;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public NewsEntity setCommentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public boolean isCanPostComment() {
        return canPostComment;
    }

    public NewsEntity setCanPostComment(boolean canPostComment) {
        this.canPostComment = canPostComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public NewsEntity setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public NewsEntity setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public NewsEntity setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public NewsEntity setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
        return this;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public NewsEntity setRepostCount(int repostCount) {
        this.repostCount = repostCount;
        return this;
    }

    public boolean isUserReposted() {
        return userReposted;
    }

    public NewsEntity setUserReposted(boolean userReposted) {
        this.userReposted = userReposted;
        return this;
    }

    public int getGeoId() {
        return geoId;
    }

    public NewsEntity setGeoId(int geoId) {
        this.geoId = geoId;
        return this;
    }

    public List<Integer> getFriendsTags() {
        return friendsTags;
    }

    public NewsEntity setFriendsTags(List<Integer> friendsTags) {
        this.friendsTags = friendsTags;
        return this;
    }

    public int getViews() {
        return views;
    }

    public NewsEntity setViews(int views) {
        this.views = views;
        return this;
    }

    public @Nullable
    List<Entity> getAttachments() {
        return attachments;
    }

    public NewsEntity setAttachments(@Nullable List<Entity> attachments) {
        this.attachments = attachments;
        return this;
    }

    public @Nullable
    List<PostEntity> getCopyHistory() {
        return copyHistory;
    }

    public NewsEntity setCopyHistory(@Nullable List<PostEntity> copyHistory) {
        this.copyHistory = copyHistory;
        return this;
    }
}