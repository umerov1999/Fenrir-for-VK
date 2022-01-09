package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class AwayLink extends AbsLink {

    public final String link;

    public AwayLink(String link) {
        super(EXTERNAL_LINK);
        this.link = link;
    }

    @NonNull
    @Override
    public String toString() {
        return "AwayLink{" +
                "link='" + link + '\'' +
                '}';
    }
}
