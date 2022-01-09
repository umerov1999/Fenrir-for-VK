package dev.ragnarok.fenrir.db.model.entity;


import dev.ragnarok.fenrir.model.Sex;
import dev.ragnarok.fenrir.model.UserPlatform;

public class UserEntity {

    private final int id;

    private String firstName;

    private String lastName;

    private boolean online;

    private boolean onlineMobile;

    private int onlineApp;

    private String photo50;

    private String photo100;

    private String photo200;

    private String photoMax;

    private long lastSeen;

    private @UserPlatform
    int platform;

    private String status;

    private @Sex
    int sex;

    private String domain;

    private String maiden_name;

    private boolean friend;
    private int friendStatus;
    private boolean can_write_private_message;
    private boolean blacklisted_by_me;
    private boolean blacklisted;
    private boolean verified;
    private boolean can_access_closed;

    public UserEntity(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public UserEntity setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public UserEntity setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public boolean isOnline() {
        return online;
    }

    public UserEntity setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public boolean isOnlineMobile() {
        return onlineMobile;
    }

    public UserEntity setOnlineMobile(boolean onlineMobile) {
        this.onlineMobile = onlineMobile;
        return this;
    }

    public int getOnlineApp() {
        return onlineApp;
    }

    public UserEntity setOnlineApp(int onlineApp) {
        this.onlineApp = onlineApp;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public UserEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public UserEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public UserEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public UserEntity setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    public @UserPlatform
    int getPlatform() {
        return platform;
    }

    public UserEntity setPlatform(@UserPlatform int platform) {
        this.platform = platform;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public @Sex
    int getSex() {
        return sex;
    }

    public UserEntity setSex(@Sex int sex) {
        this.sex = sex;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public UserEntity setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public boolean isFriend() {
        return friend;
    }

    public UserEntity setFriend(boolean friend) {
        this.friend = friend;
        return this;
    }

    public String getPhotoMax() {
        return photoMax;
    }

    public UserEntity setPhotoMax(String photoMax) {
        this.photoMax = photoMax;
        return this;
    }

    public boolean getCanWritePrivateMessage() {
        return can_write_private_message;
    }

    public UserEntity setCanWritePrivateMessage(boolean can_write_private_message) {
        this.can_write_private_message = can_write_private_message;
        return this;
    }

    public boolean getBlacklisted_by_me() {
        return blacklisted_by_me;
    }

    public UserEntity setBlacklisted_by_me(boolean blacklisted_by_me) {
        this.blacklisted_by_me = blacklisted_by_me;
        return this;
    }

    public boolean getBlacklisted() {
        return blacklisted;
    }

    public UserEntity setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
        return this;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    public UserEntity setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
        return this;
    }

    public boolean isVerified() {
        return verified;
    }

    public UserEntity setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public boolean isCan_access_closed() {
        return can_access_closed;
    }

    public UserEntity setCan_access_closed(boolean can_access_closed) {
        this.can_access_closed = can_access_closed;
        return this;
    }

    public String getMaiden_name() {
        return maiden_name;
    }

    public UserEntity setMaiden_name(String maiden_name) {
        this.maiden_name = maiden_name;
        return this;
    }
}