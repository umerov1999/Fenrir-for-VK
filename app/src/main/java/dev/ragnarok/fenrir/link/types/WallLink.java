package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class WallLink extends AbsLink {

    public final int ownerId;

    public WallLink(int ownerId) {
        super(WALL);
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "WallLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
