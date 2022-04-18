package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Commentable;
import dev.ragnarok.fenrir.api.model.VKApiComment;

public class VKApiMentionCommentFeedback extends VKApiBaseFeedback {
    @Nullable
    public VKApiComment where;
    @Nullable
    public Commentable comment_of;

}
