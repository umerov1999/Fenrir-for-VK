package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class AudioPlaylistLink extends AbsLink {

    public final int ownerId;
    public final int playlistId;
    public final String access_key;

    public AudioPlaylistLink(int ownerId, int playlistId, String access_key) {
        super(PLAYLIST);
        this.playlistId = playlistId;
        this.ownerId = ownerId;
        this.access_key = access_key;
    }

    @NonNull
    @Override
    public String toString() {
        return "AudioPlaylistLink{" +
                "ownerId=" + ownerId +
                ", playlistId=" + playlistId +
                ", access_key=" + access_key +
                '}';
    }
}
