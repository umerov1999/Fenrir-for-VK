package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.util.Objects;

public class Post extends AbsModel implements Cloneable {

    public static final int NO_STORED = -1;
    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
    private int dbid;
    private int vkid;
    private int ownerId;
    private Owner author;
    private int authorId;
    private long date;
    private String text;
    private int replyOwnerId;
    private int replyPostId;
    private boolean friendsOnly;
    private int commentsCount;
    private boolean canPostComment;
    private int likesCount;
    private boolean canLike;
    private boolean userLikes;
    private int repostCount;
    private boolean canRepost;
    private boolean userReposted;
    private int postType;
    private Attachments attachments;
    private int signerId;
    private User creator;
    private boolean canPin;
    private boolean pinned;
    private List<Post> copyHierarchy;
    private boolean deleted;
    private PostSource source;
    private int viewCount;
    private int creatorId;
    private boolean canEdit;

    public Post(Parcel in) {
        super(in);
        dbid = in.readInt();
        vkid = in.readInt();
        ownerId = in.readInt();
        author = in.readParcelable(authorId > 0 ?
                User.class.getClassLoader() : Community.class.getClassLoader());
        authorId = in.readInt();
        date = in.readLong();
        text = in.readString();
        replyOwnerId = in.readInt();
        replyPostId = in.readInt();
        friendsOnly = in.readByte() != 0;
        commentsCount = in.readInt();
        canPostComment = in.readByte() != 0;
        likesCount = in.readInt();
        canLike = in.readByte() != 0;
        repostCount = in.readInt();
        canRepost = in.readByte() != 0;
        userReposted = in.readByte() != 0;
        postType = in.readInt();
        attachments = in.readParcelable(Attachments.class.getClassLoader());
        signerId = in.readInt();
        creatorId = in.readInt();
        creator = in.readParcelable(User.class.getClassLoader());
        canPin = in.readByte() != 0;
        pinned = in.readByte() != 0;
        copyHierarchy = in.createTypedArrayList(CREATOR);
        deleted = in.readByte() != 0;
        source = in.readParcelable(PostSource.class.getClassLoader());
        viewCount = in.readInt();
        canEdit = in.readByte() != 0;
    }

    public Post() {

    }

    public PostSource getSource() {
        return source;
    }

    public Post setSource(PostSource source) {
        this.source = source;
        return this;
    }

    public int getDbid() {
        return dbid;
    }

    public Post setDbid(int dbid) {
        this.dbid = dbid;
        return this;
    }

    public int getVkid() {
        return vkid;
    }

