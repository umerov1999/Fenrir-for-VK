package dev.ragnarok.fenrir.db;

import dev.ragnarok.fenrir.Includes;
import dev.ragnarok.fenrir.db.interfaces.IStorages;

public class Stores {

    public static IStorages getInstance() {
        return Includes.getStores();
    }

}
