package dev.ragnarok.fenrir.link.internal;

public class OtherLink extends AbsInternalLink {
    public final String Link;

    public OtherLink(int start, int end, String link, String name) {
        this.start = start;
        this.end = end;
        targetLine = name;
        Link = link;
    }
}
