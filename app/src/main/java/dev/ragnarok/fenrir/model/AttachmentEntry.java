package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.concurrent.atomic.AtomicInteger;

public class AttachmentEntry implements Parcelable {

    public static final Creator<AttachmentEntry> CREATOR = new Creator<AttachmentEntry>() {
        @Override
        public AttachmentEntry createFromParcel(Parcel in) {
            return new AttachmentEntry(in);
        }

        @Override
        public AttachmentEntry[] newArray(int size) {
            return new AttachmentEntry[size];
        }
    };
    private static final AtomicInteger ID_GEN = new AtomicInteger();
    private final int id;
    private final AbsModel attachment;
    private int optionalId;
    private boolean canDelete;
    private boolean accompanying;

    public AttachmentEntry(boolean canDelete, AbsModel attachment) {
        this.canDelete = canDelete;
        this.attachment = attachment;
        id = ID_GEN.incrementAndGet();
    }

    protected AttachmentEntry(Parcel in) {
        id = in.readInt();
        if (id > ID_GEN.intValue()) {
            ID_GEN.set(id);
        }

        optionalId = in.readInt();
        canDelete = in.readByte() != 0;
        accompanying = in.readByte() != 0;

        ParcelableModelWrapper wrapper = in.readParcelable(ParcelableModelWrapper.class.getClassLoader());
        attachment = wrapper.get();
    }

    public int getId() {
        return id;
    }

    public AbsModel getAttachment() {
        return attachment;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public AttachmentEntry setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
        return this;
    }

    public boolean isAccompanying() {
        return accompanying;
    }

    public AttachmentEntry setAccompanying(boolean accompanying) {
        this.accompanying = accompanying;
        return this;
    }

    public int getOptionalId() {
        return optionalId;
    }

    public AttachmentEntry setOptionalId(int optionalId) {
        this.optionalId = optionalId;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(optionalId);
        dest.writeByte((byte) (canDelete ? 1 : 0));
        dest.writeByte((byte) (accompanying ? 1 : 0));

        ParcelableModelWrapper wrapper = ParcelableModelWrapper.wrap(attachment);
        dest.writeParcelable(wrapper, flags);
    }
}
