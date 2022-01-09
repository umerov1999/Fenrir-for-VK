package dev.ragnarok.fenrir.model.criteria;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.db.DatabaseIdRange;

public class WallCriteria extends Criteria {

    public static final int MODE_ALL = 0;
    public static final int MODE_OWNER = 1;
    public static final int MODE_SCHEDULED = 2;
    public static final int MODE_SUGGEST = 3;

    private final int accountId;
    private final int ownerId;
    private DatabaseIdRange range;
    private int mode;

    public WallCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
        mode = MODE_ALL;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getMode() {
        return mode;
    }

    public WallCriteria setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public WallCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    @NonNull
    @Override
    public String toString() {
        return "WallCriteria{" +
                "accountId=" + accountId +
                ", range=" + range +
                ", ownerId=" + ownerId +
                ", mode=" + mode +
                '}';
    }
}
