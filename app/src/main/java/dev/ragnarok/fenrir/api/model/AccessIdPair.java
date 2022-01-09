package dev.ragnarok.fenrir.api.model;


public class AccessIdPair {

    public final int id;

    public final int ownerId;

    public final String accessKey;

    public AccessIdPair(int id, int ownerId, String accessKey) {
        this.id = id;
        this.ownerId = ownerId;
        this.accessKey = accessKey;
    }

    public static String format(AccessIdPair pair) {
        return pair.ownerId + "_" + pair.id + (pair.accessKey == null ? "" : "_" + pair.accessKey);

    }
}
