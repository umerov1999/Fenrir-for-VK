package dev.ragnarok.fenrir.api.model;


public class AttachmentTokenString implements IAttachmentToken {

    public final String type;

    public final String id;

    public AttachmentTokenString(String type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public String format() {
        return type + (id == null || id.length() == 0 ? "" : id);
    }
}
