package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class AudioPlaylistEntity extends Entity {
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

    public int getId() {
        return id;
    }

    public AudioPlaylistEntity setId(int id) {
        this.id = id;
        return this;
    }

    public int getOwnerId() {
        return owner_id;
    }

    public AudioPlaylistEntity setOwnerId(int ownerId) {
        owner_id = ownerId;
        return this;
    }

    public int getCount() {
        return count;
    }

    public AudioPlaylistEntity setCount(int count) {
        this.count = count;
        return this;
    }

    public long getUpdate_time() {
        return update_time;
    }

    public AudioPlaylistEntity setUpdate_time(long update_time) {
        this.update_time = update_time;
        return this;
    }

    public int getYear() {
        return Year;
    }

    public AudioPlaylistEntity setYear(int Year) {
        this.Year = Year;
        return this;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public AudioPlaylistEntity setArtist_name(String artist_name) {
        this.artist_name = artist_name;
        return this;
    }

    public String getGenre() {
        return genre;
    }

    public AudioPlaylistEntity setGenre(String genre) {
        this.genre = genre;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AudioPlaylistEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AudioPlaylistEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public AudioPlaylistEntity setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
        return this;
    }

    public String getAccess_key() {
        return access_key;
    }

    public AudioPlaylistEntity setAccess_key(String access_key) {
        this.access_key = access_key;
        return this;
    }

    public String getOriginal_access_key() {
        return original_access_key;
    }

    public AudioPlaylistEntity setOriginal_access_key(String original_access_key) {
        this.original_access_key = original_access_key;
        return this;
    }

    public int getOriginal_id() {
        return original_id;
    }

    public AudioPlaylistEntity setOriginal_id(int original_id) {
        this.original_id = original_id;
        return this;
    }

    public int getOriginal_owner_id() {
        return original_owner_id;
    }

    public AudioPlaylistEntity setOriginal_owner_id(int original_owner_id) {
        this.original_owner_id = original_owner_id;
        return this;
    }
}
