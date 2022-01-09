package dev.ragnarok.fenrir.link.types;

public class TopicLink extends AbsLink {

    public final int ownerId;
    public final int topicId;

    public TopicLink(int topicId, int ownerId) {
        super(TOPIC);
        this.topicId = topicId;
        this.ownerId = -Math.abs(ownerId); // group only
    }
}