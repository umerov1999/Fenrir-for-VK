package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class DialogLink extends AbsLink {

    public final int peerId;

    public DialogLink(int peerId) {
        super(DIALOG);
        this.peerId = peerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "DialogLink{" +
                "peerId=" + peerId +
                '}';
    }
}
