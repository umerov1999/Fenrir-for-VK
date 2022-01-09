package dev.ragnarok.fenrir.model;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.cloneListAsArrayList;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;
import static dev.ragnarok.fenrir.util.Utils.safeCountOfMultiple;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import dev.ragnarok.fenrir.adapter.DocLink;
import dev.ragnarok.fenrir.adapter.PostImage;

public class Attachments implements Parcelable, Cloneable {

    public static final Creator<Attachments> CREATOR = new Creator<Attachments>() {
        @Override
        public Attachments createFromParcel(Parcel in) {
            return new Attachments(in);
        }

        @Override
        public Attachments[] newArray(int size) {
            return new Attachments[size];
        }
    };

    private ArrayList<Audio> audios;
    private ArrayList<Sticker> stickers;
    private ArrayList<Photo> photos;
    private ArrayList<Document> docs;
    private ArrayList<Video> videos;
    private ArrayList<Post> posts;
    private ArrayList<Link> links;
    private ArrayList<Article> articles;
    private ArrayList<Story> stories;
    private ArrayList<Call> calls;
    private ArrayList<Poll> polls;
    private ArrayList<WikiPage> pages;
    private ArrayList<VoiceMessage> voiceMessages;
    private ArrayList<GiftItem> gifts;
    private ArrayList<AudioPlaylist> audio_playlists;
    private ArrayList<Graffiti> graffity;
    private ArrayList<PhotoAlbum> photo_albums;
    private ArrayList<NotSupported> not_supported;
    private ArrayList<Event> events;
    private ArrayList<Market> markets;
    private ArrayList<MarketAlbum> market_albums;
    private ArrayList<WallReply> wall_replies;
    private ArrayList<AudioArtist> audioArtists;

    public Attachments() {
    }

    protected Attachments(Parcel in) {
        audios = in.createTypedArrayList(Audio.CREATOR);
        stickers = in.createTypedArrayList(Sticker.CREATOR);
        photos = in.createTypedArrayList(Photo.CREATOR);
        docs = in.createTypedArrayList(Document.CREATOR);
        videos = in.createTypedArrayList(Video.CREATOR);
        posts = in.createTypedArrayList(Post.CREATOR);
        links = in.createTypedArrayList(Link.CREATOR);
        articles = in.createTypedArrayList(Article.CREATOR);
        polls = in.createTypedArrayList(Poll.CREATOR);
        pages = in.createTypedArrayList(WikiPage.CREATOR);
        voiceMessages = in.createTypedArrayList(VoiceMessage.CREATOR);
        gifts = in.createTypedArrayList(GiftItem.CREATOR);
        stories = in.createTypedArrayList(Story.CREATOR);
        calls = in.createTypedArrayList(Call.CREATOR);
        audio_playlists = in.createTypedArrayList(AudioPlaylist.CREATOR);
        graffity = in.createTypedArrayList(Graffiti.CREATOR);
        photo_albums = in.createTypedArrayList(PhotoAlbum.CREATOR);
        not_supported = in.createTypedArrayList(NotSupported.CREATOR);
        events = in.createTypedArrayList(Event.CREATOR);
        markets = in.createTypedArrayList(Market.CREATOR);
        market_albums = in.createTypedArrayList(MarketAlbum.CREATOR);
        wall_replies = in.createTypedArrayList(WallReply.CREATOR);
        audioArtists = in.createTypedArrayList(AudioArtist.CREATOR);
    }

