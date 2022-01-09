package dev.ragnarok.fenrir.api.model.feedback;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VkApiLikeCommentFeedback extends VkApiBaseFeedback {

    public UserArray users;

    public VKApiComment comment;

    public Commentable commented;
}
