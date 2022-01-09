package dev.ragnarok.fenrir.model;


public class DraftComment {

    private final int id;

    private String body;

    private int attachmentsCount;

    public DraftComment(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public DraftComment setBody(String body) {
        this.body = body;
        return this;
    }

    public int getAttachmentsCount() {
        return attachmentsCount;
    }

    public DraftComment setAttachmentsCount(int attachmentsCount) {
        this.attachmentsCount = attachmentsCount;
        return this;
    }
}
