package dev.ragnarok.fenrir.db.model.entity;


public class FriendListEntity {

    private final int id;

    private final String name;

    public FriendListEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}