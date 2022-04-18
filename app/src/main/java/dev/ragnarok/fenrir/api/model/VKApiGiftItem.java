package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;

public class VKApiGiftItem implements VKApiAttachment {
    public int id;
    public String thumb_256;
    public String thumb_96;
    public String thumb_48;

    public VKApiGiftItem() {
    }

    @NonNull
    @Override
    public String getType() {
        return VKApiAttachment.TYPE_GIFT;
    }
}
