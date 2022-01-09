package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class OwnerLink extends AbsLink {

    public final int ownerId;

    public OwnerLink(int id) {
        super(id > 0 ? PROFILE : GROUP);
        ownerId = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "OwnerLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
