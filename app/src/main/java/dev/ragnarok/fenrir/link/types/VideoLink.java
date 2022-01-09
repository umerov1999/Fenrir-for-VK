package dev.ragnarok.fenrir.link.types;

import androidx.annotation.NonNull;

public class VideoLink extends AbsLink {

    public final int ownerId;
    public final int videoId;
    public final String access_key;

    public VideoLink(int ownerId, int videoId, String access_key) {
        super(VIDEO);
        this.videoId = videoId;
        this.ownerId = ownerId;
        this.access_key = access_key;
    }

    @NonNull
    @Override
    public String toString() {
        return "VideoLink{" +
                "ownerId=" + ownerId +
                ", videoId=" + videoId +
                ", Access_Key=" + access_key +
                '}';
    }
}
