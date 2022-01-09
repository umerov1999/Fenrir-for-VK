package dev.ragnarok.fenrir.model;

import androidx.annotation.NonNull;


public class VideoWithOwner {

    private final Video video;

    private final Owner owner;

    public VideoWithOwner(Video video, Owner owner) {
        this.video = video;
        this.owner = owner;
    }

    @NonNull
    public Video getVideo() {
        return video;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }
}
