package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class ParcelableModelWrapper : Parcelable {
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableModelWrapper> =
            object : Parcelable.Creator<ParcelableModelWrapper> {
                override fun createFromParcel(`in`: Parcel): ParcelableModelWrapper {
                    return ParcelableModelWrapper(`in`)
                }

                override fun newArray(size: Int): Array<ParcelableModelWrapper?> {
                    return arrayOfNulls(size)
                }
            }
        private val TYPES: MutableList<Class<*>> = ArrayList()

        fun wrap(model: AbsModel): ParcelableModelWrapper {
            return ParcelableModelWrapper(model)
        }

        fun readModel(`in`: Parcel): AbsModel? {
            val ex = `in`.readByte() != 0.toByte()
            if (!ex) {
                return null
            }
            return `in`.readParcelable<ParcelableModelWrapper>(ParcelableModelWrapper::class.java.classLoader)
                ?.get()
        }

        fun writeModel(dest: Parcel, flags: Int, owner: AbsModel?) {
            if (owner == null) {
                dest.writeByte(0)
            } else {
                dest.writeByte(1)
                dest.writeParcelable(ParcelableModelWrapper(owner), flags)
            }
        }

        init {
            TYPES.add(Photo::class.java)
            TYPES.add(Post::class.java)
            TYPES.add(Video::class.java)
            TYPES.add(FwdMessages::class.java)
            TYPES.add(VoiceMessage::class.java)
            TYPES.add(Document::class.java)
            TYPES.add(Audio::class.java)
            TYPES.add(Chat::class.java)
            TYPES.add(Poll::class.java)
            TYPES.add(Link::class.java)
            TYPES.add(Article::class.java)
            TYPES.add(Story::class.java)
            TYPES.add(Call::class.java)
            TYPES.add(NotSupported::class.java)
            TYPES.add(WallReply::class.java)
            TYPES.add(PhotoAlbum::class.java)
            TYPES.add(AudioPlaylist::class.java)
            TYPES.add(Graffiti::class.java)
            TYPES.add(Gift::class.java)
            TYPES.add(GiftItem::class.java)
            TYPES.add(Comment::class.java)
            TYPES.add(Event::class.java)
            TYPES.add(Market::class.java)
            TYPES.add(MarketAlbum::class.java)
            TYPES.add(AudioArtist::class.java)
        }
    }

    private val model: AbsModel

    private constructor(model: AbsModel) {
        this.model = model
    }

    internal constructor(`in`: Parcel) {
        val index = `in`.readInt()
        val classLoader = TYPES[index].classLoader
        model = `in`.readParcelable(classLoader)!!
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val index = TYPES.indexOf(model.javaClass)
        if (index == -1) {
            throw UnsupportedOperationException("Unsupported class: " + model.javaClass)
        }
        dest.writeInt(index)
        dest.writeParcelable(model, flags)
    }

    fun get(): AbsModel {
        return model
    }
}