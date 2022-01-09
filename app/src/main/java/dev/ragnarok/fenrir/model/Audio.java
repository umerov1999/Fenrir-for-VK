package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.stringEmptyIfNull;

import android.os.Parcel;

import androidx.annotation.DrawableRes;
import androidx.annotation.Keep;

import java.util.Map;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;

@Keep
public class Audio extends AbsModel {

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };
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
    private boolean animationNow;
    private boolean isSelected;
    private boolean isHq;
    private boolean is_local;
    private boolean is_localServer;
    private int downloadIndicator;

    @SuppressWarnings("unused")
    public Audio() {

    }

    protected Audio(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        artist = in.readString();
        title = in.readString();
        duration = in.readInt();
        url = in.readString();
        lyricsId = in.readInt();
        albumId = in.readInt();
        album_owner_id = in.readInt();
        album_access_key = in.readString();
        genre = in.readInt();
        accessKey = in.readString();
        deleted = in.readByte() != 0;
        thumb_image_big = in.readString();
        thumb_image_very_big = in.readString();
        thumb_image_little = in.readString();
        album_title = in.readString();
        animationNow = in.readByte() != 0;
        isSelected = in.readByte() != 0;
        isHq = in.readByte() != 0;
        main_artists = Utils.readStringMap(in);
        is_local = in.readByte() != 0;
        is_localServer = in.readByte() != 0;
        downloadIndicator = in.readInt();
    }

    /*
    public static String getMp3FromM3u8(String url) {
        if (Utils.isEmpty(url) || !url.contains("index.m3u8"))
            return url;
        if (url.contains("/audios/")) {
            final String regex = "^(.+?)/[^/]+?/audios/([^/]+)/.+$";
            final String subst = "$1/audios/$2.mp3";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);

            return matcher.replaceFirst(subst);
        } else {
            final String regex = "^(.+?)/(p[0-9]+)/[^/]+?/([^/]+)/.+$";
            final String subst = "$1/$2/$3.mp3";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(url);
            return matcher.replaceFirst(subst);
        }
    }
     */

    public Pair<Boolean, Boolean> needRefresh() {
        boolean empty_url = Utils.isEmpty(url);
        boolean refresh_old = Settings.get().other().isUse_api_5_90_for_audio();
        return new Pair<>(empty_url || refresh_old && isHLS(), refresh_old);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeInt(ownerId);
        dest.writeString(artist);
        dest.writeString(title);
        dest.writeInt(duration);
        dest.writeString(url);
        dest.writeInt(lyricsId);
        dest.writeInt(albumId);
        dest.writeInt(album_owner_id);
        dest.writeString(album_access_key);
        dest.writeInt(genre);
        dest.writeString(accessKey);
        dest.writeByte((byte) (deleted ? 1 : 0));
        dest.writeString(thumb_image_big);
        dest.writeString(thumb_image_very_big);
        dest.writeString(thumb_image_little);
        dest.writeString(album_title);
        dest.writeByte((byte) (animationNow ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeByte((byte) (isHq ? 1 : 0));
        Utils.writeStringMap(dest, main_artists);
        dest.writeByte((byte) (is_local ? 1 : 0));
        dest.writeByte((byte) (is_localServer ? 1 : 0));
        dest.writeInt(downloadIndicator);
    }

    public int getDownloadIndicator() {
        return downloadIndicator;
    }

    public void setDownloadIndicator(int state) {
        downloadIndicator = state;
    }

    public Audio updateDownloadIndicator() {
        downloadIndicator = DownloadWorkUtils.TrackIsDownloaded(this);
        return this;
    }

    public boolean isAnimationNow() {
        return animationNow;
    }

    public void setAnimationNow(boolean animationNow) {
        this.animationNow = animationNow;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getId() {
        return id;
    }

    public Audio setId(int id) {
        this.id = id;
        return this;
    }

    public boolean getIsHq() {
        return isHq;
    }

    public Audio setIsHq(boolean isHq) {
        this.isHq = isHq;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public Audio setOwnerId(int ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getArtist() {
        return artist;
    }

    public Audio setArtist(String artist) {
        this.artist = artist;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public Audio setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public Audio setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public @DrawableRes
    int getSongIcon() {
        if (Utils.isEmpty(url)) {
            return R.drawable.audio_died;
        } else if ("https://vk.com/mp3/audio_api_unavailable.mp3".equals(url)) {
            return R.drawable.report;
        }
        return R.drawable.song;
    }

    public String getUrl() {
        return url;
    }

    public Audio setUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isHLS() {
        return url.contains("index.m3u8");
    }

    public String getAlbum_title() {
        return album_title;
    }

    public Audio setAlbum_title(String album_title) {
        this.album_title = album_title;
        return this;
    }

    public String getThumb_image_little() {
        return thumb_image_little;
    }

    public Audio setThumb_image_little(String thumb_image_little) {
        this.thumb_image_little = thumb_image_little;
        return this;
    }

    public String getThumb_image_big() {
        return thumb_image_big;
    }

    public Audio setThumb_image_big(String thumb_image_big) {
        this.thumb_image_big = thumb_image_big;
        return this;
    }

    public String getThumb_image_very_big() {
        return thumb_image_very_big;
    }

    public Audio setThumb_image_very_big(String thumb_image_very_big) {
        this.thumb_image_very_big = thumb_image_very_big;
        return this;
    }

    public int getAlbum_owner_id() {
        return album_owner_id;
    }

    public Audio setAlbum_owner_id(int album_owner_id) {
        this.album_owner_id = album_owner_id;
        return this;
    }

    public String getAlbum_access_key() {
        return album_access_key;
    }

    public Audio setAlbum_access_key(String album_access_key) {
        this.album_access_key = album_access_key;
        return this;
    }

    public int getLyricsId() {
        return lyricsId;
    }

    public Audio setLyricsId(int lyricsId) {
        this.lyricsId = lyricsId;
        return this;
    }

    public int getAlbumId() {
        return albumId;
    }

    public Audio setAlbumId(int albumId) {
        this.albumId = albumId;
        return this;
    }

    public int getGenre() {
        return genre;
    }

    public Audio setGenre(int genre) {
        this.genre = genre;
        return this;
    }

    public int getGenreByID3() {
        switch (genre) {
            case VKApiAudio.Genre.ROCK:
                return 17;
            case VKApiAudio.Genre.POP:
            case VKApiAudio.Genre.INDIE_POP:
                return 13;
            case VKApiAudio.Genre.EASY_LISTENING:
                return 98;
            case VKApiAudio.Genre.DANCE_AND_HOUSE:
                return 125;
            case VKApiAudio.Genre.INSTRUMENTAL:
                return 33;
            case VKApiAudio.Genre.METAL:
                return 9;
            case VKApiAudio.Genre.DRUM_AND_BASS:
                return 127;
            case VKApiAudio.Genre.TRANCE:
                return 31;
            case VKApiAudio.Genre.CHANSON:
                return 102;
            case VKApiAudio.Genre.ETHNIC:
                return 48;
            case VKApiAudio.Genre.ACOUSTIC_AND_VOCAL:
                return 99;
            case VKApiAudio.Genre.REGGAE:
                return 16;
            case VKApiAudio.Genre.CLASSICAL:
                return 32;
            case VKApiAudio.Genre.OTHER:
                return 12;
            case VKApiAudio.Genre.SPEECH:
                return 101;
            case VKApiAudio.Genre.ALTERNATIVE:
                return 20;
            case VKApiAudio.Genre.ELECTROPOP_AND_DISCO:
                return 52;
            case VKApiAudio.Genre.JAZZ_AND_BLUES:
                return 8;
        }
        return 0;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public Audio setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public Map<String, String> getMain_artists() {
        return main_artists;
    }

    public Audio setMain_artists(Map<String, String> main_artists) {
        this.main_artists = main_artists;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Audio setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isLocal() {
        return is_local;
    }

    public Audio setIsLocal() {
        is_local = true;
        return this;
    }

    public boolean isLocalServer() {
        return is_localServer;
    }

    public Audio setIsLocalServer() {
        is_localServer = true;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getArtistAndTitle() {
        return stringEmptyIfNull(artist) + " - " + stringEmptyIfNull(title);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Audio))
            return false;

        Audio audio = (Audio) o;
        return id == audio.id && ownerId == audio.ownerId;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + ownerId;
        return result;
    }
}
