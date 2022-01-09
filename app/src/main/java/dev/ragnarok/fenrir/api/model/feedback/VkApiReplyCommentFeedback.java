package dev.ragnarok.fenrir.api.model.feedback;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VkApiReplyCommentFeedback extends VkApiBaseFeedback {

    public Commentable comments_of;

    public VKApiComment own_comment;

    public VKApiComment feedback_comment;
}
