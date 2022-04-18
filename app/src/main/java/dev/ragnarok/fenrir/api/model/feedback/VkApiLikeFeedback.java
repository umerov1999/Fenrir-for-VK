package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.Likeable;

public class VKApiLikeFeedback extends VKApiBaseFeedback {
    @Nullable
    public UserArray users;
    @Nullable
    public Likeable liked;

}