package dev.ragnarok.fenrir.domain;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.api.model.response.AddToPlaylistResponse;
import dev.ragnarok.fenrir.fragment.search.criteria.ArtistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioPlaylistSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioCatalog;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.CatalogBlock;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IAudioInteractor {
    Completable add(int accountId, Audio orig, Integer groupId);

    Completable delete(int accountId, int audioId, int ownerId);

    Completable edit(int accountId, int ownerId, int audioId, String artist, String title, String text);

    Completable restore(int accountId, int audioId, int ownerId);

    Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, @Nullable Collection<Integer> targetIds);

    Single<List<Audio>> get(int accountId, Integer playlist_id, int ownerId, int offset, int count, String accessKey);

    Single<List<AudioPlaylist>> getPlaylistsCustom(int accountId, String code);

    Single<List<Audio>> getAudiosByArtist(int accountId, String artist_id, int offset, int count);

    Single<List<Audio>> getById(int accountId, @NonNull List<Audio> audios);

    Single<List<Audio>> getByIdOld(int accountId, @NonNull List<Audio> audios, boolean old);

    Single<String> getLyrics(int accountId, int lyrics_id);

    Single<List<Audio>> getPopular(int accountId, int foreign, int genre, int count);

    Single<List<Audio>> getRecommendations(int accountId, int audioOwnerId, int count);

    Single<List<Audio>> getRecommendationsByAudio(int accountId, String audio, int count);

    Single<List<Audio>> search(int accountId, AudioSearchCriteria criteria, int offset, int count);

    Single<List<VkApiArtist>> searchArtists(int accountId, ArtistSearchCriteria criteria, int offset, int count);

    Single<List<AudioPlaylist>> searchPlaylists(int accountId, AudioPlaylistSearchCriteria criteria, int offset, int count);

    Single<List<AudioPlaylist>> getPlaylists(int accountId, int owner_id, int offset, int count);

    Single<AudioPlaylist> createPlaylist(int accountId, int ownerId, String title, String description);

    Single<Integer> editPlaylist(int accountId, int ownerId, int playlist_id, String title, String description);

    Single<Integer> removeFromPlaylist(int accountId, int ownerId, int playlist_id, Collection<AccessIdPair> audio_ids);

    Single<List<AddToPlaylistResponse>> addToPlaylist(int accountId, int ownerId, int playlist_id, Collection<AccessIdPair> audio_ids);

    Single<AudioPlaylist> followPlaylist(int accountId, int playlist_id, int ownerId, String accessKey);

    Single<AudioPlaylist> clonePlaylist(int accountId, int playlist_id, int ownerId);

    Single<AudioPlaylist> getPlaylistById(int accountId, int playlist_id, int ownerId, String accessKey);

    Single<Integer> deletePlaylist(int accountId, int playlist_id, int ownerId);

    Single<Integer> reorder(int accountId, int ownerId, int audio_id, Integer before, Integer after);

    Completable trackEvents(int accountId, @NonNull Audio audio);

    Single<List<AudioCatalog>> getCatalog(int accountId, String artist_id, String query);

    Single<CatalogBlock> getCatalogBlockById(int accountId, String block_id, String start_from);

    Completable PlaceToAudioCache(Context context);
}
