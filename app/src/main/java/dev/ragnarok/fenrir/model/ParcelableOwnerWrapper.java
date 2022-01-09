package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.module.parcel.ParcelNative;


public final class ParcelableOwnerWrapper implements Parcelable, ParcelNative.ParcelableNative {

    public static final Creator<ParcelableOwnerWrapper> CREATOR = new Creator<ParcelableOwnerWrapper>() {
        @Override
        public ParcelableOwnerWrapper createFromParcel(Parcel in) {
            return new ParcelableOwnerWrapper(in);
        }

        @Override
        public ParcelableOwnerWrapper[] newArray(int size) {
            return new ParcelableOwnerWrapper[size];
        }
    };
    public static final ParcelNative.Creator<ParcelableOwnerWrapper> NativeCreator = ParcelableOwnerWrapper::new;
    private final int type;
    private final boolean isNull;
    private final Owner owner;

    public ParcelableOwnerWrapper(Owner owner) {
        this.owner = owner;
        type = owner == null ? 0 : owner.getOwnerType();
        isNull = owner == null;
    }

    private ParcelableOwnerWrapper(Parcel in) {
        type = in.readInt();
        isNull = in.readByte() != 0;

        if (!isNull) {
            owner = in.readParcelable(type == OwnerType.USER
                    ? User.class.getClassLoader() : Community.class.getClassLoader());
        } else {
            owner = null;
        }
    }

    private ParcelableOwnerWrapper(ParcelNative in) {
        type = in.readInt();
        isNull = in.readByte() != 0;

        if (!isNull) {
            if (type == OwnerType.USER) {
                owner = in.readParcelable(User.NativeCreator);
            } else {
                owner = in.readParcelable(Community.NativeCreator);
            }
        } else {
            owner = null;
        }
    }

    public static ParcelableOwnerWrapper wrap(Owner owner) {
        return new ParcelableOwnerWrapper(owner);
    }

    public static Owner readOwner(Parcel in) {
        return in.<ParcelableOwnerWrapper>readParcelable(ParcelableOwnerWrapper.class.getClassLoader()).get();
    }

    public static Owner readOwner(ParcelNative in) {
        return in.<ParcelableOwnerWrapper>readParcelable(NativeCreator).get();
    }

    public static void writeOwner(Parcel dest, int flags, Owner owner) {
        dest.writeParcelable(new ParcelableOwnerWrapper(owner), flags);
    }

    public static void writeOwner(ParcelNative dest, Owner owner) {
        dest.writeParcelable(new ParcelableOwnerWrapper(owner));
    }

    public static List<Owner> readOwners(Parcel in) {
        boolean isNull = in.readInt() == 1;
        if (isNull) {
            return null;
        }

        int ownersCount = in.readInt();
        List<Owner> owners = new ArrayList<>(ownersCount);
        for (int i = 0; i < ownersCount; i++) {
            owners.add(readOwner(in));
        }

        return owners;
    }

    public static void writeOwners(Parcel dest, int flags, List<Owner> owners) {
        if (owners == null) {
            dest.writeInt(1);
            return;
        }

        dest.writeInt(owners.size());
        for (Owner owner : owners) {
            writeOwner(dest, flags, owner);
        }
    }

    public static List<Owner> readOwners(ParcelNative in) {
        boolean isNull = in.readInt() == 1;
        if (isNull) {
            return null;
        }

        int ownersCount = in.readInt();
        List<Owner> owners = new ArrayList<>(ownersCount);
        for (int i = 0; i < ownersCount; i++) {
            owners.add(readOwner(in));
        }

        return owners;
    }

    public static void writeOwners(ParcelNative dest, List<Owner> owners) {
        if (owners == null) {
            dest.writeInt(1);
            return;
        }

        dest.writeInt(owners.size());
        for (Owner owner : owners) {
            writeOwner(dest, owner);
        }
    }

    public Owner get() {
        return owner;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeByte((byte) (isNull ? 1 : 0));

        if (!isNull) {
            dest.writeParcelable(owner, flags);
        }
    }

    @Override
    public void writeToParcelNative(ParcelNative dest) {
        dest.writeInt(type);
        dest.writeByte((byte) (isNull ? 1 : 0));

        if (!isNull) {
            dest.writeParcelable(owner);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}