package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import dev.ragnarok.fenrir.api.model.Identificable;

public final class AppChatUser implements Parcelable, Identificable {

    public static final Creator<AppChatUser> CREATOR = new Creator<AppChatUser>() {
        @Override
        public AppChatUser createFromParcel(Parcel in) {
            return new AppChatUser(in);
        }

        @Override
        public AppChatUser[] newArray(int size) {
            return new AppChatUser[size];
        }
    };
    private final Owner member;
    private final int invitedBy;
    private long join_date;
    private Owner inviter;
    private boolean canRemove;
    private boolean isAdmin;
    private boolean isOwner;

    public AppChatUser(Owner member, int invitedBy) {
        this.member = member;
        this.invitedBy = invitedBy;
    }

    private AppChatUser(Parcel in) {
        inviter = in.<ParcelableOwnerWrapper>readParcelable(ParcelableOwnerWrapper.class.getClassLoader()).get();
        member = in.<ParcelableOwnerWrapper>readParcelable(ParcelableOwnerWrapper.class.getClassLoader()).get();
        invitedBy = in.readInt();
        canRemove = in.readByte() != 0;
        join_date = in.readLong();
        isAdmin = in.readByte() != 0;
        isOwner = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(new ParcelableOwnerWrapper(inviter), flags);
        dest.writeParcelable(new ParcelableOwnerWrapper(member), flags);
        dest.writeInt(invitedBy);
        dest.writeByte((byte) (canRemove ? 1 : 0));
        dest.writeLong(join_date);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
        dest.writeByte((byte) (isOwner ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isCanRemove() {
        return canRemove;
    }

    public AppChatUser setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
        return this;
    }

    public Owner getInviter() {
        return inviter;
    }

    public AppChatUser setInviter(Owner inviter) {
        this.inviter = inviter;
        return this;
    }

    public int getInvitedBy() {
        return invitedBy;
    }

    public Owner getMember() {
        return member;
    }

    @Override
    public int getId() {
        return member.getOwnerId();
    }

    public long getJoin_date() {
        return join_date;
    }

    public AppChatUser setJoin_date(long join_date) {
        this.join_date = join_date;
        return this;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public AppChatUser setAdmin(boolean admin) {
        isAdmin = admin;
        return this;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public AppChatUser setOwner(boolean owner) {
        isOwner = owner;
        return this;
    }
}