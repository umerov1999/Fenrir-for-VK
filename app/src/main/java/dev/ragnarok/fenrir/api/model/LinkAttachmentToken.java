package dev.ragnarok.fenrir.api.model;


public class LinkAttachmentToken implements IAttachmentToken {

    public final String url;

    public LinkAttachmentToken(String url) {
        this.url = url;
    }

    @Override
    public String format() {
        return url;
    }
}
