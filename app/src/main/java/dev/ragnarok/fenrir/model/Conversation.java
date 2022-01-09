package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class Conversation {

    private final int id;

    private String title;

    private int unreadCount;

    private String photo50;

    private String photo100;

    private String photo200;

    /**
     * ID of the last read incoming message.
     */
    private int inRead;

    /**
     * ID of the last read outcoming message.
     */
    private int outRead;

    private Owner interlocutor;

    private Keyboard currentKeyboard;

    private Message pinned;

    private boolean isGroupChannel;

    private int acl;

    private int major_id;

    private int minor_id;

    public Conversation(int id) {
        this.id = id;
    }

    public int getAcl() {
        return acl;
    }

    public Conversation setAcl(int acl) {
        this.acl = acl;
        return this;
    }

    public Message getPinned() {
        return pinned;
    }

    public Conversation setPinned(Message pinned) {
        this.pinned = pinned;
        return this;
    }

    public Owner getInterlocutor() {
        return interlocutor;
    }

    public Conversation setInterlocutor(Owner interlocutor) {
        this.interlocutor = interlocutor;
        return this;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        if (Peer.isUser(id)) {
            String custom = Settings.get().other().getUserNameChanges(id);
            if (!Utils.isEmpty(custom)) {
                return custom;
            }
        }
        return title;
    }

    public Conversation setTitle(String title) {
        this.title = title;
        return this;
    }

    public Keyboard getCurrentKeyboard() {
        return currentKeyboard;
    }

    public Conversation setCurrentKeyboard(Keyboard currentKeyboard) {
        this.currentKeyboard = currentKeyboard;
        return this;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public Conversation setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public Conversation setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public Conversation setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public Conversation setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public int getInRead() {
        return inRead;
    }

    public Conversation setInRead(int inRead) {
        this.inRead = inRead;
        return this;
    }

    public int getOutRead() {
        return outRead;
    }

    public Conversation setOutRead(int outRead) {
        this.outRead = outRead;
        return this;
    }

    public String get100orSmallerAvatar() {
        return firstNonEmptyString(photo100, photo50);
    }

    public String getMaxSquareAvatar() {
        return firstNonEmptyString(photo200, photo100, photo200);
    }

    public boolean isGroupChannel() {
        return isGroupChannel;
    }

    public Conversation setGroupChannel(boolean isGroupChannel) {
        this.isGroupChannel = isGroupChannel;
        return this;
    }

    public int getMajor_id() {
        return major_id;
    }

    public Conversation setMajor_id(int major_id) {
        this.major_id = major_id;
        return this;
    }

    public int getMinor_id() {
        return minor_id;
    }

    public Conversation setMinor_id(int minor_id) {
        this.minor_id = minor_id;
        return this;
    }

    public static final class AclFlags {
        public static final int CAN_INVITE = 1;
        public static final int CAN_CHANGE_INFO = 2;
        public static final int CAN_CHANGE_PIN = 4;
        public static final int CAN_PROMOTE_USERS = 8;
        public static final int CAN_SEE_INVITE_LINK = 16;
        public static final int CAN_CHANGE_INVITE_LINK = 32;
    }
}