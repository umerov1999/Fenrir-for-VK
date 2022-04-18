package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VKApiArticle implements VKApiAttachment {
    public int id;
    public int owner_id;
    @Nullable
    public String owner_name;
    public String url;
    @Nullable
    public String title;
    @Nullable
    public String subtitle;
    @Nullable
    public VKApiPhoto photo;
    public boolean is_favorite;
    @Nullable
    public String access_key;

    public VKApiArticle() {

    }

    @NonNull
    @Override
    public String getType() {
        return VKApiAttachment.TYPE_ARTICLE;
    }
}
