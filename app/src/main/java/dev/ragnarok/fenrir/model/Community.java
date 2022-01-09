package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import android.os.Parcel;

import dev.ragnarok.fenrir.CheckDonate;
import dev.ragnarok.fenrir.module.parcel.ParcelNative;
import dev.ragnarok.fenrir.util.Utils;


public class Community extends Owner {

    public static final Creator<Community> CREATOR = new Creator<Community>() {
        @Override
        public Community createFromParcel(Parcel in) {
            return new Community(in);
        }

        @Override
        public Community[] newArray(int size) {
            return new Community[size];
        }
    };
    public static final ParcelNative.Creator<Community> NativeCreator = Community::new;
    private final int id;
    private String name;
    private String screenName;
    private int closed;
    private boolean admin;
    private int adminLevel;
    private boolean member;
    private int membersCount;
    private int memberStatus;
    private int type;
    private String photo50;
    private String photo100;
    private String photo200;
    private boolean verified;

    public Community(int id) {
        super(OwnerType.COMMUNITY);
        this.id = id;
    }

    protected Community(Parcel in) {
        super(in);
        id = in.readInt();
        name = in.readString();
        screenName = in.readString();
        closed = in.readInt();
        admin = in.readByte() != 0;
        adminLevel = in.readInt();
        member = in.readByte() != 0;
        membersCount = in.readInt();
        memberStatus = in.readInt();
        type = in.readInt();
        photo50 = in.readString();
        photo100 = in.readString();
        photo200 = in.readString();
        verified = in.readByte() != 0;
    }

    protected Community(ParcelNative in) {
        super(in);
        id = in.readInt();
        name = in.readString();
        screenName = in.readString();
        closed = in.readInt();
        admin = in.readByte() != 0;
        adminLevel = in.readInt();
        member = in.readByte() != 0;
        membersCount = in.readInt();
        memberStatus = in.readInt();
        type = in.readInt();
        photo50 = in.readString();
        photo100 = in.readString();
        photo200 = in.readString();
        verified = in.readByte() != 0;
    }

    @Override
    public String getFullName() {
        return name;
    }

    @Override
    public int getOwnerId() {
        return -Math.abs(id);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(screenName);
        dest.writeInt(closed);
        dest.writeByte((byte) (admin ? 1 : 0));
        dest.writeInt(adminLevel);
        dest.writeByte((byte) (member ? 1 : 0));
        dest.writeInt(membersCount);
        dest.writeInt(memberStatus);
        dest.writeInt(type);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeByte((byte) (verified ? 1 : 0));
    }

    @Override
    public void writeToParcelNative(ParcelNative dest) {
        super.writeToParcelNative(dest);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(screenName);
        dest.writeInt(closed);
        dest.writeByte((byte) (admin ? 1 : 0));
        dest.writeInt(adminLevel);
        dest.writeByte((byte) (member ? 1 : 0));
        dest.writeInt(membersCount);
        dest.writeInt(memberStatus);
        dest.writeInt(type);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeByte((byte) (verified ? 1 : 0));
    }

    public String getName() {
        return name;
    }

    public Community setName(String name) {
        this.name = name;
        return this;
    }

    public String getScreenName() {
        return screenName;
    }

    public Community setScreenName(String screenName) {
        this.screenName = screenName;
        return this;
    }

    public int getClosed() {
        return closed;
    }

    public Community setClosed(int closed) {
        this.closed = closed;
        return this;
    }

    public boolean isAdmin() {
        return admin;
    }

    public Community setAdmin(boolean admin) {
        this.admin = admin;
        return this;
    }

    public int getAdminLevel() {
        return adminLevel;
    }

    public Community setAdminLevel(int adminLevel) {
        this.adminLevel = adminLevel;
        return this;
    }

    public boolean isMember() {
        return member;
    }

    public Community setMember(boolean member) {
        this.member = member;
        return this;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public Community setMembersCount(int membersCount) {
        this.membersCount = membersCount;
        return this;
    }

    public int getMemberStatus() {
        return memberStatus;
    }

    public Community setMemberStatus(int memberStatus) {
        this.memberStatus = memberStatus;
        return this;
    }

    public int getType() {
        return type;
    }

    public Community setType(int type) {
        this.type = type;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public Community setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public Community setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public Community setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    @Override
    public String get100photoOrSmaller() {
        return firstNonEmptyString(photo100, photo50);
    }

    @Override
    public String getDomain() {
        return screenName;
    }

    @Override
    public String getMaxSquareAvatar() {
        return firstNonEmptyString(photo200, photo100, photo50);
    }

    @Override
    public String getOriginalAvatar() {
        return getMaxSquareAvatar();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean isDonated() {
        return Utils.isValueAssigned(getOwnerId(), CheckDonate.donatedOwnersLocal);
    }

    @Override
    public boolean isVerified() {
        return verified || isDonated();
    }

    public Community setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }
}