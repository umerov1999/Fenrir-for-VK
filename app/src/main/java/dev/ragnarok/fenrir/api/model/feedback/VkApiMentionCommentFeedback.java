package dev.ragnarok.fenrir.api.model.feedback;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VkApiMentionCommentFeedback extends VkApiBaseFeedback {

    public VKApiComment where;
    public Commentable comment_of;

}
