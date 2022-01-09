package dev.ragnarok.fenrir.model.feedback;

import android.os.Parcel;

import java.util.List;

import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableModelWrapper;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;

public final class LikeFeedback extends Feedback {

    public static final Creator<LikeFeedback> CREATOR = new Creator<LikeFeedback>() {
        @Override
        public LikeFeedback createFromParcel(Parcel in) {
            return new LikeFeedback(in);
        }

        @Override
        public LikeFeedback[] newArray(int size) {
            return new LikeFeedback[size];
        }
    };
    private AbsModel liked;
    private List<Owner> owners;

    // one of FeedbackType.LIKE_PHOTO, FeedbackType.LIKE_POST, FeedbackType.LIKE_VIDEO
    public LikeFeedback(@FeedbackType int type) {
        super(type);
    }

    private LikeFeedback(Parcel in) {
        super(in);
        liked = ParcelableModelWrapper.readModel(in);
        owners = ParcelableOwnerWrapper.readOwners(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        ParcelableModelWrapper.writeModel(dest, flags, liked);
        ParcelableOwnerWrapper.writeOwners(dest, flags, owners);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public List<Owner> getOwners() {
        return owners;
    }

    public LikeFeedback setOwners(List<Owner> owners) {
        this.owners = owners;
        return this;
    }

    public AbsModel getLiked() {
        return liked;
    }

    public LikeFeedback setLiked(AbsModel liked) {
        this.liked = liked;
        return this;
    }
}