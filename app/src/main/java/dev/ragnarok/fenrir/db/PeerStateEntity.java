package dev.ragnarok.fenrir.db;

public final class PeerStateEntity {

    private final int peerId;

    private int unreadCount;
    private int lastMessageId;
    private int inRead;
    private int outRead;

    public PeerStateEntity(int peerId) {
        this.peerId = peerId;
    }

    public int getPeerId() {
        return peerId;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public PeerStateEntity setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public PeerStateEntity setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public int getInRead() {
        return inRead;
    }

    public PeerStateEntity setInRead(int inRead) {
        this.inRead = inRead;
        return this;
    }

    public int getOutRead() {
        return outRead;
    }

    public PeerStateEntity setOutRead(int outRead) {
        this.outRead = outRead;
        return this;
    }
}