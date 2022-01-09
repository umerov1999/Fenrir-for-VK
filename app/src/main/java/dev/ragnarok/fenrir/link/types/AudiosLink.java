package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class AudiosLink extends AbsLink {

    public final int ownerId;

    public AudiosLink(int ownerId) {
        super(AUDIOS);
        this.ownerId = ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "AudiosLink{" +
                "ownerId=" + ownerId +
                '}';
    }
}
