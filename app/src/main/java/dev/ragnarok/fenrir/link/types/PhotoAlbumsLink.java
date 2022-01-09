package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class PhotoAlbumsLink extends AbsLink {

    public final int ownerId;

    public PhotoAlbumsLink(int ownerId) {
        super(ALBUMS);
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "PhotoAlbumsLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
