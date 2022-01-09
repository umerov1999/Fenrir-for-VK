package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public final class ParcelableModelWrapper implements Parcelable {

    public static final Creator<ParcelableModelWrapper> CREATOR = new Creator<ParcelableModelWrapper>() {
        @Override
        public ParcelableModelWrapper createFromParcel(Parcel in) {
            return new ParcelableModelWrapper(in);
        }

        @Override
        public ParcelableModelWrapper[] newArray(int size) {
            return new ParcelableModelWrapper[size];
        }
    };
    private static final List<Class<?>> TYPES = new ArrayList<>();

    static {
        TYPES.add(Photo.class);
        TYPES.add(Post.class);
        TYPES.add(Video.class);
        TYPES.add(FwdMessages.class);
        TYPES.add(VoiceMessage.class);
        TYPES.add(Document.class);
        TYPES.add(Audio.class);
        TYPES.add(Chat.class);
        TYPES.add(Poll.class);
        TYPES.add(Link.class);
        TYPES.add(Article.class);
        TYPES.add(Story.class);
        TYPES.add(Call.class);
        TYPES.add(NotSupported.class);
        TYPES.add(WallReply.class);
        TYPES.add(PhotoAlbum.class);
        TYPES.add(AudioPlaylist.class);
        TYPES.add(Graffiti.class);
        TYPES.add(Gift.class);
        TYPES.add(GiftItem.class);
        TYPES.add(Comment.class);
        TYPES.add(Event.class);
        TYPES.add(Market.class);
        TYPES.add(MarketAlbum.class);
        TYPES.add(AudioArtist.class);
    }

    private final AbsModel model;

    private ParcelableModelWrapper(AbsModel model) {
        this.model = model;
    }

    private ParcelableModelWrapper(Parcel in) {
        int index = in.readInt();
        ClassLoader classLoader = TYPES.get(index).getClassLoader();

        model = in.readParcelable(classLoader);
    }

    public static ParcelableModelWrapper wrap(AbsModel model) {
        return new ParcelableModelWrapper(model);
    }

    public static AbsModel readModel(Parcel in) {
        return in.<ParcelableModelWrapper>readParcelable(ParcelableModelWrapper.class.getClassLoader()).get();
    }

    public static void writeModel(Parcel dest, int flags, AbsModel owner) {
        dest.writeParcelable(new ParcelableModelWrapper(owner), flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int index = TYPES.indexOf(model.getClass());
        if (index == -1) {
            throw new UnsupportedOperationException("Unsupported class: " + model.getClass());
        }

        dest.writeInt(index);
        dest.writeParcelable(model, flags);
    }

    public AbsModel get() {
        return model;
    }
}