    public ArrayList<VoiceMessage> getVoiceMessages() {
        return voiceMessages;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(audios);
        dest.writeTypedList(stickers);
        dest.writeTypedList(photos);
        dest.writeTypedList(docs);
        dest.writeTypedList(videos);
        dest.writeTypedList(posts);
        dest.writeTypedList(links);
        dest.writeTypedList(articles);
        dest.writeTypedList(polls);
        dest.writeTypedList(pages);
        dest.writeTypedList(voiceMessages);
        dest.writeTypedList(gifts);
        dest.writeTypedList(stories);
        dest.writeTypedList(calls);
        dest.writeTypedList(audio_playlists);
        dest.writeTypedList(graffity);
        dest.writeTypedList(photo_albums);
        dest.writeTypedList(not_supported);
        dest.writeTypedList(events);
        dest.writeTypedList(markets);
        dest.writeTypedList(market_albums);
        dest.writeTypedList(wall_replies);
        dest.writeTypedList(audioArtists);
    }

    public void add(AbsModel model) {
        if (model instanceof Audio) {
            prepareAudios().add((Audio) model);
            return;
        }

        if (model instanceof Sticker) {
            prepareStickers().add((Sticker) model);
            return;
        }

        if (model instanceof PhotoAlbum) {
            preparePhotoAlbums().add((PhotoAlbum) model);
            return;
        }

        if (model instanceof Photo) {
            preparePhotos().add((Photo) model);
            return;
        }

        if (model instanceof VoiceMessage) {
            prepareVoiceMessages().add((VoiceMessage) model);
            return;
        }

        if (model instanceof Document) {
            prepareDocs().add((Document) model);
            return;
        }

        if (model instanceof Video) {
            prepareVideos().add((Video) model);
            return;
        }

        if (model instanceof Post) {
            preparePosts().add((Post) model);
            return;
        }

        if (model instanceof Link) {
            prepareLinks().add((Link) model);
            return;
        }

        if (model instanceof Article) {
            prepareArticles().add((Article) model);
            return;
        }

        if (model instanceof Story) {
            prepareStories().add((Story) model);
            return;
        }

        if (model instanceof Call) {
            prepareCalls().add((Call) model);
            return;
        }

        if (model instanceof NotSupported) {
            prepareNotSupporteds().add((NotSupported) model);
            return;
        }

        if (model instanceof Event) {
            prepareEvents().add((Event) model);
            return;
        }

        if (model instanceof Market) {
            prepareMarkets().add((Market) model);
            return;
        }

        if (model instanceof MarketAlbum) {
            prepareMarketAlbums().add((MarketAlbum) model);
            return;
        }

        if (model instanceof AudioArtist) {
            prepareAudioArtist().add((AudioArtist) model);
            return;
        }

        if (model instanceof WallReply) {
            prepareWallReply().add((WallReply) model);
            return;
        }

        if (model instanceof AudioPlaylist) {
            prepareAudioPlaylists().add((AudioPlaylist) model);
            return;
        }

        if (model instanceof Graffiti) {
            prepareGraffity().add((Graffiti) model);
            return;
        }

        if (model instanceof Poll) {
            preparePolls().add((Poll) model);
            return;
        }

        if (model instanceof WikiPage) {
            prepareWikiPages().add((WikiPage) model);
        }

        if (model instanceof GiftItem) {
            prepareGifts().add((GiftItem) model);
        }
    }

