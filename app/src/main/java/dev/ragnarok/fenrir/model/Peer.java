package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class Peer implements Parcelable {

    // VKAPi
    // Для пользователя: id пользователя.
    // Для групповой беседы: 2000000000 + id беседы.
    // Для сообщества: -id сообщества.

    public static final Creator<Peer> CREATOR = new Creator<Peer>() {
        @Override
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        @Override
        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };
    public static final int USER = 1;
    public static final int GROUP = 2;
    public static final int CHAT = 3;
    private final int id;
    private String title;
    private String avaUrl;

    public Peer(int id) {
        this.id = id;
    }

    protected Peer(Parcel in) {
        id = in.readInt();
        title = in.readString();
        avaUrl = in.readString();
    }

    public static int getType(int peerId) {
        if (peerId > VKApiMessage.CHAT_PEER) {
            return CHAT;
        }

        if (peerId < 0) {
            return GROUP;
        }

        return USER;
    }

    public static int fromChatId(int chatId) {
        return chatId + VKApiMessage.CHAT_PEER;
    }

    public static int toChatId(int peerId) {
        return peerId - VKApiMessage.CHAT_PEER;
    }

    public static int fromOwnerId(int ownerId) {
        return ownerId;
    }

    public static int toOwnerId(int peerId) {
        return peerId;
    }

    public static int toUserId(int peerId) {
        return peerId;
    }

    public static boolean isGroupChat(int peerId) {
        return peerId > VKApiMessage.CHAT_PEER;
    }

    public static int fromUserId(int userId) {
        return userId;
    }

    public static boolean isUser(int peerId) {
        return getType(peerId) == USER;
    }

    public static boolean isGroup(int peerId) {
        return getType(peerId) == GROUP;
    }

    public static int fromGroupId(int groupId) {
        return -Math.abs(groupId);
    }

    public String getTitle() {
        if (isUser(id)) {
            String custom = Settings.get().other().getUserNameChanges(id);
            if (!Utils.isEmpty(custom)) {
                return custom;
            }
        }
        return title;
    }

    public Peer setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAvaUrl() {
        return avaUrl;
    }

    public Peer setAvaUrl(String avaUrl) {
        this.avaUrl = avaUrl;
        return this;
    }

    public int getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(avaUrl);
    }
}