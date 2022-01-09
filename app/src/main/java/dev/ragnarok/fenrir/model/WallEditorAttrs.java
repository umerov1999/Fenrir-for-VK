package dev.ragnarok.fenrir.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public final class WallEditorAttrs implements Parcelable {

    public static final Creator<WallEditorAttrs> CREATOR = new Creator<WallEditorAttrs>() {
        @Override
        public WallEditorAttrs createFromParcel(Parcel in) {
            return new WallEditorAttrs(in);
        }

        @Override
        public WallEditorAttrs[] newArray(int size) {
            return new WallEditorAttrs[size];
        }
    };
    private final ParcelableOwnerWrapper owner;
    private final ParcelableOwnerWrapper editor;

    public WallEditorAttrs(@NonNull Owner owner, @NonNull Owner editor) {
        this.owner = new ParcelableOwnerWrapper(owner);
        this.editor = new ParcelableOwnerWrapper(editor);
    }

    private WallEditorAttrs(Parcel in) {
        owner = in.readParcelable(ParcelableOwnerWrapper.class.getClassLoader());
        editor = in.readParcelable(ParcelableOwnerWrapper.class.getClassLoader());
    }

    @NonNull
    public Owner getOwner() {
        return owner.get();
    }

    @NonNull
    public Owner getEditor() {
        return editor.get();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(owner, flags);
        dest.writeParcelable(editor, flags);
    }
}