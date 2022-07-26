package dev.ragnarok.fenrir.model

import android.os.Parcel
import android.os.Parcelable

class CatalogBlock : AbsModel {
    private var next_from: String? = null
    private var audios: List<Audio>? = null
    private var playlists: List<AudioPlaylist>? = null
    private var videos: List<Video>? = null
    private var links: List<Link>? = null

    constructor()
    private constructor(`in`: Parcel) : super(`in`) {
        next_from = `in`.readString()
        audios = `in`.createTypedArrayList(Audio.CREATOR)
        playlists = `in`.createTypedArrayList(AudioPlaylist.CREATOR)
        videos = `in`.createTypedArrayList(Video.CREATOR)
        links = `in`.createTypedArrayList(Link.CREATOR)
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        super.writeToParcel(parcel, i)
        parcel.writeString(next_from)
        parcel.writeTypedList(audios)
        parcel.writeTypedList(playlists)
        parcel.writeTypedList(videos)
        parcel.writeTypedList(links)
    }

    fun getNext_from(): String? {
        return next_from
    }

    fun setNext_from(next_from: String?): CatalogBlock {
        this.next_from = next_from
        return this
    }

    fun getAudios(): List<Audio>? {
        return audios
    }

    fun setAudios(audios: List<Audio>?): CatalogBlock {
        this.audios = audios
        return this
    }

    fun getPlaylists(): List<AudioPlaylist>? {
        return playlists
    }

    fun setPlaylists(playlists: List<AudioPlaylist>?): CatalogBlock {
        this.playlists = playlists
        return this
    }

    fun getVideos(): List<Video>? {
        return videos
    }

    fun setVideos(videos: List<Video>?): CatalogBlock {
        this.videos = videos
        return this
    }

    fun getLinks(): List<Link>? {
        return links
    }

    fun setLinks(links: List<Link>?): CatalogBlock {
        this.links = links
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CatalogBlock> {
        override fun createFromParcel(parcel: Parcel): CatalogBlock {
            return CatalogBlock(parcel)
        }

        override fun newArray(size: Int): Array<CatalogBlock?> {
            return arrayOfNulls(size)
        }
    }
}