package dev.ragnarok.fenrir.model;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;

public class SaveMessageBuilder {

    private final int accountId;
    private final int peerId;

    private List<AbsModel> attachments;
    private List<Message> forwardMessages;
    private String body;
    private File voiceMessageFile;
    private boolean requireEncryption;

    private Integer draftMessageId;
    private String payload;
    private Keyboard keyboard;

    @KeyLocationPolicy
    private int keyLocationPolicy;

    public SaveMessageBuilder(int accountId, int peerId) {
        this.accountId = accountId;
        this.peerId = peerId;
        keyLocationPolicy = KeyLocationPolicy.PERSIST;
    }

    public Integer getDraftMessageId() {
        return draftMessageId;
    }

    public SaveMessageBuilder setDraftMessageId(Integer draftMessageId) {
        this.draftMessageId = draftMessageId;
        return this;
    }

    public SaveMessageBuilder attach(List<AbsModel> attachments) {
        if (attachments != null) {
            prepareAttachments(attachments.size()).addAll(attachments);
        }

        return this;
    }

    private List<AbsModel> prepareAttachments(int initialSize) {
        if (attachments == null) {
            attachments = new ArrayList<>(initialSize);
        }

        return attachments;
    }

    public SaveMessageBuilder attach(@NonNull AbsModel attachment) {
        prepareAttachments(1).add(attachment);
        return this;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public SaveMessageBuilder setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getPeerId() {
        return peerId;
    }

    public List<AbsModel> getAttachments() {
        return attachments;
    }

    public List<Message> getForwardMessages() {
        return forwardMessages;
    }

    public SaveMessageBuilder setForwardMessages(List<Message> forwardMessages) {
        this.forwardMessages = forwardMessages;
        return this;
    }

    public String getBody() {
        return body;
    }

    public SaveMessageBuilder setBody(String body) {
        this.body = body;
        return this;
    }

    public File getVoiceMessageFile() {
        return voiceMessageFile;
    }

    public SaveMessageBuilder setVoiceMessageFile(File voiceMessageFile) {
        this.voiceMessageFile = voiceMessageFile;
        return this;
    }

    public boolean isRequireEncryption() {
        return requireEncryption;
    }

    public SaveMessageBuilder setRequireEncryption(boolean requireEncryption) {
        this.requireEncryption = requireEncryption;
        return this;
    }

    @KeyLocationPolicy
    public int getKeyLocationPolicy() {
        return keyLocationPolicy;
    }

    public SaveMessageBuilder setKeyLocationPolicy(int keyLocationPolicy) {
        this.keyLocationPolicy = keyLocationPolicy;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public SaveMessageBuilder setPayload(String payload) {
        this.payload = payload;
        return this;
    }
}
