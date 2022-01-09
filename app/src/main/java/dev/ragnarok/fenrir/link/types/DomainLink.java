package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class DomainLink extends AbsLink {

    public final String fullLink;
    public final String domain;

    public DomainLink(String fullLink, String domain) {
        super(DOMAIN);
        this.domain = domain;
        this.fullLink = fullLink;
    }

    @NonNull
    @Override
    public String toString() {
        return "DomainLink{" +
                "fullLink='" + fullLink + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
