package dev.ragnarok.fenrir.api.model.feedback;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.model.VKApiComment;

public abstract class VKApiBaseFeedback {

    public String type;
    public long date;

    @Nullable
    public VKApiComment reply;


}
