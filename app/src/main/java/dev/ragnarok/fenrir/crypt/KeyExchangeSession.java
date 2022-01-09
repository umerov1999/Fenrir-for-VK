package dev.ragnarok.fenrir.crypt;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KeyExchangeSession {

    private final long id;

    private final int accountId;

    private final int peerId;
    @KeyLocationPolicy
    private final int keyLocationPolicy;
    private final Set<Integer> messageIds = new HashSet<>();
    @SessionState
    private int localSessionState;
    @SessionState
    private int oppenentSessionState;
    private PrivateKey myPrivateKey;
    private String myAesKey;
    private String hisAesKey;

    private KeyExchangeSession(long id, int accountId, int peerId, @KeyLocationPolicy int keyLocationPolicy) {
        this.id = id;
        this.accountId = accountId;
        this.peerId = peerId;
        this.keyLocationPolicy = keyLocationPolicy;
    }

    private static long generateNewId() {
        return System.currentTimeMillis();
    }

    public static KeyExchangeSession createOutSession(long id, int accountId, int peerId, @KeyLocationPolicy int keyLocationPolicy) {
        KeyExchangeSession session = new KeyExchangeSession(id, accountId, peerId, keyLocationPolicy);
        session.localSessionState = SessionState.INITIATOR_EMPTY;
        return session;
    }

    public static KeyExchangeSession createInputSession(long id, int accountId, int peerId, @KeyLocationPolicy int keyLocationPolicy) {
        KeyExchangeSession session = new KeyExchangeSession(id, accountId, peerId, keyLocationPolicy);
        session.localSessionState = SessionState.NO_INITIATOR_EMPTY;
        return session;
    }

    public long getId() {
        return id;
    }

    @SessionState
    public int getLocalSessionState() {
        return localSessionState;
    }

    public KeyExchangeSession setLocalSessionState(int localSessionState) {
        this.localSessionState = localSessionState;
        return this;
    }

    public PrivateKey getMyPrivateKey() {
        return myPrivateKey;
    }

    public void setMyPrivateKey(PrivateKey myPrivateKey) {
        this.myPrivateKey = myPrivateKey;
    }

    public String getMyAesKey() {
        return myAesKey;
    }

    public void setMyAesKey(String myAesKey) {
        this.myAesKey = myAesKey;
    }

    public String getHisAesKey() {
        return hisAesKey;
    }

    public void setHisAesKey(String hisAesKey) {
        this.hisAesKey = hisAesKey;
    }

    public void appendMessageId(int id) {
        messageIds.add(id);
    }

    public boolean isMessageProcessed(int id) {
        return messageIds.contains(id);
    }

    public int getStartMessageId() {
        return Collections.min(messageIds);
    }

    public int getEndMessageId() {
        return Collections.max(messageIds);
    }

    public int getAccountId() {
        return accountId;
    }

    public int getPeerId() {
        return peerId;
    }

    @SessionState
    public int getOppenentSessionState() {
        return oppenentSessionState;
    }

    public void setOppenentSessionState(@SessionState int oppenentSessionState) {
        this.oppenentSessionState = oppenentSessionState;
    }

    @KeyLocationPolicy
    public int getKeyLocationPolicy() {
        return keyLocationPolicy;
    }
}
