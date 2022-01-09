package dev.ragnarok.fenrir.api.model.feedback;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VkApiCommentFeedback extends VkApiBaseFeedback {
    public Commentable comment_of;
    public VKApiComment comment;
}