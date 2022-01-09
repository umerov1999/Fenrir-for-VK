package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class CallEntity extends Entity {
    private int initiator_id;
    private int receiver_id;
    private String state;
    private long time;

    public int getInitiator_id() {
        return initiator_id;
    }

    public CallEntity setInitiator_id(int initiator_id) {
        this.initiator_id = initiator_id;
        return this;
    }

    public int getReceiver_id() {
        return receiver_id;
    }

    public CallEntity setReceiver_id(int receiver_id) {
        this.receiver_id = receiver_id;
        return this;
    }

    public long getTime() {
        return time;
    }

    public CallEntity setTime(long time) {
        this.time = time;
        return this;
    }

    public String getState() {
        return state;
    }

    public CallEntity setState(String state) {
        this.state = state;
        return this;
    }
}
