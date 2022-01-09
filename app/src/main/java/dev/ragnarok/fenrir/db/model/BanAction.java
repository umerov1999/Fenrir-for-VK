package dev.ragnarok.fenrir.db.model;


public class BanAction {

    private final int groupId;

    private final int ownerId;

    private final boolean ban;

    public BanAction(int groupId, int ownerId, boolean ban) {
        this.groupId = groupId;
        this.ownerId = ownerId;
        this.ban = ban;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public boolean isBan() {
        return ban;
    }
}