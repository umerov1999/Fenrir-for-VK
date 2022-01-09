package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.api.model.response.AddToPlaylistResponse;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.fragment.search.criteria.ArtistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.CatalogBlock;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class AudioInteractor implements IAudioInteractor {

    private final INetworker networker;

    public AudioInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Completable add(int accountId, Audio orig, Integer groupId) {
        return networker.vkDefault(accountId)
                .audio()
                .add(orig.getId(), orig.getOwnerId(), groupId, orig.getAccessKey())
                .ignoreElement();
    }

    @Override
    public Completable delete(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .delete(audioId, ownerId)
                .ignoreElement();
    }

    @Override
    public Completable edit(int accountId, int ownerId, int audioId, String artist, String title, String text) {
        return networker.vkDefault(accountId)
                .audio()
                .edit(ownerId, audioId, artist, title, text)
                .ignoreElement();
    }

    @Override
    public Completable restore(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .restore(audioId, ownerId)
                .ignoreElement();
    }

    @Override
    public Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, Collection<Integer> targetIds) {
        return networker.vkDefault(accountId)
                .audio()
                .setBroadcast(new dev.ragnarok.fenrir.api.model.IdPair(audioId, audioOwnerId), targetIds)
                .ignoreElement();
    }

    @Override
    public Single<List<Audio>> get(int accountId, Integer playlist_id, int ownerId, int offset, int count, String accessKey) {
        return networker.vkDefault(accountId)
                .audio()
                .get(playlist_id, ownerId, offset, count, accessKey)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<AudioPlaylist>> getPlaylistsCustom(int accountId, String code) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylistsCustom(code)
                .map(items -> listEmptyIfNull(items.playlists))
                .map(out -> {
                    List<AudioPlaylist> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getAudiosByArtist(int accountId, String artist_id, int offset, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getAudiosByArtist(artist_id, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getById(int accountId, @NonNull List<Audio> audios) {
        return networker.vkDefault(accountId)
                .audio()
                .getById(audios)
                .map(Utils::listEmptyIfNull)
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getByIdOld(int accountId, @NonNull List<Audio> audios, boolean old) {
        if (!old) {
            return getById(accountId, audios);
        }
        return networker.vkDefault(accountId)
                .audio()
                .getByIdOld(audios)
                .map(Utils::listEmptyIfNull)
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<String> getLyrics(int accountId, int lyrics_id) {
        return networker.vkDefault(accountId)
                .audio().getLyrics(lyrics_id).map(out -> out.text);
    }

    @Override
    public Single<List<Audio>> getPopular(int accountId, int foreign, int genre, int count) {

        return networker.vkDefault(accountId)
                .audio()
                .getPopular(foreign, genre, count)
                .map(Utils::listEmptyIfNull)
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getRecommendations(int accountId, int audioOwnerId, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getRecommendations(audioOwnerId, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<Audio>> getRecommendationsByAudio(int accountId, String audio, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getRecommendationsByAudio(audio, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<AudioPlaylist>> getPlaylists(int accountId, int owner_id, int offset, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylists(owner_id, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<AudioPlaylist> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<AudioPlaylist> createPlaylist(int accountId, int ownerId, String title, String description) {
        return networker.vkDefault(accountId)
                .audio()
                .createPlaylist(ownerId, title, description)
                .map(out -> out != null ? Dto2Model.transform(out) : null);
    }

    @Override
    public Single<Integer> editPlaylist(int accountId, int ownerId, int playlist_id, String title, String description) {
        return networker.vkDefault(accountId)
                .audio()
                .editPlaylist(ownerId, playlist_id, title, description)
                .map(resultId -> resultId);
    }

    @Override
    public Single<Integer> removeFromPlaylist(int accountId, int ownerId, int playlist_id, Collection<AccessIdPair> audio_ids) {
        return networker.vkDefault(accountId)
                .audio()
                .removeFromPlaylist(ownerId, playlist_id, audio_ids)
                .map(resultId -> resultId);
    }

    @Override
    public Single<List<AddToPlaylistResponse>> addToPlaylist(int accountId, int ownerId, int playlist_id, Collection<AccessIdPair> audio_ids) {
        return networker.vkDefault(accountId)
                .audio()
                .addToPlaylist(ownerId, playlist_id, audio_ids)
                .map(resultId -> resultId);
    }

    @Override
    public Single<List<AudioCatalog>> getCatalog(int accountId, String artist_id, String query) {
        return networker.vkDefault(accountId)
                .audio()
                .getCatalog(artist_id, query)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<AudioCatalog> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<AudioPlaylist> followPlaylist(int accountId, int playlist_id, int ownerId, String accessKey) {
        return networker.vkDefault(accountId)
                .audio()
                .followPlaylist(playlist_id, ownerId, accessKey)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<AudioPlaylist> clonePlaylist(int accountId, int playlist_id, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .clonePlaylist(playlist_id, ownerId)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<AudioPlaylist> getPlaylistById(int accountId, int playlist_id, int ownerId, String accessKey) {
        return networker.vkDefault(accountId)
                .audio()
                .getPlaylistById(playlist_id, ownerId, accessKey)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<Integer> reorder(int accountId, int ownerId, int audio_id, Integer before, Integer after) {
        return networker.vkDefault(accountId)
                .audio()
                .reorder(ownerId, audio_id, before, after)
                .map(resultId -> resultId);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public Completable trackEvents(int accountId, @NonNull Audio audio) {
        String events = String.format("[{\"e\":\"audio_play\",\"audio_id\":\"%s\",\"source\":\"%s\",\"uuid\":%s,\"duration\":%d,\"start_time\":%d}]", audio.getOwnerId() + "_" + audio.getId(), "my", System.nanoTime(), audio.getDuration(), System.currentTimeMillis() / 1000);
        return networker.vkDefault(accountId)
                .audio()
                .trackEvents(events)
                .ignoreElement();
    }

    @Override
    public Single<Integer> deletePlaylist(int accountId, int playlist_id, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .deletePlaylist(playlist_id, ownerId)
                .map(resultId -> resultId);
    }

    @Override
    public Single<List<Audio>> search(int accountId, AudioSearchCriteria criteria, int offset, int count) {
        Boolean isMyAudio = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_ADDED);
        Boolean isbyArtist = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_BY_ARTIST);
        Boolean isautocmp = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_AUTOCOMPLETE);
        Boolean islyrics = criteria.extractBoleanValueFromOption(AudioSearchCriteria.KEY_SEARCH_WITH_LYRICS);
        SpinnerOption sortOption = criteria.findOptionByKey(AudioSearchCriteria.KEY_SORT);
        Integer sort = (sortOption == null || sortOption.value == null) ? null : sortOption.value.id;

        return networker.vkDefault(accountId)
                .audio()
                .search(criteria.getQuery(), isautocmp, islyrics, isbyArtist, sort, isMyAudio, offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<Audio> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<List<VkApiArtist>> searchArtists(int accountId, ArtistSearchCriteria criteria, int offset, int count) {
        return networker.vkDefault(accountId)
                .audio()
                .searchArtists(criteria.getQuery(), offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> out);
    }

    @Override
    public Single<List<AudioPlaylist>> searchPlaylists(int accountId, AudioPlaylistSearchCriteria criteria, int offset, int count) {

        return networker.vkDefault(accountId)
                .audio()
                .searchPlaylists(criteria.getQuery(), offset, count)
                .map(items -> listEmptyIfNull(items.getItems()))
                .map(out -> {
                    List<AudioPlaylist> ret = new ArrayList<>();
                    for (int i = 0; i < out.size(); i++)
                        ret.add(Dto2Model.transform(out.get(i)));
                    return ret;
                });
    }

    @Override
    public Single<CatalogBlock> getCatalogBlockById(int accountId, String block_id, String start_from) {
        return networker.vkDefault(accountId)
                .audio()
                .getCatalogBlockById(block_id, start_from)
                .map(Dto2Model::transform);
    }

    @Override
    public Completable PlaceToAudioCache(Context context) {
        if (!AppPerms.hasReadStoragePermissionSimple(context))
            return Completable.complete();
        return Completable.create(t -> {
            File temp = new File(Settings.get().other().getMusicDir());
            if (!temp.exists()) {
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                t.onComplete();
                return;
            }
            MusicPlaybackController.CachedAudios.clear();
            for (File u : file_list) {
                if (u.isFile())
                    MusicPlaybackController.CachedAudios.add(u.getName());
            }
        });
    }
}
