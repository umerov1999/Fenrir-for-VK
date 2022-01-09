package dev.ragnarok.fenrir.realtime;

import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate;
import dev.ragnarok.fenrir.longpoll.FullAndNonFullUpdates;

public final class Entry {

    private final int id;
    private final int accountId;
    private final boolean ignoreIfExists;
    private final FullAndNonFullUpdates updates;

    public Entry(int accountId, int id, boolean ignoreIfExists) {
        this.id = id;
        this.accountId = accountId;
        this.ignoreIfExists = ignoreIfExists;
        updates = new FullAndNonFullUpdates();
    }

    public boolean has(int id) {
        if (updates.hasNonFullMessages()) {
            for (AddMessageUpdate nonFullId : updates.getNonFull()) {
                if (id == nonFullId.getMessageId()) {
                    return true;
                }
            }
        }

        if (updates.hasFullMessages()) {
            for (AddMessageUpdate update : updates.getFullMessages()) {
                if (update.getMessageId() == id) {
                    return true;
                }
            }
        }

        return false;
    }

    public int count() {
        return safeCountOf(updates.getFullMessages()) + safeCountOf(updates.getNonFull());
    }

    public boolean isIgnoreIfExists() {
        return ignoreIfExists;
    }

    public void append(AddMessageUpdate update) {
        if (update.isFull()) {
            updates.prepareFull().add(update);
        } else {
            updates.prepareNonFull().add(update);
        }
    }

    public void append(int messageId) {
        AddMessageUpdate u = new AddMessageUpdate();
        u.message_id = messageId;
        updates.prepareNonFull().add(u);
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public FullAndNonFullUpdates getUpdates() {
        return updates;
    }
}