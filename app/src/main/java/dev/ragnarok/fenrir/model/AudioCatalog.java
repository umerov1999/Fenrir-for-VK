package dev.ragnarok.fenrir.model;

import android.os.Parcel;

import java.util.List;


public class AudioCatalog extends AbsModel {
    public static final Creator<AudioCatalog> CREATOR = new Creator<AudioCatalog>() {
        @Override
        public AudioCatalog createFromParcel(Parcel in) {
            return new AudioCatalog(in);
        }

        @Override
        public AudioCatalog[] newArray(int size) {
            return new AudioCatalog[size];
        }
    };
    private String id;
    private String source;
    private String next_from;
    private String subtitle;
    private String title;
    private String type;
    private int count;
    private List<Audio> audios;
    private List<AudioPlaylist> playlists;
    private List<Video> videos;
    private List<Link> links;
    private ArtistBlock artist;

    public AudioCatalog() {

    }

    protected AudioCatalog(Parcel in) {
        super(in);
        id = in.readString();
        source = in.readString();
        next_from = in.readString();
        subtitle = in.readString();
        title = in.readString();
        type = in.readString();
        count = in.readInt();
        audios = in.createTypedArrayList(Audio.CREATOR);
        playlists = in.createTypedArrayList(AudioPlaylist.CREATOR);
        videos = in.createTypedArrayList(Video.CREATOR);
        links = in.createTypedArrayList(Link.CREATOR);
        artist = in.readParcelable(ArtistBlock.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(id);
        dest.writeString(source);
        dest.writeString(next_from);
        dest.writeString(subtitle);
        dest.writeString(title);
        dest.writeString(type);
        dest.writeInt(count);
        dest.writeTypedList(audios);
        dest.writeTypedList(playlists);
        dest.writeTypedList(videos);
        dest.writeTypedList(links);
        dest.writeParcelable(artist, flags);
    }

    public String getId() {
        return id;
    }

    public AudioCatalog setId(String id) {
        this.id = id;
        return this;
    }

    public String getSource() {
        return source;
    }

    public AudioCatalog setSource(String source) {
        this.source = source;
        return this;
    }

    public String getNext_from() {
        return next_from;
    }

    public AudioCatalog setNext_from(String next_from) {
        this.next_from = next_from;
        return this;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public AudioCatalog setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public AudioCatalog setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getType() {
        return type;
    }

    public AudioCatalog setType(String type) {
        this.type = type;
        return this;
    }

    public int getCount() {
        return count;
    }

    public AudioCatalog setCount(int count) {
        this.count = count;
        return this;
    }

    public List<Audio> getAudios() {
        return audios;
    }

    public AudioCatalog setAudios(List<Audio> audios) {
        this.audios = audios;
        return this;
    }

    public List<AudioPlaylist> getPlaylists() {
        return playlists;
    }

    public AudioCatalog setPlaylists(List<AudioPlaylist> playlists) {
        this.playlists = playlists;
        return this;
    }

    public List<Video> getVideos() {
        return videos;
    }

    public AudioCatalog setVideos(List<Video> videos) {
        this.videos = videos;
        return this;
    }

    public List<Link> getLinks() {
        return links;
    }

    public AudioCatalog setLinks(List<Link> links) {
        this.links = links;
        return this;
    }

    public ArtistBlock getArtist() {
        return artist;
    }

    public AudioCatalog setArtist(ArtistBlock artist) {
        this.artist = artist;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AudioCatalog))
            return false;

        AudioCatalog playlist = (AudioCatalog) o;
        return id.equals(playlist.getId());
    }

    public static class ArtistBlock extends AbsModel {
        public static final Creator<ArtistBlock> CREATOR = new Creator<ArtistBlock>() {
            @Override
            public ArtistBlock createFromParcel(Parcel in) {
                return new ArtistBlock(in);
            }

            @Override
            public ArtistBlock[] newArray(int size) {
                return new ArtistBlock[size];
            }
        };
        private String name;
        private String photo;

        public ArtistBlock() {

        }

        protected ArtistBlock(Parcel in) {
            super(in);
            name = in.readString();
            photo = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(name);
            dest.writeString(photo);
        }

        public String getName() {
            return name;
        }

        public ArtistBlock setName(String name) {
            this.name = name;
            return this;
        }

        public String getPhoto() {
            return photo;
        }

        public ArtistBlock setPhoto(String photo) {
            this.photo = photo;
            return this;
        }
    }
}
