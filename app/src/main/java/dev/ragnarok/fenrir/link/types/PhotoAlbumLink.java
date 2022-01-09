package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class PhotoAlbumLink extends AbsLink {

    public final int ownerId;
    public final int albumId;

    public PhotoAlbumLink(int ownerId, int albumId) {
        super(PHOTO_ALBUM);
        this.ownerId = ownerId;
        this.albumId = albumId;
    }

    @NonNull
    @Override
    public String toString() {
        return "PhotoAlbumLink{" +
                "ownerId=" + ownerId +
                ", albumId=" + albumId +
                '}';
    }
}
