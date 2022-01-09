package dev.ragnarok.fenrir.model;

import android.os.Parcel;


public class WikiPage extends AbsModel {

    public static final Creator<WikiPage> CREATOR = new Creator<WikiPage>() {
        @Override
        public WikiPage createFromParcel(Parcel in) {
            return new WikiPage(in);
        }

        @Override
        public WikiPage[] newArray(int size) {
            return new WikiPage[size];
        }
    };
    private final int id;
    private final int ownerId;
    private int creatorId;
    private String title;
    private String source;
    private long editionTime;
    private long creationTime;
    private String parent;
    private String parent2;
    private int views;
    private String viewUrl;

    public WikiPage(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected WikiPage(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        creatorId = in.readInt();
        title = in.readString();
        source = in.readString();
        editionTime = in.readLong();
        creationTime = in.readLong();
        parent = in.readString();
        parent2 = in.readString();
        views = in.readInt();
        viewUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeInt(creatorId);
        dest.writeString(title);
        dest.writeString(source);
        dest.writeLong(editionTime);
        dest.writeLong(creationTime);
        dest.writeString(parent);
        dest.writeString(parent2);
        dest.writeInt(views);
        dest.writeString(viewUrl);
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public WikiPage setCreatorId(int creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public WikiPage setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSource() {
        return source;
    }

    public WikiPage setSource(String source) {
        this.source = source;
        return this;
    }

    public long getEditionTime() {
        return editionTime;
    }

    public WikiPage setEditionTime(long editionTime) {
        this.editionTime = editionTime;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public WikiPage setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public WikiPage setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public String getParent2() {
        return parent2;
    }

    public WikiPage setParent2(String parent2) {
        this.parent2 = parent2;
        return this;
    }

    public int getViews() {
        return views;
    }

    public WikiPage setViews(int views) {
        this.views = views;
        return this;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public WikiPage setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
