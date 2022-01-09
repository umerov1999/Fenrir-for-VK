package dev.ragnarok.fenrir.model.criteria;

import dev.ragnarok.fenrir.db.DatabaseIdRange;

public class FavePhotosCriteria extends Criteria {

    private final int accountId;

    private DatabaseIdRange range;

    public FavePhotosCriteria(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public FavePhotosCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }
}
