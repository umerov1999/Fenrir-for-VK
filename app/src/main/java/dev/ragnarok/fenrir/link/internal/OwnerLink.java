package dev.ragnarok.fenrir.link.internal;

public class OwnerLink extends AbsInternalLink {

    public final int ownerId;

    public OwnerLink(int start, int end, int ownerId, String name) {
        this.start = start;
        this.end = end;
        this.ownerId = ownerId;
        targetLine = name;
    }
}
