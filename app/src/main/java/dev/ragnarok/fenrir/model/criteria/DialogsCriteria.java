package dev.ragnarok.fenrir.model.criteria;

public class DialogsCriteria extends Criteria {

    private final int accountId;

    public DialogsCriteria(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}
