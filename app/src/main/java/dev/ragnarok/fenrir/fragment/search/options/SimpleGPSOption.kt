package dev.ragnarok.fenrir.fragment.search.options

import android.os.Parcel
import android.os.Parcelable

class SimpleGPSOption : BaseOption {
    @JvmField
    var lat_gps = 0.0

    @JvmField
    var long_gps = 0.0

    constructor(key: Int, title: Int, active: Boolean) : super(GPS, key, title, active)
    constructor(`in`: Parcel) : super(`in`) {
        lat_gps = `in`.readDouble()
        long_gps = `in`.readDouble()
    }

    fun simpleGPS(): String {
        return "{ lat=$lat_gps, long=$long_gps }"
    }

    fun has(): Boolean {
        return lat_gps != 0.0 && long_gps != 0.0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeDouble(lat_gps)
        dest.writeDouble(long_gps)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as SimpleGPSOption
        return lat_gps == that.lat_gps && long_gps == that.long_gps
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + java.lang.Double.valueOf(lat_gps).hashCode()
        result = 31 * result + java.lang.Double.valueOf(long_gps).hashCode()
        return result
    }

    @Throws(CloneNotSupportedException::class)
    override fun clone(): SimpleGPSOption {
        return super.clone() as SimpleGPSOption
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SimpleGPSOption> =
            object : Parcelable.Creator<SimpleGPSOption> {
                override fun createFromParcel(`in`: Parcel): SimpleGPSOption {
                    return SimpleGPSOption(`in`)
                }

                override fun newArray(size: Int): Array<SimpleGPSOption?> {
                    return arrayOfNulls(size)
                }
            }
    }
}