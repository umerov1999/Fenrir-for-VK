package dev.ragnarok.fenrir.longpoll;

import dev.ragnarok.fenrir.Includes;
import dev.ragnarok.fenrir.realtime.Processors;

public class LongpollInstance {

    private static volatile ILongpollManager longpollManager;

    public static ILongpollManager get() {
        if (longpollManager == null) {
            synchronized (LongpollInstance.class) {
                if (longpollManager == null) {
                    longpollManager = new AndroidLongpollManager(Includes.getNetworkInterfaces(), Processors.realtimeMessages());
                }
            }
        }
        return longpollManager;
    }
}