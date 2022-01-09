package dev.ragnarok.fenrir.crypt;

import androidx.annotation.NonNull;

public class AesKeyPair {

    private int version;

    private int accountId;

    private int peerId;

    private long sessionId;

    private long date;

    private int startMessageId;

    private int endMessageId;

    private String myAesKey;

    private String hisAesKey;

    public int getAccountId() {
        return accountId;
    }

    public AesKeyPair setAccountId(int accountId) {
        this.accountId = accountId;
        return this;
    }

    public int getPeerId() {
        return peerId;
    }

    public AesKeyPair setPeerId(int peerId) {
        this.peerId = peerId;
        return this;
    }

    public long getSessionId() {
        return sessionId;
    }

    public AesKeyPair setSessionId(long sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public AesKeyPair setDate(long date) {
        this.date = date;
        return this;
    }

    public int getStartMessageId() {
        return startMessageId;
    }

    public AesKeyPair setStartMessageId(int startMessageId) {
        this.startMessageId = startMessageId;
        return this;
    }

    public int getEndMessageId() {
        return endMessageId;
    }

    public AesKeyPair setEndMessageId(int endMessageId) {
        this.endMessageId = endMessageId;
        return this;
    }

    public String getMyAesKey() {
        return myAesKey;
    }

    public AesKeyPair setMyAesKey(String myAesKey) {
        this.myAesKey = myAesKey;
        return this;
    }

    public String getHisAesKey() {
        return hisAesKey;
    }

    public AesKeyPair setHisAesKey(String hisAesKey) {
        this.hisAesKey = hisAesKey;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AesKeyPair pair = (AesKeyPair) o;
        return sessionId == pair.sessionId;
    }

    @Override
    public int hashCode() {
        return (int) (sessionId ^ (sessionId >>> 32));
    }

    @NonNull
    @Override
    public String toString() {
        return "AesKeyPair{" +
                "version=" + version +
                ", accountId=" + accountId +
                ", peerId=" + peerId +
                ", sessionId=" + sessionId +
                ", date=" + date +
                ", startMessageId=" + startMessageId +
                ", endMessageId=" + endMessageId +
                ", myAesKey='" + myAesKey + '\'' +
                ", hisAesKey='" + hisAesKey + '\'' +
                '}';
    }

    public int getVersion() {
        return version;
    }

    public AesKeyPair setVersion(int version) {
        this.version = version;
        return this;
    }
}
