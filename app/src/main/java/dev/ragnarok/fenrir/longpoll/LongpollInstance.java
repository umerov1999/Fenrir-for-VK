package dev.ragnarok.fenrir.longpoll;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.realtime.Processors;

public class LongpollInstance {

    private static volatile ILongpollManager longpollManager;

    public static ILongpollManager get() {
        if (longpollManager == null) {
            synchronized (LongpollInstance.class) {
                if (longpollManager == null) {
                    longpollManager = new AndroidLongpollManager(Injection.provideNetworkInterfaces(), Processors.realtimeMessages());
                }
            }
        }
        return longpollManager;
    }
}