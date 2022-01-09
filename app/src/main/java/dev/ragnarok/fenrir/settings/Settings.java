package dev.ragnarok.fenrir.settings;

import dev.ragnarok.fenrir.Injection;

public class Settings {

    public static ISettings get() {
        return Injection.provideSettings();
    }

}
