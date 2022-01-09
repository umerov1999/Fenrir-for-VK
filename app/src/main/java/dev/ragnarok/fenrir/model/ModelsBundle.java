package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


public final class ModelsBundle implements Parcelable, Iterable<AbsModel> {

    public static final Creator<ModelsBundle> CREATOR = new Creator<ModelsBundle>() {
        @Override
        public ModelsBundle createFromParcel(Parcel in) {
            return new ModelsBundle(in);
        }

        @Override
        public ModelsBundle[] newArray(int size) {
            return new ModelsBundle[size];
        }
    };
    private final List<ParcelableModelWrapper> wrappers;

    public ModelsBundle() {
        wrappers = new ArrayList<>();
    }

    public ModelsBundle(int capacity) {
        wrappers = new ArrayList<>(capacity);
    }

    private ModelsBundle(Parcel in) {
        wrappers = in.createTypedArrayList(ParcelableModelWrapper.CREATOR);
    }

    public int size() {
        return wrappers.size();
    }

    public void clear() {
        wrappers.clear();
    }

    public ModelsBundle append(AbsModel model) {
        wrappers.add(ParcelableModelWrapper.wrap(model));
        return this;
    }

    public void remove(AbsModel model) {
        wrappers.remove(model);
    }

    public ModelsBundle append(Collection<? extends AbsModel> data) {
        for (AbsModel model : data) {
            wrappers.add(ParcelableModelWrapper.wrap(model));
        }
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(wrappers);
    }

    @NonNull
    @Override
    public Iterator<AbsModel> iterator() {
        return new Iter(wrappers.iterator());
    }

    private static class Iter implements Iterator<AbsModel> {

        final Iterator<ParcelableModelWrapper> internal;

        private Iter(Iterator<ParcelableModelWrapper> internal) {
            this.internal = internal;
        }

        @Override
        public boolean hasNext() {
            return internal.hasNext();
        }

        @Override
        public AbsModel next() {
            return internal.next().get();
        }

        @Override
        public void remove() {
            internal.remove();
        }
    }
}
