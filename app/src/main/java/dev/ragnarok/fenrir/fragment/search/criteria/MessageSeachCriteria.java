package dev.ragnarok.fenrir.fragment.search.criteria;

import android.os.Parcel;

import dev.ragnarok.fenrir.util.ParcelUtils;

public final class MessageSeachCriteria extends BaseSearchCriteria {

    public static final Creator<MessageSeachCriteria> CREATOR = new Creator<MessageSeachCriteria>() {
        @Override
        public MessageSeachCriteria createFromParcel(Parcel in) {
            return new MessageSeachCriteria(in);
        }

        @Override
        public MessageSeachCriteria[] newArray(int size) {
            return new MessageSeachCriteria[size];
        }
    };
    private Integer peerId;

    public MessageSeachCriteria(String query) {
        super(query);

        // for test
        //appendOption(new SimpleBooleanOption(1, R.string.photo, true));
    }

    private MessageSeachCriteria(Parcel in) {
        super(in);
        peerId = ParcelUtils.readObjectInteger(in);
    }

    public Integer getPeerId() {
        return peerId;
    }

    public MessageSeachCriteria setPeerId(Integer peerId) {
        this.peerId = peerId;
        return this;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelUtils.writeObjectInteger(dest, peerId);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
