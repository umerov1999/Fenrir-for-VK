package dev.ragnarok.fenrir.api.model;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AttachmentTokenString implements IAttachmentToken {

    public final @Nullable
    String type;

    public final @Nullable
    String id;

    public AttachmentTokenString(@Nullable String type, @Nullable String id) {
        this.type = type;
        this.id = id;
    }

    @NonNull
    @Override
    public String format() {
        return type + (id == null || id.length() == 0 ? "" : id);
    }
}
