package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.Identificable;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class Dialog implements Identificable, Parcelable {

    public static final Creator<Dialog> CREATOR = new Creator<Dialog>() {
        @Override
        public Dialog createFromParcel(Parcel in) {
            return new Dialog(in);
        }

        @Override
        public Dialog[] newArray(int size) {
            return new Dialog[size];
        }
    };
    private int peerId;
    private String title;
    private int unreadCount;
    private String photo50;
    private String photo100;
    private String photo200;
    private Message message;
    private Owner interlocutor;
    private int lastMessageId;
    private int inRead;
    private int outRead;
    private boolean isGroupChannel;
    private int major_id;
    private int minor_id;

    public Dialog() {

    }

    protected Dialog(Parcel in) {
        peerId = in.readInt();
        title = in.readString();
        unreadCount = in.readInt();
        photo50 = in.readString();
        photo100 = in.readString();
        photo200 = in.readString();
        message = in.readParcelable(Message.class.getClassLoader());

        boolean interlocutorIsNull = in.readInt() == 1;
        if (!interlocutorIsNull) {
            int ownerType = in.readInt();
            interlocutor = in.readParcelable(ownerType == OwnerType.COMMUNITY
                    ? Community.class.getClassLoader() : User.class.getClassLoader());
        }

        lastMessageId = in.readInt();
        inRead = in.readInt();
        outRead = in.readInt();
        major_id = in.readInt();
        minor_id = in.readInt();
        isGroupChannel = in.readInt() == 1;
    }

    public int getPeerId() {
        return peerId;
    }

    public Dialog setPeerId(int peerId) {
        this.peerId = peerId;
        return this;
    }

    public String getTitle() {
        if (Peer.isUser(peerId)) {
            String custom = Settings.get().other().getUserNameChanges(peerId);
            if (!Utils.isEmpty(custom)) {
                return custom;
            }
        }
        return title;
    }

    public Dialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public Dialog setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public Dialog setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public Dialog setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public Dialog setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public boolean isChat() {
        return Peer.isGroupChat(peerId);
    }

    public boolean isUser() {
        return Peer.isUser(peerId);
    }

    public boolean isGroup() {
        return Peer.isGroup(peerId);
    }

    public Message getMessage() {
        return message;
    }

    public Dialog setMessage(Message message) {
        this.message = message;
        return this;
    }

    public Owner getInterlocutor() {
        return interlocutor;
    }

    public Dialog setInterlocutor(Owner interlocutor) {
        this.interlocutor = interlocutor;
        return this;
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public Dialog setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public boolean isLastMessageOut() {
        return message != null && message.isOut();
    }

    @ChatAction
    public Integer getLastMessageAction() {
        return message == null ? null : message.getAction();
    }

    public boolean hasForwardMessages() {
        return getForwardMessagesCount() > 0;
    }

    public int getForwardMessagesCount() {
        return message == null ? 0 : message.getForwardMessagesCount();
    }

    public boolean hasAttachments() {
        return message != null && message.isHasAttachments();
    }

    public String getLastMessageBody() {
        return Objects.isNull(message) ? "..." : message.getCryptStatus() == CryptStatus.DECRYPTED ? message.getDecryptedBody() : message.getBody();
    }

    @NonNull
    public String getSenderShortName(@NonNull Context context) {
        String targerText = null;
        if (interlocutor instanceof User) {
            targerText = ((User) interlocutor).getFirstName();
        } else if (interlocutor instanceof Community) {
            targerText = ((Community) interlocutor).getName();
        }

        return targerText == null ? context.getString(R.string.unknown_first_name) : targerText;
    }

    public String getDisplayTitle(@NonNull Context context) {
        switch (Peer.getType(peerId)) {
            case Peer.USER:
            case Peer.GROUP:
                return interlocutor == null ? context.getString(R.string.unknown_first_name) + " " + context.getString(R.string.unknown_last_name) : interlocutor.getFullName();
            case Peer.CHAT:
                return title;
            default:
                throw new IllegalStateException("Unknown peer id: " + peerId);
        }
    }

    public long getLastMessageDate() {
        return message == null ? 0 : message.getDate();
    }

    public String getImageUrl() {
        if (Peer.getType(peerId) == Peer.CHAT) {

            //if (isEmpty(img) && interlocutor != null) {
            //img = interlocutor.getMaxSquareAvatar();
            // }

            return firstNonEmptyString(photo200, photo100, photo50);
        }
        return interlocutor == null ? null : interlocutor.getMaxSquareAvatar();
    }

    public int getInRead() {
        return inRead;
    }

    public Dialog setInRead(int inRead) {
        this.inRead = inRead;
        return this;
    }

    public boolean isLastMessageRead() {
        return isLastMessageOut() ? getLastMessageId() <= outRead : getLastMessageId() <= inRead;
    }

    public int getOutRead() {
        return outRead;
    }

    public Dialog setOutRead(int outRead) {
        this.outRead = outRead;
        return this;
    }

    public boolean isGroupChannel() {
        return isGroupChannel;
    }

    public Dialog setGroupChannel(boolean groupChannel) {
        isGroupChannel = groupChannel;
        return this;
    }

    public int getMajor_id() {
        return major_id;
    }

    public Dialog setMajor_id(int major_id) {
        this.major_id = major_id;
        return this;
    }

    public int getMinor_id() {
        if (minor_id == 0) {
            return getLastMessageId();
        }
        return minor_id;
    }

    public Dialog setMinor_id(int minor_id) {
        this.minor_id = minor_id;
        return this;
    }

    @Override
    public int getId() {
        return peerId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(peerId);
        dest.writeString(title);
        dest.writeInt(unreadCount);
        dest.writeString(photo50);
        dest.writeString(photo100);
        dest.writeString(photo200);
        dest.writeParcelable(message, flags);

        dest.writeInt(interlocutor == null ? 1 : 0);
        if (interlocutor != null) {
            dest.writeInt(interlocutor.getOwnerType());
            dest.writeParcelable(interlocutor, flags);
        }

        dest.writeInt(lastMessageId);
        dest.writeInt(inRead);
        dest.writeInt(outRead);
        dest.writeInt(major_id);
        dest.writeInt(minor_id);
        dest.writeInt(isGroupChannel ? 1 : 0);
    }
}