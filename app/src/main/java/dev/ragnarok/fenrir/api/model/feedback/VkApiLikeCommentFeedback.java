package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VKApiLikeCommentFeedback extends VKApiBaseFeedback {
    @Nullable
    public UserArray users;
    @Nullable
    public VKApiComment comment;
    @Nullable
    public Commentable commented;
}
