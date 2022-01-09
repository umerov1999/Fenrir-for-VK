package dev.ragnarok.fenrir.api.model;

public class VKApiAudioPlaylist implements VKApiAttachment {
    public int id;
    public int owner_id;
    public int count;
    public long update_time;
    public int Year;
    public String artist_name;
    public String genre;
    public String title;
    public String description;
    public String thumb_image;
    public String access_key;

    public String original_access_key;
    public int original_id;
    public int original_owner_id;

    public VKApiAudioPlaylist() {

    }

    @Override
    public String getType() {
        return VKApiAttachment.TYPE_AUDIO_PLAYLIST;
    }
}