    public Post setVkid(int vkid) {
        this.vkid = vkid;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Post setOwnerId(int ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public Owner getAuthor() {
        return author;
    }

    public Post setAuthor(Owner author) {
        this.author = author;
        return this;
    }

    public int getAuthorId() {
        return authorId;
    }

    public Post setAuthorId(int authorId) {
        this.authorId = authorId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public Post setDate(long date) {
        this.date = date;
        return this;
    }

    public String getText() {
        return text;
    }

    public Post setText(String text) {
        this.text = text;
        return this;
    }

    public int getReplyOwnerId() {
        return replyOwnerId;
    }

    public Post setReplyOwnerId(int replyOwnerId) {
        this.replyOwnerId = replyOwnerId;
        return this;
    }

    public int getReplyPostId() {
        return replyPostId;
    }

    public Post setReplyPostId(int replyPostId) {
        this.replyPostId = replyPostId;
        return this;
    }

    public boolean isFriendsOnly() {
        return friendsOnly;
    }

    public Post setFriendsOnly(boolean friendsOnly) {
        this.friendsOnly = friendsOnly;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public Post setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public boolean isCanPostComment() {
        return canPostComment;
    }

    public Post setCanPostComment(boolean canPostComment) {
        this.canPostComment = canPostComment;
        return this;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public Post setLikesCount(int likesCount) {
        this.likesCount = likesCount;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public Post setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public int getRepostCount() {
        return repostCount;
    }

    public Post setRepostCount(int repostCount) {
        this.repostCount = repostCount;
        return this;
    }

    public boolean isUserReposted() {
        return userReposted;
    }

    public Post setUserReposted(boolean userReposted) {
        this.userReposted = userReposted;
        return this;
    }

    public int getPostType() {
        return postType;
    }

    public Post setPostType(int postType) {
        this.postType = postType;
        return this;
    }

    public Attachments getAttachments() {
        return attachments;
    }

    public Post setAttachments(Attachments attachments) {
        this.attachments = attachments;
        return this;
    }

    public int getSignerId() {
        return signerId;
    }

    public Post setSignerId(int signerId) {
        this.signerId = signerId;
        return this;
    }

    public User getCreator() {
        return creator;
    }

    public Post setCreator(User creator) {
        this.creator = creator;
        return this;
    }

    public boolean isCanPin() {
        return canPin;
    }

    public Post setCanPin(boolean canPin) {
        this.canPin = canPin;
        return this;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Post setPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public List<Post> getCopyHierarchy() {
        return copyHierarchy;
    }

    public Post setCopyHierarchy(List<Post> copyHierarchy) {
        this.copyHierarchy = copyHierarchy;
        return this;
    }

    public boolean isCanRepost() {
        return canRepost;
    }

    public Post setCanRepost(boolean canRepost) {
        this.canRepost = canRepost;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Post setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isUserLikes() {
        return userLikes;
    }

    public Post setUserLikes(boolean userLikes) {
        this.userLikes = userLikes;
        return this;
    }

    public int getViewCount() {
        return viewCount;
    }

    public Post setViewCount(int viewCount) {
        this.viewCount = viewCount;
        return this;
    }

    @NonNull
    public List<Post> prepareCopyHierarchy(int initialSize) {
        if (copyHierarchy == null) {
            copyHierarchy = new ArrayList<>(initialSize);
        }

        return copyHierarchy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "POST[dbid: " + dbid + ", vkid: " + vkid + ", ownerid: " + ownerId + "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(dbid);
        dest.writeInt(vkid);
        dest.writeInt(ownerId);
        dest.writeParcelable(author, flags);
        dest.writeInt(authorId);
        dest.writeLong(date);
        dest.writeString(text);
        dest.writeInt(replyOwnerId);
        dest.writeInt(replyPostId);
        dest.writeByte((byte) (friendsOnly ? 1 : 0));
        dest.writeInt(commentsCount);
        dest.writeByte((byte) (canPostComment ? 1 : 0));
        dest.writeInt(likesCount);
        dest.writeByte((byte) (canLike ? 1 : 0));
        dest.writeInt(repostCount);
        dest.writeByte((byte) (canRepost ? 1 : 0));
        dest.writeByte((byte) (userReposted ? 1 : 0));
        dest.writeInt(postType);
        dest.writeParcelable(attachments, flags);
        dest.writeInt(signerId);
        dest.writeInt(creatorId);
        dest.writeParcelable(creator, flags);
        dest.writeByte((byte) (canPin ? 1 : 0));
        dest.writeByte((byte) (pinned ? 1 : 0));
        dest.writeTypedList(copyHierarchy);
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeParcelable(source, flags);
        dest.writeInt(viewCount);
        dest.writeByte((byte) (canEdit ? 1 : 0));
    }

    /**
     * Получить аватар автора поста
     *
     * @return ссылка на квадратное изображение в разрешении до 200px
     */
    public String getAuthorPhoto() {
        return author == null ? null : author.getMaxSquareAvatar();
    }

    public String getAuthorName() {
        return author == null ? null : author.getFullName();
    }

    public boolean isPostponed() {
        return postType == VKApiPost.Type.POSTPONE;
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    public boolean hasText() {
        return nonEmpty(text);
    }

    public String generateVkPostLink() {
        return String.format("vk.com/wall%s_%s", ownerId, vkid);
    }

    public boolean hasPhotos() {
        return attachments != null && !safeIsEmpty(attachments.getPhotos());
    }

    public boolean hasVideos() {
        return attachments != null && !safeIsEmpty(attachments.getVideos());
    }

    public boolean hasCopyHierarchy() {
        return !safeIsEmpty(copyHierarchy);
    }

    @NonNull
    @Override
    public Post clone() throws CloneNotSupportedException {
        Post clone = (Post) super.clone();
        clone.attachments = attachments == null ? null : attachments.clone();
        clone.copyHierarchy = copyHierarchy == null ? null : new ArrayList<>(copyHierarchy.size());

        if (copyHierarchy != null) {
            clone.copyHierarchy.addAll(copyHierarchy);
        }

        return clone;
    }

    public String getTextCopiesInclude() {
        if (nonEmpty(text)) {
            return text;
        }

        if (hasCopyHierarchy()) {
            for (Post copy : copyHierarchy) {
                if (nonEmpty(copy.text)) {
                    return copy.text;
                }
            }
        }

        return null;
    }

    public String findFirstImageCopiesInclude(@PhotoSize int prefferedSize, boolean excludeNonAspectRatio) {
        if (hasPhotos()) {
            return getAttachments().getPhotos().get(0).getUrlForSize(prefferedSize, excludeNonAspectRatio);
        }

        if (hasVideos()) {
            return getAttachments().getVideos().get(0).getImage();
        }

        if (hasDocs()) {
            return getAttachments().getDocs().get(0).getPreviewWithSize(prefferedSize, excludeNonAspectRatio);
        }

        if (hasCopyHierarchy()) {
            for (Post copy : getCopyHierarchy()) {
                String url = copy.findFirstImageCopiesInclude(prefferedSize, excludeNonAspectRatio);
                if (nonEmpty(url)) {
                    return url;
                }
            }
        }

        return null;
    }

    private boolean hasDocs() {
        return Objects.nonNull(attachments) && nonEmpty(attachments.getDocs());
    }

    public String findFirstImageCopiesInclude() {
        return findFirstImageCopiesInclude(PhotoSize.Q, false);
    }

    public int getCreatorId() {
        return creatorId;
    }

    public Post setCreatorId(int creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public Post setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }
}
