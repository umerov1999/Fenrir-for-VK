package dev.ragnarok.fenrir.util

import android.os.Parcel

object ParcelUtils {
    @JvmStatic
    fun writeIntStringMap(dest: Parcel, map: Map<Int, String>?) {
        val isNull = map == null
        dest.writeByte(if (isNull) 1.toByte() else 0.toByte())
        if (isNull) {
            return
        }
        val size = (map ?: return).size
        dest.writeInt(size)
        for ((key, value) in map) {
            writeObjectInteger(dest, key)
            dest.writeString(value)
        }
    }

    @JvmStatic
    fun readIntStringMap(p: Parcel): Map<Int?, String?>? {
        val isNull = p.readByte() == 1.toByte()
        if (isNull) {
            return null
        }
        val size = p.readInt()
        val map: MutableMap<Int?, String?> = HashMap(size)
        for (i in 0 until size) {
            val key = readObjectInteger(p)
            val value = p.readString()
            map[key] = value
        }
        return map
    }

    @JvmStatic
    fun writeObjectDouble(dest: Parcel, value: Double?) {
        dest.writeByte(if (value == null) 1.toByte() else 0.toByte())
        if (value != null) {
            dest.writeDouble(value)
        }
    }

    @JvmStatic
    fun readObjectDouble(p: Parcel): Double? {
        val isNull = p.readByte() == 1.toByte()
        return if (!isNull) {
            p.readDouble()
        } else null
    }

    @JvmStatic
    fun writeObjectInteger(dest: Parcel, value: Int?) {
        dest.writeByte(if (value == null) 1.toByte() else 0.toByte())
        if (value != null) {
            dest.writeInt(value)
        }
    }

    @JvmStatic
    fun readObjectInteger(p: Parcel): Int? {
        val isNull = p.readByte() == 1.toByte()
        return if (!isNull) {
            p.readInt()
        } else null
    }

    @JvmStatic
    fun writeObjectLong(dest: Parcel, value: Long?) {
        dest.writeByte(if (value == null) 1.toByte() else 0.toByte())
        if (value != null) {
            dest.writeLong(value)
        }
    }

    @JvmStatic
    fun readObjectLong(p: Parcel): Long? {
        val isNull = p.readByte() == 1.toByte()
        return if (!isNull) {
            p.readLong()
        } else null
    }
}