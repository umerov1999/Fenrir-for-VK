package dev.ragnarok.fenrir.api;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.interfaces.INetworker;

public class Apis {

    public static INetworker get() {
        return Injection.provideNetworkInterfaces();
    }

}
