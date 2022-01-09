package dev.ragnarok.fenrir.realtime;

import static dev.ragnarok.fenrir.util.Objects.isNull;

public final class Processors {

    private static IRealtimeMessagesProcessor realtimeMessagesProcessor;

    public static IRealtimeMessagesProcessor realtimeMessages() {
        if (isNull(realtimeMessagesProcessor)) {
            synchronized (Processors.class) {
                if (isNull(realtimeMessagesProcessor)) {
                    realtimeMessagesProcessor = new RealtimeMessagesProcessor();
                }
            }
        }
        return realtimeMessagesProcessor;
    }
}