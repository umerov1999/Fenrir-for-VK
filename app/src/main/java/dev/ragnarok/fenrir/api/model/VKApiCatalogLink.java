package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;

public class VKApiCatalogLink implements VKApiAttachment {

    public String url;

    public String title;

    public String subtitle;

    public String preview_photo;

    public VKApiCatalogLink() {

    }

    @NonNull
    @Override
    public String getType() {
        return VKApiAttachment.TYPE_LINK;
    }
}
