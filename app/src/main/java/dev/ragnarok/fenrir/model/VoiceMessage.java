package dev.ragnarok.fenrir.model;

import android.os.Parcel;


public class VoiceMessage extends AbsModel {

    public static final Creator<VoiceMessage> CREATOR = new Creator<VoiceMessage>() {
        @Override
        public VoiceMessage createFromParcel(Parcel in) {
            return new VoiceMessage(in);
        }

        @Override
        public VoiceMessage[] newArray(int size) {
            return new VoiceMessage[size];
        }
    };
    private final int id;
    private final int ownerId;
    private int duration;
    private byte[] waveform;
    private String linkOgg;
    private String linkMp3;
    private String accessKey;
    private boolean showTranscript;
    private String transcript;

    public VoiceMessage(int id, int ownerId) {
        this.id = id;
        this.ownerId = ownerId;
    }

    protected VoiceMessage(Parcel in) {
        super(in);
        id = in.readInt();
        ownerId = in.readInt();
        duration = in.readInt();
        waveform = in.createByteArray();
        linkOgg = in.readString();
        linkMp3 = in.readString();
        accessKey = in.readString();
        transcript = in.readString();
        showTranscript = in.readByte() != 0;
    }

    public boolean isShowTranscript() {
        return showTranscript;
    }

    public VoiceMessage setShowTranscript(boolean showTranscript) {
        this.showTranscript = showTranscript;
        return this;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public VoiceMessage setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public VoiceMessage setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public byte[] getWaveform() {
        return waveform;
    }

    public VoiceMessage setWaveform(byte[] waveform) {
        this.waveform = waveform;
        return this;
    }

    public String getLinkOgg() {
        return linkOgg;
    }

    public VoiceMessage setLinkOgg(String linkOgg) {
        this.linkOgg = linkOgg;
        return this;
    }

    public String getLinkMp3() {
        return linkMp3;
    }

    public VoiceMessage setLinkMp3(String linkMp3) {
        this.linkMp3 = linkMp3;
        return this;
    }

    public String getTranscript() {
        return transcript;
    }

    public VoiceMessage setTranscript(String transcript) {
        this.transcript = transcript;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeInt(id);
        parcel.writeInt(ownerId);
        parcel.writeInt(duration);
        parcel.writeByteArray(waveform);
        parcel.writeString(linkOgg);
        parcel.writeString(linkMp3);
        parcel.writeString(accessKey);
        parcel.writeString(transcript);
        parcel.writeByte((byte) (showTranscript ? 1 : 0));
    }
}