    public ArrayList<AbsModel> toList() {
        ArrayList<AbsModel> result = new ArrayList<>();
        if (nonEmpty(audios)) {
            result.addAll(audios);
        }

        if (nonEmpty(stickers)) {
            result.addAll(stickers);
        }

        if (nonEmpty(photo_albums)) {
            result.addAll(photo_albums);
        }

        if (nonEmpty(photos)) {
            result.addAll(photos);
        }

        if (nonEmpty(docs)) {
            result.addAll(docs);
        }

        if (nonEmpty(voiceMessages)) {
            result.addAll(voiceMessages);
        }

        if (nonEmpty(videos)) {
            result.addAll(videos);
        }

        if (nonEmpty(posts)) {
            result.addAll(posts);
        }

        if (nonEmpty(links)) {
            result.addAll(links);
        }

        if (nonEmpty(articles)) {
            result.addAll(articles);
        }

        if (nonEmpty(stories)) {
            result.addAll(stories);
        }

        if (nonEmpty(calls)) {
            result.addAll(calls);
        }

        if (nonEmpty(audio_playlists)) {
            result.addAll(audio_playlists);
        }

        if (nonEmpty(not_supported)) {
            result.addAll(not_supported);
        }

        if (nonEmpty(events)) {
            result.addAll(events);
        }

        if (nonEmpty(markets)) {
            result.addAll(markets);
        }

        if (nonEmpty(market_albums)) {
            result.addAll(market_albums);
        }

        if (nonEmpty(audioArtists)) {
            result.addAll(audioArtists);
        }

        if (nonEmpty(wall_replies)) {
            result.addAll(wall_replies);
        }

        if (nonEmpty(graffity)) {
            result.addAll(graffity);
        }

        if (nonEmpty(polls)) {
            result.addAll(polls);
        }

        if (nonEmpty(pages)) {
            result.addAll(pages);
        }

        if (nonEmpty(gifts)) {
            result.addAll(gifts);
        }

        return result;
    }

    public ArrayList<Audio> prepareAudios() {
        if (audios == null) {
            audios = new ArrayList<>(1);
        }

        return audios;
    }

    public ArrayList<WikiPage> prepareWikiPages() {
        if (pages == null) {
            pages = new ArrayList<>(1);
        }

        return pages;
    }

    public ArrayList<Photo> preparePhotos() {
        if (photos == null) {
            photos = new ArrayList<>(1);
        }

        return photos;
    }

    public ArrayList<Video> prepareVideos() {
        if (videos == null) {
            videos = new ArrayList<>(1);
        }

        return videos;
    }

    public ArrayList<Link> prepareLinks() {
        if (links == null) {
            links = new ArrayList<>(1);
        }

        return links;
    }

    public ArrayList<Article> prepareArticles() {
        if (articles == null) {
            articles = new ArrayList<>(1);
        }

        return articles;
    }

    public ArrayList<Story> prepareStories() {
        if (stories == null) {
            stories = new ArrayList<>(1);
        }

        return stories;
    }

    public ArrayList<Call> prepareCalls() {
        if (calls == null) {
            calls = new ArrayList<>(1);
        }

        return calls;
    }

    public ArrayList<WallReply> prepareWallReply() {
        if (wall_replies == null) {
            wall_replies = new ArrayList<>(1);
        }

        return wall_replies;
    }

    public ArrayList<NotSupported> prepareNotSupporteds() {
        if (not_supported == null) {
            not_supported = new ArrayList<>(1);
        }

        return not_supported;
    }

    public ArrayList<Event> prepareEvents() {
        if (events == null) {
            events = new ArrayList<>(1);
        }

        return events;
    }

    public ArrayList<Market> prepareMarkets() {
        if (markets == null) {
            markets = new ArrayList<>(1);
        }

        return markets;
    }

    public ArrayList<MarketAlbum> prepareMarketAlbums() {
        if (market_albums == null) {
            market_albums = new ArrayList<>(1);
        }

        return market_albums;
    }

    public ArrayList<AudioArtist> prepareAudioArtist() {
        if (audioArtists == null) {
            audioArtists = new ArrayList<>(1);
        }

        return audioArtists;
    }

    public ArrayList<AudioPlaylist> prepareAudioPlaylists() {
        if (audio_playlists == null) {
            audio_playlists = new ArrayList<>(1);
        }

        return audio_playlists;
    }

    public ArrayList<Graffiti> prepareGraffity() {
        if (graffity == null) {
            graffity = new ArrayList<>(1);
        }

        return graffity;
    }

    public ArrayList<Document> prepareDocs() {
        if (docs == null) {
            docs = new ArrayList<>(1);
        }

        return docs;
    }

    public ArrayList<VoiceMessage> prepareVoiceMessages() {
        if (voiceMessages == null) {
            voiceMessages = new ArrayList<>(1);
        }

        return voiceMessages;
    }

