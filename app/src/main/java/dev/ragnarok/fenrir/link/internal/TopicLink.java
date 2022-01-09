package dev.ragnarok.fenrir.link.internal;

import androidx.annotation.NonNull;

public class TopicLink extends AbsInternalLink {

    public int replyToOwner;
    public int topicOwnerId;
    public int replyToCommentId;

    @NonNull
    @Override
    public String toString() {
        return "TopicLink{" +
                "replyToOwner=" + replyToOwner +
                ", topicOwnerId=" + topicOwnerId +
                ", replyToCommentId=" + replyToCommentId +
                "} " + super.toString();
    }
}