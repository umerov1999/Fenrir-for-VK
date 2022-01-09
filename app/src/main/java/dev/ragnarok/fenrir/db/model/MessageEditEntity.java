package dev.ragnarok.fenrir.db.model;

import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.db.model.entity.MessageEntity;
import dev.ragnarok.fenrir.model.Keyboard;


public class MessageEditEntity {

    private final int status;
    private final int senderId;
    private boolean encrypted;
    private long date;
    private boolean out;
    private boolean deleted;
    private boolean important;
    private List<MessageEntity> forward;
    private List<Entity> attachments;
    private boolean read;
    private String payload;
    private Keyboard keyboard;

    private String body;

    private Map<Integer, String> extras;

    public MessageEditEntity(int status, int senderId) {
        this.status = status;
        this.senderId = senderId;
    }

    public String getBody() {
        return body;
    }

    public MessageEditEntity setBody(String body) {
        this.body = body;
        return this;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public MessageEditEntity setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public Map<Integer, String> getExtras() {
        return extras;
    }

    public MessageEditEntity setExtras(Map<Integer, String> extras) {
        this.extras = extras;
        return this;
    }

    public boolean isRead() {
        return read;
    }

    public MessageEditEntity setRead(boolean read) {
        this.read = read;
        return this;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public MessageEditEntity setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    public long getDate() {
        return date;
    }

    public MessageEditEntity setDate(long date) {
        this.date = date;
        return this;
    }

    public boolean isOut() {
        return out;
    }

    public MessageEditEntity setOut(boolean out) {
        this.out = out;
        return this;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public MessageEditEntity setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public boolean isImportant() {
        return important;
    }

    public MessageEditEntity setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public List<MessageEntity> getForward() {
        return forward;
    }

    public MessageEditEntity setForward(List<MessageEntity> forward) {
        this.forward = forward;
        return this;
    }

    public List<Entity> getAttachments() {
        return attachments;
    }

    public MessageEditEntity setAttachments(List<Entity> attachments) {
        this.attachments = attachments;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public MessageEditEntity setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public int getSenderId() {
        return senderId;
    }
}