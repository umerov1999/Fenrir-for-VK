package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import java.util.List;

import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;

public final class UsersFeedback extends Feedback {

    public static final Creator<UsersFeedback> CREATOR = new Creator<UsersFeedback>() {
        @Override
        public UsersFeedback createFromParcel(Parcel in) {
            return new UsersFeedback(in);
        }

        @Override
        public UsersFeedback[] newArray(int size) {
            return new UsersFeedback[size];
        }
    };
    private List<Owner> owners;

    public UsersFeedback(@FeedbackType int type) {
        super(type);
    }

    private UsersFeedback(Parcel in) {
        super(in);
        owners = ParcelableOwnerWrapper.readOwners(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableOwnerWrapper.writeOwners(dest, flags, owners);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public UsersFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }
}