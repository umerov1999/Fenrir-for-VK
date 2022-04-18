package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VKApiCommentFeedback extends VKApiBaseFeedback {
    @Nullable
    public Commentable comment_of;
    @Nullable
    public VKApiComment comment;
}