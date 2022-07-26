package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.CallSuper

abstract class AbsModel : Parcelable {
    constructor()

    @Suppress("UNUSED_PARAMETER")
    constructor(`in`: Parcel?)

    @CallSuper
    override fun writeToParcel(parcel: Parcel, i: Int) {
    }
}