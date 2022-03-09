package dev.ragnarok.fenrir.settings;

import dev.ragnarok.fenrir.Includes;

public class Settings {

    public static ISettings get() {
        return Includes.getSettings();
    }

}
