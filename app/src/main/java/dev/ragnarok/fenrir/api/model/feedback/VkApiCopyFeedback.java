package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Copyable;

public class VKApiCopyFeedback extends VKApiBaseFeedback {
    @Nullable
    public Copyable what;
    @Nullable
    public Copies copies;

}
