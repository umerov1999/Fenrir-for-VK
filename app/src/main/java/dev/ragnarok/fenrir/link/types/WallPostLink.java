package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class WallPostLink extends AbsLink {

    public final int ownerId;
    public final int postId;

    public WallPostLink(int ownerId, int postId) {
        super(WALL_POST);
        this.ownerId = ownerId;
        this.postId = postId;
    }

    @NonNull
    @Override
    public String toString() {
        return "WallPostLink{" +
                "ownerId=" + ownerId +
                ", postId=" + postId +
                '}';
    }
}
