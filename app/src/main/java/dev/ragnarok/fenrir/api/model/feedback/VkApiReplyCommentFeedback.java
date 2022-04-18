package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VKApiReplyCommentFeedback extends VKApiBaseFeedback {
    @Nullable
    public Commentable comments_of;
    @Nullable
    public VKApiComment own_comment;
    @Nullable
    public VKApiComment feedback_comment;
}
