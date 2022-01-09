package dev.ragnarok.fenrir.model;

import android.os.Parcel;


public class Article extends AbsModel {

    public static final Creator<Article> CREATOR = new Creator<Article>() {
        @Override
        public Article createFromParcel(Parcel in) {
            return new Article(in);
        }

        @Override
        public Article[] newArray(int size) {
            return new Article[size];
        }
    };

    private final int id;
    private final int owner_id;
    private String owner_name;
    private String url;
    private String title;
    private String subtitle;
    private Photo photo;
    private String access_key;
    private boolean is_favorite;

    public Article(int id, int owner_id) {
        this.id = id;
        this.owner_id = owner_id;
    }

    protected Article(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        owner_name = in.readString();
        url = in.readString();
        title = in.readString();
        subtitle = in.readString();
        access_key = in.readString();
        photo = in.readParcelable(Photo.class.getClassLoader());
        is_favorite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeString(owner_name);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(access_key);
        dest.writeParcelable(photo, flags);
        dest.writeByte((byte) (is_favorite ? 1 : 0));
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public String getOwnerName() {
        return owner_name;
    }

    public Article setOwnerName(String owner_name) {
        this.owner_name = owner_name;
        return this;
    }

    public String getURL() {
        return url;
    }

    public Article setURL(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSubTitle() {
        return subtitle;
    }

    public Article setSubTitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public Photo getPhoto() {
        return photo;
    }

    public Article setPhoto(Photo photo) {
        this.photo = photo;
        return this;
    }

    public String getAccessKey() {
        return access_key;
    }

    public Article setAccessKey(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public boolean getIsFavorite() {
        return is_favorite;
    }

    public Article setIsFavorite(boolean is_favorite) {
        this.is_favorite = is_favorite;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Graffiti))
            return false;

        Article article = (Article) o;
        return id == article.id && owner_id == article.owner_id;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + owner_id;
        return result;
    }
}
