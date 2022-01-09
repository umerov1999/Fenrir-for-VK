package dev.ragnarok.fenrir.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents owner of some VK object.
 */
public class VKApiOwner {

    /**
     * User or group
     */
    public final int owner_type;
    /**
     * User or group ID.
     */
    public int id;

    /**
     * Creates an owner with empty ID.
     */
    public VKApiOwner(int owner_type) {
        this.owner_type = owner_type;
    }

    public static List<VKApiOwner> createListFrom(List<? extends VKApiOwner> first, List<? extends VKApiOwner> second) {
        List<VKApiOwner> data = new ArrayList<>();
        if (first != null) {
            data.addAll(first);
        }

        if (second != null) {
            data.addAll(second);
        }

        return data;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getFullName() {
        return null;
    }

    public String getMaxSquareAvatar() {
        throw new IllegalStateException("Do implement the method in child classes");
    }

    public static class Type {
        public static final int USER = 1;
        public static final int COMMUNITY = 2;
    }
}