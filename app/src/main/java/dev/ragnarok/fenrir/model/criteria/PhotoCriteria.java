package dev.ragnarok.fenrir.model.criteria;

import dev.ragnarok.fenrir.db.DatabaseIdRange;

public class PhotoCriteria {

    private final int accountId;
    private Integer ownerId;
    private Integer albumId;
    private String orderBy;
    private boolean sortInvert;

    private DatabaseIdRange range;

    public PhotoCriteria(int accountId) {
        this.accountId = accountId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public PhotoCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public boolean getSortInvert() {
        return sortInvert;
    }

    public PhotoCriteria setSortInvert(boolean invert) {
        sortInvert = invert;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public PhotoCriteria setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public PhotoCriteria setAlbumId(Integer albumId) {
        this.albumId = albumId;
        return this;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public PhotoCriteria setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }
}