package dev.ragnarok.fenrir.model;

public final class LastReadId {

    private int outgoing;

    private int incoming;

    public LastReadId(int outgoing, int incoming) {
        this.outgoing = outgoing;
        this.incoming = incoming;
    }

    public int getOutgoing() {
        return outgoing;
    }

    public LastReadId setOutgoing(int outgoing) {
        this.outgoing = outgoing;
        return this;
    }

    public int getIncoming() {
        return incoming;
    }

    public LastReadId setIncoming(int in) {
        incoming = in;
        return this;
    }
}