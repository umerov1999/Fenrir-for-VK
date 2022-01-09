package dev.ragnarok.fenrir.model.criteria;

import java.util.Objects;

import dev.ragnarok.fenrir.db.DatabaseIdRange;

public class NotificationsCriteria extends Criteria {

    private final int accountId;

    private DatabaseIdRange range;

    public NotificationsCriteria(int accountId) {
        this.accountId = accountId;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public NotificationsCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotificationsCriteria that = (NotificationsCriteria) o;

        return accountId == that.accountId
                && (Objects.equals(range, that.range));
    }

    @Override
    public int hashCode() {
        int result = accountId;
        result = 31 * result + (range != null ? range.hashCode() : 0);
        return result;
    }
}
