package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import java.util.List;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableModelWrapper;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;

public final class CopyFeedback extends Feedback {

    public static final Creator<CopyFeedback> CREATOR = new Creator<CopyFeedback>() {
        @Override
        public CopyFeedback createFromParcel(Parcel in) {
            return new CopyFeedback(in);
        }

        @Override
        public CopyFeedback[] newArray(int size) {
            return new CopyFeedback[size];
        }
    };
    private AbsModel what;
    private List<Owner> owners;

    public CopyFeedback(@FeedbackType int type) {
        super(type);
    }

    private CopyFeedback(Parcel in) {
        super(in);
        what = ParcelableModelWrapper.readModel(in);
        owners = ParcelableOwnerWrapper.readOwners(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, what);
        ParcelableOwnerWrapper.writeOwners(dest, flags, owners);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public AbsModel getWhat() {
        return what;
    }

    public CopyFeedback setWhat(AbsModel what) {
        this.what = what;
        return this;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public CopyFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }
}