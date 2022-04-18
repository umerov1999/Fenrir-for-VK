package dev.ragnarok.fenrir.api.model;


import androidx.annotation.Nullable;

public class Captcha {
    @Nullable
    private final String sid;

    @Nullable
    private final String img;

    public Captcha(@Nullable String sid, @Nullable String img) {
        this.sid = sid;
        this.img = img;
    }

    @Nullable
    public String getImg() {
        return img;
    }

    @Nullable
    public String getSid() {
        return sid;
    }
}
