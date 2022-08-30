package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.getBoolean
import dev.ragnarok.fenrir.readTypedObjectCompat
import dev.ragnarok.fenrir.writeTypedObjectCompat

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
        private val TYPES: MutableList<Class<*>> = ArrayList(25)
        private val LOADERS: MutableList<(`in`: Parcel) -> AbsModel?> = ArrayList(25)

        fun wrap(model: AbsModel): ParcelableModelWrapper {
            return ParcelableModelWrapper(model)
        }

        fun readModel(`in`: Parcel): AbsModel? {
            val ex = `in`.getBoolean()
            if (!ex) {
                return null
            }
            return `in`.readTypedObjectCompat(CREATOR)
                ?.get()
        }

        fun writeModel(dest: Parcel, flags: Int, owner: AbsModel?) {
            if (owner == null) {
                dest.writeInt(0)
            } else {
                dest.writeInt(1)
                dest.writeTypedObjectCompat(ParcelableModelWrapper(owner), flags)
            }
        }

        init {
            //Types
            TYPES.add(Photo::class.java)
            TYPES.add(Post::class.java)
            TYPES.add(News::class.java)
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

            //Loaders
            LOADERS.add { it.readTypedObjectCompat(Photo.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Post.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(News.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Video.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(FwdMessages.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(VoiceMessage.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Document.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Audio.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Chat.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Poll.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Link.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Article.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Story.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Call.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(NotSupported.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(WallReply.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(PhotoAlbum.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(AudioPlaylist.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Graffiti.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Gift.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(GiftItem.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Comment.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Event.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(Market.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(MarketAlbum.CREATOR) }
            LOADERS.add { it.readTypedObjectCompat(AudioArtist.CREATOR) }
        }
    }

    private val model: AbsModel

    private constructor(model: AbsModel) {
        this.model = model
    }

    internal constructor(`in`: Parcel) {
        val index = `in`.readInt()
        model = LOADERS[index].invoke(`in`)!!
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
        dest.writeTypedObjectCompat(model, flags)
    }

    fun get(): AbsModel {
        return model
    }
}