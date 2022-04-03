package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.model.ChatAction;
import dev.ragnarok.fenrir.model.MessageStatus;

@Keep
public class MessageEntity extends Entity {

    private int id;

    private int peerId;

    private int fromId;

    private long date;

    private boolean out;

    private String body;

    private boolean encrypted;

    private boolean important;

    private boolean deleted;

    private boolean deletedForAll;

    private int forwardCount;

    private boolean hasAttachmens;

    private KeyboardEntity keyboard;

    @MessageStatus
    private int status;

    private int originalId;

    @ChatAction
    private int action;

    private int actionMemberId;

    private String actionEmail;

    private String actionText;

    private String photo50;

    private String photo100;

    private String photo200;

    private int randomId;

    private Map<Integer, String> extras;

    private AttachmentsEntity attachments;

    private List<MessageEntity> forwardMessages;

    private String payload;

    private long updateTime;

    public MessageEntity set(int id, int peerId, int fromId) {
        this.id = id;
        this.peerId = peerId;
        this.fromId = fromId;
        return this;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public MessageEntity setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public boolean isDeletedForAll() {
        return deletedForAll;
    }

    public MessageEntity setDeletedForAll(boolean deletedForAll) {
        this.deletedForAll = deletedForAll;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getPeerId() {
        return peerId;
    }

    public int getFromId() {
        return fromId;
    }

    public MessageEntity setFromId(int fromId) {
        this.fromId = fromId;
        return this;
    }

    public long getDate() {
        return date;
    }

    public MessageEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public boolean isOut() {
        return out;
    }

    public MessageEntity setOut(boolean out) {
        this.out = out;
        return this;
    }

    public String getBody() {
        return body;
    }

    public MessageEntity setBody(String body) {
        this.body = body;
        return this;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public MessageEntity setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    public KeyboardEntity getKeyboard() {
        return keyboard;
    }

    public MessageEntity setKeyboard(KeyboardEntity keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public boolean isImportant() {
        return important;
    }

    public MessageEntity setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public MessageEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public int getForwardCount() {
        return forwardCount;
    }

    public MessageEntity setForwardCount(int forwardCount) {
        this.forwardCount = forwardCount;
        return this;
    }

    public boolean isHasAttachmens() {
        return hasAttachmens;
    }

    public MessageEntity setHasAttachmens(boolean hasAttachmens) {
        this.hasAttachmens = hasAttachmens;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public MessageEntity setStatus(int status) {
        this.status = status;
        return this;
    }

    public int getOriginalId() {
        return originalId;
    }

    public MessageEntity setOriginalId(int originalId) {
        this.originalId = originalId;
        return this;
    }

    public int getAction() {
        return action;
    }

    public MessageEntity setAction(int action) {
        this.action = action;
        return this;
    }

    public int getActionMemberId() {
        return actionMemberId;
    }

    public MessageEntity setActionMemberId(int actionMemberId) {
        this.actionMemberId = actionMemberId;
        return this;
    }

    public String getActionEmail() {
        return actionEmail;
    }

    public MessageEntity setActionEmail(String actionEmail) {
        this.actionEmail = actionEmail;
        return this;
    }

    public String getActionText() {
        return actionText;
    }

    public MessageEntity setActionText(String actionText) {
        this.actionText = actionText;
        return this;
    }

    public String getPhoto50() {
        return photo50;
    }

    public MessageEntity setPhoto50(String photo50) {
        this.photo50 = photo50;
        return this;
    }

    public String getPhoto100() {
        return photo100;
    }

    public MessageEntity setPhoto100(String photo100) {
        this.photo100 = photo100;
        return this;
    }

    public String getPhoto200() {
        return photo200;
    }

    public MessageEntity setPhoto200(String photo200) {
        this.photo200 = photo200;
        return this;
    }

    public int getRandomId() {
        return randomId;
    }

    public MessageEntity setRandomId(int randomId) {
        this.randomId = randomId;
        return this;
    }

    public Map<Integer, String> getExtras() {
        return extras;
    }

    public MessageEntity setExtras(Map<Integer, String> extras) {
        this.extras = extras;
        return this;
    }

    public @Nullable
    List<MessageEntity> getForwardMessages() {
        return forwardMessages;
    }

    public MessageEntity setForwardMessages(@Nullable List<MessageEntity> forwardMessages) {
        this.forwardMessages = forwardMessages;
        return this;
    }

    public @Nullable
    List<Entity> getAttachments() {
        return attachments != null ? attachments.getEntities() : null;
    }

    public MessageEntity setAttachments(@Nullable List<Entity> attachments) {
        this.attachments = AttachmentsEntity.from(attachments);
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public MessageEntity setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}