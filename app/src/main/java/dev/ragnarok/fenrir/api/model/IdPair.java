package dev.ragnarok.fenrir.api.model;


public class IdPair {

    public final int id;

    public final int ownerId;

    public IdPair(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }
}
