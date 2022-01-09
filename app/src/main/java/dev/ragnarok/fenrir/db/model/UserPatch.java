package dev.ragnarok.fenrir.db.model;


public class UserPatch {

    private final int userId;

    private Status status;

    private Online online;

    public UserPatch(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public UserPatch setOnlineUpdate(Online online) {
        this.online = online;
        return this;
    }

    public Online getOnline() {
        return online;
    }

    public Status getStatus() {
        return status;
    }

    public UserPatch setStatus(Status status) {
        this.status = status;
        return this;
    }

    public static final class Online {

        private final boolean online;
        private final long lastSeen;
        private final int platform;

        public Online(boolean online, long lastSeen, int platform) {
            this.online = online;
            this.lastSeen = lastSeen;
            this.platform = platform;
        }

        public int getPlatform() {
            return platform;
        }

        public boolean isOnline() {
            return online;
        }

        public long getLastSeen() {
            return lastSeen;
        }
    }

    public static final class Status {

        private final String status;

        public Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}