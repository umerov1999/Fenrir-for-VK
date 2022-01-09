package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class GiftItemEntity extends Entity {
    private int id;
    private String thumb256;
    private String thumb96;
    private String thumb48;

    public int getId() {
        return id;
    }

    public GiftItemEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getThumb256() {
        return thumb256;
    }

    public GiftItemEntity setThumb256(String thumb256) {
        this.thumb256 = thumb256;
        return this;
    }

    public String getThumb96() {
        return thumb96;
    }

    public GiftItemEntity setThumb96(String thumb96) {
        this.thumb96 = thumb96;
        return this;
    }

    public String getThumb48() {
        return thumb48;
    }

    public GiftItemEntity setThumb48(String thumb48) {
        this.thumb48 = thumb48;
        return this;
    }
}
