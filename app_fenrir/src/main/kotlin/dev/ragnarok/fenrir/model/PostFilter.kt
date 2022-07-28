package dev.ragnarok.fenrir.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.adapter.horizontal.Entry

class PostFilter : Entry, Parcelable {
    private val mode: Int
    private val title: String
    private var active = false
    private var count = 0

    constructor(mode: Int, title: String) {
        this.mode = mode
        this.title = title
    }

    internal constructor(`in`: Parcel) {
        mode = `in`.readInt()
        title = `in`.readString()!!
        active = `in`.readByte().toInt() != 0
        count = `in`.readInt()
    }

    fun getTitle(): String {
        return if (count > 0) "$title $count" else title
    }

    override fun getTitle(context: Context): String {
        return if (count > 0) "$title $count" else title
    }

    override val isActive: Boolean
        get() = active

    fun setActive(active: Boolean) {
        this.active = active
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(mode)
        dest.writeString(title)
        dest.writeByte((if (active) 1 else 0).toByte())
        dest.writeInt(count)
    }

    fun getMode(): Int {
        return mode
    }

    fun getCount(): Int {
        return count
    }

    fun setCount(count: Int) {
        this.count = count
    }

    override val isCustom: Boolean
        get() = false

    companion object CREATOR : Parcelable.Creator<PostFilter> {
        override fun createFromParcel(parcel: Parcel): PostFilter {
            return PostFilter(parcel)
        }

        override fun newArray(size: Int): Array<PostFilter?> {
            return arrayOfNulls(size)
        }
    }
}