    public ArrayList<Poll> preparePolls() {
        if (polls == null) {
            polls = new ArrayList<>(1);
        }

        return polls;
    }

    public ArrayList<Sticker> prepareStickers() {
        if (stickers == null) {
            stickers = new ArrayList<>(1);
        }

        return stickers;
    }

    public ArrayList<PhotoAlbum> preparePhotoAlbums() {
        if (photo_albums == null) {
            photo_albums = new ArrayList<>(1);
        }

        return photo_albums;
    }

    public ArrayList<Post> preparePosts() {
        if (posts == null) {
            posts = new ArrayList<>(1);
        }

        return posts;
    }

    public ArrayList<GiftItem> prepareGifts() {
        if (gifts == null) {
            gifts = new ArrayList<>(1);
        }

        return gifts;
    }

    public int size() {
        return safeCountOfMultiple(
                audios,
                stickers,
                photos,
                docs,
                videos,
                posts,
                links,
                articles,
                stories,
                photo_albums,
                calls,
                audio_playlists,
                graffity,
                polls,
                pages,
                voiceMessages,
                gifts,
                not_supported,
                events,
                markets,
                market_albums,
                wall_replies,
                audioArtists
        );
    }

    public int size_no_stickers() {
        return safeCountOfMultiple(
                audios,
                photos,
                docs,
                videos,
                posts,
                links,
                articles,
                stories,
                photo_albums,
                calls,
                audio_playlists,
                graffity,
                polls,
                pages,
                voiceMessages,
                gifts,
                not_supported,
                events,
                markets,
                market_albums,
                wall_replies,
                audioArtists
        );
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isPhotosVideosGifsOnly() {
        boolean hasGifWithPreview = false;

        if (nonEmpty(docs)) {
            for (Document document : docs) {
                if (document.isGif() && nonNull(document.getPhotoPreview())) {
                    hasGifWithPreview = true;
                } else {
                    return false;
                }
            }
        }

        if (safeIsEmpty(photos) && safeIsEmpty(videos) && !hasGifWithPreview) {
            return false;
        }

        return safeIsEmpty(audios) &&
                safeIsEmpty(stickers) &&
                safeIsEmpty(posts) &&
                safeIsEmpty(links) &&
                safeIsEmpty(articles) &&
                safeIsEmpty(stories) &&
                safeIsEmpty(photo_albums) &&
                safeIsEmpty(calls) &&
                safeIsEmpty(audio_playlists) &&
                safeIsEmpty(graffity) &&
                safeIsEmpty(pages) &&
                safeIsEmpty(polls) &&
                safeIsEmpty(voiceMessages) &&
                safeIsEmpty(not_supported) &&
                safeIsEmpty(events) &&
                safeIsEmpty(markets) &&
                safeIsEmpty(market_albums) &&
                safeIsEmpty(wall_replies) &&
                safeIsEmpty(audioArtists) &&
                safeIsEmpty(gifts);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public ArrayList<PostImage> getPostImagesVideos() {
        ArrayList<PostImage> result = new ArrayList<>(safeCountOf(videos));
        if (nonNull(videos)) {
            for (Video video : videos) {
                result.add(new PostImage(video, PostImage.TYPE_VIDEO));
            }
        }
        return result;
    }

    public ArrayList<PostImage> getPostImages() {
        ArrayList<PostImage> result = new ArrayList<>(safeCountOfMultiple(photos, videos));
        if (nonNull(photos)) {
            for (Photo photo : photos) {
                result.add(new PostImage(photo, PostImage.TYPE_IMAGE));
            }
        }

        if (nonNull(docs)) {
            for (Document document : docs) {
                if (document.isGif() && nonNull(document.getPhotoPreview())) {
                    result.add(new PostImage(document, PostImage.TYPE_GIF));
                }
            }
        }

        return result;
    }

    public ArrayList<DocLink> getDocLinks(boolean postsAsLink, boolean excludeGifWithImages) {
        ArrayList<DocLink> result = new ArrayList<>();
        if (docs != null) {
            for (Document doc : docs) {
                if (excludeGifWithImages && doc.isGif() && nonNull(doc.getPhotoPreview())) {
                    continue;
                }

                result.add(new DocLink(doc));
            }
        }

        if (postsAsLink && posts != null) {
            for (Post post : posts) {
                if (post != null) {
                    result.add(new DocLink(post));
                }
            }
        }

        if (links != null) {
            for (Link link : links) {
                result.add(new DocLink(link));
            }
        }

        if (polls != null) {
            for (Poll poll : polls) {
                result.add(new DocLink(poll));
            }
        }

        if (pages != null) {
            for (WikiPage page : pages) {
                result.add(new DocLink(page));
            }
        }

        if (stories != null) {
            for (Story story : stories) {
                result.add(new DocLink(story));
            }
        }

        if (calls != null) {
            for (Call call : calls) {
                result.add(new DocLink(call));
            }
        }

        if (audio_playlists != null) {
            for (AudioPlaylist playlist : audio_playlists) {
                result.add(new DocLink(playlist));
            }
        }

        if (graffity != null) {
            for (Graffiti graff : graffity) {
                result.add(new DocLink(graff));
            }
        }

        if (photo_albums != null) {
            for (PhotoAlbum album : photo_albums) {
                result.add(new DocLink(album));
            }
        }

        if (not_supported != null) {
            for (NotSupported ns : not_supported) {
                result.add(new DocLink(ns));
            }
        }

        if (events != null) {
            for (Event event : events) {
                result.add(new DocLink(event));
            }
        }

        if (markets != null) {
            for (Market market : markets) {
                result.add(new DocLink(market));
            }
        }

        if (market_albums != null) {
            for (MarketAlbum market_album : market_albums) {
                result.add(new DocLink(market_album));
            }
        }

        if (audioArtists != null) {
            for (AudioArtist audio_artist : audioArtists) {
                result.add(new DocLink(audio_artist));
            }
        }

        if (wall_replies != null) {
            for (WallReply ns : wall_replies) {
                result.add(new DocLink(ns));
            }
        }

        return result;
    }

    @NonNull
    @Override
    public Attachments clone() throws CloneNotSupportedException {
        Attachments clone = (Attachments) super.clone();
        clone.audios = cloneListAsArrayList(audios);
        clone.stickers = cloneListAsArrayList(stickers);
        clone.photos = cloneListAsArrayList(photos);
        clone.docs = cloneListAsArrayList(docs);
        clone.videos = cloneListAsArrayList(videos);
        clone.posts = cloneListAsArrayList(posts);
        clone.links = cloneListAsArrayList(links);
        clone.articles = cloneListAsArrayList(articles);
        clone.stories = cloneListAsArrayList(stories);
        clone.photo_albums = cloneListAsArrayList(photo_albums);
        clone.calls = cloneListAsArrayList(calls);
        clone.audio_playlists = cloneListAsArrayList(audio_playlists);
        clone.graffity = cloneListAsArrayList(graffity);
        clone.polls = cloneListAsArrayList(polls);
        clone.pages = cloneListAsArrayList(pages);
        clone.voiceMessages = cloneListAsArrayList(voiceMessages);
        clone.not_supported = cloneListAsArrayList(not_supported);
        clone.events = cloneListAsArrayList(events);
        clone.markets = cloneListAsArrayList(markets);
        clone.market_albums = cloneListAsArrayList(market_albums);
        clone.audioArtists = cloneListAsArrayList(audioArtists);
        clone.wall_replies = cloneListAsArrayList(wall_replies);
        return clone;
    }

    @NonNull
    @Override
    public String toString() {
        String line = "";
        if (nonNull(audios)) {
            line = line + " audios=" + safeCountOf(audios);
        }

        if (nonNull(stickers)) {
            line = line + " stickers=" + safeCountOf(stickers);
        }

        if (nonNull(photos)) {
            line = line + " photos=" + safeCountOf(photos);
        }

        if (nonNull(docs)) {
            line = line + " docs=" + safeCountOf(docs);
        }

        if (nonNull(videos)) {
            line = line + " videos=" + safeCountOf(videos);
        }

        if (nonNull(posts)) {
            line = line + " posts=" + safeCountOf(posts);
        }

        if (nonNull(links)) {
            line = line + " links=" + safeCountOf(links);
        }

        if (nonNull(articles)) {
            line = line + " articles=" + safeCountOf(articles);
        }

        if (nonNull(stories)) {
            line = line + " stories=" + safeCountOf(stories);
        }

        if (nonNull(photo_albums)) {
            line = line + " photo_albums=" + safeCountOf(photo_albums);
        }

        if (nonNull(calls)) {
            line = line + " calls=" + safeCountOf(calls);
        }

        if (nonNull(audio_playlists)) {
            line = line + " audio_playlists=" + safeCountOf(audio_playlists);
        }

        if (nonNull(graffity)) {
            line = line + " graffity=" + safeCountOf(graffity);
        }

        if (nonNull(polls)) {
            line = line + " polls=" + safeCountOf(polls);
        }

        if (nonNull(pages)) {
            line = line + " pages=" + safeCountOf(pages);
        }

        if (nonNull(voiceMessages)) {
            line = line + " voiceMessages=" + safeCountOf(voiceMessages);
        }

        if (nonNull(gifts)) {
            line = line + " gifts=" + safeCountOf(gifts);
        }

        if (nonNull(not_supported)) {
            line = line + " not_supported=" + safeCountOf(not_supported);
        }

        if (nonNull(events)) {
            line = line + " events=" + safeCountOf(events);
        }

        if (nonNull(markets)) {
            line = line + " markets=" + safeCountOf(markets);
        }

        if (nonNull(market_albums)) {
            line = line + " market_albums=" + safeCountOf(market_albums);
        }

        if (nonNull(wall_replies)) {
            line = line + " wall_replies=" + safeCountOf(wall_replies);
        }

        if (nonNull(audioArtists)) {
            line = line + " audioArtists=" + safeCountOf(audioArtists);
        }

        return line.trim();
    }

    public ArrayList<Post> getPosts() {
        return posts;
    }

    public void setPosts(ArrayList<Post> posts) {
        this.posts = posts;
    }

    public ArrayList<Audio> getAudios() {
        return audios;
    }

    public ArrayList<Sticker> getStickers() {
        return stickers;
    }

    public ArrayList<NotSupported> getNotSupported() {
        return not_supported;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public ArrayList<Market> getMarkets() {
        return markets;
    }

    public ArrayList<MarketAlbum> getMarketAlbums() {
        return market_albums;
    }

    public ArrayList<AudioArtist> getAudioArtists() {
        return audioArtists;
    }

    public ArrayList<WallReply> getWallReplies() {
        return wall_replies;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public ArrayList<Document> getDocs() {
        return docs;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public ArrayList<Link> getLinks() {
        return links;
    }

    public ArrayList<Article> getArticles() {
        return articles;
    }

    public ArrayList<Story> getStories() {
        return stories;
    }

    public ArrayList<Call> getCalls() {
        return calls;
    }

    public ArrayList<PhotoAlbum> getPhotoAlbums() {
        return photo_albums;
    }

    public ArrayList<AudioPlaylist> getAudioPlaylists() {
        return audio_playlists;
    }

    public ArrayList<Graffiti> getGraffity() {
        return graffity;
    }

    public ArrayList<Poll> getPolls() {
        return polls;
    }

    public ArrayList<WikiPage> getPages() {
        return pages;
    }

    public ArrayList<GiftItem> getGifts() {
        return gifts;
    }
}
