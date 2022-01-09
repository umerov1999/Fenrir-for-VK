package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class VideoAlbumLink extends AbsLink {

    public final int ownerId;
    public final int albumId;

    public VideoAlbumLink(int ownerId, int albumId) {
        super(VIDEO_ALBUM);
        this.albumId = albumId;
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoAlbumLink{" +
                "ownerId=" + ownerId +
                ", albumId=" + albumId +
                '}';
    }
}
