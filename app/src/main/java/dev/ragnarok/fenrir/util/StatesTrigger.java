package dev.ragnarok.fenrir.util;


public class StatesTrigger {
    private final int max_state;
    private int state;

    public StatesTrigger(int states_count) {
        max_state = states_count;
        state = 0;
    }

    public StatesTrigger doTrigger() {
        state++;
        if (state == max_state) {
            state = 0;
        }
        return this;
    }

    public int getState() {
        return state;
    }

    public int getMax_state() {
        return max_state;
    }
}
