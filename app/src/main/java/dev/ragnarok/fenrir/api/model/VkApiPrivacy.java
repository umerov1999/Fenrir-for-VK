package dev.ragnarok.fenrir.api.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.api.util.VKStringUtils;

public class VkApiPrivacy {

    public String category;
    public ArrayList<Entry> entries;

    public VkApiPrivacy(String category) {
        this.category = category;
        entries = new ArrayList<>();
    }

    private void putIfNotExist(Entry entry) {
        if (!entries.contains(entry)) {
            entries.add(entry);
        }
    }

    public String buildJsonArray() {
        String typeValue = category;
        String entriesLine = VKStringUtils.join(", ", entries);

        ArrayList<String> option = new ArrayList<>();

        if (typeValue != null && !typeValue.isEmpty()) {
            option.add(typeValue);
        }

        if (!entriesLine.isEmpty()) {
            option.add(entriesLine);
        }

        return VKStringUtils.join(", ", option);
    }

    @NonNull
    @Override
    public String toString() {
        return buildJsonArray();
    }

    public void includeOwner(int id) {
        putIfNotExist(Entry.includedOwner(id));
    }

    public void excludeOwner(int id) {
        putIfNotExist(Entry.excludedOwner(id));
    }

    public void includeFriendsList(int id) {
        putIfNotExist(Entry.includedFriendsList(id));
    }

    public void excludeFriendsList(int id) {
        putIfNotExist(Entry.excludedFriendsList(id));
    }

    public static class Entry {

        public static final int TYPE_OWNER = 1;
        public static final int TYPE_FRIENDS_LIST = 2;

        public int type;
        public int id;
        public boolean allowed;

        public Entry(int type, int id, boolean allowed) {
            this.type = type;
            this.id = id;
            this.allowed = allowed;
        }

        public static Entry excludedOwner(int id) {
            return new Entry(TYPE_OWNER, id, false);
        }

        public static Entry includedOwner(int id) {
            return new Entry(TYPE_OWNER, id, true);
        }

        public static Entry includedFriendsList(int id) {
            return new Entry(TYPE_FRIENDS_LIST, id, true);
        }

        public static Entry excludedFriendsList(int id) {
            return new Entry(TYPE_FRIENDS_LIST, id, false);
        }

        @NonNull
        @Override
        public String toString() {
            switch (type) {
                case TYPE_FRIENDS_LIST:
                    return allowed ? "list" + id : "-list" + id;
                case TYPE_OWNER:
                    return allowed ? String.valueOf(id) : String.valueOf(-id);
                default:
                    throw new IllegalStateException("Unknown type");
            }
        }
    }
}
