package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.module.parcel.ParcelNative


class ParcelableOwnerWrapper : Parcelable, ParcelNative.ParcelableNative {
    private val type: Int
    private val isNull: Boolean
    private val owner: Owner?

    constructor(owner: Owner?) {
        this.owner = owner
        type = owner?.ownerType ?: 0
        isNull = owner == null
    }

    private constructor(`in`: Parcel) {
        type = `in`.readInt()
        isNull = `in`.readByte().toInt() != 0
        owner = if (!isNull) {
            `in`.readParcelable(if (type == OwnerType.USER) User::class.java.classLoader else Community::class.java.classLoader)
        } else {
            null
        }
    }

    private constructor(`in`: ParcelNative) {
        type = `in`.readInt()
        isNull = `in`.readBoolean()
        owner = if (!isNull) {
            if (type == OwnerType.USER) {
                `in`.readParcelable(User.NativeCreator)
            } else {
                `in`.readParcelable(Community.NativeCreator)
            }
        } else {
            null
        }
    }

    fun get(): Owner? {
        return owner
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(type)
        dest.writeByte((if (isNull) 1 else 0).toByte())
        if (!isNull) {
            dest.writeParcelable(owner, flags)
        }
    }

    override fun writeToParcelNative(dest: ParcelNative) {
        dest.writeInt(type)
        dest.writeBoolean(isNull)
        if (!isNull) {
            dest.writeParcelable(owner)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableOwnerWrapper> =
            object : Parcelable.Creator<ParcelableOwnerWrapper> {
                override fun createFromParcel(`in`: Parcel): ParcelableOwnerWrapper {
                    return ParcelableOwnerWrapper(`in`)
                }

                override fun newArray(size: Int): Array<ParcelableOwnerWrapper?> {
                    return arrayOfNulls(size)
                }
            }
        val NativeCreator: ParcelNative.Creator<ParcelableOwnerWrapper> =
            object : ParcelNative.Creator<ParcelableOwnerWrapper> {
                override fun readFromParcelNative(dest: ParcelNative): ParcelableOwnerWrapper {
                    return ParcelableOwnerWrapper(
                        dest
                    )
                }

            }

        fun wrap(owner: Owner?): ParcelableOwnerWrapper {
            return ParcelableOwnerWrapper(owner)
        }

        fun readOwner(`in`: ParcelNative): Owner? {
            return `in`.readParcelable(NativeCreator)?.get()
        }

        fun writeOwner(dest: ParcelNative, owner: Owner?) {
            dest.writeParcelable(ParcelableOwnerWrapper(owner))
        }

        fun readOwner(`in`: Parcel): Owner? {
            return `in`.readParcelable<ParcelableOwnerWrapper>(ParcelableOwnerWrapper::class.java.classLoader)
                ?.get()
        }

        fun writeOwner(dest: Parcel, flags: Int, owner: Owner?) {
            dest.writeParcelable(ParcelableOwnerWrapper(owner), flags)
        }

        fun readOwners(`in`: Parcel): List<Owner>? {
            val isNull = `in`.readByte() == 1.toByte()
            if (isNull) {
                return null
            }
            val ownersCount = `in`.readInt()
            val owners: MutableList<Owner> = ArrayList(ownersCount)
            for (i in 0 until ownersCount) {
                readOwner(`in`)?.let { owners.add(it) }
            }
            return owners
        }

        fun writeOwners(dest: Parcel, flags: Int, owners: List<Owner>?) {
            if (owners == null) {
                dest.writeByte(1.toByte())
                return
            }
            dest.writeByte(0.toByte())
            dest.writeInt(owners.size)
            for (owner in owners) {
                writeOwner(dest, flags, owner)
            }
        }

        fun readOwners(`in`: ParcelNative): List<Owner>? {
            val isNull = `in`.readByte() == 1.toByte()
            if (isNull) {
                return null
            }
            val ownersCount = `in`.readInt()
            val owners: MutableList<Owner> = ArrayList(ownersCount)
            for (i in 0 until ownersCount) {
                readOwner(`in`)?.let { owners.add(it) }
            }
            return owners
        }

        fun writeOwners(dest: ParcelNative, owners: List<Owner>?) {
            if (owners == null) {
                dest.writeByte(1.toByte())
                return
            }
            dest.writeByte(0.toByte())
            dest.writeInt(owners.size)
            for (owner in owners) {
                writeOwner(dest, owner)
            }
        }
    }
}