package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.VKApiPost;

/**
 * Base class for types [mention_comments, mention, mention_comment_photo, mention_comment_video]
 */
public class VKApiMentionWallFeedback extends VKApiBaseFeedback {
    @Nullable
    public VKApiPost post;
}
