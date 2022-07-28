package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable
import dev.ragnarok.fenrir.util.Objects.safeEquals

class AudioCatalog : AbsModel {
    private var id: String? = null
    private var source: String? = null
    private var next_from: String? = null
    private var subtitle: String? = null
    private var title: String? = null
    private var type: String? = null
    private var count = 0
    private var audios: ArrayList<Audio>? = null
    private var playlists: List<AudioPlaylist>? = null
    private var videos: List<Video>? = null
    private var links: ArrayList<Link>? = null
    private var artist: ArtistBlock? = null

    constructor()
    internal constructor(`in`: Parcel) : super(`in`) {
        id = `in`.readString()
        source = `in`.readString()
        next_from = `in`.readString()
        subtitle = `in`.readString()
        title = `in`.readString()
        type = `in`.readString()
        count = `in`.readInt()
        audios = `in`.createTypedArrayList(Audio.CREATOR)
        playlists = `in`.createTypedArrayList(AudioPlaylist.CREATOR)
        videos = `in`.createTypedArrayList(Video.CREATOR)
        links = `in`.createTypedArrayList(Link.CREATOR)
        artist = `in`.readParcelable(ArtistBlock::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
        parcel.writeString(id)
        parcel.writeString(source)
        parcel.writeString(next_from)
        parcel.writeString(subtitle)
        parcel.writeString(title)
        parcel.writeString(type)
        parcel.writeInt(count)
        parcel.writeTypedList(audios)
        parcel.writeTypedList(playlists)
        parcel.writeTypedList(videos)
        parcel.writeTypedList(links)
        parcel.writeParcelable(artist, i)
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String?): AudioCatalog {
        this.id = id
        return this
    }

    fun getSource(): String? {
        return source
    }

    fun setSource(source: String?): AudioCatalog {
        this.source = source
        return this
    }

    fun getNext_from(): String? {
        return next_from
    }

    fun setNext_from(next_from: String?): AudioCatalog {
        this.next_from = next_from
        return this
    }

    fun getSubtitle(): String? {
        return subtitle
    }

    fun setSubtitle(subtitle: String?): AudioCatalog {
        this.subtitle = subtitle
        return this
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?): AudioCatalog {
        this.title = title
        return this
    }

    fun getType(): String? {
        return type
    }

    fun setType(type: String?): AudioCatalog {
        this.type = type
        return this
    }

    fun getCount(): Int {
        return count
    }

    fun setCount(count: Int): AudioCatalog {
        this.count = count
        return this
    }

    fun getAudios(): ArrayList<Audio>? {
        return audios
    }

    fun setAudios(audios: ArrayList<Audio>?): AudioCatalog {
        this.audios = audios
        return this
    }

    fun getPlaylists(): List<AudioPlaylist>? {
        return playlists
    }

    fun setPlaylists(playlists: List<AudioPlaylist>?): AudioCatalog {
        this.playlists = playlists
        return this
    }

    fun getVideos(): List<Video>? {
        return videos
    }

    fun setVideos(videos: List<Video>?): AudioCatalog {
        this.videos = videos
        return this
    }

    fun getLinks(): ArrayList<Link>? {
        return links
    }

    fun setLinks(links: ArrayList<Link>?): AudioCatalog {
        this.links = links
        return this
    }

    fun getArtist(): ArtistBlock? {
        return artist
    }

    fun setArtist(artist: ArtistBlock?): AudioCatalog {
        this.artist = artist
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AudioCatalog) return false
        return safeEquals(id, other.getId())
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    class ArtistBlock : AbsModel {
        private var name: String? = null
        private var photo: String? = null

        constructor()
        internal constructor(`in`: Parcel) : super(`in`) {
            name = `in`.readString()
            photo = `in`.readString()
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(parcel: Parcel, i: Int) {
            super.writeToParcel(parcel, i)
            parcel.writeString(name)
            parcel.writeString(photo)
        }

        fun getName(): String? {
            return name
        }

        fun setName(name: String?): ArtistBlock {
            this.name = name
            return this
        }

        fun getPhoto(): String? {
            return photo
        }

        fun setPhoto(photo: String?): ArtistBlock {
            this.photo = photo
            return this
        }

        companion object CREATOR : Parcelable.Creator<ArtistBlock> {
            override fun createFromParcel(parcel: Parcel): ArtistBlock {
                return ArtistBlock(parcel)
            }

            override fun newArray(size: Int): Array<ArtistBlock?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<AudioCatalog> {
        override fun createFromParcel(parcel: Parcel): AudioCatalog {
            return AudioCatalog(parcel)
        }

        override fun newArray(size: Int): Array<AudioCatalog?> {
            return arrayOfNulls(size)
        }
    }
}