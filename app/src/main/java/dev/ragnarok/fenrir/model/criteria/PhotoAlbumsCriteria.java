package dev.ragnarok.fenrir.model.criteria;

public class PhotoAlbumsCriteria extends Criteria {

    private final int accountId;
    private final int ownerId;

    public PhotoAlbumsCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getAccountId() {
        return accountId;
    }
}
