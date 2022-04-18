package dev.ragnarok.fenrir.api.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AttachmentToken implements IAttachmentToken {

    @NonNull
    public final String type;

    public final int id;

    public final int ownerId;

    @Nullable
    public final String accessKey;

    public AttachmentToken(@NonNull String type, int id, int ownerId) {
        this.type = type;
        this.id = id;
        this.ownerId = ownerId;
        accessKey = null;
    }

    public AttachmentToken(@NonNull String type, int id, int ownerId, @Nullable String accessKey) {
        this.type = type;
        this.id = id;
        this.ownerId = ownerId;
        this.accessKey = accessKey;
    }

    @NonNull
    @Override
    public String format() {
        return type + ownerId + "_" + id + (accessKey == null || accessKey.length() == 0 ? "" : ("_" + accessKey));
    }
}