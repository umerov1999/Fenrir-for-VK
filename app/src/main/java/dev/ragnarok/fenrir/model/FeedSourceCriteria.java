package dev.ragnarok.fenrir.model;

import dev.ragnarok.fenrir.model.criteria.Criteria;

public class FeedSourceCriteria extends Criteria {

    private final int accountId;

    public FeedSourceCriteria(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}
