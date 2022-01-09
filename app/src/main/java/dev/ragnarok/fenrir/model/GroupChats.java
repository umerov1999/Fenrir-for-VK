package dev.ragnarok.fenrir.model;

public class GroupChats {
    private final int id;
    private int members_count;
    private boolean is_closed;
    private String invite_link;
    private String photo;
    private String title;
    private long lastUpdateTime;

    public GroupChats(int groupId) {
        id = groupId;
    }

    public int getId() {
        return id;
    }

    public int getMembers_count() {
        return members_count;
    }

    public GroupChats setMembers_count(int members_count) {
        this.members_count = members_count;
        return this;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public GroupChats setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
        return this;
    }

    public boolean isIs_closed() {
        return is_closed;
    }

    public GroupChats setIs_closed(boolean is_closed) {
        this.is_closed = is_closed;
        return this;
    }

    public String getInvite_link() {
        return invite_link;
    }

    public GroupChats setInvite_link(String invite_link) {
        this.invite_link = invite_link;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public GroupChats setPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public GroupChats setTitle(String title) {
        this.title = title;
        return this;
    }
}
