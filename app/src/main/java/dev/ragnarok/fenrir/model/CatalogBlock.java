package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import java.util.List;


public class CatalogBlock extends AbsModel {
    public static final Creator<CatalogBlock> CREATOR = new Creator<CatalogBlock>() {
        @Override
        public CatalogBlock createFromParcel(Parcel in) {
            return new CatalogBlock(in);
        }

        @Override
        public CatalogBlock[] newArray(int size) {
            return new CatalogBlock[size];
        }
    };
    private String next_from;
    private List<Audio> audios;
    private List<AudioPlaylist> playlists;
    private List<Video> videos;
    private List<Link> links;

    public CatalogBlock() {

    }

    protected CatalogBlock(Parcel in) {
        super(in);
        next_from = in.readString();
        audios = in.createTypedArrayList(Audio.CREATOR);
        playlists = in.createTypedArrayList(AudioPlaylist.CREATOR);
        videos = in.createTypedArrayList(Video.CREATOR);
        links = in.createTypedArrayList(Link.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(next_from);
        dest.writeTypedList(audios);
        dest.writeTypedList(playlists);
        dest.writeTypedList(videos);
        dest.writeTypedList(links);
    }

    public String getNext_from() {
        return next_from;
    }

    public CatalogBlock setNext_from(String next_from) {
        this.next_from = next_from;
        return this;
    }

    public List<Audio> getAudios() {
        return audios;
    }

    public CatalogBlock setAudios(List<Audio> audios) {
        this.audios = audios;
        return this;
    }

    public List<AudioPlaylist> getPlaylists() {
        return playlists;
    }

    public CatalogBlock setPlaylists(List<AudioPlaylist> playlists) {
        this.playlists = playlists;
        return this;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public CatalogBlock setVideos(List<Video> videos) {
        this.videos = videos;
        return this;
    }

    public List<Link> getLinks() {
        return links;
    }

    public CatalogBlock setLinks(List<Link> links) {
        this.links = links;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
