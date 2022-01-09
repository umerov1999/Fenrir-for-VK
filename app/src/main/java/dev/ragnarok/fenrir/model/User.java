package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import android.os.Parcel;

import dev.ragnarok.fenrir.CheckDonate;
import dev.ragnarok.fenrir.api.model.Identificable;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class User extends Owner implements Identificable {

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    public static final ParcelNative.Creator<User> NativeCreator = User::new;
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
    @UserPlatform
    private int platform;
    private String status;
    @Sex
    private int sex;
    private String domain;
    private String maiden_name;
    private boolean friend;
    private int friendStatus;
    private boolean can_write_private_message;
    private boolean blacklisted_by_me;
    private boolean blacklisted;
    private boolean verified;
    private boolean can_access_closed;

    public User(int id) {
        super(OwnerType.USER);
        this.id = id;
    }

    protected User(Parcel in) {
        super(in);
        id = in.readInt();
        firstName = in.readString();
        lastName = in.readString();
        online = in.readByte() != 0;
        onlineMobile = in.readByte() != 0;
        onlineApp = in.readInt();
        photo50 = in.readString();
        photo100 = in.readString();
        photo200 = in.readString();
        photoMax = in.readString();
        lastSeen = in.readLong();
        //noinspection ResourceType
        platform = in.readInt();
        status = in.readString();
        //noinspection ResourceType
        sex = in.readInt();
        domain = in.readString();
        maiden_name = in.readString();
        friend = in.readByte() != 0;
        friendStatus = in.readInt();
        can_write_private_message = in.readByte() != 0;
        blacklisted_by_me = in.readByte() != 0;
        blacklisted = in.readByte() != 0;
        verified = in.readByte() != 0;
        can_access_closed = in.readByte() != 0;
    }

    protected User(ParcelNative in) {
        super(in);
        id = in.readInt();
        firstName = in.readString();
        lastName = in.readString();
        online = in.readByte() != 0;
        onlineMobile = in.readByte() != 0;
        onlineApp = in.readInt();
        photo50 = in.readString();
        photo100 = in.readString();
        photo200 = in.readString();
        photoMax = in.readString();
        lastSeen = in.readLong();
        //noinspection ResourceType
        platform = in.readInt();
        status = in.readString();
        //noinspection ResourceType
        sex = in.readInt();
        domain = in.readString();
        maiden_name = in.readString();
        friend = in.readByte() != 0;
        friendStatus = in.readInt();
        can_write_private_message = in.readByte() != 0;
        blacklisted_by_me = in.readByte() != 0;
        blacklisted = in.readByte() != 0;
        verified = in.readByte() != 0;
        can_access_closed = in.readByte() != 0;
    }

    @Override
    public String getFullName() {
        String custom = Settings.get().other().getUserNameChanges(id);
        if (!Utils.isEmpty(custom)) {
            return custom;
        }
        return firstName + " " + lastName;
    }

    public String getShortFullName() {
        if (!Utils.isEmpty(firstName)) {
            return lastName + " " + firstName.charAt(0) + ".";
        }
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public boolean isOnline() {
        return online;
    }

    public User setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public boolean isOnlineMobile() {
        return onlineMobile;
    }

    public User setOnlineMobile(boolean onlineMobile) {
        this.onlineMobile = onlineMobile;
        return this;
    }

    public int getOnlineApp() {
        return onlineApp;
    }

    public User setOnlineApp(int onlineApp) {
        this.onlineApp = onlineApp;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public User setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public User setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public User setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public String getPhotoMax() {
        return photoMax;
    }

    public User setPhotoMax(String photoMax) {
        this.photoMax = photoMax;
        return this;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public User setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
        return this;
    }

    @UserPlatform
    public int getPlatform() {
        return platform;
    }

    public User setPlatform(@UserPlatform int platform) {
        this.platform = platform;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public User setStatus(String status) {
        this.status = status;
        return this;
    }

    @Sex
    public int getSex() {
        return sex;
    }

    public User setSex(@Sex int sex) {
        this.sex = sex;
        return this;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public User setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public boolean isFriend() {
        return friend;
    }

    public User setFriend(boolean friend) {
        this.friend = friend;
        return this;
    }

    public int getFriendStatus() {
        return friendStatus;
    }

    public User setFriendStatus(int friendStatus) {
        this.friendStatus = friendStatus;
        return this;
    }

    public boolean getCanWritePrivateMessage() {
        return can_write_private_message;
    }

    public User setCanWritePrivateMessage(boolean can_write_private_message) {
        this.can_write_private_message = can_write_private_message;
        return this;
    }

    public boolean getBlacklisted_by_me() {
        return blacklisted_by_me;
    }

    public User setBlacklisted_by_me(boolean blacklisted_by_me) {
        this.blacklisted_by_me = blacklisted_by_me;
        return this;
    }

    public boolean getBlacklisted() {
        return blacklisted;
    }

    public User setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
        return this;
    }

    @Override
    public boolean isDonated() {
        return Utils.isValueAssigned(getOwnerId(), CheckDonate.donatedOwnersLocal);
    }

    @Override
    public boolean isVerified() {
        return verified || isDonated();
    }

    public User setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public boolean isCan_access_closed() {
        return can_access_closed;
    }

    public User setCan_access_closed(boolean can_access_closed) {
        this.can_access_closed = can_access_closed;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeByte((byte) (onlineMobile ? 1 : 0));
        dest.writeInt(onlineApp);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeString(photoMax);
        dest.writeLong(lastSeen);
        dest.writeInt(platform);
        dest.writeString(status);
        dest.writeInt(sex);
        dest.writeString(domain);
        dest.writeString(maiden_name);
        dest.writeByte((byte) (friend ? 1 : 0));
        dest.writeInt(friendStatus);
        dest.writeByte((byte) (can_write_private_message ? 1 : 0));
        dest.writeByte((byte) (blacklisted_by_me ? 1 : 0));
        dest.writeByte((byte) (blacklisted ? 1 : 0));
        dest.writeByte((byte) (verified ? 1 : 0));
        dest.writeByte((byte) (can_access_closed ? 1 : 0));
    }

    @Override
    public void writeToParcelNative(ParcelNative dest) {
        super.writeToParcelNative(dest);
        dest.writeInt(id);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeByte((byte) (onlineMobile ? 1 : 0));
        dest.writeInt(onlineApp);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeString(photoMax);
        dest.writeLong(lastSeen);
        dest.writeInt(platform);
        dest.writeString(status);
        dest.writeInt(sex);
        dest.writeString(domain);
        dest.writeString(maiden_name);
        dest.writeByte((byte) (friend ? 1 : 0));
        dest.writeInt(friendStatus);
        dest.writeByte((byte) (can_write_private_message ? 1 : 0));
        dest.writeByte((byte) (blacklisted_by_me ? 1 : 0));
        dest.writeByte((byte) (blacklisted ? 1 : 0));
        dest.writeByte((byte) (verified ? 1 : 0));
        dest.writeByte((byte) (can_access_closed ? 1 : 0));
    }

    @Override
    public int getOwnerId() {
        return Math.abs(id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    @Override
    public String get100photoOrSmaller() {
        return firstNonEmptyString(photo100, photo50);
    }

    @Override
    public String getMaxSquareAvatar() {
        return firstNonEmptyString(photo200, photo100, photo50);
    }

    @Override
    public String getOriginalAvatar() {
        return firstNonEmptyString(photoMax, photo200, photo100, photo50);
    }

    public String getMaiden_name() {
        return maiden_name;
    }

    public User setMaiden_name(String maiden_name) {
        this.maiden_name = maiden_name;
        return this;
    }
}
