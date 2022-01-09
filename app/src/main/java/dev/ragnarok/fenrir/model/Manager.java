package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;


public final class Manager implements Parcelable {

    public static final Creator<Manager> CREATOR = new Creator<Manager>() {
        @Override
        public Manager createFromParcel(Parcel in) {
            return new Manager(in);
        }

        @Override
        public Manager[] newArray(int size) {
            return new Manager[size];
        }
    };
    private final User user;
    private final String role;
    private boolean displayAsContact;
    private ContactInfo contactInfo;

    public Manager(User user, String role) {
        this.user = user;
        this.role = role;
    }

    private Manager(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
        displayAsContact = in.readByte() != 0;
        role = in.readString();
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public Manager setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
        return this;
    }

    public boolean isDisplayAsContact() {
        return displayAsContact;
    }

    public Manager setDisplayAsContact(boolean displayAsContact) {
        this.displayAsContact = displayAsContact;
        return this;
    }

    public User getUser() {
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getRole() {
        return role;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
        dest.writeByte((byte) (displayAsContact ? 1 : 0));
        dest.writeString(role);
    }
}