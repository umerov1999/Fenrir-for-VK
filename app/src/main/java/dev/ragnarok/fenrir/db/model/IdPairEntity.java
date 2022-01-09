package dev.ragnarok.fenrir.db.model;


import com.google.gson.annotations.SerializedName;

public class IdPairEntity {

    @SerializedName("id")
    private int id;
    @SerializedName("ownerId")
    private int ownerId;

    public IdPairEntity set(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return id;
    }
}