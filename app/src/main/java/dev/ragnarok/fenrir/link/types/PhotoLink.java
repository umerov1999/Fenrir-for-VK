package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class PhotoLink extends AbsLink {

    public final int id;
    public final int ownerId;
    public final String access_key;

    public PhotoLink(int id, int ownerId, String access_key) {
        super(PHOTO);
        this.id = id;
        this.ownerId = ownerId;
        this.access_key = access_key;
    }

    @NonNull
    @Override
    public String toString() {
        return "PhotoLink{" +
                "ownerId=" + ownerId +
                ", Id=" + id +
                ", Access_Key=" + access_key +
                '}';
    }
}