package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import java.util.List;

import dev.ragnarok.fenrir.util.Objects;

public class News extends AbsModel {

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
    /**
     * Каждый из элементов массива в поле friends содержит поля: uid— идентификатор пользователя
     */
    private List<User> friends;
    /**
     * тип списка новости, соответствующий одному из значений параметра filters;
     */
    private String type;
    /**
     * идентификатор источника новости (положительный — новость пользователя, отрицательный — новость группы);
     */
    private int sourceId;
    private Owner source;
    /**
     * находится в записях со стен, содержит тип новости (post или copy);
     */
    private String postType;
    /**
     * передается в случае, если этот пост сделан при удалении;
     */
    private boolean finalPost;
    /**
     * находится в записях со стен, если сообщение является копией сообщения с чужой стены,
     * и содержит идентификатор владельца стены, у которого было скопировано сообщение;
     */
    private int copyOwnerId;
    /**
     * находится в записях со стен, если сообщение является копией сообщения с чужой стены,
     * и содержит идентификатор скопированного сообщения на стене его владельца;
     */
    private int copyPostId;
    /**
     * находится в записях со стен, если сообщение является копией сообщения с чужой стены,
     * и содержит дату скопированного сообщения;
     */
    private long copyPostDate;
    /**
     * время публикации новости в формате unixtime;
     */
    private long date;
    /**
     * находится в записях со стен и содержит идентификатор записи на стене владельца;
     */
    private int postId;
    /**
     * массив, содержащий историю репостов для записи. Возвращается только в том случае, если запись
     * является репостом. Каждый из объектов массива, в свою очередь, является объектом-записью стандартного формата.
     */
    private List<Post> copyHistory;
    /**
     * находится в записях со стен и содержит текст записи;
     */
    private String text;
    /**
     * содержит true, если текущий пользователь может редактировать запись;
     */
    private boolean canEdit;
    /**
     * возвращается, если пользователь может удалить новость, всегда содержит true;
     */
    private boolean canDelete;
    /**
     * количество комментариев
     */
    private int commentCount;
    /**
     * информация о том, может ли текущий пользователь комментировать запись
     * (true — может, false — не может);
     */
    private boolean commentCanPost;
    /**
     * число пользователей, которым понравилась запись,
     */
    private int likeCount;
    /**
     * наличие отметки «Мне нравится» от текущего пользователя
     */
    private boolean userLike;
    /**
     * информация о том, может ли текущий пользователь поставить отметку «Мне нравится»
     */
    private boolean canLike;
    /**
     * информация о том, может ли текущий пользователь сделать репост записи
     */
    private boolean canPublish;
    /**
     * число пользователей, сделавших репост
     */
    private int repostsCount;


    //private int attachmentsMask;
    /**
     * наличие репоста от текущего пользователя
     */
    private boolean userReposted;
    /**
     * находится в записях со стен и содержит массив объектов,
     * которые прикреплены к текущей новости (фотография, ссылка и т.п.
     */
    private Attachments attachments;
    private transient Object tag;
    private int viewCount;

    public News() {

    }

    protected News(Parcel in) {
        super(in);
        type = in.readString();
        sourceId = in.readInt();
        source = in.readParcelable(sourceId > 0 ? User.class.getClassLoader() : Community.class.getClassLoader());
        postType = in.readString();
        finalPost = in.readByte() != 0;
        copyOwnerId = in.readInt();
        copyPostId = in.readInt();
        copyPostDate = in.readLong();
        date = in.readLong();
        postId = in.readInt();
        copyHistory = in.createTypedArrayList(Post.CREATOR);
        text = in.readString();
        canEdit = in.readByte() != 0;
        canDelete = in.readByte() != 0;
        commentCount = in.readInt();
        commentCanPost = in.readByte() != 0;
        likeCount = in.readInt();
        userLike = in.readByte() != 0;
        canLike = in.readByte() != 0;
        canPublish = in.readByte() != 0;
        repostsCount = in.readInt();
        userReposted = in.readByte() != 0;
        attachments = in.readParcelable(Attachments.class.getClassLoader());
        friends = in.createTypedArrayList(User.CREATOR);
        viewCount = in.readInt();
    }

    public Object getTag() {
        return tag;
    }

    public News setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    public String getType() {
        return type;
    }

    public News setType(String type) {
        this.type = type;
        return this;
    }

    public int getSourceId() {
        return sourceId;
    }

