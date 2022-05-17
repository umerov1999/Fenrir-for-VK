package dev.ragnarok.fenrir.crypt

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.util.ParcelUtils.readObjectInteger
import dev.ragnarok.fenrir.util.ParcelUtils.writeObjectInteger

class ExchangeMessage : Parcelable {
    @SerializedName("v")
    var version = 0
        private set

    @SerializedName("sid")
    var sessionId: Long = 0
        private set

    @SerializedName("public_key")
    var publicKey: String? = null
        private set

    @SerializedName("aes_key")
    var aesKey: String? = null
        private set

    @get:SessionState
    @SessionState
    @SerializedName("session_state")
    var senderSessionState = 0
        private set

    @SerializedName("error_code")
    var errorCode = 0
        private set

    @KeyLocationPolicy
    @SerializedName("klp")
    private var keyLocationPolicy: Int? = null

    @Suppress("UNUSED")
    constructor()
    private constructor(builder: Builder) {
        publicKey = builder.publicKey
        version = builder.version
        sessionId = builder.sessionId
        aesKey = builder.aesKey
        senderSessionState = builder.sessionState
        errorCode = builder.errorCode
        keyLocationPolicy = builder.keyLocationPolicy
    }

    private constructor(`in`: Parcel) {
        version = `in`.readInt()
        sessionId = `in`.readLong()
        publicKey = `in`.readString()
        aesKey = `in`.readString()
        @SessionState val s = `in`.readInt()
        senderSessionState = s
        errorCode = `in`.readInt()
        @KeyLocationPolicy val klp = readObjectInteger(`in`)
        keyLocationPolicy = klp
    }

    val isError: Boolean
        get() = errorCode != 0

    override fun toString(): String {
        return "RSA" + Gson().toJson(this)
    }

    @KeyLocationPolicy
    fun getKeyLocationPolicy(): Int {
        return keyLocationPolicy.orZero()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(version)
        dest.writeLong(sessionId)
        dest.writeString(publicKey)
        dest.writeString(aesKey)
        dest.writeInt(senderSessionState)
        dest.writeInt(errorCode)
        writeObjectInteger(dest, keyLocationPolicy)
    }

    class Builder(
        val version: Int,
        val sessionId: Long,
        @SessionState val sessionState: Int
    ) {
        var publicKey: String? = null
        var aesKey: String? = null

        @KeyLocationPolicy
        var keyLocationPolicy: Int? = null
        var errorCode = 0
        fun setPublicKey(publicKey: String?): Builder {
            this.publicKey = publicKey
            return this
        }

        fun setAesKey(aesKey: String?): Builder {
            this.aesKey = aesKey
            return this
        }

        fun setErrorCode(errorCode: Int): Builder {
            this.errorCode = errorCode
            return this
        }

        fun create(): ExchangeMessage {
            return ExchangeMessage(this)
        }

        fun setKeyLocationPolicy(@KeyLocationPolicy keyLocationPolicy: Int): Builder {
            this.keyLocationPolicy = keyLocationPolicy
            return this
        }
    }

    companion object CREATOR : Parcelable.Creator<ExchangeMessage> {
        override fun createFromParcel(parcel: Parcel): ExchangeMessage {
            return ExchangeMessage(parcel)
        }

        override fun newArray(size: Int): Array<ExchangeMessage?> {
            return arrayOfNulls(size)
        }
    }
}