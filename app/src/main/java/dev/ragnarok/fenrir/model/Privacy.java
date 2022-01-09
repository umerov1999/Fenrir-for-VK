package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.join;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;

public class Privacy implements Parcelable, Cloneable {

    public static final Creator<Privacy> CREATOR = new Creator<Privacy>() {
        @Override
        public Privacy createFromParcel(Parcel in) {
            return new Privacy(in);
        }

        @Override
        public Privacy[] newArray(int size) {
            return new Privacy[size];
        }
    };
    private String type;
    private ArrayList<User> allowedUsers;
    private ArrayList<User> disallowedUsers;
    private ArrayList<FriendList> allowedLists;
    private ArrayList<FriendList> disallowedLists;

    public Privacy(String type) {
        this.type = type;
        allowedUsers = new ArrayList<>();
        disallowedUsers = new ArrayList<>();
        allowedLists = new ArrayList<>();
        disallowedLists = new ArrayList<>();
    }

    public Privacy() {
        this(Type.ALL);
    }

    protected Privacy(Parcel in) {
        type = in.readString();
        allowedUsers = in.createTypedArrayList(User.CREATOR);
        disallowedUsers = in.createTypedArrayList(User.CREATOR);
        allowedLists = in.createTypedArrayList(FriendList.CREATOR);
        disallowedLists = in.createTypedArrayList(FriendList.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeTypedList(allowedUsers);
        dest.writeTypedList(disallowedUsers);
        dest.writeTypedList(allowedLists);
        dest.writeTypedList(disallowedLists);
    }

    public String getType() {
        return type;
    }

    public Privacy setType(String type) {
        this.type = type;
        return this;
    }

    public List<User> getAllowedUsers() {
        return Collections.unmodifiableList(allowedUsers);
    }

    public List<User> getDisallowedUsers() {
        return Collections.unmodifiableList(disallowedUsers);
    }

    public List<FriendList> getAllowedLists() {
        return Collections.unmodifiableList(allowedLists);
    }

    public List<FriendList> getDisallowedLists() {
        return Collections.unmodifiableList(disallowedLists);
    }

    public Privacy allowFor(User user) {
        if (!allowedUsers.contains(user)) {
            allowedUsers.add(user);
        }

        return this;
    }

    public Privacy disallowFor(User user) {
        if (!disallowedUsers.contains(user)) {
            disallowedUsers.add(user);
        }

        return this;
    }

    public Privacy allowFor(FriendList friendList) {
        if (!allowedLists.contains(friendList)) {
            allowedLists.add(friendList);
        }

        return this;
    }

    public Privacy disallowFor(FriendList friendList) {
        if (!disallowedLists.contains(friendList)) {
            disallowedLists.add(friendList);
        }

        return this;
    }

    public void removeFromAllowed(@NonNull User user) {
        allowedUsers.remove(user);
    }

    public void removeFromAllowed(@NonNull FriendList friendList) {
        allowedLists.remove(friendList);
    }

    public void removeFromDisallowed(@NonNull User user) {
        disallowedUsers.remove(user);
    }

    public void removeFromDisallowed(@NonNull FriendList friendList) {
        disallowedLists.remove(friendList);
    }

    public String createAllowedString(Context context) {
        String sufix;
        switch (type) {
            default:
                sufix = context.getString(R.string.privacy_to_all_users);
                break;
            case Type.FRIENDS:
                sufix = context.getString(R.string.privacy_to_friends_only);
                break;
            case Type.FRIENDS_OF_FRIENDS:
            case Type.FRIENDS_OF_FRIENDS_ONLY:
                sufix = context.getString(R.string.privacy_to_friends_and_friends_of_friends);
                break;
            case Type.ONLY_ME:
            case Type.NOBODY:
                sufix = context.getString(R.string.privacy_to_only_me);
                break;
        }

        String users = join(", ", allowedUsers);
        String friendsLists = join(", ", allowedLists);
        String additional = isEmpty(users) ? friendsLists : (isEmpty(friendsLists) ? users : users + ", " + friendsLists);
        String and = context.getString(R.string.and);
        return isEmpty(additional) ? sufix : sufix + " " + and + " " + additional;
    }

    public String createDisallowedString() {
        String users = join(", ", disallowedUsers);
        String friendsLists = join(", ", disallowedLists);
        String additional = isEmpty(users) ? friendsLists : (isEmpty(friendsLists) ? users : users + ", " + friendsLists);
        return isEmpty(additional) ? "-" : additional;
    }

    @NonNull
    @Override
    public Privacy clone() throws CloneNotSupportedException {
        Privacy clone = (Privacy) super.clone();
        clone.allowedUsers = new ArrayList<>(allowedUsers);
        clone.allowedLists = new ArrayList<>(allowedLists);
        clone.disallowedUsers = new ArrayList<>(disallowedUsers);
        clone.disallowedLists = new ArrayList<>(disallowedLists);
        return clone;
    }

    public static final class Type {
        public static final String ALL = "all";
        public static final String FRIENDS = "friends";
        public static final String FRIENDS_OF_FRIENDS = "friends_of_friends";
        public static final String FRIENDS_OF_FRIENDS_ONLY = "friends_of_friends_only";
        public static final String NOBODY = "nobody";
        public static final String ONLY_ME = "only_me";
    }
}