    public News setSourceId(int sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public Owner getSource() {
        return source;
    }

    public News setSource(Owner source) {
        this.source = source;
        return this;
    }

    public String getPostType() {
        return postType;
    }

    public News setPostType(String postType) {
        this.postType = postType;
        return this;
    }

    public boolean isFinalPost() {
        return finalPost;
    }

    public News setFinalPost(boolean finalPost) {
        this.finalPost = finalPost;
        return this;
    }

    public int getCopyOwnerId() {
        return copyOwnerId;
    }

    public News setCopyOwnerId(int copyOwnerId) {
        this.copyOwnerId = copyOwnerId;
        return this;
    }

    public int getCopyPostId() {
        return copyPostId;
    }

    public News setCopyPostId(int copyPostId) {
        this.copyPostId = copyPostId;
        return this;
    }

    public long getCopyPostDate() {
        return copyPostDate;
    }

    public News setCopyPostDate(long copyPostDate) {
        this.copyPostDate = copyPostDate;
        return this;
    }

    public long getDate() {
        return date;
    }

    public News setDate(long date) {
        this.date = date;
        return this;
    }

    public List<Post> getCopyHistory() {
        return copyHistory;
    }

    public News setCopyHistory(List<Post> copyHistory) {
        this.copyHistory = copyHistory;
        return this;
    }

    public String getText() {
        return text;
    }

    public News setText(String text) {
        this.text = text;
        return this;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public News setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
        return this;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public News setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
        return this;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public News setCommentCount(int commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public boolean isCommentCanPost() {
        return commentCanPost;
    }

    public News setCommentCanPost(boolean commentCanPost) {
        this.commentCanPost = commentCanPost;
        return this;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public News setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public boolean isUserLike() {
        return userLike;
    }

    public News setUserLike(boolean userLike) {
        this.userLike = userLike;
        return this;
    }

    public boolean isCanLike() {
        return canLike;
    }

    public News setCanLike(boolean canLike) {
        this.canLike = canLike;
        return this;
    }

    public boolean isCanPublish() {
        return canPublish;
    }

    public News setCanPublish(boolean canPublish) {
        this.canPublish = canPublish;
        return this;
    }

    public int getRepostsCount() {
        return repostsCount;
    }

    public News setRepostsCount(int repostsCount) {
        this.repostsCount = repostsCount;
        return this;
    }

    public boolean isUserReposted() {
        return userReposted;
    }

    public News setUserReposted(boolean userReposted) {
        this.userReposted = userReposted;
        return this;
    }

    public Attachments getAttachments() {
        return attachments;
    }

    public News setAttachments(Attachments attachments) {
        this.attachments = attachments;
        return this;
    }

    public int getPostId() {
        return postId;
    }

    public News setPostId(int postId) {
        this.postId = postId;
        return this;
    }

    public List<User> getFriends() {
        return friends;
    }

    public News setFriends(List<User> friends) {
        this.friends = friends;
        return this;
    }

    public String getOwnerMaxSquareAvatar() {
        return source == null ? null : source.getMaxSquareAvatar();
    }

    public String getOwnerName() {
        return source == null ? null : source.getFullName();
    }

    public Post toPost() {
        if (!type.equals("post")) {
            return null;
        }

        return new Post()
                .setVkid(postId)
                .setOwnerId(sourceId)
                .setText(text)
                .setAttachments(attachments)
                .setCopyHierarchy(copyHistory)
                .setDate(date)
                .setAuthor(source)
                .setCanLike(canLike)
                .setLikesCount(likeCount)
                .setUserLikes(userLike)
                .setReplyOwnerId(copyOwnerId)
                .setReplyPostId(copyPostId)
                .setRepostCount(repostsCount)
                .setUserReposted(userReposted)
                .setCommentsCount(commentCount)
                .setCanPostComment(commentCanPost)
                .setCanRepost(canPublish)
                .setViewCount(viewCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(type);
        dest.writeInt(sourceId);
        dest.writeParcelable(source, flags);
        dest.writeString(postType);
        dest.writeByte((byte) (finalPost ? 1 : 0));
        dest.writeInt(copyOwnerId);
        dest.writeInt(copyPostId);
        dest.writeLong(copyPostDate);
        dest.writeLong(date);
        dest.writeInt(postId);
        dest.writeTypedList(copyHistory);
        dest.writeString(text);
        dest.writeByte((byte) (canEdit ? 1 : 0));
        dest.writeByte((byte) (canDelete ? 1 : 0));
        dest.writeInt(commentCount);
        dest.writeByte((byte) (commentCanPost ? 1 : 0));
        dest.writeInt(likeCount);
        dest.writeByte((byte) (userLike ? 1 : 0));
        dest.writeByte((byte) (canLike ? 1 : 0));
        dest.writeByte((byte) (canPublish ? 1 : 0));
        dest.writeInt(repostsCount);
        dest.writeByte((byte) (userReposted ? 1 : 0));
        dest.writeParcelable(attachments, flags);
        dest.writeTypedList(friends);
        dest.writeInt(viewCount);
    }

    public boolean hasAttachments() {
        return Objects.nonNull(attachments) && !attachments.isEmpty();
    }

    public int getViewCount() {
        return viewCount;
    }

    public News setViewCount(int viewCount) {
        this.viewCount = viewCount;
        return this;
    }
}