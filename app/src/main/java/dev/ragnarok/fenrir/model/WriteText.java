package dev.ragnarok.fenrir.model;

import java.util.ArrayList;
import java.util.List;

public class WriteText {

    private final int accountId;

    private final int peerId;

    private final boolean isText;

    private final List<Integer> from_ids;

    public WriteText(int accountId, int peerId, int[] from_ids, boolean isText) {
        this.accountId = accountId;
        this.isText = isText;
        this.from_ids = new ArrayList<>();
        if (from_ids != null && from_ids.length > 0) {
            for (int from_id : from_ids) {
                if (accountId != from_id) {
                    this.from_ids.add(from_id);
                }
            }
        }
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public List<Integer> getFrom_ids() {
        return from_ids;
    }

    public int getAccountId() {
        return accountId;
    }

    public boolean getIsText() {
        return isText;
    }
}