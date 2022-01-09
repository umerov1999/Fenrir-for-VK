package dev.ragnarok.fenrir.model;


public class SentMsg {

    private final int dbid;

    private final int vkid;

    private final int peerId;

    private final int accountId;

    public SentMsg(int dbid, int vkid, int peerId, int accountId) {
        this.dbid = dbid;
        this.vkid = vkid;
        this.peerId = peerId;
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getDbid() {
        return dbid;
    }

    public int getPeerId() {
        return peerId;
    }

    public int getVkid() {
        return vkid;
    }
}