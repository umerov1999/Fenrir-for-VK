package dev.ragnarok.fenrir.longpoll;

public interface ILongpoll {
    int getAccountId();

    void connect();

    void shutdown();
}