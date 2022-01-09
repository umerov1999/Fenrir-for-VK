package dev.ragnarok.fenrir.api.model;


public class Captcha {

    private final String sid;

    private final String img;

    public Captcha(String sid, String img) {
        this.sid = sid;
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public String getSid() {
        return sid;
    }
}
