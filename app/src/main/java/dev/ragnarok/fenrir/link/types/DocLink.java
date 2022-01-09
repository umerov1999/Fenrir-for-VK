package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class DocLink extends AbsLink {

    public final int ownerId;
    public final int docId;
    public final String access_key;

    public DocLink(int ownerId, int docId, String access_key) {
        super(DOC);
        this.docId = docId;
        this.ownerId = ownerId;
        this.access_key = access_key;
    }

    @NonNull
    @Override
    public String toString() {
        return "DocLink{" +
                "ownerId=" + ownerId +
                ", docId=" + docId +
                ", Access_Key=" + access_key +
                '}';
    }
}
