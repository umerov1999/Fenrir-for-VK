package dev.ragnarok.fenrir.api.impl;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.interfaces.IAudioApi;
import dev.ragnarok.fenrir.api.model.AccessIdPair;
import dev.ragnarok.fenrir.api.model.IdPair;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioCatalog;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VkApiArtist;
import dev.ragnarok.fenrir.api.model.VkApiLyrics;
import dev.ragnarok.fenrir.api.model.response.AddToPlaylistResponse;
import dev.ragnarok.fenrir.api.model.response.CatalogResponse;
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse;
import dev.ragnarok.fenrir.api.model.server.VkApiAudioUploadServer;
import dev.ragnarok.fenrir.api.services.IAudioService;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Single;


class AudioApi extends AbsApi implements IAudioApi {

    AudioApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<int[]> setBroadcast(IdPair audio, Collection<Integer> targetIds) {
        String audioStr = Objects.isNull(audio) ? null : audio.ownerId + "_" + audio.id;
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .setBroadcast(audioStr, join(targetIds, ","))
                        .map(extractResponseWithErrorHandling()));

    }

    @Override
    public Single<Items<VKApiAudio>> search(String query, Boolean autoComplete, Boolean lyrics, Boolean performerOnly, Integer sort, Boolean searchOwn, Integer offset, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .search(query, integerFromBoolean(autoComplete), integerFromBoolean(lyrics),
                                integerFromBoolean(performerOnly), sort, integerFromBoolean(searchOwn), offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VkApiArtist>> searchArtists(String query, Integer offset, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .searchArtists(query, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudioPlaylist>> searchPlaylists(String query, Integer offset, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .searchPlaylists(query, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudio> restore(int audioId, Integer ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .restore(audioId, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> delete(int audioId, int ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .delete(audioId, ownerId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> edit(int ownerId, int audioId, String artist, String title, String text) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .edit(ownerId, audioId, artist, title, text)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> add(int audioId, int ownerId, Integer groupId, String accessKey) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .add(audioId, ownerId, groupId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudioPlaylist> createPlaylist(int ownerId, String title, String description) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .createPlaylist(ownerId, title, description)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> editPlaylist(int ownerId, int playlist_id, String title, String description) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .editPlaylist(ownerId, playlist_id, title, description)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> removeFromPlaylist(int ownerId, int playlist_id, Collection<AccessIdPair> audio_ids) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .removeFromPlaylist(ownerId, playlist_id, join(audio_ids, ",", AccessIdPair::format))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<AddToPlaylistResponse>> addToPlaylist(int ownerId, int playlist_id, Collection<AccessIdPair> audio_ids) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .addToPlaylist(ownerId, playlist_id, join(audio_ids, ",", AccessIdPair::format))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> reorder(int ownerId, int audio_id, Integer before, Integer after) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .reorder(ownerId, audio_id, before, after)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> trackEvents(String events) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .trackEvents(events)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deletePlaylist(int playlist_id, int ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .deletePlaylist(playlist_id, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudioPlaylist> followPlaylist(int playlist_id, int ownerId, String accessKey) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .followPlaylist(playlist_id, ownerId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudioPlaylist> clonePlaylist(int playlist_id, int ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .clonePlaylist(playlist_id, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudioPlaylist> getPlaylistById(int playlist_id, int ownerId, String accessKey) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getPlaylistById(playlist_id, ownerId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudioCatalog>> getCatalog(String artist_id, String query) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getCatalog(artist_id, query)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> get(Integer playlist_id, Integer ownerId, Integer offset, Integer count, String accessKey) {
        return provideService(IAudioService.class).flatMap(service -> service.get(playlist_id, ownerId, offset, count, accessKey).map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> getAudiosByArtist(String artist_id, Integer offset, Integer count) {
        return provideService(IAudioService.class).flatMap(service -> service.getAudiosByArtist(artist_id, offset, count).map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiAudio>> getPopular(Integer foreign,
                                               Integer genre, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getPopular(foreign, genre, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> getRecommendations(Integer audioOwnerId, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getRecommendations(audioOwnerId, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> getRecommendationsByAudio(String audio, Integer count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getRecommendationsByAudio(audio, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudioPlaylist>> getPlaylists(int owner_id, int offset, int count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getPlaylists(owner_id, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<ServicePlaylistResponse> getPlaylistsCustom(String code) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getPlaylistsCustom(code));
    }

    @Override
    public Single<List<VKApiAudio>> getById(@NonNull List<Audio> audios) {
        ArrayList<AccessIdPair> ids = new ArrayList<>(audios.size());
        for (Audio i : audios) {
            ids.add(new AccessIdPair(i.getId(), i.getOwnerId(), i.getAccessKey()));
        }
        String audio_string = join(ids, ",", AccessIdPair::format);
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getById(audio_string)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiAudio>> getByIdOld(@NonNull List<Audio> audios) {
        ArrayList<AccessIdPair> ids = new ArrayList<>(audios.size());
        for (Audio i : audios) {
            ids.add(new AccessIdPair(i.getId(), i.getOwnerId(), i.getAccessKey()));
        }
        String audio_string = join(ids, ",", AccessIdPair::format);
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getByIdVersioned(audio_string, "5.90")
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiLyrics> getLyrics(int lyrics_id) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getLyrics(lyrics_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<CatalogResponse> getCatalogBlockById(String block_id, String start_from) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getCatalogBlockById(block_id, start_from)
                        .map(extractBlockResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiAudioUploadServer> getUploadServer() {
        return provideService(IAudioService.class)
                .flatMap(service -> service.getUploadServer()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudio> save(String server, String audio, String hash, String artist, String title) {
        return provideService(IAudioService.class)
                .flatMap(service -> service.save(server, audio, hash, artist, title)
                        .map(extractResponseWithErrorHandling()));
    }
}
