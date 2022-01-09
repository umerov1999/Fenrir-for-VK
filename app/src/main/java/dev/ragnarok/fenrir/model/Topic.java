package dev.ragnarok.fenrir.model;

import android.os.Parcel;


public class Topic extends AbsModel {

    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        @Override
        public Topic createFromParcel(Parcel in) {
            return new Topic(in);
        }

        @Override
        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };
    private final int id;
    private final int ownerId;
    private String title;
    private long creationTime;
    private int createdByOwnerId;
    private long lastUpdateTime;
    private int updatedByOwnerId;
    private boolean closed;
    private boolean fixed;
    private int commentsCount;
    private String firstCommentBody;
    private String lastCommentBody;
    private Owner creator;
    private Owner updater;

    public Topic(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected Topic(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        title = in.readString();
        creationTime = in.readLong();
        createdByOwnerId = in.readInt();
        lastUpdateTime = in.readLong();
        updatedByOwnerId = in.readInt();
        closed = in.readByte() != 0;
        fixed = in.readByte() != 0;
        commentsCount = in.readInt();
        firstCommentBody = in.readString();
        lastCommentBody = in.readString();
        creator = ((ParcelableOwnerWrapper) in.readParcelable(ParcelableOwnerWrapper.class.getClassLoader())).get();
        updater = ((ParcelableOwnerWrapper) in.readParcelable(ParcelableOwnerWrapper.class.getClassLoader())).get();
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

    public Topic setTitle(String title) {
        this.title = title;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public Topic setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public int getCreatedByOwnerId() {
        return createdByOwnerId;
    }

    public Topic setCreatedByOwnerId(int createdByOwnerId) {
        this.createdByOwnerId = createdByOwnerId;
        return this;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Topic setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public int getUpdatedByOwnerId() {
        return updatedByOwnerId;
    }

    public Topic setUpdatedByOwnerId(int updatedByOwnerId) {
        this.updatedByOwnerId = updatedByOwnerId;
        return this;
    }

    public boolean isClosed() {
        return closed;
    }

    public Topic setClosed(boolean closed) {
        this.closed = closed;
        return this;
    }

    public boolean isFixed() {
        return fixed;
    }

    public Topic setFixed(boolean fixed) {
        this.fixed = fixed;
        return this;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public Topic setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
        return this;
    }

    public String getFirstCommentBody() {
        return firstCommentBody;
    }

    public Topic setFirstCommentBody(String firstCommentBody) {
        this.firstCommentBody = firstCommentBody;
        return this;
    }

    public String getLastCommentBody() {
        return lastCommentBody;
    }

    public Topic setLastCommentBody(String lastCommentBody) {
        this.lastCommentBody = lastCommentBody;
        return this;
    }

    public Owner getCreator() {
        return creator;
    }

    public Topic setCreator(Owner creator) {
        this.creator = creator;
        return this;
    }

    public Owner getUpdater() {
        return updater;
    }

    public Topic setUpdater(Owner updater) {
        this.updater = updater;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeString(title);
        dest.writeLong(creationTime);
        dest.writeInt(createdByOwnerId);
        dest.writeLong(lastUpdateTime);
        dest.writeInt(updatedByOwnerId);
        dest.writeByte((byte) (closed ? 1 : 0));
        dest.writeByte((byte) (fixed ? 1 : 0));
        dest.writeInt(commentsCount);
        dest.writeString(firstCommentBody);
        dest.writeString(lastCommentBody);
        dest.writeParcelable(new ParcelableOwnerWrapper(creator), flags);
        dest.writeParcelable(new ParcelableOwnerWrapper(updater), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}