package dev.ragnarok.fenrir.api.model;

import androidx.annotation.Nullable;

public class ChatUserDto {

    @Nullable
    public VKApiOwner user;

    public int invited_by;

    @Nullable
    public String type;
}
