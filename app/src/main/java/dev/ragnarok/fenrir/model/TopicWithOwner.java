package dev.ragnarok.fenrir.model;

import androidx.annotation.NonNull;

public class TopicWithOwner {

    private final Topic topic;

    private final Owner owner;

    public TopicWithOwner(Topic topic, Owner owner) {
        this.topic = topic;
        this.owner = owner;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }

    @NonNull
    public Topic getTopic() {
        return topic;
    }
}