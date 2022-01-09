package dev.ragnarok.fenrir.model;

import android.os.Parcel;


public class AudioPlaylist extends AbsModel {
    public static final Creator<AudioPlaylist> CREATOR = new Creator<AudioPlaylist>() {
        @Override
        public AudioPlaylist createFromParcel(Parcel in) {
            return new AudioPlaylist(in);
        }

        @Override
        public AudioPlaylist[] newArray(int size) {
            return new AudioPlaylist[size];
        }
    };
    private int id;
    private int owner_id;
    private int count;
    private long update_time;
    private int Year;
    private String artist_name;
    private String genre;
    private String title;
    private String description;
    private String thumb_image;
    private String access_key;

    private String original_access_key;
    private int original_id;
    private int original_owner_id;

    public AudioPlaylist() {

    }

    protected AudioPlaylist(Parcel in) {
        super(in);
        id = in.readInt();
        owner_id = in.readInt();
        count = in.readInt();
        update_time = in.readLong();
        Year = in.readInt();
        artist_name = in.readString();
        genre = in.readString();
        title = in.readString();
        description = in.readString();
        thumb_image = in.readString();
        access_key = in.readString();
        original_access_key = in.readString();
        original_id = in.readInt();
        original_owner_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(owner_id);
        dest.writeInt(count);
        dest.writeLong(update_time);
        dest.writeInt(Year);
        dest.writeString(artist_name);
        dest.writeString(genre);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(thumb_image);
        dest.writeString(access_key);
        dest.writeString(original_access_key);
        dest.writeInt(original_id);
        dest.writeInt(original_owner_id);
    }

    public int getId() {
        return id;
    }

    public AudioPlaylist setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public AudioPlaylist setOwnerId(int ownerId) {
        owner_id = ownerId;
        return this;
    }

    public int getCount() {
        return count;
    }

    public AudioPlaylist setCount(int count) {
        this.count = count;
        return this;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public AudioPlaylist setUpdate_time(long update_time) {
        this.update_time = update_time;
        return this;
    }

    public int getYear() {
        return Year;
    }

    public AudioPlaylist setYear(int Year) {
        this.Year = Year;
        return this;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public AudioPlaylist setArtist_name(String artist_name) {
        this.artist_name = artist_name;
        return this;
    }

    public String getGenre() {
        return genre;
    }

    public AudioPlaylist setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AudioPlaylist setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AudioPlaylist setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public AudioPlaylist setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
        return this;
    }

    public String getAccess_key() {
        return access_key;
    }

    public AudioPlaylist setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public String getOriginal_access_key() {
        return original_access_key;
    }

    public AudioPlaylist setOriginal_access_key(String original_access_key) {
        this.original_access_key = original_access_key;
        return this;
    }

    public int getOriginal_id() {
        return original_id;
    }

    public AudioPlaylist setOriginal_id(int original_id) {
        this.original_id = original_id;
        return this;
    }

    public int getOriginal_owner_id() {
        return original_owner_id;
    }

    public AudioPlaylist setOriginal_owner_id(int original_owner_id) {
        this.original_owner_id = original_owner_id;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AudioPlaylist))
            return false;

        AudioPlaylist playlist = (AudioPlaylist) o;
        return id == playlist.id && owner_id == playlist.owner_id;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + owner_id;
        return result;
    }
}
