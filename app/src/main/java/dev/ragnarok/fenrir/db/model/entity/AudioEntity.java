package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

import java.util.Map;

@Keep
public class AudioEntity extends Entity {

    private int id;

    private int ownerId;

    private String artist;

    private String title;

    private int duration;

    private String url;

    private int lyricsId;

    private int albumId;

    private int album_owner_id;

    private String album_access_key;

    private int genre;

    private String accessKey;

    private boolean deleted;

    private String thumb_image_little;

    private String thumb_image_big;

    private String thumb_image_very_big;

    private String album_title;

    private Map<String, String> main_artists;

    private boolean isHq;

    public AudioEntity set(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        return this;
    }

    public String getAlbum_title() {
        return album_title;
    }

    public AudioEntity setAlbum_title(String album_title) {
        this.album_title = album_title;
        return this;
    }

    public String getThumb_image_little() {
        return thumb_image_little;
    }

    public AudioEntity setThumb_image_little(String thumb_image_little) {
        this.thumb_image_little = thumb_image_little;
        return this;
    }

    public String getThumb_image_big() {
        return thumb_image_big;
    }

    public AudioEntity setThumb_image_big(String thumb_image_big) {
        this.thumb_image_big = thumb_image_big;
        return this;
    }

    public String getThumb_image_very_big() {
        return thumb_image_very_big;
    }

    public AudioEntity setThumb_image_very_big(String thumb_image_very_big) {
        this.thumb_image_very_big = thumb_image_very_big;
        return this;
    }

    public int getAlbum_owner_id() {
        return album_owner_id;
    }

    public AudioEntity setAlbum_owner_id(int album_owner_id) {
        this.album_owner_id = album_owner_id;
        return this;
    }

    public String getAlbum_access_key() {
        return album_access_key;
    }

    public AudioEntity setAlbum_access_key(String album_access_key) {
        this.album_access_key = album_access_key;
        return this;
    }

    public boolean getIsHq() {
        return isHq;
    }

    public AudioEntity setIsHq(boolean isHq) {
        this.isHq = isHq;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getArtist() {
        return artist;
    }

    public AudioEntity setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AudioEntity setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public AudioEntity setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AudioEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public int getLyricsId() {
        return lyricsId;
    }

    public AudioEntity setLyricsId(int lyricsId) {
        this.lyricsId = lyricsId;
        return this;
    }

    public int getAlbumId() {
        return albumId;
    }

    public AudioEntity setAlbumId(int albumId) {
        this.albumId = albumId;
        return this;
    }

    public int getGenre() {
        return genre;
    }

    public AudioEntity setGenre(int genre) {
        this.genre = genre;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public AudioEntity setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public AudioEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public Map<String, String> getMain_artists() {
        return main_artists;
    }

    public AudioEntity setMain_artists(Map<String, String> main_artists) {
        this.main_artists = main_artists;
        return this;
    }
}