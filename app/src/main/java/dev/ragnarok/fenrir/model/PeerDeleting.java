package dev.ragnarok.fenrir.model;

public final class PeerDeleting {

    private final int accountId;

    private final int peerId;

    public PeerDeleting(int accountId, int peerId) {
        this.accountId = accountId;
        this.peerId = peerId;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getPeerId() {
        return peerId;
    }
}