package dev.ragnarok.fenrir.api.model.feedback;

import dev.ragnarok.fenrir.api.model.VKApiComment;

public abstract class VkApiBaseFeedback {

    public String type;
    public long date;

    public VKApiComment reply;


}
