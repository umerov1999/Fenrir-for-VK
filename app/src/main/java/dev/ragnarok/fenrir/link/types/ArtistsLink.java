package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class ArtistsLink extends AbsLink {

    public final String Id;

    public ArtistsLink(String Id) {
        super(ARTISTS);
        this.Id = Id;
    }

    @NonNull
    @Override
    public String toString() {
        return "ArtistsLink{" +
                "Id=" + Id +
                '}';
    }
}
