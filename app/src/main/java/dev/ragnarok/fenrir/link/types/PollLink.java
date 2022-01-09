package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class PollLink extends AbsLink {

    public final int ownerId;
    public final int Id;

    public PollLink(int ownerId, int Id) {
        super(POLL);
        this.Id = Id;
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "PollLink{" +
                "ownerId=" + ownerId +
                ", Id=" + Id +
                '}';
    }
}
