package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.StringRes

class SideDrawerCategory : Parcelable {
    @StringRes
    private val title: Int

    @SideSwitchableCategory
    private val key: Int
    private var checked = false

    constructor(@SwitchableCategory key: Int, @StringRes title: Int) {
        this.title = title
        this.key = key
    }

    private constructor(`in`: Parcel) {
        title = `in`.readInt()
        key = `in`.readInt()
        checked = `in`.readInt() == 1
    }

    @StringRes
    fun getTitle(): Int {
        return title
    }

    @SideSwitchableCategory
    fun getKey(): Int {
        return key
    }

    fun isChecked(): Boolean {
        return checked
    }

    fun setChecked(checked: Boolean): SideDrawerCategory {
        this.checked = checked
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(title)
        dest.writeInt(key)
        dest.writeInt(if (checked) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<SideDrawerCategory> {
        override fun createFromParcel(parcel: Parcel): SideDrawerCategory {
            return SideDrawerCategory(parcel)
        }

        override fun newArray(size: Int): Array<SideDrawerCategory?> {
            return arrayOfNulls(size)
        }
    }
}