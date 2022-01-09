package dev.ragnarok.fenrir.model.criteria;

public class DocsCriteria extends Criteria {

    private final int accountId;

    private final int ownerId;

    private Integer filter;

    public DocsCriteria(int accountId, int ownerId) {
        this.accountId = accountId;
        this.ownerId = ownerId;
    }

    public Integer getFilter() {
        return filter;
    }

    public DocsCriteria setFilter(Integer filter) {
        this.filter = filter;
        return this;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getAccountId() {
        return accountId;
    }
}
