package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.interfaces.IAudioApi
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.response.AddToPlaylistResponse
import dev.ragnarok.fenrir.api.model.response.CatalogResponse
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse
import dev.ragnarok.fenrir.api.model.server.VkApiAudioUploadServer
import dev.ragnarok.fenrir.api.services.IAudioService
import dev.ragnarok.fenrir.model.Audio
import io.reactivex.rxjava3.core.Single

internal class AudioApi(accountId: Int, provider: IServiceProvider) :
    AbsApi(accountId, provider), IAudioApi {
    override fun setBroadcast(audio: IdPair?, targetIds: Collection<Int>): Single<IntArray> {
        val audioStr = if (audio == null) null else audio.ownerId.toString() + "_" + audio.id
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .setBroadcast(audioStr, join(targetIds, ","))
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun search(
        query: String?,
        autoComplete: Boolean?,
        lyrics: Boolean?,
        performerOnly: Boolean?,
        sort: Int?,
        searchOwn: Boolean?,
        offset: Int?,
        count: Int?
    ): Single<Items<VKApiAudio>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .search(
                        query,
                        integerFromBoolean(autoComplete),
                        integerFromBoolean(lyrics),
                        integerFromBoolean(performerOnly),
                        sort,
                        integerFromBoolean(searchOwn),
                        offset,
                        count
                    )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun searchArtists(
        query: String?,
        offset: Int?,
        count: Int?
    ): Single<Items<VkApiArtist>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .searchArtists(query, offset, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun searchPlaylists(
        query: String?,
        offset: Int?,
        count: Int?
    ): Single<Items<VKApiAudioPlaylist>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .searchPlaylists(query, offset, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun restore(audioId: Int, ownerId: Int?): Single<VKApiAudio> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .restore(audioId, ownerId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun delete(audioId: Int, ownerId: Int): Single<Boolean> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .delete(audioId, ownerId)
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override fun edit(
        ownerId: Int,
        audioId: Int,
        artist: String?,
        title: String?,
        text: String?
    ): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .edit(ownerId, audioId, artist, title, text)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun add(audioId: Int, ownerId: Int, groupId: Int?, accessKey: String?): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .add(audioId, ownerId, groupId, accessKey)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun createPlaylist(
        ownerId: Int,
        title: String?,
        description: String?
    ): Single<VKApiAudioPlaylist> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .createPlaylist(ownerId, title, description)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun editPlaylist(
        ownerId: Int,
        playlist_id: Int,
        title: String?,
        description: String?
    ): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .editPlaylist(ownerId, playlist_id, title, description)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun removeFromPlaylist(
        ownerId: Int,
        playlist_id: Int,
        audio_ids: Collection<AccessIdPair>
    ): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .removeFromPlaylist(
                        ownerId,
                        playlist_id,
                        join(audio_ids, ",") { AccessIdPair.format(it) })
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun addToPlaylist(
        ownerId: Int,
        playlist_id: Int,
        audio_ids: Collection<AccessIdPair>
    ): Single<List<AddToPlaylistResponse>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .addToPlaylist(
                        ownerId,
                        playlist_id,
                        join(audio_ids, ",") { AccessIdPair.format(it) })
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun reorder(ownerId: Int, audio_id: Int, before: Int?, after: Int?): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .reorder(ownerId, audio_id, before, after)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun trackEvents(events: String?): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .trackEvents(events)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun deletePlaylist(playlist_id: Int, ownerId: Int): Single<Int> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .deletePlaylist(playlist_id, ownerId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun followPlaylist(
        playlist_id: Int,
        ownerId: Int,
        accessKey: String?
    ): Single<VKApiAudioPlaylist> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .followPlaylist(playlist_id, ownerId, accessKey)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun clonePlaylist(playlist_id: Int, ownerId: Int): Single<VKApiAudioPlaylist> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .clonePlaylist(playlist_id, ownerId)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getPlaylistById(
        playlist_id: Int,
        ownerId: Int,
        accessKey: String?
    ): Single<VKApiAudioPlaylist> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getPlaylistById(playlist_id, ownerId, accessKey)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getCatalog(artist_id: String?, query: String?): Single<Items<VKApiAudioCatalog>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getCatalog(artist_id, query)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun get(
        playlist_id: Int?,
        ownerId: Int?,
        offset: Int?,
        count: Int?,
        accessKey: String?
    ): Single<Items<VKApiAudio>> {
        return provideService(IAudioService::class.java).flatMap { service: IAudioService ->
            service[playlist_id, ownerId, offset, count, accessKey].map(
                extractResponseWithErrorHandling()
            )
        }
    }

    override fun getAudiosByArtist(
        artist_id: String?,
        offset: Int?,
        count: Int?
    ): Single<Items<VKApiAudio>> {
        return provideService(IAudioService::class.java).flatMap { service: IAudioService ->
            service.getAudiosByArtist(artist_id, offset, count).map(
                extractResponseWithErrorHandling()
            )
        }
    }

    override fun getPopular(
        foreign: Int?,
        genre: Int?, count: Int?
    ): Single<List<VKApiAudio>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getPopular(foreign, genre, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getRecommendations(audioOwnerId: Int?, count: Int?): Single<Items<VKApiAudio>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getRecommendations(audioOwnerId, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getRecommendationsByAudio(audio: String?, count: Int?): Single<Items<VKApiAudio>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getRecommendationsByAudio(audio, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getPlaylists(
        owner_id: Int,
        offset: Int,
        count: Int
    ): Single<Items<VKApiAudioPlaylist>> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getPlaylists(owner_id, offset, count)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getPlaylistsCustom(code: String?): Single<ServicePlaylistResponse> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getPlaylistsCustom(code)
            }
    }

    override fun getById(audios: List<Audio>): Single<List<VKApiAudio>> {
        val ids = ArrayList<AccessIdPair>(audios.size)
        for (i in audios) {
            ids.add(AccessIdPair(i.id, i.ownerId, i.accessKey))
        }
        val audio_string = join(ids, ",") { AccessIdPair.format(it) }
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getById(audio_string)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getByIdOld(audios: List<Audio>): Single<List<VKApiAudio>> {
        val ids = ArrayList<AccessIdPair>(audios.size)
        for (i in audios) {
            ids.add(AccessIdPair(i.id, i.ownerId, i.accessKey))
        }
        val audio_string = join(ids, ",") { AccessIdPair.format(it) }
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getByIdVersioned(audio_string, "5.90")
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getLyrics(lyrics_id: Int): Single<VkApiLyrics> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getLyrics(lyrics_id)
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun getCatalogBlockById(
        block_id: String?,
        start_from: String?
    ): Single<CatalogResponse> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service
                    .getCatalogBlockById(block_id, start_from)
                    .map(extractBlockResponseWithErrorHandling())
            }
    }

    override val uploadServer: Single<VkApiAudioUploadServer>
        get() = provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service.uploadServer
                    .map(extractResponseWithErrorHandling())
            }

    override fun save(
        server: String?,
        audio: String?,
        hash: String?,
        artist: String?,
        title: String?
    ): Single<VKApiAudio> {
        return provideService(IAudioService::class.java)
            .flatMap { service: IAudioService ->
                service.save(server, audio, hash, artist, title)
                    .map(extractResponseWithErrorHandling())
            }
    }
}