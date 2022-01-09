package dev.ragnarok.fenrir.db.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class PrivacyEntity {

    @SerializedName("type")
    private String type;
    @SerializedName("entries")
    private List<Entry> entries;

    public PrivacyEntity set(String type, List<Entry> entries) {
        this.type = type;
        this.entries = entries;
        return this;
    }

    public String getType() {
        return type;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static final class Entry {
        @SerializedName("type")
        private int type;
        @SerializedName("id")
        private int id;
        @SerializedName("allowed")
        private boolean allowed;

        public Entry set(int type, int id, boolean allowed) {
            this.type = type;
            this.id = id;
            this.allowed = allowed;
            return this;
        }

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public boolean isAllowed() {
            return allowed;
        }
    }
}