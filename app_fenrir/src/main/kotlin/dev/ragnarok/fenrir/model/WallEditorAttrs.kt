package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class WallEditorAttrs : Parcelable {
    private val owner: ParcelableOwnerWrapper
    private val editor: ParcelableOwnerWrapper

    constructor(owner: Owner, editor: Owner) {
        this.owner = ParcelableOwnerWrapper(owner)
        this.editor = ParcelableOwnerWrapper(editor)
    }

    private constructor(`in`: Parcel) {
        owner = `in`.readParcelable(ParcelableOwnerWrapper::class.java.classLoader)!!
        editor = `in`.readParcelable(ParcelableOwnerWrapper::class.java.classLoader)!!
    }

    fun getOwner(): Owner {
        return owner.get()!!
    }

    fun getEditor(): Owner {
        return editor.get()!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(owner, flags)
        dest.writeParcelable(editor, flags)
    }

    companion object CREATOR : Parcelable.Creator<WallEditorAttrs> {
        override fun createFromParcel(parcel: Parcel): WallEditorAttrs {
            return WallEditorAttrs(parcel)
        }

        override fun newArray(size: Int): Array<WallEditorAttrs?> {
            return arrayOfNulls(size)
        }
    }
}