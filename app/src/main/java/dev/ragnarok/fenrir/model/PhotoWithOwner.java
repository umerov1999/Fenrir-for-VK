package dev.ragnarok.fenrir.model;

import androidx.annotation.NonNull;


public class PhotoWithOwner {

    private final Photo photo;

    private final Owner owner;

    public PhotoWithOwner(Photo photo, Owner owner) {
        this.photo = photo;
        this.owner = owner;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }

    @NonNull
    public Photo getPhoto() {
        return photo;
    }
}
