package dev.ragnarok.fenrir.api.model.response;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiOwner;

public class LikesListResponse {

    public int count;

    @Nullable
    public List<VKApiOwner> owners;

}
